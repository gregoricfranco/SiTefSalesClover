package br.com.gregori.sdk.models

import android.os.Bundle
import br.com.gregori.sdk.dto.ReturnedFieldsDTO
import br.com.gregori.sdk.utils.DateUtils

data class PaymentResult(
    val responseCode: String? = null,
    val transactionType: String? = null,
    val installmentType: String? = null,
    val cashbackAmount: String? = null,
    val acquirerId: String? = null,
    val cardBrand: String? = null,
    val sitefTransactionId: String? = null,
    val hostTransactionId: String? = null,
    val authorizationCode: String? = null,
    val transactionInstallments: String? = null,
    val merchantReceipt: String? = null,
    val customerReceipt: String? = null,
    val returnedFields: String? = null,
    val transactionAmount: String?,   // valor da transação em centavos
    val invoiceNumber: String?,       // número do documento
    val invoiceDate: String?,         // data da transação (DDMMAAAA)
    val paymentMethodDescription: String? = null,

) {
    companion object {
        /**
         * Constrói PaymentResult a partir de Bundle do SiTef e campos retornados.
         */
        fun fromExtras(extras: Bundle, campos: ReturnedFieldsDTO): PaymentResult {
            val transactionAmount = campos.valorTransacao.toString()
            val invoiceNumber = DateUtils.generateGenericInvoiceNumber(campos.nsu)
            val invoiceDate = DateUtils.formatTransactionDateForCancel(campos.dataTransacao)

            return PaymentResult(
                responseCode = extras.getString("responseCode"),
                transactionType = extras.getString("transactionType"),
                installmentType = extras.getString("installmentType"),
                cashbackAmount = extras.getString("cashbackAmount"),
                acquirerId = extras.getString("acquirerId"),
                cardBrand = extras.getString("cardBrand"),
                sitefTransactionId = extras.getString("sitefTransactionId"),
                hostTransactionId = extras.getString("hostTransactionId"),
                authorizationCode = extras.getString("authCode"),
                transactionInstallments = extras.getString("transactionInstallments"),
                merchantReceipt = extras.getString("merchantReceipt"),
                customerReceipt = extras.getString("customerReceipt"),
                returnedFields = extras.getString("returnedFields"),
                transactionAmount = transactionAmount,
                invoiceNumber = invoiceNumber,
                invoiceDate = invoiceDate,
                paymentMethodDescription = campos.formaPagamento

            )
        }
    }
}
