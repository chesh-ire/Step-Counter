package com.example.stepcounter.ui.friends

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stepcounter.data.repository.AddFriendResult
import com.example.stepcounter.data.repository.FriendProfile
import com.example.stepcounter.data.repository.GroupResult
import com.example.stepcounter.data.repository.StepGroup
import com.example.stepcounter.ui.MainViewModel
import com.example.stepcounter.ui.components.BottomNavigationBar
import com.example.stepcounter.ui.theme.*
import kotlin.random.Random

private val Gold = Color(0xFFFFD54F)
private val Silver = Color(0xFFCBD5E1)
private val Bronze = Color(0xFFD9915B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
    val identity by viewModel.friendIdentity.collectAsStateWithLifecycle()
    val addResult by viewModel.addFriendResult.collectAsStateWithLifecycle()

    val groups by viewModel.myGroups.collectAsStateWithLifecycle()
    val selectedGroupId by viewModel.selectedGroupId.collectAsStateWithLifecycle()
    val groupLeaderboard by viewModel.groupLeaderboard.collectAsStateWithLifecycle()
    val groupResult by viewModel.groupActionResult.collectAsStateWithLifecycle()

    var tab by remember { mutableStateOf(FriendsTab.Friends) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showCreateGroup by remember { mutableStateOf(false) }
    var showJoinGroup by remember { mutableStateOf(false) }
    var showAddMembers by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedGroup = groups.firstOrNull { it.id == selectedGroupId }
    val myId = identity?.userId ?: ""

    // Auto-select the first group once groups load and nothing is selected.
    LaunchedEffect(groups, selectedGroupId) {
        if (selectedGroupId == null && groups.isNotEmpty()) {
            viewModel.selectGroup(groups.first().id)
        }
    }

    // Confetti when my rank improves (I overtake a friend).
    var showConfetti by remember { mutableStateOf(false) }
    var prevRank by remember { mutableStateOf(-1) }
    val myRank = leaderboard.indexOfFirst { it.isMe }
    LaunchedEffect(myRank, leaderboard.size) {
        if (myRank in 0 until prevRank && prevRank != -1 && leaderboard.size > 1) {
            showConfetti = true
        }
        if (myRank != -1) prevRank = myRank
    }
    LaunchedEffect(showConfetti) {
        if (showConfetti) {
            kotlinx.coroutines.delay(2200)
            showConfetti = false
        }
    }

    // Feedback when adding a friend.
    LaunchedEffect(addResult) {
        addResult?.let { result ->
            val msg = when (result) {
                is AddFriendResult.Success -> "You're now racing ${result.name}! 🏃"
                AddFriendResult.NotFound -> "No friend found with that code"
                AddFriendResult.Self -> "That's your own code 😄"
                AddFriendResult.AlreadyFriend -> "You're already friends"
                is AddFriendResult.Error -> "Couldn't add friend: ${result.message}"
            }
            snackbarHostState.showSnackbar(msg)
            viewModel.clearAddFriendResult()
        }
    }

    // Feedback for group actions.
    LaunchedEffect(groupResult) {
        groupResult?.let { result ->
            val msg = when (result) {
                is GroupResult.Created -> "Group \"${result.group.name}\" created! 🎉"
                is GroupResult.Joined -> "You joined ${result.name}!"
                is GroupResult.MemberAdded -> "Added ${result.name} to the group"
                GroupResult.NotFound -> "No group found with that code"
                GroupResult.AlreadyMember -> "Already in that group"
                GroupResult.NotAllowed -> "Only the group creator can add members"
                is GroupResult.Error -> "Something went wrong: ${result.message}"
            }
            snackbarHostState.showSnackbar(msg)
            viewModel.clearGroupActionResult()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = DeepNavy,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (tab == FriendsTab.Friends) "Friends Race" else "Group Race",
                                    color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp
                                )
                                Spacer(Modifier.width(8.dp))
                                LivePulse()
                            }
                            Text("Today's step leaderboard", color = TextGray, fontSize = 12.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                if (tab == FriendsTab.Friends) {
                    ExtendedFloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = AccentCyan,
                        contentColor = DeepNavy,
                        icon = { Icon(Icons.Rounded.PersonAdd, contentDescription = null) },
                        text = { Text("Add friend", fontWeight = FontWeight.Bold) }
                    )
                } else {
                    ExtendedFloatingActionButton(
                        onClick = { showCreateGroup = true },
                        containerColor = AccentCyan,
                        contentColor = DeepNavy,
                        icon = { Icon(Icons.Rounded.GroupAdd, contentDescription = null) },
                        text = { Text("New group", fontWeight = FontWeight.Bold) }
                    )
                }
            },
            bottomBar = {
                BottomNavigationBar(
                    currentRoute = "friends",
                    onHome = onNavigateToHome,
                    onFriends = {},
                    onWater = onNavigateToWater,
                    onStats = onNavigateToStats,
                    onGoals = onNavigateToGoals,
                    onProfile = onNavigateToProfile
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                FriendsGroupsToggle(
                    selected = tab,
                    onSelect = { tab = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                if (tab == FriendsTab.Friends) {
                    val maxSteps = (leaderboard.maxOfOrNull { it.steps } ?: 1).coerceAtLeast(1)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item { Spacer(Modifier.height(4.dp)) }

                        if (!viewModel.friendsAvailable) {
                            item { NotConnectedBanner() }
                        }

                        if (leaderboard.size <= 1) {
                            item {
                                EmptyFriendsCard(
                                    myCode = identity?.friendCode ?: "······",
                                    onAdd = { showAddDialog = true }
                                )
                            }
                        }

                        items(leaderboard, key = { it.id }) { profile ->
                            val rank = leaderboard.indexOf(profile)
                            LeaderboardRow(
                                profile = profile,
                                rank = rank,
                                maxSteps = maxSteps,
                                onEdit = if (profile.isMe) ({ showRenameDialog = true }) else null
                            )
                        }

                        item { Spacer(Modifier.height(88.dp)) } // room for the FAB
                    }
                } else {
                    GroupsPane(
                        groups = groups,
                        selectedGroup = selectedGroup,
                        groupLeaderboard = groupLeaderboard,
                        myId = myId,
                        connected = viewModel.friendsAvailable,
                        onSelectGroup = { viewModel.selectGroup(it) },
                        onCreateGroup = { showCreateGroup = true },
                        onJoinGroup = { showJoinGroup = true },
                        onAddMembers = { showAddMembers = true },
                        onEditName = { showRenameDialog = true }
                    )
                }
            }
        }

        if (showConfetti) {
            ConfettiOverlay(modifier = Modifier.fillMaxSize())
        }
    }

    if (showAddDialog) {
        AddFriendDialog(
            myCode = identity?.friendCode ?: "",
            myName = identity?.displayName ?: "Me",
            onAdd = { viewModel.addFriend(it) },
            onRename = { viewModel.setDisplayName(it) },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showRenameDialog) {
        RenameDialog(
            currentName = identity?.displayName ?: "Me",
            onRename = { viewModel.setDisplayName(it) },
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showCreateGroup) {
        CreateGroupDialog(
            onCreate = { viewModel.createGroup(it) },
            onDismiss = { showCreateGroup = false }
        )
    }

    if (showJoinGroup) {
        JoinGroupDialog(
            onJoin = { viewModel.joinGroup(it) },
            onDismiss = { showJoinGroup = false }
        )
    }

    if (showAddMembers && selectedGroup != null) {
        // Only my friends who aren't already in the group can be added.
        val candidates = leaderboard.filter { !it.isMe && it.id !in selectedGroup.memberIds }
        AddMembersDialog(
            groupName = selectedGroup.name,
            candidates = candidates,
            onAdd = { friendId -> viewModel.addFriendToGroup(selectedGroup.id, friendId) },
            onDismiss = { showAddMembers = false }
        )
    }
}

enum class FriendsTab { Friends, Groups }

@Composable
private fun FriendsGroupsToggle(
    selected: FriendsTab,
    onSelect: (FriendsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardNavy)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ToggleSegment(
            label = "Friends",
            icon = Icons.Rounded.People,
            selected = selected == FriendsTab.Friends,
            onClick = { onSelect(FriendsTab.Friends) },
            modifier = Modifier.weight(1f)
        )
        ToggleSegment(
            label = "Groups",
            icon = Icons.Rounded.Groups,
            selected = selected == FriendsTab.Groups,
            onClick = { onSelect(FriendsTab.Groups) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ToggleSegment(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) AccentCyan else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) DeepNavy else TextGray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            color = if (selected) DeepNavy else TextGray,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun GroupsPane(
    groups: List<StepGroup>,
    selectedGroup: StepGroup?,
    groupLeaderboard: List<FriendProfile>,
    myId: String,
    connected: Boolean,
    onSelectGroup: (String) -> Unit,
    onCreateGroup: () -> Unit,
    onJoinGroup: () -> Unit,
    onAddMembers: () -> Unit,
    onEditName: () -> Unit
) {
    val maxSteps = (groupLeaderboard.maxOfOrNull { it.steps } ?: 1).coerceAtLeast(1)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        if (!connected) {
            item { NotConnectedBanner() }
        }

        if (groups.isEmpty()) {
            item { EmptyGroupsCard(onCreate = onCreateGroup, onJoin = onJoinGroup) }
        } else {
            // Group picker chips.
            item {
                GroupChips(
                    groups = groups,
                    selectedId = selectedGroup?.id,
                    onSelect = onSelectGroup,
                    onJoin = onJoinGroup
                )
            }

            if (selectedGroup != null) {
                item {
                    GroupHeaderCard(
                        group = selectedGroup,
                        memberCount = groupLeaderboard.size.coerceAtLeast(selectedGroup.memberIds.size),
                        isCreator = selectedGroup.isCreator(myId),
                        onAddMembers = onAddMembers
                    )
                }

                items(groupLeaderboard, key = { it.id }) { profile ->
                    val rank = groupLeaderboard.indexOf(profile)
                    LeaderboardRow(
                        profile = profile,
                        rank = rank,
                        maxSteps = maxSteps,
                        onEdit = if (profile.isMe) onEditName else null
                    )
                }
            }
        }

        item { Spacer(Modifier.height(88.dp)) } // room for the FAB
    }
}

@Composable
private fun GroupChips(
    groups: List<StepGroup>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onJoin: () -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(groups, key = { it.id }) { group ->
            val isSelected = group.id == selectedId
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) AccentCyan.copy(alpha = 0.18f) else CardNavy)
                    .then(
                        if (isSelected) Modifier.border(
                            BorderStroke(1.5.dp, AccentCyan),
                            RoundedCornerShape(20.dp)
                        ) else Modifier
                    )
                    .clickable { onSelect(group.id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Groups,
                    contentDescription = null,
                    tint = if (isSelected) AccentCyan else TextGray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    group.name,
                    color = if (isSelected) TextWhite else TextGray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardNavy)
                    .clickable(onClick = onJoin)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Login, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Join by code", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun GroupHeaderCard(
    group: StepGroup,
    memberCount: Int,
    isCreator: Boolean,
    onAddMembers: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardNavy)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(group.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "$memberCount ${if (memberCount == 1) "member" else "members"}",
                        color = TextGray, fontSize = 12.sp
                    )
                }
                IconButton(onClick = {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Join my step group \"${group.name}\"! Use code ${group.code} in the Friends Race app."
                        )
                    }
                    context.startActivity(Intent.createChooser(share, "Share group code"))
                }) {
                    Icon(Icons.Rounded.Share, contentDescription = "Share group code", tint = AccentCyan)
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepNavy)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("GROUP CODE", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Text(
                    group.code,
                    color = AccentCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
            }

            if (isCreator) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onAddMembers,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = DeepNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add friends to group", fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Only the group creator can add members. Share the code to invite others.",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyGroupsCard(onCreate: () -> Unit, onJoin: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.Groups, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("Create a group", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "Make a group, add your friends, and everyone races on one shared leaderboard — no need for members to add each other.",
                color = TextGray,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onCreate,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = DeepNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("New group", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onJoin) {
                Text("Join an existing group by code", color = AccentCyan, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateGroupDialog(
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardNavy,
        titleContentColor = TextWhite,
        textContentColor = TextGray,
        title = { Text("New group", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Give your group a name. You'll be the creator, so you can add friends to it.",
                    color = TextGray,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it.take(30) },
                    label = { Text("Group name") },
                    singleLine = true,
                    colors = cyanFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = nameInput.trim()
                    if (trimmed.isNotBlank()) onCreate(trimmed)
                    onDismiss()
                },
                enabled = nameInput.isNotBlank()
            ) {
                Text("Create", color = AccentCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextGray) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinGroupDialog(
    onJoin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var codeInput by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardNavy,
        titleContentColor = TextWhite,
        textContentColor = TextGray,
        title = { Text("Join a group", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter the group's share code to join its leaderboard.", color = TextGray, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it.uppercase().take(6) },
                    label = { Text("Group code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = cyanFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (codeInput.isNotBlank()) onJoin(codeInput)
                    onDismiss()
                },
                enabled = codeInput.isNotBlank()
            ) {
                Text("Join", color = AccentCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextGray) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMembersDialog(
    groupName: String,
    candidates: List<FriendProfile>,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Track who we've added this session so the row updates immediately.
    val added = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardNavy,
        titleContentColor = TextWhite,
        textContentColor = TextGray,
        title = { Text("Add friends to $groupName", fontWeight = FontWeight.Bold) },
        text = {
            if (candidates.isEmpty()) {
                Text(
                    "All your friends are already in this group. Add more friends first, or share the group code.",
                    color = TextGray,
                    fontSize = 13.sp
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(candidates, key = { it.id }) { friend ->
                        val isAdded = friend.id in added
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                friend.name,
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isAdded) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = "Added",
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                TextButton(onClick = {
                                    onAdd(friend.id)
                                    added.add(friend.id)
                                }) {
                                    Text("Add", color = AccentCyan, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done", color = AccentCyan, fontWeight = FontWeight.Bold) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun cyanFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentCyan,
    unfocusedBorderColor = TextGray,
    focusedLabelColor = AccentCyan,
    unfocusedLabelColor = TextGray,
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    cursorColor = AccentCyan
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenameDialog(
    currentName: String,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardNavy,
        titleContentColor = TextWhite,
        textContentColor = TextGray,
        title = { Text("Edit your name", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "This is how you'll appear to friends on the leaderboard.",
                    color = TextGray,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it.take(20) },
                    label = { Text("Display name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = TextGray,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextGray,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = AccentCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = nameInput.trim()
                    if (trimmed.isNotBlank() && trimmed != currentName) onRename(trimmed)
                    onDismiss()
                },
                enabled = nameInput.isNotBlank() && nameInput.trim() != currentName
            ) {
                Text("Save", color = AccentCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextGray) }
        }
    )
}

@Composable
private fun LeaderboardRow(profile: FriendProfile, rank: Int, maxSteps: Int, onEdit: (() -> Unit)? = null) {
    val targetFraction = (profile.steps.toFloat() / maxSteps).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "bar"
    )
    val animatedSteps by animateIntAsState(
        targetValue = profile.steps,
        animationSpec = tween(durationMillis = 900),
        label = "steps"
    )

    val accent = when (rank) {
        0 -> Gold
        1 -> Silver
        2 -> Bronze
        else -> AccentBlue
    }
    val isLive = System.currentTimeMillis() - profile.lastUpdated < 120_000 && profile.lastUpdated > 0

    val containerColor = if (profile.isMe) AccentCyan.copy(alpha = 0.12f) else CardNavy
    val border = if (profile.isMe) BorderStroke(1.5.dp, AccentCyan) else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RankBadge(rank = rank, accent = accent)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (profile.isMe) "${profile.name} (You)" else profile.name,
                            color = TextWhite,
                            fontWeight = if (profile.isMe) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isLive) {
                            Spacer(Modifier.width(6.dp))
                            LiveDot()
                        }
                        if (onEdit != null) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit your name",
                                tint = AccentCyan,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onEdit)
                            )
                        }
                    }
                    Text(
                        text = "Goal ${"%,d".format(profile.goal)}",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "%,d".format(animatedSteps),
                        color = accent,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    Text("steps", color = TextGray, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Animated race bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(DeepNavy)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(accent.copy(alpha = 0.7f), accent)
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun RankBadge(rank: Int, accent: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = if (rank < 3) 0.9f else 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        if (rank < 3) {
            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(20.dp))
        } else {
            Text("${rank + 1}", color = accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun LivePulse() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "a"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF4ADE80).copy(alpha = alpha))
        )
        Spacer(Modifier.width(4.dp))
        Text("LIVE", color = Color(0xFF4ADE80), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LiveDot() {
    val transition = rememberInfiniteTransition(label = "dot")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "a"
    )
    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(Color(0xFF4ADE80).copy(alpha = alpha))
    )
}

@Composable
private fun NotConnectedBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CalorieOrange.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = CalorieOrange)
            Spacer(Modifier.width(12.dp))
            Text(
                "Offline mode — add google-services.json to sync with friends in real time.",
                color = TextWhite,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun EmptyFriendsCard(myCode: String, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.Groups, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("Race your friends!", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "Share your code so friends can add you, then watch the steps race update live.",
                color = TextGray,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text("YOUR CODE", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                myCode,
                color = AccentCyan,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFriendDialog(
    myCode: String,
    myName: String,
    onAdd: (String) -> Unit,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var codeInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf(myName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardNavy,
        titleContentColor = TextWhite,
        textContentColor = TextGray,
        title = { Text("Add a friend", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                // Share my code
                Text("YOUR CODE", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        myCode,
                        color = AccentCyan,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Join my step race! Add my code $myCode in the Friends Race app."
                            )
                        }
                        context.startActivity(Intent.createChooser(share, "Share your code"))
                    }) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share", tint = AccentCyan)
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = DeepNavy)
                Spacer(Modifier.height(16.dp))

                // Enter a friend's code
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it.uppercase().take(6) },
                    label = { Text("Friend's code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = TextGray,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextGray,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = AccentCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Rename myself
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it.take(20) },
                    label = { Text("Your display name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = TextGray,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextGray,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = AccentCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nameInput.isNotBlank() && nameInput != myName) onRename(nameInput)
                    if (codeInput.isNotBlank()) onAdd(codeInput)
                    onDismiss()
                },
                enabled = codeInput.isNotBlank() || (nameInput.isNotBlank() && nameInput != myName)
            ) {
                Text("Save", color = AccentCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = TextGray) }
        }
    )
}

// ---------------- Confetti ----------------

private data class Confetto(
    val startX: Float,
    val color: Color,
    val size: Float,
    val delay: Float,
    val drift: Float,
    val rotationSpeed: Float
)

@Composable
private fun ConfettiOverlay(modifier: Modifier = Modifier) {
    val colors = listOf(Gold, AccentCyan, AccentBlue, DistancePink, Color(0xFF4ADE80), CalorieOrange)
    val confetti = remember {
        List(80) {
            Confetto(
                startX = Random.nextFloat(),
                color = colors[Random.nextInt(colors.size)],
                size = Random.nextFloat() * 10f + 6f,
                delay = Random.nextFloat() * 0.3f,
                drift = (Random.nextFloat() - 0.5f) * 0.3f,
                rotationSpeed = Random.nextFloat() * 6f + 2f
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(2200, easing = LinearEasing))
    }

    Canvas(modifier = modifier) {
        confetti.forEach { c ->
            val t = ((progress.value - c.delay) / (1f - c.delay)).coerceIn(0f, 1f)
            if (t <= 0f) return@forEach
            val x = (c.startX + c.drift * t) * size.width
            val y = t * (size.height + 40f) - 20f
            val alpha = (1f - t).coerceIn(0f, 1f)
            drawCircle(
                color = c.color.copy(alpha = alpha),
                radius = c.size,
                center = Offset(x, y)
            )
        }
    }
}
