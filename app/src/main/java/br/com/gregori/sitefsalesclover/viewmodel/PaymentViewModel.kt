package br.com.gregori.sitefsalesclover.viewmodel

import androidx.lifecycle.ViewModel
import br.com.gregori.sdk.callbacks.PaymentCallback
import br.com.gregori.sdk.core.SiTefPayment
import br.com.gregori.sdk.enums.PaymentMethod
import br.com.gregori.sdk.models.PaymentResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PaymentViewModel(
    private val siTefPayment: SiTefPayment
) : ViewModel() {

    var valor: Int = 1000 // R$10,00

    private val _resultadoPagamento = MutableStateFlow("Nenhum pagamento realizado")
    val resultadoPagamento: StateFlow<String> = _resultadoPagamento

    private val _transacoes = MutableStateFlow<List<PaymentResult>>(emptyList())
    val transacoes: StateFlow<List<PaymentResult>> = _transacoes

    private val _ultimoPagamento = MutableStateFlow<PaymentResult?>(null)
    val ultimoPagamento: StateFlow<PaymentResult?> = _ultimoPagamento

    // ===============================================
    // Função única: iniciar pagamento com callback obrigatório
    // ===============================================
    fun iniciarPagamento(
        valorCentavos: Int,
        metodo: PaymentMethod,
        invoiceNumber: String = "1",
        installments: Int = 1,
        callback: PaymentCallback
    ) {
        siTefPayment.pagar(valorCentavos, metodo, invoiceNumber, installments, object : PaymentCallback {
            override fun onSuccess(result: PaymentResult) {
                atualizarEstados(result)
                callback.onSuccess(result) // repassa para quem chamou
            }

            override fun onFailure(errorMessage: String) {
                _resultadoPagamento.value = "Falha: $errorMessage"
                callback.onFailure(errorMessage)
            }
        })
    }

    fun cancelarTransacao(transacao: PaymentResult) {
        siTefPayment.cancelar(transacao, object : PaymentCallback {
            override fun onSuccess(result: PaymentResult) {
                _transacoes.value = _transacoes.value - transacao
                _resultadoPagamento.value = "Cancelamento OK! NSU: ${transacao.sitefTransactionId}"
            }

            override fun onFailure(errorMessage: String) {
                _resultadoPagamento.value = "Falha ao cancelar: $errorMessage"
            }
        })
    }

    fun atualizarEstados(result: PaymentResult) {
        _ultimoPagamento.value = result
        _transacoes.value = _transacoes.value + result
        _resultadoPagamento.value = "Pagamento OK! NSU: ${result.sitefTransactionId}"
    }

    fun atualizarFalha(errorMessage: String) {
        _resultadoPagamento.value = "Falha: $errorMessage"
    }

    fun formatarResultado(result: PaymentResult): String = """
        Código de resposta: ${result.responseCode ?: "N/A"}
        Tipo de transação: ${result.transactionType ?: "N/A"}
        Tipo de parcelamento: ${result.installmentType ?: "N/A"}
        Cashback: ${result.cashbackAmount ?: "N/A"}
        Adquirente: ${result.acquirerId ?: "N/A"}
        Bandeira do cartão: ${result.cardBrand ?: "N/A"}
        NSU Sitef: ${result.sitefTransactionId ?: "N/A"}
        NSU Host: ${result.hostTransactionId ?: "N/A"}
        Código de autorização: ${result.authorizationCode ?: "N/A"}
        Parcelas da transação: ${result.transactionInstallments ?: "N/A"}
        Valor da transação: R$${result.transactionAmount?.toIntOrNull()?.div(100.0) ?: 0.0}
        Número do documento: ${result.invoiceNumber ?: "N/A"}
        Data da transação: ${result.invoiceDate ?: "N/A"}
        Campos retornados: ${result.returnedFields ?: "N/A"}
    """.trimIndent()
}
