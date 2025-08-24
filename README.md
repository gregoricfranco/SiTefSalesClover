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

## 📱 Exemplos de Uso

### Crédito à Vista
```kotlin
// Crédito à vista
Button(
    onClick = { onPagamento(valor, PaymentMethod.CREDIT, 1) },
    modifier = Modifier.fillMaxWidth()
) { Text("Crédito à vista (R${valor / 100})") }
```

### Crédito Parcelado
```kotlin
// Crédito parcelado fixo (3x, 6x, etc)
Button(
    onClick = { onPagamento(valor, PaymentMethod.CREDIT_INSTALLMENTS, 3) },
    modifier = Modifier.fillMaxWidth()
) { Text("Crédito parcelado 3x (R${valor / 100})") }

Button(
    onClick = { onPagamento(valor, PaymentMethod.CREDIT_INSTALLMENTS, 6) },
    modifier = Modifier.fillMaxWidth()
) { Text("Crédito parcelado 6x (R${valor / 100})") }
```

### Débito
```kotlin
Button(
    onClick = { onPagamento(valor, PaymentMethod.DEBIT, 1) },
    modifier = Modifier.fillMaxWidth()
) { Text("Débito (R${valor / 100})") }
```

## 🧪 App de Exemplo

O projeto inclui um app Android completo (`MainActivity.kt`) que demonstra como integrar a biblioteca:

### Funcionalidades do App:
- 💳 **Transações de Crédito e Débito** com diferentes valores
- 🎯 **Interface simples** com botões para cada tipo de pagamento  
- 📱 **Configuração automática** para dispositivos Clover
- 🧾 **Exibição de resultados** das transações
- ⚙️ **Configuração SiTef** integrada

### Como testar:

1. Clone o repositório: `git clone https://github.com/gregoricfranco/SiTefSalesClover.git`
2. Abra no Android Studio
3. Configure suas credenciais SiTef no código
4. Execute no dispositivo Clover
5. Use os botões na tela para testar diferentes tipos de transação

> 💡 O app é um exemplo prático de como implementar a biblioteca em um projeto real.
