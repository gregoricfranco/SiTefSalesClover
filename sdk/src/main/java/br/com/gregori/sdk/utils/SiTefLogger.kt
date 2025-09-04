package br.com.gregori.sdk.utils

import android.content.Context
import android.util.Log
import br.com.gregori.sdk.dto.ReturnedFieldsDTO
import br.com.gregori.sdk.dto.AutoFieldsDTO
import br.com.gregori.sdk.models.PaymentResult
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object SiTefLogger {

    private const val TAG = "SiTefPayment"
    private const val LOG_FILE_NAME = "sitref_logs.txt"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun getTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
    }

    private fun writeLogToFile(message: String) {
        try {
            val context = appContext ?: return
            val file = File(context.filesDir, LOG_FILE_NAME)
            FileWriter(file, true).use { writer ->
                writer.appendLine("${getTimestamp()} - $message")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Erro ao escrever log em arquivo: ${e.message}", e)
        }
    }

    fun logInfo(tag: String, message: String) {
        Log.i(tag, message)
        writeLogToFile("INFO [$tag] $message")
    }

    fun logDebug(tag: String, message: String) {
        Log.d(tag, message)
        writeLogToFile("DEBUG [$tag] $message")
    }

    fun logWarning(tag: String, message: String) {
        Log.w(tag, message)
        writeLogToFile("WARN [$tag] $message")
    }

    fun logError(tag: String, message: String, exception: Exception? = null) {
        if (exception != null) {
            Log.e(tag, message, exception)
            writeLogToFile("ERROR [$tag] $message - Ex: ${exception.message}")
        } else {
            Log.e(tag, message)
            writeLogToFile("ERROR [$tag] $message")
        }
    }

    fun logTransacao(result: PaymentResult, campos: ReturnedFieldsDTO) {
        val logMessage = buildString {
            appendLine("=== Transação Salva ===")
            appendLine("NSU SiTef: ${result.sitefTransactionId} | NSU Host: ${campos.nsu}")
            appendLine("Valor da transação (centavos): ${result.transactionAmount}")
            appendLine("Número do documento: ${result.invoiceNumber}")
            appendLine("Data da transação: ${result.invoiceDate}")
            appendLine("ResponseCode: ${result.responseCode}")
            appendLine("Tipo de transação: ${result.transactionType}")
            appendLine("InstallmentType: ${result.installmentType}")
            appendLine("Cashback: ${result.cashbackAmount}")
            appendLine("AcquirerId: ${result.acquirerId}")
            appendLine("CardBrand: ${result.cardBrand}")
            appendLine("AuthorizationCode: ${result.authorizationCode} | Campo 38: ${campos.autorizacao}")
            appendLine("MerchantReceipt: ${result.merchantReceipt}")
            appendLine("CustomerReceipt: ${result.customerReceipt}")
            appendLine("ReturnedFields JSON: ${result.returnedFields}")
        }
        Log.d(TAG, logMessage)
        writeLogToFile(logMessage)
    }

    fun logCancelamento(autoFields: AutoFieldsDTO) {
        val logMessage = buildString {
            appendLine("=== Cancelamento Iniciado ===")
            appendLine("NSU: ${autoFields.nsu}")
            appendLine("Valor (centavos): ${autoFields.valor}")
            appendLine("Data (MMDD): ${autoFields.data}")
            appendLine("Forma : ${autoFields.formaDePagamento}")
            appendLine("AutoFields JSON: ${autoFields.toJson()}")
        }
        Log.d(TAG, logMessage)
        writeLogToFile(logMessage)
    }
}
