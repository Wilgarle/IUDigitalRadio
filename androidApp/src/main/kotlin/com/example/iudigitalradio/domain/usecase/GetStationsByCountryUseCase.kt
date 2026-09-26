package com.example.iudigitalradio.domain.usecase

import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.domain.repository.RadioRepository
import javax.inject.Inject

class GetStationsByCountryUseCase @Inject constructor(
    private val repository: RadioRepository
) {
    suspend operator fun invoke(countryCode: String): Result<List<RadioStation>> =
        repository.getStationsByCountry(countryCode)
}
