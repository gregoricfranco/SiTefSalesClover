package br.com.gregori.sdk.callbacks

import br.com.gregori.sdk.models.PaymentResult

interface PaymentCallback {
    fun onSuccess(result: PaymentResult)
    fun onFailure(errorMessage: String)
}
