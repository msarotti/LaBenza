package com.example.labenza

import com.example.labenza.data.repository.FuelRepository
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests [FuelRepository] against a local [MockWebServer], exercising the real
 * Retrofit + serialization stack without hitting the network. Verifies the search
 * radius is sent and that responses are parsed and sorted by distance.
 */
class FuelRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: FuelRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = FuelRepository(baseUrl = server.url("/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getNearbyStations_sendsRequestedRadiusInBody() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"success":true,"results":[]}"""))

        repository.getNearbyStations(45.0, 9.0, radiusKm = 17)

        val body = server.takeRequest().body.readUtf8()
        assertTrue("Radius missing from request body: $body", body.contains("\"radius\":17"))
    }

    @Test
    fun getNearbyStations_parsesResultsSortedByDistance() = runBlocking {
        val json = """
            {"success":true,"results":[
              {"id":1,"distance":"3.0","fuels":[{"fuelId":1,"price":1.9}]},
              {"id":2,"distance":"1.0","fuels":[{"fuelId":1,"price":1.8}]}
            ]}
        """.trimIndent()
        server.enqueue(MockResponse().setBody(json))

        val result = repository.getNearbyStations(45.0, 9.0, radiusKm = 5)

        assertTrue(result.isSuccess)
        assertEquals(listOf(2L, 1L), result.getOrThrow().map { it.id })
    }

    @Test
    fun getNearbyStations_failsWhenServiceReportsNoSuccess() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"success":false,"results":[]}"""))

        val result = repository.getNearbyStations(45.0, 9.0, radiusKm = 5)

        assertTrue(result.isFailure)
    }
}
