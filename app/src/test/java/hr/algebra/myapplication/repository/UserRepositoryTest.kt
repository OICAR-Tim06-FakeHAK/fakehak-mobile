package hr.algebra.myapplication.repository

import com.google.common.truth.Truth.assertThat
import hr.algebra.myapplication.api.ApiService
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.CaseReport
import hr.algebra.myapplication.models.LoginRequest
import hr.algebra.myapplication.models.RegisterRequest
import hr.algebra.myapplication.models.UserProfileUpdate
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ApiService
    private lateinit var repo: UserRepository

    @Before fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        repo = UserRepository(api)
    }

    @After fun tearDown() { server.shutdown() }

    // ─── login ────────────────────────────────────────────────────────────────

    @Test fun `login returns Success on 200`() = runTest {
        server.enqueue(jsonOk("""{"token":"abc.def.ghi","role":"USER"}"""))

        val result = repo.login(LoginRequest("u@test", "pw"))

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        assertThat((result as ApiResult.Success).data.token).isEqualTo("abc.def.ghi")
        assertThat(result.data.role).isEqualTo("USER")
    }

    @Test fun `login maps 401 to Error with parsed ApiError`() = runTest {
        val body = """{"timestamp":"2024-01-01T00:00:00Z","status":401,"message":"bad creds"}"""
        server.enqueue(MockResponse().setResponseCode(401).setBody(body))

        val result = repo.login(LoginRequest("u@test", "pw"))

        val err = result as ApiResult.Error
        assertThat(err.apiError?.status).isEqualTo(401)
        assertThat(err.apiError?.message).isEqualTo("bad creds")
    }

    @Test fun `login maps 500 with non-JSON body to Error without ApiError`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("oops"))

        val result = repo.login(LoginRequest("u@test", "pw"))

        val err = result as ApiResult.Error
        assertThat(err.message).isEqualTo("oops")
    }

    @Test fun `login on IOException returns Error`() = runTest {
        server.shutdown() // force connection failure

        val result = repo.login(LoginRequest("u@test", "pw"))

        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
    }

    // ─── register ─────────────────────────────────────────────────────────────

    @Test fun `register success returns Success of Unit`() = runTest {
        server.enqueue(MockResponse().setResponseCode(201))

        val result = repo.register(
            RegisterRequest(
                firstName = "F",
                lastName = "L",
                phoneNumber = "+1",
                email = "e@e.test",
                password = "pw",
            )
        )

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
    }

    // ─── getUserProfile ──────────────────────────────────────────────────────

    @Test fun `getUserProfile parses the body`() = runTest {
        val body = """
            {
              "id": 7, "firstName":"F", "lastName":"L",
              "phoneNumber":"+1", "email":"e@e.test",
              "accountStatus":"ACTIVE", "vehicles":[], "createdAt":"2024-01-01"
            }
        """.trimIndent()
        server.enqueue(jsonOk(body))

        val result = repo.getUserProfile() as ApiResult.Success

        assertThat(result.data.id).isEqualTo(7)
        assertThat(result.data.firstName).isEqualTo("F")
        assertThat(result.data.vehicles).isEmpty()
    }

    // ─── updateUser ──────────────────────────────────────────────────────────

    @Test fun `updateUser PUTs request body`() = runTest {
        val body = """
            {
              "id":7,"firstName":"NewF","lastName":"L",
              "phoneNumber":"+1","email":"e@e.test",
              "accountStatus":"ACTIVE","vehicles":[],"createdAt":"2024-01-01"
            }
        """.trimIndent()
        server.enqueue(jsonOk(body))

        val result = repo.updateUser(7, UserProfileUpdate("NewF", "L", "+1", "e@e.test"))

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        val recorded = server.takeRequest()
        assertThat(recorded.method).isEqualTo("PUT")
        assertThat(recorded.path).isEqualTo("/api/users/7")
        assertThat(recorded.body.readUtf8()).contains("\"firstName\":\"NewF\"")
    }

    // ─── createReport ────────────────────────────────────────────────────────

    @Test fun `createReport POSTs to api cases`() = runTest {
        server.enqueue(MockResponse().setResponseCode(201))

        val result = repo.createReport(
            userId = 7,
            caseReport = CaseReport(7, 3, 10.0, 20.0, "scratch"),
        )

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        val recorded = server.takeRequest()
        assertThat(recorded.method).isEqualTo("POST")
        assertThat(recorded.path).isEqualTo("/api/cases")
    }

    // ─── getUserCases ────────────────────────────────────────────────────────

    @Test fun `getUserCases parses list`() = runTest {
        val body = """
            [
              {"id":1,"userName":"Foo","vehicleInfo":"BMW (HR-1)","latitude":1.0,
               "longitude":2.0,"status":"ACTIVE","assignedEmployeeName":null,
               "createdAt":"2024-01-01"},
              {"id":2,"userName":"Foo","vehicleInfo":"Audi (HR-2)","latitude":3.0,
               "longitude":4.0,"status":"RESOLVED","assignedEmployeeName":"Bob",
               "createdAt":"2024-01-02"}
            ]
        """.trimIndent()
        server.enqueue(jsonOk(body))

        val result = repo.getUserCases(userId = 7) as ApiResult.Success

        assertThat(result.data).hasSize(2)
        assertThat(result.data[0].status).isEqualTo("ACTIVE")
        assertThat(result.data[1].assignedEmployeeName).isEqualTo("Bob")
        val recorded = server.takeRequest()
        assertThat(recorded.path).isEqualTo("/api/cases/user/7")
    }

    private fun jsonOk(body: String) = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body)
}
