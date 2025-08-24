package br.com.gregori.sdk.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
object DateUtils {

    fun formatDate(calendar: Calendar): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.time)
    }

    fun formatTime(calendar: Calendar): String {
        return SimpleDateFormat("HHmmss", Locale.getDefault()).format(calendar.time)
    }

    /**
     * Converte qualquer data recebida para o formato DDMMAAAA para cancelamento.
     * Aceita MMDD ou DDMMAAAA, retorna "00000000" se inválido.
     */
    fun formatTransactionDateForCancel(data: String?): String {
        if (data.isNullOrBlank()) return "00000000"

        return when (data.length) {
            4 -> { // MMDD -> assume ano atual
                val mes = data.substring(0, 2)
                val dia = data.substring(2, 4)
                val ano = Calendar.getInstance().get(Calendar.YEAR).toString()
                "$dia$mes$ano"
            }
            8 -> data // já está DDMMAAAA
            else -> "00000000" // qualquer outro caso
        }
    }

    /**
     * Gera um número de documento genérico caso não exista.
     */
    fun generateGenericInvoiceNumber(existing: String?): String {
        return existing?.takeIf { it.isNotBlank() } ?: (1000..9999).random().toString()
    }

    fun String.toDDMMAAAA(): String =
        if (length == 8) substring(6, 8) + substring(4, 6) + substring(0, 4) else "00000000"
}
