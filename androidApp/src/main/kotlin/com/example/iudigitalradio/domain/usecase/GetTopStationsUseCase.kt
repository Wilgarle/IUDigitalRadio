package com.example.iudigitalradio.domain.usecase

import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.domain.repository.RadioRepository
import javax.inject.Inject

class GetTopStationsUseCase @Inject constructor(
    private val repository: RadioRepository
) {
    suspend operator fun invoke(): Result<List<RadioStation>> = repository.getTopStations()
}
