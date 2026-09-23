package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.MainTopAppBar
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.geometry.GeometryScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.practice.PracticeScreen
import com.example.ui.screens.review.ErrorsScreen
import com.example.ui.screens.review.SavedQuestionsScreen
import com.example.ui.screens.stats.StatisticsScreen
import com.example.ui.screens.test.ActiveTestScreen
import com.example.ui.screens.test.TestListScreen
import com.example.ui.screens.test.TestResultScreen
import com.example.ui.theme.Blue700
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Rose600

@Composable
fun MainApp(
    viewModel: MainViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val currentUser = authState.currentUser

    val showBottomBar = currentUser != null && currentScreen in listOf(
        AppScreen.HOME,
        AppScreen.PRACTICE,
        AppScreen.TESTS,
        AppScreen.ERRORS,
        AppScreen.STATISTICS
    )

    val showTopBar = currentUser != null && currentScreen != AppScreen.ADMIN_PANEL

    Scaffold(
        topBar = {
            if (showTopBar) {
                val title = when (currentScreen) {
                    AppScreen.HOME -> "Milliy Sertifikat"
                    AppScreen.PRACTICE -> "Misol Ishlash"
                    AppScreen.TESTS -> "Testlar"
                    AppScreen.ACTIVE_TEST -> "Imtihon Jarayoni"
                    AppScreen.TEST_RESULT -> "Imtihon Natijasi"
                    AppScreen.ERRORS -> "Xatolarim"
                    AppScreen.SAVED -> "Saqlanganlar"
                    AppScreen.STATISTICS -> "Statistika"
                    AppScreen.GEOMETRY -> "Geometriya"
                    AppScreen.PRIVACY_POLICY -> "Maxfiylik Siyosati"
                    AppScreen.DATA_MANAGEMENT -> "Shaxsiy Ma'lumotlar"
                    else -> "Milliy Sertifikat"
                }

                val showBack = currentScreen != AppScreen.HOME

                MainTopAppBar(
                    title = title,
                    currentUser = currentUser,
                    onBack = if (showBack) {
                        { viewModel.navigateTo(AppScreen.HOME) }
                    } else null,
                    onLogout = { viewModel.logout() },
                    onAdminClick = if (currentUser?.role == "ADMIN") {
                        { viewModel.navigateTo(AppScreen.ADMIN_PANEL) }
                    } else null,
                    onPrivacyClick = { viewModel.navigateTo(AppScreen.PRIVACY_POLICY) }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) },
                    isAdmin = currentUser?.role == "ADMIN"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                currentUser == null || currentScreen == AppScreen.AUTH -> {
                    AuthScreen(viewModel = viewModel)
                }
                currentScreen == AppScreen.HOME -> {
                    HomeScreen(viewModel = viewModel)
                }
                currentScreen == AppScreen.PRACTICE -> {
                    PracticeScreen(viewModel = viewModel)
                }
                currentScreen == AppScreen.TESTS -> {
                    TestListScreen(viewModel = viewModel)
                }
                currentScreen == AppScreen.ACTIVE_TEST -> {
                    ActiveTestScreen(viewModel = viewModel)
                }
                currentScreen == AppScreen.TEST_RESULT -> {
                    TestResultScreen(viewModel = viewModel)
                }
                currentScreen == AppScreen.ERRORS -> {
                    ErrorsScreen(viewModel = viewModel)
                }
                currentScreen == AppScreen.SAVED -> {
                    SavedQuestionsScreen(viewModel = viewModel)
                }
                currentScreen == AppScreen.STATISTICS -> {
                    StatisticsScreen(viewModel = viewModel)
                }
                currentScreen == AppScreen.GEOMETRY -> {
                    GeometryScreen(viewModel = viewModel)
                }
                currentScreen == AppScreen.ADMIN_PANEL -> {
                    AdminDashboardScreen(viewModel = viewModel)
                }
                currentScreen == AppScreen.PRIVACY_POLICY -> {
                    com.example.ui.screens.privacy.PrivacyPolicyScreen(viewModel = viewModel)
                }
                currentScreen == AppScreen.DATA_MANAGEMENT -> {
                    com.example.ui.screens.privacy.DataManagementScreen(viewModel = viewModel)
                }
            }

            // Notification / Feedback Banner
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    color = Navy900,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 6.dp
                ) {
                    Text(
                        text = toastMessage ?: "",
                        color = PureWhite,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                LaunchedEffect(toastMessage) {
                    if (toastMessage != null) {
                        kotlinx.coroutines.delay(3500)
                        viewModel.clearToast()
                    }
                }
            }
        }
    }
}
