package com.example.labenza.data.repository

import com.example.labenza.data.model.RegionalAverages

/**
 * Abstraction over the regional-average price source. The production
 * implementation is [AveragePriceRepository].
 */
interface AveragePriceDataSource {
    suspend fun getRegionalAverages(): Result<RegionalAverages>
}
