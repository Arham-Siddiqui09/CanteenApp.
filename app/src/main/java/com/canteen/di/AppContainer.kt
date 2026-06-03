package com.canteen.di

import android.content.Context
import com.canteen.data.local.LocalCanteenStore
import com.canteen.data.local.LocalMenuStore
import com.canteen.data.local.LocalOrderStore
import com.canteen.data.local.LocalProfileStore
import com.canteen.data.local.SessionPreferences
import com.canteen.data.repository.FirebaseAuthRepository
import com.canteen.data.repository.FirebaseCampusRepository
import com.canteen.domain.model.UserRole
import com.canteen.domain.repository.AuthRepository
import com.canteen.domain.repository.CampusRepository
import com.canteen.domain.usecase.DeleteMenuItemUseCase
import com.canteen.domain.usecase.GetCanteenOrdersUseCase
import com.canteen.domain.usecase.GetCanteensUseCase
import com.canteen.domain.usecase.GetCollegesUseCase
import com.canteen.domain.usecase.GetMenuItemsUseCase
import com.canteen.domain.usecase.GetOwnerCanteenUseCase
import com.canteen.domain.usecase.GetUserOrdersUseCase
import com.canteen.domain.usecase.GetUserProfileUseCase
import com.canteen.domain.usecase.LoginUseCase
import com.canteen.domain.usecase.LogoutUseCase
import com.canteen.domain.usecase.PlaceOrderUseCase
import com.canteen.domain.usecase.RegisterFcmTokenUseCase
import com.canteen.domain.usecase.RegisterUseCase
import com.canteen.domain.usecase.ResolveAppStartUseCase
import com.canteen.domain.usecase.SaveCanteenUseCase
import com.canteen.domain.usecase.SaveMenuItemUseCase
import com.canteen.domain.usecase.SaveUserProfileUseCase
import com.canteen.domain.usecase.UpdateOrderStatusUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(context: Context) {

    private val sessionPreferences = SessionPreferences(context)
    private val localProfileStore = LocalProfileStore(context)
    private val localCanteenStore = LocalCanteenStore(context)
    private val localMenuStore = LocalMenuStore(context)
    private val localOrderStore = LocalOrderStore(context)

    private val authRepository: AuthRepository =
        FirebaseAuthRepository(FirebaseAuth.getInstance())

    private val campusRepository: CampusRepository =
        FirebaseCampusRepository(
            firestore = FirebaseFirestore.getInstance(),
            localProfileStore = localProfileStore,
            localCanteenStore = localCanteenStore,
            localMenuStore = localMenuStore,
            localOrderStore = localOrderStore
        )

    val registerFcmTokenUseCase = RegisterFcmTokenUseCase(context)
    val loginUseCase = LoginUseCase(authRepository)
    val registerUseCase = RegisterUseCase(authRepository)
    val logoutUseCase = LogoutUseCase(
        authRepository,
        sessionPreferences,
        localProfileStore,
        localCanteenStore,
        localMenuStore
    )
    val resolveAppStartUseCase = ResolveAppStartUseCase(
        authRepository = authRepository,
        getUserProfileUseCase = GetUserProfileUseCase(campusRepository),
        getOwnerCanteenUseCase = GetOwnerCanteenUseCase(campusRepository),
        sessionPreferences = sessionPreferences
    )
    val getCollegesUseCase = GetCollegesUseCase(campusRepository)
    val getUserProfileUseCase = GetUserProfileUseCase(campusRepository)
    val getCanteensUseCase = GetCanteensUseCase(campusRepository)
    val getOwnerCanteenUseCase = GetOwnerCanteenUseCase(campusRepository)
    val getMenuItemsUseCase = GetMenuItemsUseCase(campusRepository)
    val saveMenuItemUseCase = SaveMenuItemUseCase(campusRepository)
    val deleteMenuItemUseCase = DeleteMenuItemUseCase(campusRepository)
    val saveUserProfileUseCase = SaveUserProfileUseCase(campusRepository)
    val saveCanteenUseCase = SaveCanteenUseCase(campusRepository)
    val placeOrderUseCase = PlaceOrderUseCase(campusRepository)
    val getUserOrdersUseCase = GetUserOrdersUseCase(campusRepository)
    val getCanteenOrdersUseCase = GetCanteenOrdersUseCase(campusRepository)
    val updateOrderStatusUseCase = UpdateOrderStatusUseCase(campusRepository)

    fun currentUserId(): String? = authRepository.currentUserId()

    fun saveSelectedRole(role: UserRole) {
        sessionPreferences.saveRole(role)
    }
}
