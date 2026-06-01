package com.canteen.domain.usecase

import com.canteen.domain.repository.CampusRepository

class GetCollegesUseCase(
    private val repository: CampusRepository
) {
    operator fun invoke(): List<String> = repository.colleges()
}
