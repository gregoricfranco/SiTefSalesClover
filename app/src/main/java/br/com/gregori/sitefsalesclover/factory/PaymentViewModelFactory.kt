package br.com.gregori.sitefsalesclover.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.gregori.sdk.core.SiTefPayment
import br.com.gregori.sitefsalesclover.viewmodel.PaymentViewModel

class PaymentViewModelFactory(
    private val siTefPayment: SiTefPayment
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(siTefPayment) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
