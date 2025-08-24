// PaymentScreen.kt
package br.com.gregori.sitefsalesclover.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.gregori.sdk.enums.PaymentMethod
import br.com.gregori.sdk.models.PaymentResult
import br.com.gregori.sitefsalesclover.viewmodel.PaymentViewModel

@Composable
fun PaymentScreen(
    valor: Int,
    viewModel: PaymentViewModel,
    onPagamento: (Int, PaymentMethod, Int) -> Unit,
    onCancelar: (PaymentResult) -> Unit
) {
    val ultimoPagamento = viewModel.ultimoPagamento.collectAsState()
    val listaTransacoes = viewModel.transacoes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Teste de Pagamento pela SiTef", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Crédito à vista
        Button(
            onClick = { onPagamento(valor, PaymentMethod.CREDIT, 1) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Crédito à vista (R$${valor / 100})") }

        // Crédito parcelado fixo (3x, 6x, etc)
        Button(
            onClick = { onPagamento(valor, PaymentMethod.CREDIT_INSTALLMENTS, 3) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Crédito parcelado 3x (R$${valor / 100})") }

        Button(
            onClick = { onPagamento(valor, PaymentMethod.CREDIT_INSTALLMENTS, 6) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Crédito parcelado 6x (R$${valor / 100})") }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onPagamento(valor, PaymentMethod.DEBIT, 1) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Débito (R$${valor / 100})") }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onPagamento(valor, PaymentMethod.PIX, 1) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("PIX (R$${valor / 100})") }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Último pagamento:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        ultimoPagamento.value?.let { Text(viewModel.formatarResultado(it)) } ?: Text("Nenhum pagamento realizado")

        Spacer(modifier = Modifier.height(24.dp))
        Text("Transações realizadas:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        listaTransacoes.value.forEach { transacao ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("NSU: ${transacao.sitefTransactionId}")
                Button(onClick = { onCancelar(transacao) }) { Text("Cancelar") }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
