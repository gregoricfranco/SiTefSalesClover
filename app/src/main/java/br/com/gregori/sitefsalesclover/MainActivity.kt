package br.com.gregori.sitefsalesclover

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import br.com.gregori.sitefsalesclover.factory.PaymentViewModelFactory
import br.com.gregori.sitefsalesclover.screen.PaymentScreen
import br.com.gregori.sitefsalesclover.ui.theme.SiTefSalesCloverTheme
import br.com.gregori.sdk.config.SiTefPaymentConfig
import br.com.gregori.sdk.core.SiTefPayment
import br.com.gregori.sdk.enums.PaymentMethod
import br.com.gregori.sdk.callbacks.PaymentCallback
import br.com.gregori.sdk.models.PaymentResult
import br.com.gregori.sitefsalesclover.viewmodel.PaymentViewModel

/**
 * MainActivity da aplicação SiTef Sales Clover.
 *
 * Esta Activity é responsável por inicializar a integração com o SiTef, gerenciar o
 * ViewModel de pagamentos e exibir a tela de pagamento utilizando Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    /** Instância do gerenciador de pagamentos SiTef */
    private lateinit var siTefPayment: SiTefPayment

    /** ViewModel responsável por manter o estado do pagamento */
    private lateinit var paymentViewModel: PaymentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Permite que a UI ocupe toda a tela

        // Configuração da integração com SiTef
        val config = SiTefPaymentConfig(
            merchantTaxId = "12345678912345",
            isvTaxId = "12341234123412",
            userInputTimeout = 30 // Timeout para input do usuário em segundos
        )

        /**
         * Inicializa o launcher para receber o resultado do SiTef.
         *
         * Ao receber o resultado, chama processarRetorno do SiTefPayment e atualiza
         * os estados no ViewModel, chamando callbacks de sucesso ou falha.
         */
        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            siTefPayment.processarRetorno(result.data, result.resultCode, object : PaymentCallback {
                override fun onSuccess(result: PaymentResult) {
                    paymentViewModel.atualizarEstados(result)
                    println("Pagamento aprovado: NSU=${result.sitefTransactionId}")
                }

                override fun onFailure(errorMessage: String) {
                    paymentViewModel.atualizarFalha(errorMessage)
                    println("Falha no pagamento: $errorMessage")
                }
            })
        }

        // Inicializa o gerenciador de pagamentos com o launcher e configuração
        siTefPayment = SiTefPayment(this, launcher, config)

        // Inicializa o ViewModel com fábrica personalizada para injetar SiTefPayment
        paymentViewModel = ViewModelProvider(
            this,
            PaymentViewModelFactory(siTefPayment)
        )[PaymentViewModel::class.java]

        /**
         * Configura o conteúdo da tela usando Jetpack Compose.
         *
         * Exibe a PaymentScreen, passando valores e callbacks para iniciar e cancelar pagamentos.
         */
        setContent {
            SiTefSalesCloverTheme {
                PaymentScreen(
                    valor = paymentViewModel.valor,
                    viewModel = paymentViewModel,
                    onPagamento = { valor, metodo, parcelas ->
                        paymentViewModel.iniciarPagamento(
                            valorCentavos = valor,
                            metodo = metodo,
                            installments = parcelas,
                            callback = object : PaymentCallback {
                                override fun onSuccess(result: PaymentResult) {
                                    paymentViewModel.atualizarEstados(result)
                                    println("Pagamento aprovado: NSU=${result.sitefTransactionId}")
                                }

                                override fun onFailure(errorMessage: String) {
                                    paymentViewModel.atualizarFalha(errorMessage)
                                    println("Falha no pagamento: $errorMessage")
                                }
                            }
                        )
                    },
                    onCancelar = { transacao ->
                        paymentViewModel.cancelarTransacao(transacao)
                    }
                )
            }
        }
    }
}
