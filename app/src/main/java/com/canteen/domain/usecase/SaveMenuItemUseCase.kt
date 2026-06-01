package com.canteen.domain.usecase

import com.canteen.domain.model.MenuItem
import com.canteen.domain.repository.CampusRepository

class SaveMenuItemUseCase(
    private val repository: CampusRepository
) {
    suspend operator fun invoke(item: MenuItem): Result<Unit> =
        repository.saveMenuItem(item)
}
