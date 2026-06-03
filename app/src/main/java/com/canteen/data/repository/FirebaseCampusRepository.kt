package com.canteen.data.repository

import com.canteen.domain.model.Canteen
import com.canteen.domain.model.MenuItem
import com.canteen.domain.model.Order
import com.canteen.domain.model.OrderItem
import com.canteen.domain.model.OrderStatus
import com.canteen.domain.model.UserProfile
import com.canteen.domain.model.UserRole
import com.canteen.data.local.LocalCanteenStore
import com.canteen.data.local.LocalMenuStore
import com.canteen.data.local.LocalOrderStore
import com.canteen.data.local.LocalProfileStore
import com.canteen.domain.repository.CampusRepository
import com.canteen.utils.awaitResult
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

class FirebaseCampusRepository(
    private val firestore: FirebaseFirestore,
    private val localProfileStore: LocalProfileStore,
    private val localCanteenStore: LocalCanteenStore,
    private val localMenuStore: LocalMenuStore,
    private val localOrderStore: LocalOrderStore
) : CampusRepository {

    override fun colleges(): List<String> =
        listOf(
            "Select college",
            "Government Engineering College",
            "City Arts and Science College",
            "National Institute of Technology",
            "Campus University"
        )

    override suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        localProfileStore.get(userId)?.let { return Result.success(it) }

        return try {
            withTimeout(FIRESTORE_READ_TIMEOUT_MS) {
                firestore.collection(USERS)
                    .document(userId)
                    .get()
                    .awaitResult()
                    .map { snapshot ->
                        if (snapshot.exists()) snapshot.toUserProfile() else null
                    }
            }
        } catch (_: TimeoutCancellationException) {
            Result.success(null)
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        localProfileStore.save(profile)

        val data = mapOf(
            "userId" to profile.userId,
            "name" to profile.name,
            "college" to profile.college,
            "role" to profile.role.name
        )

        firestore.collection(USERS)
            .document(profile.userId)
            .set(data)
            .addOnFailureListener { /* Local profile is already saved; sync can retry later. */ }

        return Result.success(Unit)
    }

    override suspend fun getCanteensByCollege(college: String): Result<List<Canteen>> =
        try {
            withTimeout(FIRESTORE_READ_TIMEOUT_MS) {
                firestore.collection(CANTEENS)
                    .whereEqualTo("college", college)
                    .get()
                    .awaitResult()
                    .map { snapshot ->
                        snapshot.documents.map { it.toCanteen() }
                    }
            }
        } catch (_: TimeoutCancellationException) {
            Result.success(emptyList())
        }

    override suspend fun getOwnerCanteen(ownerId: String): Result<Canteen?> {
        val localCanteen = localCanteenStore.get(ownerId)
        return try {
            withTimeout(FIRESTORE_READ_TIMEOUT_MS) {
                firestore.collection(CANTEENS)
                    .whereEqualTo("ownerId", ownerId)
                    .limit(1)
                    .get()
                    .awaitResult()
            }.fold(
                onSuccess = { snapshot ->
                    val canteen = snapshot.documents.firstOrNull()?.toCanteen()
                    if (canteen != null) {
                        localCanteenStore.save(canteen)
                        Result.success(canteen)
                    } else {
                        Result.success(localCanteen)
                    }
                },
                onFailure = { Result.success(localCanteen) }
            )
        } catch (_: TimeoutCancellationException) {
            Result.success(localCanteen)
        }
    }

    override suspend fun saveCanteen(canteen: Canteen): Result<Unit> {
        val canteenId = canteen.id.ifBlank { "canteen_${canteen.ownerId}" }
        val savedCanteen = canteen.copy(id = canteenId)
        localCanteenStore.save(savedCanteen)

        val data = mapOf(
            "id" to canteenId,
            "ownerId" to savedCanteen.ownerId,
            "name" to savedCanteen.name,
            "description" to savedCanteen.description,
            "college" to savedCanteen.college
        )

        firestore.collection(CANTEENS)
            .document(canteenId)
            .set(data)
            .addOnFailureListener { /* Local canteen is already saved; sync can retry later. */ }

        return Result.success(Unit)
    }

    override suspend fun getMenuItems(canteenId: String): Result<List<MenuItem>> {
        val localItems = localMenuStore.getAll(canteenId)
        return try {
            withTimeout(FIRESTORE_READ_TIMEOUT_MS) {
                menuCollection(canteenId).get().awaitResult()
            }.fold(
                onSuccess = { snapshot ->
                    val cloudItems = snapshot.documents.map { it.toMenuItem(canteenId) }
                    if (cloudItems.isNotEmpty()) {
                        cloudItems.forEach { localMenuStore.save(it) }
                        Result.success(cloudItems)
                    } else {
                        Result.success(localItems)
                    }
                },
                onFailure = { Result.success(localItems) }
            )
        } catch (_: TimeoutCancellationException) {
            Result.success(localItems)
        }
    }

    override suspend fun saveMenuItem(item: MenuItem): Result<Unit> {
        val savedItem = localMenuStore.save(item)

        val data = mapOf(
            "id" to savedItem.id,
            "canteenId" to savedItem.canteenId,
            "name" to savedItem.name,
            "description" to savedItem.description,
            "priceCents" to savedItem.priceCents,
            "isAvailable" to savedItem.isAvailable
        )

        menuCollection(savedItem.canteenId)
            .document(savedItem.id)
            .set(data)
            .addOnFailureListener { /* Local menu item is already saved; sync can retry later. */ }

        return Result.success(Unit)
    }

    override suspend fun deleteMenuItem(canteenId: String, itemId: String): Result<Unit> {
        localMenuStore.delete(canteenId, itemId)

        menuCollection(canteenId)
            .document(itemId)
            .delete()
            .addOnFailureListener { /* Local delete already applied. */ }

        return Result.success(Unit)
    }

    override suspend fun placeOrder(order: Order): Result<Unit> {
        val orderId = order.id.ifBlank { "order_${System.currentTimeMillis()}" }
        val savedOrder = order.copy(id = orderId)

        return try {
            withTimeout(FIRESTORE_WRITE_TIMEOUT_MS) {
                firestore.collection(ORDERS)
                    .document(savedOrder.id)
                    .set(savedOrder.toFirestoreMap())
                    .awaitResult()
            }.fold(
                onSuccess = {
                    localOrderStore.save(savedOrder)
                    Result.success(Unit)
                },
                onFailure = { exception ->
                    Result.failure(
                        exception.takeIf { it.message != null }
                            ?: Exception("Could not send order to canteen. Check internet and Firestore rules.")
                    )
                }
            )
        } catch (_: TimeoutCancellationException) {
            Result.failure(
                Exception("Could not send order to canteen. Check internet and try again.")
            )
        }
    }

    override suspend fun getOrdersForUser(userId: String): Result<List<Order>> {
        val localOrders = localOrderStore.getForUser(userId)
        return fetchOrdersFromCloud(
            localOrders = localOrders,
            queryOrders = {
                firestore.collection(ORDERS)
                    .whereEqualTo("userId", userId)
                    .get()
                    .awaitResult()
                    .map { snapshot -> snapshot.documents.map { it.toOrder() } }
            }
        )
    }

    override suspend fun getOrdersForCanteen(canteenId: String): Result<List<Order>> {
        val localOrders = localOrderStore.getForCanteen(canteenId)
        return fetchOrdersFromCloud(
            localOrders = localOrders,
            queryOrders = {
                firestore.collection(ORDERS)
                    .whereEqualTo("canteenId", canteenId)
                    .get()
                    .awaitResult()
                    .map { snapshot -> snapshot.documents.map { it.toOrder() } }
            }
        )
    }

    private suspend fun fetchOrdersFromCloud(
        localOrders: List<Order>,
        queryOrders: suspend () -> Result<List<Order>>
    ): Result<List<Order>> =
        try {
            withTimeout(FIRESTORE_READ_TIMEOUT_MS) {
                queryOrders()
            }.fold(
                onSuccess = { cloudOrders ->
                    cloudOrders.forEach { localOrderStore.save(it) }
                    Result.success(mergeOrders(cloudOrders, localOrders))
                },
                onFailure = { exception ->
                    if (localOrders.isNotEmpty()) {
                        Result.success(localOrders)
                    } else {
                        Result.failure(
                            exception.takeIf { it.message != null }
                                ?: Exception("Could not load orders from cloud.")
                        )
                    }
                }
            )
        } catch (_: TimeoutCancellationException) {
            if (localOrders.isNotEmpty()) {
                Result.success(localOrders)
            } else {
                Result.failure(Exception("Loading orders timed out. Pull to refresh."))
            }
        }

    private fun mergeOrders(cloudOrders: List<Order>, localOrders: List<Order>): List<Order> =
        (cloudOrders + localOrders)
            .distinctBy { it.id }
            .sortedByDescending { it.createdAt }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> {
        localOrderStore.updateStatus(orderId, status)

        firestore.collection(ORDERS)
            .document(orderId)
            .update("status", status.name)
            .addOnFailureListener { /* Local status is already updated. */ }

        return Result.success(Unit)
    }

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
        const val FIRESTORE_READ_TIMEOUT_MS = 15_000L
        const val FIRESTORE_WRITE_TIMEOUT_MS = 15_000L
    }
}
