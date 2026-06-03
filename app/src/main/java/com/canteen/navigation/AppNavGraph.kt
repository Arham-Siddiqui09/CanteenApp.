package com.canteen.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.canteen.di.AppContainer
import com.canteen.domain.model.AppDestination
import com.canteen.domain.model.UserRole
import com.canteen.presentation.auth.AuthScreen
import com.canteen.presentation.auth.AuthViewModel
import com.canteen.presentation.auth.RoleSelectionScreen
import com.canteen.presentation.auth.RoleViewModel
import com.canteen.presentation.owner.OwnerHomeScreen
import com.canteen.presentation.owner.OwnerHomeViewModel
import com.canteen.presentation.owner.OwnerSetupScreen
import com.canteen.presentation.owner.OwnerSetupViewModel
import com.canteen.presentation.session.SplashScreen
import com.canteen.presentation.session.SplashViewModel
import com.canteen.presentation.user.MenuScreen
import com.canteen.presentation.user.MenuViewModel
import com.canteen.presentation.user.ProfileSetupScreen
import com.canteen.presentation.user.ProfileSetupViewModel
import com.canteen.presentation.user.UserHomeScreen
import com.canteen.presentation.user.UserHomeViewModel
import com.canteen.presentation.user.UserOrdersScreen
import com.canteen.presentation.user.UserOrdersViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val appContainer = remember { AppContainer(context.applicationContext) }
    val roleViewModel: RoleViewModel = viewModel()
    val scope = rememberCoroutineScope()

    LaunchedEffect(appContainer) {
        if (appContainer.currentUserId() != null) {
            appContainer.registerFcmTokenUseCase()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            val splashViewModel: SplashViewModel = viewModel(
                factory = viewModelFactory {
                    SplashViewModel(resolveAppStartUseCase = appContainer.resolveAppStartUseCase)
                }
            )

            SplashScreen(
                viewModel = splashViewModel,
                onDestinationResolved = { destination ->
                    navController.navigate(destination.toRoute()) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                viewModel = roleViewModel,
                onContinue = { role ->
                    appContainer.saveSelectedRole(role)
                    navController.navigate(Screen.Auth.createRoute(role))
                }
            )
        }

        composable(
            route = Screen.Auth.route,
            arguments = listOf(navArgument(Screen.Auth.roleArg) { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments
                ?.getString(Screen.Auth.roleArg)
                ?.let(UserRole::valueOf)
                ?: UserRole.USER
            val authViewModel: AuthViewModel = viewModel(
                factory = viewModelFactory {
                    AuthViewModel(
                        loginUseCase = appContainer.loginUseCase,
                        registerUseCase = appContainer.registerUseCase
                    )
                }
            )

            AuthScreen(
                role = role,
                viewModel = authViewModel,
                onAuthenticated = {
                    appContainer.registerFcmTokenUseCase()
                    scope.launch {
                        val destination = appContainer.resolveAppStartUseCase(role)
                        navController.navigate(destination.toRoute()) {
                            popUpTo(Screen.RoleSelection.route)
                        }
                    }
                }
            )
        }

        composable(Screen.ProfileSetup.route) {
            val profileSetupViewModel: ProfileSetupViewModel = viewModel(
                factory = viewModelFactory {
                    ProfileSetupViewModel(
                        getCollegesUseCase = appContainer.getCollegesUseCase,
                        saveUserProfileUseCase = appContainer.saveUserProfileUseCase,
                        currentUserId = appContainer::currentUserId
                    )
                }
            )

            ProfileSetupScreen(
                viewModel = profileSetupViewModel,
                onCompleted = {
                    appContainer.registerFcmTokenUseCase()
                    navController.navigate(Screen.UserHome.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.OwnerSetup.route) {
            val ownerSetupViewModel: OwnerSetupViewModel = viewModel(
                factory = viewModelFactory {
                    OwnerSetupViewModel(
                        getCollegesUseCase = appContainer.getCollegesUseCase,
                        saveCanteenUseCase = appContainer.saveCanteenUseCase,
                        currentUserId = appContainer::currentUserId
                    )
                }
            )

            OwnerSetupScreen(
                viewModel = ownerSetupViewModel,
                onCompleted = {
                    appContainer.registerFcmTokenUseCase()
                    navController.navigate(Screen.OwnerHome.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UserHome.route) {
            val userHomeViewModel: UserHomeViewModel = viewModel(
                factory = viewModelFactory {
                    UserHomeViewModel(
                        getUserProfileUseCase = appContainer.getUserProfileUseCase,
                        getCanteensUseCase = appContainer.getCanteensUseCase,
                        getUserOrdersUseCase = appContainer.getUserOrdersUseCase,
                        currentUserId = appContainer::currentUserId
                    )
                }
            )

            UserHomeScreen(
                viewModel = userHomeViewModel,
                onOpenCanteen = { canteen ->
                    navController.navigate(
                        Screen.CanteenMenu.createRoute(canteen.id, canteen.name)
                    )
                },
                onOpenOrders = {
                    navController.navigate(Screen.UserOrders.route)
                },
                onLogout = {
                    appContainer.logoutUseCase()
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.CanteenMenu.route,
            arguments = listOf(
                navArgument(Screen.CanteenMenu.canteenIdArg) { type = NavType.StringType },
                navArgument(Screen.CanteenMenu.canteenNameArg) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val canteenId = backStackEntry.arguments
                ?.getString(Screen.CanteenMenu.canteenIdArg)
                .orEmpty()
            val canteenName = backStackEntry.arguments
                ?.getString(Screen.CanteenMenu.canteenNameArg)
                ?.let(Uri::decode)
                .orEmpty()
            val menuViewModel: MenuViewModel = viewModel(
                key = canteenId,
                factory = viewModelFactory {
                    MenuViewModel(
                        canteenId = canteenId,
                        canteenName = canteenName,
                        getMenuItemsUseCase = appContainer.getMenuItemsUseCase,
                        placeOrderUseCase = appContainer.placeOrderUseCase,
                        currentUserId = appContainer::currentUserId
                    )
                }
            )

            MenuScreen(
                viewModel = menuViewModel,
                onBack = { navController.popBackStack() },
                onOrderPlaced = {
                    navController.navigate(Screen.UserOrders.route) {
                        popUpTo(Screen.UserHome.route)
                    }
                }
            )
        }

        composable(Screen.UserOrders.route) {
            val userOrdersViewModel: UserOrdersViewModel = viewModel(
                factory = viewModelFactory {
                    UserOrdersViewModel(
                        getUserOrdersUseCase = appContainer.getUserOrdersUseCase,
                        currentUserId = appContainer::currentUserId
                    )
                }
            )

            UserOrdersScreen(
                viewModel = userOrdersViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OwnerHome.route) {
            val ownerHomeViewModel: OwnerHomeViewModel = viewModel(
                factory = viewModelFactory {
                    OwnerHomeViewModel(
                        getOwnerCanteenUseCase = appContainer.getOwnerCanteenUseCase,
                        getMenuItemsUseCase = appContainer.getMenuItemsUseCase,
                        saveMenuItemUseCase = appContainer.saveMenuItemUseCase,
                        deleteMenuItemUseCase = appContainer.deleteMenuItemUseCase,
                        getCanteenOrdersUseCase = appContainer.getCanteenOrdersUseCase,
                        updateOrderStatusUseCase = appContainer.updateOrderStatusUseCase,
                        currentUserId = appContainer::currentUserId
                    )
                }
            )

            OwnerHomeScreen(
                viewModel = ownerHomeViewModel,
                onLogout = {
                    appContainer.logoutUseCase()
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

private fun AppDestination.toRoute(): String =
    when (this) {
        AppDestination.RoleSelection -> Screen.RoleSelection.route
        AppDestination.ProfileSetup -> Screen.ProfileSetup.route
        AppDestination.OwnerSetup -> Screen.OwnerSetup.route
        AppDestination.UserHome -> Screen.UserHome.route
        AppDestination.OwnerHome -> Screen.OwnerHome.route
    }

private inline fun <reified T : ViewModel> viewModelFactory(
    crossinline creator: () -> T
): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
    }
