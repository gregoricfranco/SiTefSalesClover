package br.com.gregori.sdk.dto

/** DTO para os campos retornados do SiTef */
data class ReturnedFieldsDTO(
    val functionId: String = "",
    val terminal: String = "",
    val cartao: String = "",
    val valorTransacao: Long = 0L,
    val dataTransacao: String = "",
    val horaTransacao: String = "",
    val formaPagamento: String = "",
    val nsu: String = "",
    val autorizacao: String = ""
)