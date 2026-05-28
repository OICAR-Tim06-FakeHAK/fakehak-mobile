package hr.algebra.myapplication.repository

import com.google.common.truth.Truth.assertThat
import hr.algebra.myapplication.api.ApiService
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.VehicleProfile
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class VehicleRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ApiService
    private lateinit var repo: VehicleRepository

    @Before fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        repo = VehicleRepository(api)
    }

    @After fun tearDown() { server.shutdown() }

    @Test fun `getVehicles returns parsed list`() = runTest {
        val body = """
            [{"id":1,"brand":"BMW","model":"3","vin":"V1","registrationPlate":"P1",
              "firstRegistrationDate":"2024-01-01","createdAt":"2024-01-01"}]
        """.trimIndent()
        server.enqueue(jsonOk(body))

        val result = repo.getVehicles(userId = 7) as ApiResult.Success

        assertThat(result.data).hasSize(1)
        assertThat(result.data[0].brand).isEqualTo("BMW")
        assertThat(server.takeRequest().path).isEqualTo("/api/users/7/vehicles")
    }

    @Test fun `addVehicle POSTs body and returns created vehicle`() = runTest {
        val responseBody = """
            {"id":2,"brand":"Audi","model":"A4","vin":"V2","registrationPlate":"P2",
             "firstRegistrationDate":"2024-02-02","createdAt":"2024-02-02"}
        """.trimIndent()
        server.enqueue(jsonOk(responseBody))

        val newVehicle = VehicleProfile(
            id = 0, brand = "Audi", model = "A4", vin = "V2",
            registrationPlate = "P2", firstRegistrationDate = "2024-02-02",
            createdAt = "",
        )
        val result = repo.addVehicle(userId = 7, vehicle = newVehicle) as ApiResult.Success

        assertThat(result.data.id).isEqualTo(2)
        val recorded = server.takeRequest()
        assertThat(recorded.method).isEqualTo("POST")
        assertThat(recorded.path).isEqualTo("/api/users/7/vehicles")
        assertThat(recorded.body.readUtf8()).contains("\"vin\":\"V2\"")
    }

    @Test fun `updateVehicle PUTs to nested path`() = runTest {
        server.enqueue(
            jsonOk(
                """{"id":2,"brand":"Audi","model":"A4","vin":"V2","registrationPlate":"P2",
                    "firstRegistrationDate":"2024-02-02","createdAt":"2024-02-02"}""".trimIndent()
            )
        )

        val result = repo.updateVehicle(
            userId = 7, vehicleId = 2,
            vehicle = VehicleProfile(2, "Audi", "A4", "V2", "P2", "2024-02-02", ""),
        )

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        val recorded = server.takeRequest()
        assertThat(recorded.method).isEqualTo("PUT")
        assertThat(recorded.path).isEqualTo("/api/users/7/vehicles/2")
    }

    @Test fun `deleteVehicle issues DELETE`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))

        val result = repo.deleteVehicle(userId = 7, vehicleId = 2)

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        val recorded = server.takeRequest()
        assertThat(recorded.method).isEqualTo("DELETE")
        assertThat(recorded.path).isEqualTo("/api/users/7/vehicles/2")
    }

    @Test fun `addVehicle 400 maps to Error`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(400)
                .setBody("""{"timestamp":"t","status":400,"message":"bad vin"}""")
        )

        val result = repo.addVehicle(
            userId = 7,
            vehicle = VehicleProfile(0, "Audi", "A4", "", "", "", ""),
        ) as ApiResult.Error

        assertThat(result.apiError?.status).isEqualTo(400)
        assertThat(result.apiError?.message).isEqualTo("bad vin")
    }

    private fun jsonOk(body: String) = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body)
}
