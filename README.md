# SiTefSalesClover 💳

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Clover](https://img.shields.io/badge/Clover-Compatible-green?style=flat-square)](https://clover.com)
[![JitPack](https://jitpack.io/v/gregoricfranco/SiTefSalesClover.svg)](https://jitpack.io/#gregoricfranco/SiTefSalesClover)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

> Uma biblioteca Android robusta e eficiente para integração do sistema de pagamentos SiTef com dispositivos Clover.

## 📋 Visão Geral

O **SiTefSalesClover** é uma biblioteca desenvolvida em Kotlin que simplifica a integração entre o sistema de pagamentos SiTef e dispositivos Clover. Oferece uma interface intuitiva para processar transações financeiras de forma segura e eficiente em ambientes Android.

### ✨ Principais Funcionalidades

- 🏦 **Integração SiTef**: Comunicação direta e segura com o sistema de pagamentos SiTef
- 📱 **Compatibilidade Clover**: Suporte completo para dispositivos Clover
- 💰 **Métodos de Pagamento Diversos**: Crédito, Débito, PIX e parcelamento
- ⚙️ **Configuração Simplificada**: Processo de integração streamlined
- 📚 **Documentação Abrangente**: Exemplos práticos e guias detalhados
- 🛡️ **Transações Seguras**: Implementação de práticas de segurança financeira

## 🏗️ Tecnologias Utilizadas

| Tecnologia | Versão |
|------------|--------|
| **Linguagem** | Kotlin |
| **SDK Android** | API Level 36 |
| **Build Tools** | Gradle 8.11.1 |
| **JDK** | OpenJDK 17.0.14-amzn |

## 📋 Pré-requisitos

Antes de começar, certifique-se de ter:

- **Android Studio 2023** ou superior
- **SDK Android API Level 36**
- **JDK 17** ou superior
- **Dispositivo Clover** para testes
- **Credenciais SiTef** válidas

## 🚀 Instalação

### Método 1: Via JitPack (Recomendado)

#### 1. Adicione o repositório JitPack no seu `settings.gradle` (Gradle 7.0+):

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Ou no `build.gradle` do projeto (versões anteriores):

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

#### 2. Adicione a dependência no `build.gradle` do módulo da aplicação:

```gradle
dependencies {
    implementation 'com.github.gregoricfranco:SiTefSalesClover:Tag'
}
```

> **💡 Dica**: Substitua `Tag` pela versão desejada. Veja as versões disponíveis em: https://jitpack.io/#gregoricfranco/SiTefSalesClover

#### 3. Sincronize o projeto

Após adicionar as dependências, sincronize o projeto no Android Studio.

### Método 2: Clonando o Repositório

Se preferir trabalhar com o código fonte:

#### 1. Clone o Repositório

```bash
git clone https://github.com/gregoricfranco/SiTefSalesClover.git
cd SiTefSalesClover
```

#### 2. Configure o Projeto

1. Abra o projeto no Android Studio
2. Sincronize as dependências do Gradle
3. Configure suas credenciais SiTef no arquivo de configuração

### 3. Configuração SiTef

```kotlin
// Exemplo de configuração básica
val sitefConfig = SiTefConfig.Builder()
    .setEndpoint("SEU_ENDPOINT_SITEF")
    .setCodLoja("SEU_CODIGO_LOJA")
    .setCodTerminal("SEU_CODIGO_TERMINAL")
    .build()
```

### ⚡ Início Rápido com JitPack

1. **Adicione o repositório JitPack**
2. **Inclua a dependência**: `implementation 'com.github.gregoricfranco:SiTefSalesClover:latest-version'`
3. **Sincronize o projeto**
4. **Configure o SiTef** com suas credenciais
5. **Comece a usar** a biblioteca!

## 📖 Uso Básico

### Inicializando a Biblioteca

```kotlin
import com.gregoricfranco.sitefsalesclover.SiTefSalesClover

class MainActivity : AppCompatActivity() {
    private lateinit var sitefSales: SiTefSalesClover
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sitefSales = SiTefSalesClover.Builder(this)
            .setConfig(sitefConfig)
            .build()
    }
}
```

### Realizando uma Transação de Crédito

```kotlin
val transacao = TransacaoCredito.Builder()
    .setValor(1000) // R$ 10,00 (valor em centavos)
    .setParcelas(1)
    .setComprovanteVia(2) // Cliente e estabelecimento
    .build()

sitefSales.executarTransacao(transacao) { resultado ->
    when (resultado.status) {
        StatusTransacao.APROVADA -> {
            // Transação aprovada
            Log.d("SiTef", "Transação aprovada: ${resultado.nsu}")
        }
        StatusTransacao.NEGADA -> {
            // Transação negada
            Log.e("SiTef", "Transação negada: ${resultado.mensagem}")
        }
        StatusTransacao.ERRO -> {
            // Erro na transação
            Log.e("SiTef", "Erro: ${resultado.mensagem}")
        }
    }
}
```

### Transação PIX

```kotlin
val transacaoPix = TransacaoPix.Builder()
    .setValor(2500) // R$ 25,00
    .setTipoChave(TipoChavePix.QR_CODE)
    .build()

sitefSales.executarTransacao(transacaoPix) { resultado ->
    // Processar resultado
}
```

## 🔧 Configurações Avançadas

### Timeout Customizado

```kotlin
val sitefSales = SiTefSalesClover.Builder(this)
    .setConfig(sitefConfig)
    .setTimeout(30000) // 30 segundos
    .setRetryAttempts(3)
    .build()
```

### Callbacks de Evento

```kotlin
sitefSales.setEventListener(object : SiTefEventListener {
    override fun onTransacaoIniciada() {
        // Transação iniciada
    }
    
    override fun onAguardandoCartao() {
        // Aguardando inserção do cartão
    }
    
    override fun onAguardandoSenha() {
        // Aguardando digitação da senha
    }
    
    override fun onProcessando() {
        // Processando transação
    }
})
```

## 📱 Exemplo de Integração Completa

```kotlin
class PagamentoActivity : AppCompatActivity() {
    
    private fun processarPagamento(valor: Long, tipoPagamento: TipoPagamento) {
        val transacao = when (tipoPagamento) {
            TipoPagamento.CREDITO -> TransacaoCredito.Builder()
                .setValor(valor)
                .setParcelas(1)
                .build()
                
            TipoPagamento.DEBITO -> TransacaoDebito.Builder()
                .setValor(valor)
                .build()
                
            TipoPagamento.PIX -> TransacaoPix.Builder()
                .setValor(valor)
                .build()
        }
        
        sitefSales.executarTransacao(transacao) { resultado ->
            runOnUiThread {
                when (resultado.status) {
                    StatusTransacao.APROVADA -> {
                        exibirSucesso(resultado)
                        imprimirComprovante(resultado.comprovante)
                    }
                    StatusTransacao.NEGADA -> {
                        exibirErro("Transação negada: ${resultado.mensagem}")
                    }
                    StatusTransacao.ERRO -> {
                        exibirErro("Erro na transação: ${resultado.mensagem}")
                    }
                }
            }
        }
    }
}
```

## 🧪 Testes

### Executar Testes Unitários

```bash
./gradlew test
```

### Executar Testes de Integração

```bash
./gradlew connectedAndroidTest
```

### Testes em Dispositivo Clover

1. Configure um dispositivo Clover no modo desenvolvedor
2. Instale o APK de exemplo
3. Execute os cenários de teste incluídos

## 📁 Estrutura do Projeto

```
SiTefSalesClover/
├── app/                    # Aplicação de exemplo
├── library/               # Código da biblioteca principal
│   ├── src/main/kotlin/  # Código fonte Kotlin
│   └── src/test/kotlin/  # Testes unitários
├── docs/                 # Documentação adicional
├── samples/              # Exemplos de uso
└── README.md            # Este arquivo
```

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 🆘 Suporte

### Documentação SiTef
- [Documentação Oficial SiTef](https://www.softwareexpress.com.br/)
- [Guia de Integração](docs/integracao.md)

### Documentação Clover
- [Clover Developer Documentation](https://docs.clover.com/)
- [Clover Android SDK](https://github.com/clover/clover-android-sdk)

### Contato

- **Autor**: Gregoric Franco
- **GitHub**: [@gregoricfranco](https://github.com/gregoricfranco)
- **JitPack**: [SiTefSalesClover](https://jitpack.io/#gregoricfranco/SiTefSalesClover)

### Issues e Sugestões

Encontrou um bug ou tem uma sugestão? [Abra uma issue](https://github.com/gregoricfranco/SiTefSalesClover/issues/new) no GitHub.

---

