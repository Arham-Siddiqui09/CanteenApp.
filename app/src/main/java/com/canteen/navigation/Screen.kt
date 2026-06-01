package com.canteen.navigation

import android.net.Uri
import com.canteen.domain.model.UserRole

sealed class Screen(val route: String) {
    data object RoleSelection : Screen("role_selection")
    data object Auth : Screen("auth/{role}") {
        const val roleArg = "role"

        fun createRoute(role: UserRole): String = "auth/${role.name}"
    }

    data object ProfileSetup : Screen("profile_setup")
    data object OwnerSetup : Screen("owner_setup")
    data object UserHome : Screen("user_home")
    data object CanteenMenu : Screen("canteen_menu/{canteenId}/{canteenName}") {
        const val canteenIdArg = "canteenId"
        const val canteenNameArg = "canteenName"

        fun createRoute(canteenId: String, canteenName: String): String =
            "canteen_menu/$canteenId/${Uri.encode(canteenName)}"
    }

    data object UserOrders : Screen("user_orders")
    data object OwnerHome : Screen("owner_home")
}
