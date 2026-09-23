package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.AppScreen
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue100
import com.example.ui.theme.Blue600
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String,
    currentUser: UserEntity?,
    onBack: (() -> Unit)? = null,
    onLogout: () -> Unit,
    onAdminClick: (() -> Unit)? = null,
    onPrivacyClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                )
                if (currentUser != null) {
                    Text(
                        text = "${currentUser.firstName} (${currentUser.role})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Blue100,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Ortga",
                        tint = PureWhite
                    )
                }
            }
        },
        actions = {
            if (onPrivacyClick != null) {
                IconButton(
                    onClick = onPrivacyClick,
                    modifier = Modifier.testTag("privacy_policy_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Maxfiylik va Xavfsizlik",
                        tint = Blue100
                    )
                }
            }
            if (currentUser?.role == "ADMIN" && onAdminClick != null) {
                IconButton(
                    onClick = onAdminClick,
                    modifier = Modifier.testTag("admin_panel_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Panel",
                        tint = Amber500
                    )
                }
            }
            if (currentUser != null) {
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.testTag("logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Chiqish",
                        tint = PureWhite
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Navy900,
            titleContentColor = PureWhite
        )
    )
}

@Composable
fun AppBottomNavBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    isAdmin: Boolean = false
) {
    NavigationBar(
        containerColor = Navy900,
        contentColor = PureWhite
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.HOME,
            onClick = { onNavigate(AppScreen.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Asosiy") },
            label = { Text("Asosiy", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PureWhite,
                selectedTextColor = Amber500,
                indicatorColor = Blue700,
                unselectedIconColor = Slate400,
                unselectedTextColor = Slate400
            ),
            modifier = Modifier.testTag("nav_item_home")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.PRACTICE,
            onClick = { onNavigate(AppScreen.PRACTICE) },
            icon = { Icon(Icons.Default.EditNote, contentDescription = "Mashq") },
            label = { Text("Mashq", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PureWhite,
                selectedTextColor = Amber500,
                indicatorColor = Blue700,
                unselectedIconColor = Slate400,
                unselectedTextColor = Slate400
            ),
            modifier = Modifier.testTag("nav_item_practice")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.TESTS || currentScreen == AppScreen.ACTIVE_TEST,
            onClick = { onNavigate(AppScreen.TESTS) },
            icon = { Icon(Icons.Default.Quiz, contentDescription = "Testlar") },
            label = { Text("Testlar", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PureWhite,
                selectedTextColor = Amber500,
                indicatorColor = Blue700,
                unselectedIconColor = Slate400,
                unselectedTextColor = Slate400
            ),
            modifier = Modifier.testTag("nav_item_tests")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.ERRORS,
            onClick = { onNavigate(AppScreen.ERRORS) },
            icon = { Icon(Icons.Default.Warning, contentDescription = "Xatolarim") },
            label = { Text("Xatolarim", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PureWhite,
                selectedTextColor = Amber500,
                indicatorColor = Blue700,
                unselectedIconColor = Slate400,
                unselectedTextColor = Slate400
            ),
            modifier = Modifier.testTag("nav_item_errors")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.STATISTICS,
            onClick = { onNavigate(AppScreen.STATISTICS) },
            icon = { Icon(Icons.Default.Assessment, contentDescription = "Statistika") },
            label = { Text("Statistika", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PureWhite,
                selectedTextColor = Amber500,
                indicatorColor = Blue700,
                unselectedIconColor = Slate400,
                unselectedTextColor = Slate400
            ),
            modifier = Modifier.testTag("nav_item_stats")
        )
    }
}

@Composable
fun OptionButton(
    letter: String,
    text: String,
    isSelected: Boolean,
    isCorrectReview: Boolean? = null, // null = neutral, true = correct, false = wrong
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (isCorrectReview) {
        true -> Color(0xFFDCFCE7) // Light Green
        false -> Color(0xFFFFE4E6) // Light Red
        null -> if (isSelected) Blue100 else PureWhite
    }

    val borderColor = when (isCorrectReview) {
        true -> Emerald600
        false -> Rose500
        null -> if (isSelected) Blue700 else Slate200
    }

    val letterBg = when (isCorrectReview) {
        true -> Emerald600
        false -> Rose500
        null -> if (isSelected) Blue700 else Slate700
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("option_${letter.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(letterBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = Slate900,
                    fontSize = 16.sp
                ),
                modifier = Modifier.weight(1f)
            )
            if (isCorrectReview == true) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "To'g'ri",
                    tint = Emerald600,
                    modifier = Modifier.size(24.dp)
                )
            } else if (isCorrectReview == false) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Noto'g'ri",
                    tint = Rose500,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun DifficultyBadge(difficulty: String) {
    val (label, bg, fg) = when (difficulty) {
        "EASY" -> Triple("OSON", Color(0xFFD1FAE5), Color(0xFF065F46))
        "MEDIUM" -> Triple("O'RTA", Color(0xFFFEF3C7), Color(0xFF92400E))
        "HARD" -> Triple("QIYIN", Color(0xFFFFEDD5), Color(0xFF9A3412))
        else -> Triple("MILLIY SERTIFIKAT", Color(0xFFFFE4E6), Color(0xFF9F1239))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Text(
            text = label,
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
