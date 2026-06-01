package com.canteen.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.canteen.di.AppContainer
import com.canteen.domain.model.UserRole
import com.canteen.presentation.auth.AuthScreen
import com.canteen.presentation.auth.AuthViewModel
import com.canteen.presentation.auth.RoleSelectionScreen
import com.canteen.presentation.auth.RoleViewModel
import com.canteen.presentation.owner.OwnerHomeScreen
import com.canteen.presentation.owner.OwnerHomeViewModel
import com.canteen.presentation.owner.OwnerSetupScreen
import com.canteen.presentation.owner.OwnerSetupViewModel
import com.canteen.presentation.user.MenuScreen
import com.canteen.presentation.user.MenuViewModel
import com.canteen.presentation.user.ProfileSetupScreen
import com.canteen.presentation.user.ProfileSetupViewModel
import com.canteen.presentation.user.UserHomeScreen
import com.canteen.presentation.user.UserHomeViewModel
import com.canteen.presentation.user.UserOrdersScreen
import com.canteen.presentation.user.UserOrdersViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val appContainer = remember { AppContainer() }
    val roleViewModel: RoleViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.RoleSelection.route
    ) {

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                viewModel = roleViewModel,
                onContinue = { role ->
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
                onAuthenticated = { authenticatedRole ->
                    val nextRoute = if (authenticatedRole == UserRole.USER) {
                        Screen.ProfileSetup.route
                    } else {
                        Screen.OwnerSetup.route
                    }
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.RoleSelection.route)
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

            UserOrdersScreen(viewModel = userOrdersViewModel)
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

            OwnerHomeScreen(viewModel = ownerHomeViewModel)
        }
    }
}

private inline fun <reified T : ViewModel> viewModelFactory(
    crossinline creator: () -> T
): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
    }
