package com.example.iudigitalradio.domain.usecase

import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.domain.repository.RadioRepository
import javax.inject.Inject

class SearchStationsUseCase @Inject constructor(
    private val repository: RadioRepository
) {
    suspend operator fun invoke(query: String): Result<List<RadioStation>> =
        repository.searchStations(query)
}
