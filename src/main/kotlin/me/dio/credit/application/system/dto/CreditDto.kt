package me.dio.credit.application.system.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotNull
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import java.math.BigDecimal
import java.time.LocalDate

data class CreditDto(
    @field:NotNull(message = "Credit value can't be null") val creditValue: BigDecimal,
    @field:NotNull(message = "Day of first installment can't be null")
    @field:Future(message = "Day of first installment must be in the future")
    val dayOfFirstInstallment: LocalDate,
    @field:NotNull(message = "Number of installments can't be null")
    @field:Max(48, message = "Max number of installments is 48") val numberOfInstallments: Int,
    val customerId: Long,
) {
    fun toEntity(): Credit = Credit(
        creditValue = this.creditValue,
        dayFirstInstallment = this.dayOfFirstInstallment,
        numberOfInstallments = this.numberOfInstallments,
        customer = Customer(id = this.customerId)
    )
}
