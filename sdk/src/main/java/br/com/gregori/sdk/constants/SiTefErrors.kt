package br.com.gregori.sdk.constants

object SiTefErrors {
    const val TAG = "SiTefPayment"

    const val INVALID_AMOUNT = "Valor inválido para transação"
    const val TRANSACTION_CANCELLED = "Transação cancelada pelo usuário"
    const val TRANSACTION_NO_DATA = "Transação finalizada sem dados de retorno"
    const val TRANSACTION_FAILED = "Falha na transação SiTef"
    const val JSON_PARSING = "Erro ao processar dados da transação"
}
