package br.com.gregori.sdk.enums

enum class PaymentMethod(val code: String) {
    CREDIT("3"),                  // Crédito à vista
    CREDIT_INSTALLMENTS("3"),     // Crédito parcelado
    DEBIT("2"),                   // Débito
    PIX("122"),                   // PIX / Carteira digital

    CREDIT_CANCEL("210"),         // Cancelamento Crédito
    DEBIT_CANCELLATION("211"),    // Cancelamento Débito
    PIX_CANCELLATION("123");      // Cancelamento PIX / Carteira digital
}
