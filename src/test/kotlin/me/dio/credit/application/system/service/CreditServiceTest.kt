package me.dio.credit.application.system.service

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.service.impl.CreditService
import me.dio.credit.application.system.service.impl.CustomerService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.*

@ExtendWith(MockKExtension::class)
class CreditServiceTest {
    @MockK
    lateinit var creditRepository: CreditRepository

    @MockK
    lateinit var customerService: CustomerService

    @InjectMockKs
    lateinit var creditService: CreditService

    @Test
    fun `should create credit`() {
        //given
        val fakeCustomer: Customer = buildCustomer()
        val fakeCredit: Credit = buildCredit(customer = fakeCustomer)
        every { customerService.findById(1L) } returns fakeCustomer
        every { creditRepository.save(any()) } returns fakeCredit
        //when
        val sut: Credit = creditService.save(fakeCredit)
        //then
        Assertions.assertThat(sut).isNotNull
        Assertions.assertThat(sut).isSameAs(fakeCredit)
        verify(exactly = 1) {
            creditRepository.save(fakeCredit)
        }
    }

    @Test
    fun `should find credits by customer`() {
        //given
        val fakeCredit: Credit = buildCredit(customer = buildCustomer())
        every { customerService.customerExists(1L) } returns true
        every { creditRepository.findAllByCustomer(1L) } returns listOf(fakeCredit)
        //when
        val sut: List<Credit> = creditService.findAllByCustomer(1L)
        //then
        Assertions.assertThat(sut).isNotNull
        Assertions.assertThat(sut).contains(fakeCredit)
        Assertions.assertThat(sut).size().isEqualTo(1)
        verify(exactly = 1) {
            creditRepository.findAllByCustomer(1L)
        }
    }

    @Test
    fun `should throw BusinessException when customer is invalid`() {
        //given
        val customerId = 1L
        every { customerService.customerExists(customerId) } returns false
        //when
        //then
        Assertions.assertThatExceptionOfType(BusinessException::class.java)
            .isThrownBy { creditService.findAllByCustomer(customerId) }.withMessage("Customer $customerId not found")
    }

    @Test
    fun `should find credit by creditCode`() {
        //given
        val fakeCredit: Credit = buildCredit(customer = buildCustomer())
        every { creditRepository.findByCreditCode(any()) } returns fakeCredit
        //when
        val sut: Credit = creditService.findByCreditCode(1, fakeCredit.creditCode)
        //then
        Assertions.assertThat(sut).isSameAs(fakeCredit)
        Assertions.assertThat(sut).isNotNull
        verify(exactly = 1) {
            creditRepository.findByCreditCode(any())
        }
    }
    @Test
    fun `should throw BusinessException when wrong id`() {
        //given
        val fakeCredit: Credit = buildCredit(customer = buildCustomer())
        every { creditRepository.findByCreditCode(any()) } returns fakeCredit
        //when
        //then
        Assertions.assertThatExceptionOfType(BusinessException::class.java)
            .isThrownBy { creditService.findByCreditCode(2, fakeCredit.creditCode) }
            .withMessage("Contact admin")
    }

    @Test
    fun `should throw BusinessException when credit not found`() {
        //given
        val creditCode = UUID.randomUUID()
        every { creditRepository.findByCreditCode(any()) } returns null
        //when
        //then
        Assertions.assertThatExceptionOfType(BusinessException::class.java)
            .isThrownBy { creditService.findByCreditCode(2, creditCode) }
            .withMessage("CreditCode $creditCode not found")
    }

    private fun buildCredit(
        creditValue: BigDecimal = BigDecimal.valueOf(56.0),
        dayOfFirstInstallment: LocalDate = LocalDate.of(2023, Month.APRIL, 27),
        numberOfInstallments: Int = 3,
        customer: Customer
    ): Credit = Credit(
        creditValue = creditValue,
        dayFirstInstallment = dayOfFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customer = customer
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