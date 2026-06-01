package com.canteen.domain.repository

import com.canteen.domain.model.Canteen
import com.canteen.domain.model.MenuItem
import com.canteen.domain.model.Order
import com.canteen.domain.model.OrderStatus
import com.canteen.domain.model.UserProfile

interface CampusRepository {
    fun colleges(): List<String>
    suspend fun getUserProfile(userId: String): Result<UserProfile?>
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit>
    suspend fun getCanteensByCollege(college: String): Result<List<Canteen>>
    suspend fun getOwnerCanteen(ownerId: String): Result<Canteen?>
    suspend fun saveCanteen(canteen: Canteen): Result<Unit>
    suspend fun getMenuItems(canteenId: String): Result<List<MenuItem>>
    suspend fun saveMenuItem(item: MenuItem): Result<Unit>
    suspend fun deleteMenuItem(canteenId: String, itemId: String): Result<Unit>
    suspend fun placeOrder(order: Order): Result<Unit>
    suspend fun getOrdersForUser(userId: String): Result<List<Order>>
    suspend fun getOrdersForCanteen(canteenId: String): Result<List<Order>>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit>
}
