package com.canteen.di

import com.canteen.data.repository.FirebaseAuthRepository
import com.canteen.data.repository.FirebaseCampusRepository
import com.canteen.domain.repository.AuthRepository
import com.canteen.domain.repository.CampusRepository
import com.canteen.domain.usecase.GetCollegesUseCase
import com.canteen.domain.usecase.DeleteMenuItemUseCase
import com.canteen.domain.usecase.GetCanteenOrdersUseCase
import com.canteen.domain.usecase.GetCanteensUseCase
import com.canteen.domain.usecase.GetMenuItemsUseCase
import com.canteen.domain.usecase.GetOwnerCanteenUseCase
import com.canteen.domain.usecase.GetUserOrdersUseCase
import com.canteen.domain.usecase.GetUserProfileUseCase
import com.canteen.domain.usecase.LoginUseCase
import com.canteen.domain.usecase.PlaceOrderUseCase
import com.canteen.domain.usecase.RegisterUseCase
import com.canteen.domain.usecase.SaveCanteenUseCase
import com.canteen.domain.usecase.SaveMenuItemUseCase
import com.canteen.domain.usecase.SaveUserProfileUseCase
import com.canteen.domain.usecase.UpdateOrderStatusUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer {
    private val authRepository: AuthRepository =
        FirebaseAuthRepository(FirebaseAuth.getInstance())

    private val campusRepository: CampusRepository =
        FirebaseCampusRepository(FirebaseFirestore.getInstance())

    val loginUseCase = LoginUseCase(authRepository)
    val registerUseCase = RegisterUseCase(authRepository)
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
}
