package com.example.stepcounter.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** One competitor on the live leaderboard. */
data class FriendProfile(
    val id: String,
    val name: String,
    val code: String,
    val steps: Int,
    val goal: Int,
    val lastUpdated: Long,
    val isMe: Boolean = false
)

sealed class AddFriendResult {
    data class Success(val name: String) : AddFriendResult()
    object NotFound : AddFriendResult()
    object Self : AddFriendResult()
    object AlreadyFriend : AddFriendResult()
    data class Error(val message: String) : AddFriendResult()
}

/**
 * Minimal real-time backend on top of Firebase Realtime Database.
 *
 * Schema:
 *   /users/{userId} = { name, code, steps, goal, lastUpdated, friends/{friendId}: true }
 *   /codes/{CODE}   = userId   (lookup table so friends can be added by a short code)
 *
 * No auth is used — each device owns a locally-generated userId. Secure the DB with
 * test-mode rules for the demo (see setup notes).
 */
class FriendsRepository {

    // Throws (and we fall back to offline mode) when google-services.json is a placeholder
    // or Firebase isn't initialized. The Realtime Database URL is bound explicitly so the
    // app connects regardless of whether google-services.json carries a firebase_url.
    private val db: DatabaseReference? = try {
        FirebaseDatabase.getInstance(DATABASE_URL).reference
    } catch (e: Throwable) {
        Log.w(TAG, "Firebase not configured; friends will stay offline", e)
        null
    }

    val isAvailable: Boolean get() = db != null

    /** Push my latest profile + today's steps so friends see me live. */
    suspend fun publishProfile(id: String, name: String, code: String, steps: Int, goal: Int) {
        val root = db ?: return
        val updates = hashMapOf<String, Any>(
            "/users/$id/name" to name,
            "/users/$id/code" to code,
            "/users/$id/steps" to steps,
            "/users/$id/goal" to goal,
            "/users/$id/lastUpdated" to System.currentTimeMillis(),
            "/codes/$code" to id
        )
        try {
            root.updateChildrenAwait(updates)
        } catch (e: Exception) {
            Log.w(TAG, "publishProfile failed", e)
        }
    }

    /** Add a friend by their share code. The link is mutual. */
    suspend fun addFriendByCode(myId: String, rawCode: String): AddFriendResult {
        val root = db ?: return AddFriendResult.Error("Not connected")
        val code = rawCode.trim().uppercase()
        if (code.isEmpty()) return AddFriendResult.NotFound

        return try {
            val friendId = root.child("codes").child(code).readOnce()
                .getValue(String::class.java)
                ?: return AddFriendResult.NotFound

            if (friendId == myId) return AddFriendResult.Self

            val existing = root.child("users").child(myId).child("friends").child(friendId)
                .readOnce().getValue(Boolean::class.java)
            if (existing == true) return AddFriendResult.AlreadyFriend

            val friendName = root.child("users").child(friendId).child("name")
                .readOnce().getValue(String::class.java) ?: "Friend"

            root.updateChildrenAwait(
                hashMapOf(
                    "/users/$myId/friends/$friendId" to true,
                    "/users/$friendId/friends/$myId" to true
                )
            )
            AddFriendResult.Success(friendName)
        } catch (e: Exception) {
            Log.w(TAG, "addFriendByCode failed", e)
            AddFriendResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Live leaderboard: me + my friends, newest step counts streamed in real time.
     * Emits an empty list when Firebase isn't configured.
     */
    fun observeLeaderboard(myId: String): Flow<List<FriendProfile>> {
        val root = db ?: return flowOf(emptyList())
        return combine(
            observeFriendIds(root, myId),
            observeAllUsers(root)
        ) { friendIds, allUsers ->
            val visible = friendIds + myId
            allUsers
                .filter { it.id in visible }
                .map { if (it.id == myId) it.copy(isMe = true) else it }
                .sortedByDescending { it.steps }
        }
    }

    private fun observeFriendIds(root: DatabaseReference, myId: String): Flow<Set<String>> =
        callbackFlow {
            val ref = root.child("users").child(myId).child("friends")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    trySend(snapshot.children.mapNotNull { it.key }.toSet())
                }
                override fun onCancelled(error: DatabaseError) {
                    trySend(emptySet())
                }
            }
            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }

    private fun observeAllUsers(root: DatabaseReference): Flow<List<FriendProfile>> =
        callbackFlow {
            val ref = root.child("users")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val users = snapshot.children.mapNotNull { child ->
                        val id = child.key ?: return@mapNotNull null
                        FriendProfile(
                            id = id,
                            name = child.child("name").getValue(String::class.java) ?: "Friend",
                            code = child.child("code").getValue(String::class.java) ?: "",
                            steps = child.child("steps").getValue(Long::class.java)?.toInt() ?: 0,
                            goal = child.child("goal").getValue(Long::class.java)?.toInt() ?: 10000,
                            lastUpdated = child.child("lastUpdated").getValue(Long::class.java) ?: 0L
                        )
                    }
                    trySend(users)
                }
                override fun onCancelled(error: DatabaseError) {
                    trySend(emptyList())
                }
            }
            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }

    companion object {
        private const val TAG = "FriendsRepository"
        private const val DATABASE_URL =
            "https://stepcounter-d43d0-default-rtdb.asia-southeast1.firebasedatabase.app"
    }
}

/** One-shot read as a coroutine. */
private suspend fun DatabaseReference.readOnce(): DataSnapshot =
    suspendCancellableCoroutine { cont ->
        addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (cont.isActive) cont.resume(snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
                if (cont.isActive) cont.resumeWith(Result.failure(error.toException()))
            }
        })
    }

/** updateChildren as a suspend fun. */
private suspend fun DatabaseReference.updateChildrenAwait(updates: Map<String, Any>) =
    suspendCancellableCoroutine { cont ->
        updateChildren(updates) { error, _ ->
            if (error == null) {
                if (cont.isActive) cont.resume(Unit)
            } else {
                if (cont.isActive) cont.resumeWith(Result.failure(error.toException()))
            }
        }
    }
