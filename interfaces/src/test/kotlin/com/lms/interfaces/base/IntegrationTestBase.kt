package com.lms.interfaces.base

import com.fasterxml.jackson.databind.ObjectMapper
import com.lms.infrastructure.security.jwt.JwtTokenProvider
import com.lms.interfaces.config.TestConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig::class)
abstract class IntegrationTestBase : FunSpec() {

    override fun extensions() = listOf(SpringExtension)

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var jwtTokenProvider: JwtTokenProvider

    /**
     * Generate test JWT token
     */
    protected fun generateToken(employeeId: String, role: String, storeId: String? = null): String =
        jwtTokenProvider.generateAccessToken(employeeId, role, storeId)

    /**
     * POST request helper
     */
    protected fun post(url: String, body: Any, token: String? = null): ResultActions = mockMvc.perform(
        MockMvcRequestBuilders.post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body))
            .apply { token?.let { header("Authorization", "Bearer $it") } }
    ).andDo(MockMvcResultHandlers.print())

    /**
     * GET request helper
     */
    protected fun get(url: String, token: String? = null): ResultActions = mockMvc.perform(
        MockMvcRequestBuilders.get(url)
            .contentType(MediaType.APPLICATION_JSON)
            .apply { token?.let { header("Authorization", "Bearer $it") } }
    ).andDo(MockMvcResultHandlers.print())

    /**
     * PUT request helper
     */
    protected fun put(url: String, body: Any, token: String? = null): ResultActions = mockMvc.perform(
        MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body))
            .apply { token?.let { header("Authorization", "Bearer $it") } }
    ).andDo(MockMvcResultHandlers.print())

    /**
     * DELETE request helper
     */
    protected fun delete(url: String, token: String? = null): ResultActions = mockMvc.perform(
        MockMvcRequestBuilders.delete(url)
            .contentType(MediaType.APPLICATION_JSON)
            .apply { token?.let { header("Authorization", "Bearer $it") } }
    ).andDo(MockMvcResultHandlers.print())

    /**
     * Parse JSON response
     */
    protected fun <T> parseResponse(jsonContent: String, clazz: Class<T>): T =
        objectMapper.readValue(jsonContent, clazz)

    /**
     * Extension function to add authorization header
     */
    private fun MockHttpServletRequestBuilder.apply(
        block: MockHttpServletRequestBuilder.() -> Unit
    ): MockHttpServletRequestBuilder {
        block()
        return this
    }
}
