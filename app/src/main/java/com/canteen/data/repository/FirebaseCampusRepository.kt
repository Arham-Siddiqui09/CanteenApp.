package com.canteen.data.repository

import com.canteen.domain.model.Canteen
import com.canteen.domain.model.MenuItem
import com.canteen.domain.model.Order
import com.canteen.domain.model.OrderItem
import com.canteen.domain.model.OrderStatus
import com.canteen.domain.model.UserProfile
import com.canteen.domain.model.UserRole
import com.canteen.domain.repository.CampusRepository
import com.canteen.utils.awaitResult
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirebaseCampusRepository(
    private val firestore: FirebaseFirestore
) : CampusRepository {

    override fun colleges(): List<String> =
        listOf(
            "Select college",
            "Government Engineering College",
            "City Arts and Science College",
            "National Institute of Technology",
            "Campus University"
        )

    override suspend fun getUserProfile(userId: String): Result<UserProfile?> =
        firestore.collection(USERS)
            .document(userId)
            .get()
            .awaitResult()
            .map { snapshot ->
                if (snapshot.exists()) snapshot.toUserProfile() else null
            }

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val data = mapOf(
            "userId" to profile.userId,
            "name" to profile.name,
            "college" to profile.college,
            "role" to profile.role.name
        )

        return firestore.collection(USERS)
            .document(profile.userId)
            .set(data)
            .awaitResult()
            .map { Unit }
    }

    override suspend fun getCanteensByCollege(college: String): Result<List<Canteen>> =
        firestore.collection(CANTEENS)
            .whereEqualTo("college", college)
            .get()
            .awaitResult()
            .map { snapshot ->
                snapshot.documents.map { it.toCanteen() }
            }

    override suspend fun getOwnerCanteen(ownerId: String): Result<Canteen?> =
        firestore.collection(CANTEENS)
            .whereEqualTo("ownerId", ownerId)
            .limit(1)
            .get()
            .awaitResult()
            .map { snapshot ->
                snapshot.documents.firstOrNull()?.toCanteen()
            }

    override suspend fun saveCanteen(canteen: Canteen): Result<Unit> {
        val document = if (canteen.id.isBlank()) {
            firestore.collection(CANTEENS).document()
        } else {
            firestore.collection(CANTEENS).document(canteen.id)
        }
        val data = mapOf(
            "id" to document.id,
            "ownerId" to canteen.ownerId,
            "name" to canteen.name,
            "description" to canteen.description,
            "college" to canteen.college
        )

        return document.set(data)
            .awaitResult()
            .map { Unit }
    }

    override suspend fun getMenuItems(canteenId: String): Result<List<MenuItem>> =
        menuCollection(canteenId)
            .get()
            .awaitResult()
            .map { snapshot ->
                snapshot.documents.map { it.toMenuItem(canteenId) }
            }

    override suspend fun saveMenuItem(item: MenuItem): Result<Unit> {
        val document = if (item.id.isBlank()) {
            menuCollection(item.canteenId).document()
        } else {
            menuCollection(item.canteenId).document(item.id)
        }
        val data = mapOf(
            "id" to document.id,
            "canteenId" to item.canteenId,
            "name" to item.name,
            "description" to item.description,
            "priceCents" to item.priceCents,
            "isAvailable" to item.isAvailable
        )

        return document.set(data)
            .awaitResult()
            .map { Unit }
    }

    override suspend fun deleteMenuItem(canteenId: String, itemId: String): Result<Unit> =
        menuCollection(canteenId)
            .document(itemId)
            .delete()
            .awaitResult()
            .map { Unit }

    override suspend fun placeOrder(order: Order): Result<Unit> {
        val document = firestore.collection(ORDERS).document()
        val data = order.copy(id = document.id).toFirestoreMap()

        return document.set(data)
            .awaitResult()
            .map { Unit }
    }

    override suspend fun getOrdersForUser(userId: String): Result<List<Order>> =
        firestore.collection(ORDERS)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .awaitResult()
            .map { snapshot ->
                snapshot.documents.map { it.toOrder() }
            }

    override suspend fun getOrdersForCanteen(canteenId: String): Result<List<Order>> =
        firestore.collection(ORDERS)
            .whereEqualTo("canteenId", canteenId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .awaitResult()
            .map { snapshot ->
                snapshot.documents.map { it.toOrder() }
            }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> =
        firestore.collection(ORDERS)
            .document(orderId)
            .update("status", status.name)
            .awaitResult()
            .map { Unit }

    private fun menuCollection(canteenId: String) =
        firestore.collection(CANTEENS)
            .document(canteenId)
            .collection(MENU_ITEMS)

    private fun DocumentSnapshot.toUserProfile(): UserProfile =
        UserProfile(
            userId = getString("userId").orEmpty(),
            name = getString("name").orEmpty(),
            college = getString("college").orEmpty(),
            role = runCatching {
                UserRole.valueOf(getString("role").orEmpty())
            }.getOrDefault(UserRole.USER)
        )

    private fun DocumentSnapshot.toCanteen(): Canteen =
        Canteen(
            id = getString("id") ?: id,
            ownerId = getString("ownerId").orEmpty(),
            name = getString("name").orEmpty(),
            description = getString("description").orEmpty(),
            college = getString("college").orEmpty()
        )

    private fun DocumentSnapshot.toMenuItem(canteenId: String): MenuItem =
        MenuItem(
            id = getString("id") ?: id,
            canteenId = getString("canteenId") ?: canteenId,
            name = getString("name").orEmpty(),
            description = getString("description").orEmpty(),
            priceCents = getLong("priceCents")?.toInt() ?: 0,
            isAvailable = getBoolean("isAvailable") ?: true
        )

    private fun DocumentSnapshot.toOrder(): Order {
        val items = (get("items") as? List<*>)
            .orEmpty()
            .filterIsInstance<Map<*, *>>()

        return Order(
            id = getString("id") ?: id,
            userId = getString("userId").orEmpty(),
            canteenId = getString("canteenId").orEmpty(),
            canteenName = getString("canteenName").orEmpty(),
            items = items.map { it.toOrderItem() },
            totalCents = getLong("totalCents")?.toInt() ?: 0,
            status = runCatching {
                OrderStatus.valueOf(getString("status").orEmpty())
            }.getOrDefault(OrderStatus.PENDING),
            createdAt = getLong("createdAt") ?: 0L
        )
    }

    private fun Order.toFirestoreMap(): Map<String, Any> =
        mapOf(
            "id" to id,
            "userId" to userId,
            "canteenId" to canteenId,
            "canteenName" to canteenName,
            "items" to items.map { it.toFirestoreMap() },
            "totalCents" to totalCents,
            "status" to status.name,
            "createdAt" to createdAt
        )

    private fun OrderItem.toFirestoreMap(): Map<String, Any> =
        mapOf(
            "menuItemId" to menuItemId,
            "name" to name,
            "priceCents" to priceCents,
            "quantity" to quantity
        )

    private fun Map<*, *>.toOrderItem(): OrderItem =
        OrderItem(
            menuItemId = this["menuItemId"] as? String ?: "",
            name = this["name"] as? String ?: "",
            priceCents = (this["priceCents"] as? Number)?.toInt() ?: 0,
            quantity = (this["quantity"] as? Number)?.toInt() ?: 0
        )

    private companion object {
        const val USERS = "users"
        const val CANTEENS = "canteens"
        const val MENU_ITEMS = "menuItems"
        const val ORDERS = "orders"
    }
}
