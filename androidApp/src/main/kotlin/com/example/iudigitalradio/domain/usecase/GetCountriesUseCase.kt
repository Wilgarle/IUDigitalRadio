package com.example.iudigitalradio.domain.usecase

import com.example.iudigitalradio.domain.model.Country
import com.example.iudigitalradio.domain.repository.RadioRepository
import javax.inject.Inject

class GetCountriesUseCase @Inject constructor(
    private val repository: RadioRepository
) {
    suspend operator fun invoke(): Result<List<Country>> = repository.getCountries()
}
