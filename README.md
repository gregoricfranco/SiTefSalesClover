# SiTefSalesClover 💳

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Clover](https://img.shields.io/badge/Clover-Compatible-green?style=flat-square)](https://clover.com)
[![JitPack](https://jitpack.io/v/gregoricfranco/SiTefSalesClover.svg)](https://jitpack.io/#gregoricfranco/SiTefSalesClover)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

> Biblioteca Android para integrar o sistema de pagamentos SiTef com dispositivos Clover. Inclui um app de teste pronto para uso.

## 🚀 Instalação

### Via JitPack

1. Adicione o repositório JitPack no `settings.gradle`:

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

2. Adicione a dependência no `build.gradle` do módulo:

```gradle
dependencies {
    implementation 'com.github.gregoricfranco:SiTefSalesClover:Tag'
}
```

> 💡 Substitua `Tag` pela versão desejada disponível em: https://jitpack.io/#gregoricfranco/SiTefSalesClover

## 📱 Uso Básico

### Inicialização

```kotlin
val sitefSales = SiTefSalesClover.Builder(this)
    .setConfig(sitefConfig)
    .build()
```

### Transação de Crédito

```kotlin
val transacao = TransacaoCredito.Builder()
    .setValor(1000) // R$ 10,00 (em centavos)
    .setParcelas(1)
    .build()

sitefSales.executarTransacao(transacao) { resultado ->
    when (resultado.status) {
        StatusTransacao.APROVADA -> {
            // Sucesso
        }
        StatusTransacao.NEGADA -> {
            // Transação negada
        }
    }
}
```

## 🧪 App de Teste

O projeto inclui um app Android de exemplo que demonstra todas as funcionalidades da biblioteca. Para usar:

1. Clone o repositório
2. Abra no Android Studio
3. Configure suas credenciais SiTef
4. Execute o app no dispositivo Clover

## 📄 Licença

MIT License - veja [LICENSE](LICENSE) para detalhes.

---
