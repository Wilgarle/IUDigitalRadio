package com.example.iudigitalradio.domain.usecase

import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.domain.repository.RadioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteStationsUseCase @Inject constructor(
    private val repository: RadioRepository
) {
    operator fun invoke(): Flow<List<RadioStation>> = repository.getFavorites()
}
