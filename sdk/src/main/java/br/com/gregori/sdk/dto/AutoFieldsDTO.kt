package br.com.gregori.sdk.dto

import br.com.gregori.sdk.enums.PaymentMethod
import org.json.JSONObject

/** DTO para os campos enviados ao SiTef (autoFields) */
data class AutoFieldsDTO(
    val valor: Long,
    val nsu: String,
    val data: String,// MMDD
    val formaDePagamento: PaymentMethod

) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("146", valor.toString())
        json.put("516", nsu)
        json.put("515", data)
        json.put("formaDePagamento", formaDePagamento.code)
        return json.toString()
    }
}
