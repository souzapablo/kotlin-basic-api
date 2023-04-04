package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.CustomerDto
import me.dio.credit.application.system.dto.CustomerUpdateDto
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CustomerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CustomerControllerTest {
    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/v1/customers"
    }

    @BeforeEach
    fun setup() = customerRepository.deleteAll()

    @AfterEach
    fun tearDown() = customerRepository.deleteAll()

    @Test
    fun `should create a customer and return status 201`() {
        //given
        val customerDto: CustomerDto = buildCustomerDto()
        val valueAsString: String = objectMapper.writeValueAsString(customerDto)
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Pablo"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Souza"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("82842151011"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("pablo@email.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("3333"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Rua dos bobos"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.income").value(33.0))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not create customer with already registered CPF and return status 409`() {
        //given
        customerRepository.save(buildCustomerDto().toEntity())
        val customerDto: CustomerDto = buildCustomerDto()
        val valueAsString: String = objectMapper.writeValueAsString(customerDto)
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Conflict: consult the documentation"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not create customer with empty first name and return status 400`() {
        //given
        val customerDto: CustomerDto = buildCustomerDto(firstName = "")
        val valueAsString: String = objectMapper.writeValueAsString(customerDto)
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request: consult the documentation"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find customer by id and return status 200`() {
        //given
        val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL/${customer.id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Pablo"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Souza"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("82842151011"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("pablo@email.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("3333"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Rua dos bobos"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.income").value(33.0))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find customer by invalid id and return `() {
        //given
        val invalidId = 2L
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL/${invalidId}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request: consult the documentation"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should delete customer and return status 204`() {
        //given
        val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.delete("$URL/${customer.id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNoContent)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not delete invalid customer and return status 400`() {
        //given
        val id = 2L
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.delete("$URL/${id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request: consult the documentation"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should update customer and return status 200`() {
        //given
        val customer: Customer = customerRepository.save(buildCustomerDto().toEntity())
        val updateCustomerDto: CustomerUpdateDto = buildCustomerUpdateDto()
        val valueAsString: String = objectMapper.writeValueAsString(updateCustomerDto)
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.patch("${URL}?customerId=${customer.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("PabloUpdate"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("SouzaUpdate"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("82842151011"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("pablo@email.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("65465"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Rua atualizada"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.income").value(500.0))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not update invalid customer and return status 400`() {
        //given
        val customerId = 1L
        val updateCustomerDto: CustomerUpdateDto = buildCustomerUpdateDto()
        val valueAsString: String = objectMapper.writeValueAsString(updateCustomerDto)
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.patch("${URL}?customerId=${customerId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request: consult the documentation"))
            .andDo(MockMvcResultHandlers.print())
    }

    private fun buildCustomerDto(
        firstName: String = "Pablo",
        lastName: String = "Souza",
        cpf: String = "82842151011",
        email: String = "pablo@email.com",
        password: String = "447788",
        zipCode: String = "3333",
        street: String = "Rua dos bobos",
        income: BigDecimal = BigDecimal.valueOf(33.0)
    ) = CustomerDto(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        password = password,
        zipCode = zipCode,
        street = street,
        income = income
    )

    private fun buildCustomerUpdateDto(
        firstName: String = "PabloUpdate",
        lastName: String = "SouzaUpdate",
        income: BigDecimal = BigDecimal.valueOf(500.0),
        zipCode: String = "65465",
        street: String = "Rua atualizada"
    ): CustomerUpdateDto = CustomerUpdateDto(
        firstName = firstName,
        lastName = lastName,
        income = income,
        zipCode = zipCode,
        street = street
    )
}