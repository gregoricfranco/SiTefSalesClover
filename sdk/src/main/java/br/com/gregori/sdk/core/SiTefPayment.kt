package br.com.gregori.sdk.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import br.com.gregori.sdk.callbacks.PaymentCallback
import br.com.gregori.sdk.config.SiTefPaymentConfig
import br.com.gregori.sdk.dto.AutoFieldsDTO
import br.com.gregori.sdk.dto.ReturnedFieldsDTO
import br.com.gregori.sdk.enums.PaymentMethod
import br.com.gregori.sdk.models.PaymentResult
import br.com.gregori.sdk.utils.DateUtils
import br.com.gregori.sdk.utils.DateUtils.toDDMMAAAA
import br.com.gregori.sdk.utils.SiTefLogger
import br.com.gregori.sdk.constants.SiTefActions
import br.com.gregori.sdk.constants.SiTefExtras
import br.com.gregori.sdk.constants.SiTefErrors
import org.json.JSONObject
import java.util.*

/**
 * Gerencia transações de pagamento via SiTef.
 *
 * Esta classe permite iniciar pagamentos, cancelar transações e processar retornos do SiTef.
 *
 * @param context Contexto da aplicação
 * @param launcher Launcher para iniciar atividades com resultado
 * @param config Configurações do SiTef (IDs do estabelecimento, timeout etc.)
 */
class SiTefPayment(
    private val context: Context,
    private val launcher: ActivityResultLauncher<Intent>,
    private val config: SiTefPaymentConfig
) {

    /**
     * Inicia uma transação de pagamento.
     *
     * @param valorCentavos Valor da transação em centavos
     * @param paymentMethod Método de pagamento (PIX, Crédito, Débito etc.)
     * @param invoiceNumber Número da nota fiscal ou identificador da transação
     * @param installments Número de parcelas (1 para à vista)
     * @param callback Callback para informar sucesso ou falha
     */
    fun pagar(
        valorCentavos: Int,
        paymentMethod: PaymentMethod,
        invoiceNumber: String = "1",
        installments: Int = 1,
        callback: PaymentCallback
    ) {

        SiTefLogger.logInfo("TAG", "Iniciando transação: método=${paymentMethod.name}, valor=$valorCentavos centavos, invoice=$invoiceNumber, parcelas=$installments")

        if (!validateTransactionParameters(valorCentavos, paymentMethod, callback)) return

        try {
            val intent = createPaymentIntent(valorCentavos, paymentMethod, invoiceNumber, installments)
            launcher.launch(intent)
        } catch (e: Exception) {
            SiTefLogger.logError(SiTefErrors.TAG, "Erro crítico ao preparar transação", e)
            callback.onFailure(SiTefErrors.TRANSACTION_FAILED)
        }
    }

    /**
     * Cancela uma transação existente.
     *
     * @param transacao Objeto PaymentResult da transação que será cancelada
     * @param callback Callback para informar sucesso ou falha
     */
    fun cancelar(transacao: PaymentResult, callback: PaymentCallback) {
        if (!validateCancelParameters(transacao, callback)) return

        try {
            val autoFields = createAutoFieldsForCancel(transacao)
            val intent = createCancelIntent(autoFields)
            launcher.launch(intent)
            SiTefLogger.logCancelamento(autoFields)
        } catch (e: Exception) {
            SiTefLogger.logError(SiTefErrors.TAG, "Erro crítico ao preparar cancelamento", e)
            callback.onFailure(SiTefErrors.TRANSACTION_FAILED)
        }
    }

    /**
     * Processa o retorno do SiTef após uma transação.
     *
     * @param data Intent recebida no onActivityResult
     * @param resultCode Código de resultado da atividade
     * @param callback Callback para informar sucesso ou falha
     */
    fun processarRetorno(data: Intent?, resultCode: Int, callback: PaymentCallback) {
        if (resultCode != Activity.RESULT_OK) {
            val errorMessage = data?.getStringExtra(SiTefExtras.ERROR_MESSAGE)
            callback.onFailure(errorMessage ?: SiTefErrors.TRANSACTION_CANCELLED)
            return
        }

        if (data?.extras == null) {
            callback.onFailure(SiTefErrors.TRANSACTION_NO_DATA)
            return
        }

        val campos = tratarCamposExtras(data.extras!!.getString(SiTefExtras.RETURNED_FIELDS))
        val result = PaymentResult.fromExtras(data.extras!!, campos)
        SiTefLogger.logTransacao(result, campos)
        callback.onSuccess(result)
    }

    /**
     * Valida os parâmetros básicos da transação.
     *
     * @return true se os parâmetros forem válidos, false caso contrário
     */
    private fun validateTransactionParameters(valorCentavos: Int, paymentMethod: PaymentMethod, callback: PaymentCallback): Boolean {
        if (valorCentavos <= 0) {
            callback.onFailure(SiTefErrors.INVALID_AMOUNT)
            return false
        }
        if (paymentMethod.code.isBlank()) {
            callback.onFailure("Método de pagamento não suportado")
            return false
        }
        return true
    }

    /**
     * Valida se uma transação pode ser cancelada.
     *
     * @return true se for possível cancelar, false caso contrário
     */
    private fun validateCancelParameters(transacao: PaymentResult, callback: PaymentCallback): Boolean {
        if (transacao.sitefTransactionId.isNullOrBlank() && transacao.returnedFields.isNullOrBlank()) {
            callback.onFailure("Transação não pode ser cancelada - dados insuficientes")
            return false
        }
        return  true
    }

    /**
     * Cria a Intent para iniciar a transação de pagamento.
     */
    private fun createPaymentIntent(
        valorCentavos: Int,
        paymentMethod: PaymentMethod,
        invoiceNumber: String,
        installments: Int
    ): Intent {
        val now = Calendar.getInstance()
        val invoiceDate = DateUtils.formatDate(now)
        val invoiceTime = DateUtils.formatTime(now)

        return Intent(SiTefActions.ACTION_TRANSACTION).apply {
            putExtra(SiTefExtras.AMOUNT, valorCentavos.toString())
            putExtra(SiTefExtras.MERCHANT_ID, config.merchantTaxId)
            putExtra(SiTefExtras.ISV_ID, config.isvTaxId)
            putExtra(SiTefExtras.INVOICE_NUMBER, invoiceNumber)
            putExtra(SiTefExtras.INVOICE_DATE, invoiceDate)
            putExtra(SiTefExtras.INVOICE_TIME, invoiceTime)
            putExtra(SiTefExtras.TIMEOUT, config.userInputTimeout.toString())
            putExtra(SiTefExtras.TACTILE_PIN, SiTefActions.ENABLE_TACTILE_PIN)
            applyPaymentMethod(paymentMethod, installments)

        }
    }

    /**
     * Aplica parâmetros específicos de cada método de pagamento à Intent.
     */
    private fun Intent.applyPaymentMethod(paymentMethod: PaymentMethod, installments: Int = 1) {
        putExtra(SiTefExtras.FUNCTION_ID, paymentMethod.code) // Código do método de pagamento

        SiTefLogger.logInfo("TAG", "Forma de pagamento=${paymentMethod.name}, parcelas=$installments")

        when (paymentMethod) {
            PaymentMethod.PIX -> {
                putExtra("enabledTransactions", "7;8;")
                putExtra("functionAdditionalParameters", "CarteirasDigitaisHabilitadas=027160110024")
            }
            PaymentMethod.CREDIT -> {
                putExtra("transactionInstallments", "1")
                putExtra("functionAdditionalParameters", "[27;28]")
            }

            PaymentMethod.CREDIT_INSTALLMENTS -> {
                putExtra("transactionInstallments", installments.toString())
                putExtra("functionAdditionalParameters", "[26;27]")
            }
            PaymentMethod.DEBIT -> {
                putExtra("functionAdditionalParameters", "TransacoesHabilitadas=16")
            }
            else -> throw IllegalArgumentException("Método de pagamento inválido para esta função: $paymentMethod")
        }
    }

    /**
     * Cria a Intent para cancelar uma transação.
     */
    private fun createCancelIntent(autoFields: AutoFieldsDTO): Intent {
        return Intent(SiTefActions.ACTION_TRANSACTION).apply {
            putExtra(SiTefExtras.FUNCTION_ID, autoFields.formaDePagamento.code)
            putExtra(SiTefExtras.MERCHANT_ID, config.merchantTaxId)
            putExtra(SiTefExtras.ISV_ID, config.isvTaxId)
            putExtra(SiTefExtras.AUTO_FIELDS, autoFields.toJson())
        }
    }

    /**
     * Gera os campos automáticos necessários para o cancelamento.
     */
    private fun createAutoFieldsForCancel(transacao: PaymentResult): AutoFieldsDTO {
        val campos = tratarCamposExtras(transacao.returnedFields)
        val valorCentavos = transacao.transactionAmount?.toLongOrNull() ?: 0L
        val nsu = transacao.sitefTransactionId ?: campos.nsu.ifEmpty {
            Calendar.getInstance().timeInMillis.toString().takeLast(6).padStart(6, '0')
        }
        val dataCancelamento = campos.dataTransacao.toDDMMAAAA()

        SiTefLogger.logInfo("TAG", "CaNCELAR CANCELAR=${transacao.paymentMethodDescription}")

        // Mapeia diretamente para o enum de cancelamento
        val paymentMethodCancel = when (transacao.paymentMethodDescription) {
            "Cartão de Crédito à Vista" -> PaymentMethod.CREDIT_CANCEL           // Crédito à vista
            "Cartão de Crédito Parcelado Administradora" -> PaymentMethod.CREDIT_CANCEL           // Crédito parcelado (ou criar CREDIT_INSTALLMENTS_CANCEL se quiser detalhar)
            "Cartão de Débito" -> PaymentMethod.DEBIT_CANCELLATION     // Débito
            "Carteira Digital", "PIX" -> PaymentMethod.PIX_CANCELLATION      // PIX / carteira digital
            else -> PaymentMethod.CREDIT_CANCEL          // fallback seguro
        }

        return AutoFieldsDTO(
            valor = valorCentavos,
            nsu = nsu, data = dataCancelamento,
            formaDePagamento = paymentMethodCancel
        )
    }

    /**
     * Converte o JSON de campos retornados do SiTef em um DTO acessível.
     *
     * Exemplo de JSON:
     * {
     *   "1330":["1000"],          // valor da transação em centavos
     *   "105":["20250822150945"], // data e hora da transação (YYYYMMDDHHMMSS)
     *   "37":["26"],              // NSU
     *   "38":["123456"],          // código de autorização
     *   ...
     * }
     */
    private fun tratarCamposExtras(returnedFieldsJson: String?): ReturnedFieldsDTO {
        if (returnedFieldsJson.isNullOrEmpty()) return ReturnedFieldsDTO()

        return try {
            val json = JSONObject(returnedFieldsJson)
            val dataHoraTransacao = json.optJSONArray("105")?.optString(0) ?: ""
            val (dataTransacao, horaTransacao) = if (dataHoraTransacao.length >= 14) {
                dataHoraTransacao.substring(0, 8) to dataHoraTransacao.substring(8, 14)
            } else "" to ""

            ReturnedFieldsDTO(
                functionId = json.optString("functionId", ""),
                terminal = json.optJSONArray("1002")?.optString(0) ?: "",
                cartao = json.optJSONArray("1003")?.optString(0) ?: "",
                valorTransacao = json.optJSONArray("1330")?.optString(0)?.toLongOrNull() ?: 0L,
                dataTransacao = dataTransacao,
                horaTransacao = horaTransacao,
                nsu = json.optJSONArray("37")?.optString(0) ?: "",
                autorizacao = json.optJSONArray("38")?.optString(0) ?: "",
                formaPagamento = json.optJSONArray("101")?.optString(0) ?: ""
            )

        } catch (e: Exception) {
            SiTefLogger.logError(
                SiTefErrors.TAG,
                "${SiTefErrors.JSON_PARSING}: ${e.message}", e
            )
            ReturnedFieldsDTO()
        }
    }
}
