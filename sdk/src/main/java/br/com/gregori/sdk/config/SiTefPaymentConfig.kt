package br.com.gregori.sdk.config

data class SiTefPaymentConfig(
    val merchantTaxId: String,
    val isvTaxId: String,
    val userInputTimeout: Int = 60
)
