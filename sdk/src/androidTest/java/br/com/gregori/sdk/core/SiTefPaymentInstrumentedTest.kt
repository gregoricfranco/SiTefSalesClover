package br.com.gregori.sdk.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.gregori.sdk.callbacks.PaymentCallback
import br.com.gregori.sdk.config.SiTefPaymentConfig
import br.com.gregori.sdk.dto.ReturnedFieldsDTO
import br.com.gregori.sdk.enums.PaymentMethod
import br.com.gregori.sdk.models.PaymentResult
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class SiTefPaymentInstrumentedTest {

    private lateinit var context: Context
    private lateinit var config: SiTefPaymentConfig
    private lateinit var callback: PaymentCallback

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        config = SiTefPaymentConfig(
            merchantTaxId = "123456789",
            isvTaxId = "987654321",
            userInputTimeout = 30
        )
        callback = mock()
    }

    @Test
    fun pagar_chama_launcher_e_verifica_extras() {
        PaymentMethod.values().forEach { method ->
            val mockLauncher = mock<ActivityResultLauncher<Intent>>()
            val payment = SiTefPayment(context, mockLauncher, config)
            val intentCaptor = argumentCaptor<Intent>()

            payment.pagar(1000, method, "123", callback)

            verify(mockLauncher).launch(intentCaptor.capture())
            val intent = intentCaptor.firstValue

            // Verifica extras
            assertEquals(method.code, intent.getStringExtra("functionId"))
            assertEquals("123", intent.getStringExtra("invoiceNumber"))
            assertEquals("123456789", intent.getStringExtra("merchantTaxId"))
            assertEquals("987654321", intent.getStringExtra("isvTaxId"))
            assertEquals("1000", intent.getStringExtra("transactionAmount"))
            assertEquals(method.code, intent.getStringExtra("paymentMethod"))
        }

        verify(callback, never()).onFailure(any())
    }

    @Test
    fun pagar_retorna_erro_quando_valor_invalido() {
        val mockLauncher = mock<ActivityResultLauncher<Intent>>()
        val payment = SiTefPayment(context, mockLauncher, config)

        payment.pagar(0, PaymentMethod.CREDIT, "123", callback)

        verify(callback).onFailure("Valor inválido")
        verify(mockLauncher, never()).launch(any())
    }

    @Test
    fun cancelar_lanca_intent_e_verifica_autofields() {
        val mockLauncher = mock<ActivityResultLauncher<Intent>>()
        val payment = SiTefPayment(context, mockLauncher, config)

        val transacao = PaymentResult(
            responseCode = "00",
            transactionType = "SALE",
            installmentType = "FULL",
            cashbackAmount = "0",
            acquirerId = "123",
            cardBrand = "VISA",
            sitefTransactionId = "NSU123",
            hostTransactionId = "HOST123",
            authorizationCode = "AUTH123",
            transactionInstallments = "1",
            merchantReceipt = "REC123",
            customerReceipt = "REC456",
            returnedFields = """{"105":["20250821123045"],"37":["NSU123"],"1330":["1000"]}""",
            transactionAmount = "1000",
            invoiceNumber = "123",
            invoiceDate = "20250821"
        )

        val intentCaptor = argumentCaptor<Intent>()
        payment.cancelar(transacao, callback)

        verify(mockLauncher).launch(intentCaptor.capture())
        val intent = intentCaptor.firstValue

        // Verifica extras básicos
        assertEquals("200", intent.getStringExtra("functionId"))
        assertEquals("123456789", intent.getStringExtra("merchantTaxId"))
        assertEquals("987654321", intent.getStringExtra("isvTaxId"))

        // Verifica se autoFields foi criado
        val autoFieldsJson = intent.getStringExtra("autoFields")
        assertNotNull("autoFields não deve ser null", autoFieldsJson)
        assertTrue("autoFields não deve estar vazio", autoFieldsJson!!.isNotEmpty())

        // Verifica se é um JSON válido
        val json = JSONObject(autoFieldsJson)
        assertTrue("JSON deve conter pelo menos um campo", json.length() > 0)

        // Log para debug - remova depois de confirmar a estrutura
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            println("AutoFields key: $key, value: ${json.get(key)}")
        }
    }

    @Test
    fun processarRetorno_chama_onSuccess_quando_OK() {
        val extras = Bundle().apply {
            putString("responseCode", "00")
            putString("transactionType", "SALE")
            putString("installmentType", "FULL")
            putString("cashbackAmount", "0")
            putString("acquirerId", "123")
            putString("cardBrand", "VISA")
            putString("sitefTransactionId", "NSU123")
            putString("hostTrasactionId", "HOST123") // Note: typo exists in original
            putString("authCode", "AUTH123")
            putString("transactionInstallments", "1")
            putString("merchantReceipt", "REC123")
            putString("customerReceipt", "REC456")
            putString("returnedFields", """{"105":["20250821123045"],"1330":["1000"],"37":["NSU123"]}""")
        }

        val intent = Intent().apply { putExtras(extras) }
        val mockLauncher = mock<ActivityResultLauncher<Intent>>()
        val payment = SiTefPayment(context, mockLauncher, config)

        payment.processarRetorno(intent, Activity.RESULT_OK, callback)

        verify(callback).onSuccess(any())
        assertTrue(payment.transacoes.isNotEmpty())

        // Verifica se a transação foi adicionada corretamente
        val transacao = payment.transacoes[0]
        assertEquals("00", transacao.responseCode)
        assertEquals("NSU123", transacao.sitefTransactionId)
    }

    @Test
    fun processarRetorno_chama_onFailure_quando_cancelado() {
        val mockLauncher = mock<ActivityResultLauncher<Intent>>()
        val payment = SiTefPayment(context, mockLauncher, config)

        payment.processarRetorno(null, Activity.RESULT_CANCELED, callback)
        verify(callback).onFailure("Transação cancelada ou falhou")
    }

    @Test
    fun processarRetorno_chama_onFailure_quando_dados_nulos() {
        val mockLauncher = mock<ActivityResultLauncher<Intent>>()
        val payment = SiTefPayment(context, mockLauncher, config)
        val intentSemExtras = Intent()

        payment.processarRetorno(intentSemExtras, Activity.RESULT_OK, callback)
        verify(callback).onFailure("Transação finalizada sem dados")
    }

    @Test
    fun tratarCamposExtras_parseia_JSON_corretamente() {
        val json = """{"105":["20250821123045"],"1330":["1000"],"37":["NSU123"],"38":["AUTH123"]}"""
        val mockLauncher = mock<ActivityResultLauncher<Intent>>()
        val payment = SiTefPayment(context, mockLauncher, config)

        // Usa reflexão para acessar método privado
        val campos: ReturnedFieldsDTO = payment.run {
            val method = this::class.java.getDeclaredMethod("tratarCamposExtras", String::class.java)
            method.isAccessible = true
            method.invoke(this, json) as ReturnedFieldsDTO
        }

        assertEquals("NSU123", campos.nsu)
        assertEquals("AUTH123", campos.autorizacao)
        assertEquals(1000L, campos.valorTransacao)
        assertEquals("20250821", campos.dataTransacao)
        assertEquals("123045", campos.horaTransacao)
    }

    @Test
    fun tratarCamposExtras_retorna_default_quando_JSON_invalido() {
        val mockLauncher = mock<ActivityResultLauncher<Intent>>()
        val payment = SiTefPayment(context, mockLauncher, config)

        val campos: ReturnedFieldsDTO = payment.run {
            val method = this::class.java.getDeclaredMethod("tratarCamposExtras", String::class.java)
            method.isAccessible = true
            method.invoke(this, "json_invalido") as ReturnedFieldsDTO
        }

        assertEquals("", campos.nsu)
        assertEquals("", campos.autorizacao)
        assertEquals(0L, campos.valorTransacao)
        assertEquals("", campos.dataTransacao)
        assertEquals("", campos.horaTransacao)
    }

    @Test
    fun tratarCamposExtras_retorna_default_quando_JSON_null() {
        val mockLauncher = mock<ActivityResultLauncher<Intent>>()
        val payment = SiTefPayment(context, mockLauncher, config)

        val campos: ReturnedFieldsDTO = payment.run {
            val method = this::class.java.getDeclaredMethod("tratarCamposExtras", String::class.java)
            method.isAccessible = true
            method.invoke(this, null) as ReturnedFieldsDTO
        }

        assertEquals("", campos.nsu)
        assertEquals("", campos.autorizacao)
        assertEquals(0L, campos.valorTransacao)
        assertEquals("", campos.dataTransacao)
        assertEquals("", campos.horaTransacao)
    }
}