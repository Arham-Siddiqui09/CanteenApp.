package com.canteen.domain.usecase

import com.canteen.domain.model.MenuItem
import com.canteen.domain.repository.CampusRepository

class GetMenuItemsUseCase(
    private val repository: CampusRepository
) {
    suspend operator fun invoke(canteenId: String): Result<List<MenuItem>> =
        repository.getMenuItems(canteenId)
}
