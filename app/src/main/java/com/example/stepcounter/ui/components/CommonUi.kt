package com.example.stepcounter.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepcounter.ui.theme.*

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onHome: () -> Unit,
    onFriends: () -> Unit,
    onWater: () -> Unit,
    onStats: () -> Unit,
    onGoals: () -> Unit,
    onProfile: () -> Unit
) {
    NavigationBar(
        containerColor = DeepNavy,
        contentColor = TextWhite,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = onHome,
            icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentCyan,
                selectedTextColor = AccentCyan,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )
        NavigationBarItem(
            selected = currentRoute == "friends",
            onClick = onFriends,
            icon = { Icon(Icons.Rounded.Groups, contentDescription = null) },
            label = { Text("Friends", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentCyan,
                selectedTextColor = AccentCyan,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )
        NavigationBarItem(
            selected = currentRoute == "stats",
            onClick = onStats,
            icon = { Icon(Icons.Rounded.BarChart, contentDescription = null) },
            label = { Text("Stats", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentCyan,
                selectedTextColor = AccentCyan,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )
        NavigationBarItem(
            selected = currentRoute == "goals",
            onClick = onGoals,
            icon = { Icon(Icons.Rounded.EmojiEvents, contentDescription = null) },
            label = { Text("Goals", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentCyan,
                selectedTextColor = AccentCyan,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )
    }
}
