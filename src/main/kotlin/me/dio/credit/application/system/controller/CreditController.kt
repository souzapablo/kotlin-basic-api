package me.dio.credit.application.system.controller

import jakarta.validation.Valid
import me.dio.credit.application.system.dto.CreditDto
import me.dio.credit.application.system.dto.CreditListView
import me.dio.credit.application.system.dto.CreditView
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.service.impl.CreditService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.stream.Collectors

@RestController
@RequestMapping("api/v1/credits")
class CreditController(
    private val creditService: CreditService
) {
    @PostMapping
    fun saveCredit(@RequestBody @Valid creditDto: CreditDto): ResponseEntity<CreditView> {
        val credit: Credit = this.creditService.save(creditDto.toEntity())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CreditView(credit))
    }

    @GetMapping
    fun findAllByCustomerId(@RequestParam(value = "customerId") customerId: Long): ResponseEntity<List<CreditListView>> {
        val customerListView = this.creditService.findAllByCustomer(customerId).stream()
            .map { credit: Credit -> CreditListView(credit) }
            .collect(Collectors.toList())
        return ResponseEntity.status(HttpStatus.OK)
            .body(customerListView)
    }


    @GetMapping("/{creditCode}")
    fun findByCreditCode(
        @RequestParam(value = "customerId") customerId: Long,
        @PathVariable creditCode: UUID
    ): ResponseEntity<CreditView> {
        val foundCredit: Credit = this.creditService.findByCreditCode(customerId, creditCode)
        val creditView = CreditView(foundCredit)
        return ResponseEntity.status(HttpStatus.OK)
            .body(creditView)
    }
}