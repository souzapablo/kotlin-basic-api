package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.CreditDto
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.enumeration.Status
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.service.impl.CustomerService
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
import java.time.LocalDate
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditControllerTest {
    @Autowired
    private lateinit var creditRepository: CreditRepository

    @Autowired
    private lateinit var customerService: CustomerService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var customer: Customer
    private var hasCustomer: Boolean = false

    companion object {
        const val URL: String = "/api/v1/credits"
    }

    @BeforeEach
    fun setup() {
        if (!hasCustomer) {
            customer = customerService.save(buildCustomer())
            hasCustomer = true
        }


        creditRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        creditRepository.deleteAll()
    }

    @Test
    fun `should create credit and return status 201`() {
        //given
        val creditDto: CreditDto = buildCreditDto()
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditValue").value(BigDecimal.valueOf(25.0)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfInstallments").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(Status.IN_PROGRESS.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.customerEmail").value("pablo@email.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.customerIncome").value(BigDecimal.valueOf(33.0)))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not create credit and return status 400`() {
        //given
        val creditDto: CreditDto = buildCreditDto(customerId = 2)
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)
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
    fun `should find credit and return status 200`() {
        //given
        val credit: Credit = creditRepository.save(buildCreditDto().toEntity())
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL/${credit.creditCode}?customerId=${customer.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditCode").value(credit.creditCode.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditValue").value(BigDecimal.valueOf(25.0)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfInstallments").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(Status.IN_PROGRESS.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.customerEmail").value("pablo@email.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.customerIncome").value(BigDecimal.valueOf(33.0)))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should throw BusinessException with invalid creditCode and return status 400`() {
        //given
        val creditCode: UUID = UUID.randomUUID()
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL/${creditCode}?customerId=${customer.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request: consult the documentation"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should throw BusinessException with invalid customerId and return status 400`() {
        //given
        val credit: Credit = creditRepository.save(buildCreditDto().toEntity())
        val customerId: Long = 2
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL/${credit.creditCode}?customerId=$customerId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request: consult the documentation"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find customer credits and return status 200`() {
        //given
        creditRepository.save(buildCreditDto().toEntity())
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL?customerId=${customer.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find customer credits and return status 400`() {
        //given
        val customerId: Long = 2
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL?customerId=$customerId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request: consult the documentation"))
            .andDo(MockMvcResultHandlers.print())
    }

    private fun buildCreditDto(
        creditValue: BigDecimal = BigDecimal.valueOf(25.0),
        numberOfInstallments: Int = 2,
        dayOfFirstInstallment: LocalDate = LocalDate.now().plusDays(15),
        customerId: Long = 1
    ): CreditDto = CreditDto(
        creditValue = creditValue,
        numberOfInstallments = numberOfInstallments,
        dayOfFirstInstallment = dayOfFirstInstallment,
        customerId = customerId
    )

    private fun buildCustomer(
        firstName: String = "Pablo",
        lastName: String = "Souza",
        cpf: String = "82842151011",
        email: String = "pablo@email.com",
        password: String = "447788",
        zipCode: String = "3333",
        street: String = "Rua dos bobos",
        income: BigDecimal = BigDecimal.valueOf(33.0),
        id: Long = 1L
    ) = Customer(
        firstName = firstName, lastName = lastName, cpf = cpf, email = email, password = password, address = Address(
            zipCode = zipCode, street = street
        ), income = income, id = id
    )
}