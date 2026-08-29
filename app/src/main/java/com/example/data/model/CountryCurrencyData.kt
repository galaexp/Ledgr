package com.example.data.model

data class CountryInfo(
    val code: String,
    val name: String,
    val flagEmoji: String,
    val defaultCurrency: String,
    val currencySymbol: String
)

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val flagEmoji: String
)

object CountryCurrencyCatalog {
    val supportedCountries = listOf(
        CountryInfo("US", "United States", "🇺🇸", "USD", "$"),
        CountryInfo("GB", "United Kingdom", "🇬🇧", "GBP", "£"),
        CountryInfo("EU", "European Union / Germany", "🇪🇺", "EUR", "€"),
        CountryInfo("IN", "India", "🇮🇳", "INR", "₹"),
        CountryInfo("CA", "Canada", "🇨🇦", "CAD", "C$"),
        CountryInfo("AU", "Australia", "🇦🇺", "AUD", "A$"),
        CountryInfo("AE", "United Arab Emirates", "🇦🇪", "AED", "AED"),
        CountryInfo("SG", "Singapore", "🇸🇬", "SGD", "S$"),
        CountryInfo("CH", "Switzerland", "🇨🇭", "CHF", "CHF"),
        CountryInfo("JP", "Japan", "🇯🇵", "JPY", "¥"),
        CountryInfo("SA", "Saudi Arabia", "🇸🇦", "SAR", "SAR"),
        CountryInfo("NZ", "New Zealand", "🇳🇿", "NZD", "NZ$"),
        CountryInfo("ZA", "South Africa", "🇿🇦", "ZAR", "R"),
        CountryInfo("MX", "Mexico", "🇲🇽", "MXN", "Mex$"),
        CountryInfo("BR", "Brazil", "🇧🇷", "BRL", "R$"),
        CountryInfo("SE", "Sweden", "🇸🇪", "SEK", "kr"),
        CountryInfo("NO", "Norway", "🇳🇴", "NOK", "kr"),
        CountryInfo("DK", "Denmark", "🇩🇰", "DKK", "kr"),
        CountryInfo("PL", "Poland", "🇵🇱", "PLN", "zł"),
        CountryInfo("TH", "Thailand", "🇹🇭", "THB", "฿"),
        CountryInfo("MY", "Malaysia", "🇲🇾", "MYR", "RM"),
        CountryInfo("PH", "Philippines", "🇵🇭", "PHP", "₱"),
        CountryInfo("ID", "Indonesia", "🇮🇩", "IDR", "Rp"),
        CountryInfo("VN", "Vietnam", "🇻🇳", "VND", "₫"),
        CountryInfo("KR", "South Korea", "🇰🇷", "KRW", "₩"),
        CountryInfo("TR", "Turkey", "🇹🇷", "TRY", "₺"),
        CountryInfo("NG", "Nigeria", "🇳🇬", "NGN", "₦"),
        CountryInfo("EG", "Egypt", "🇪🇬", "EGP", "E£"),
        CountryInfo("KE", "Kenya", "🇰🇪", "KES", "KSh")
    )

    val supportedCurrencies = listOf(
        CurrencyInfo("USD", "US Dollar", "$", "🇺🇸"),
        CurrencyInfo("EUR", "Euro", "€", "🇪🇺"),
        CurrencyInfo("GBP", "British Pound", "£", "🇬🇧"),
        CurrencyInfo("INR", "Indian Rupee", "₹", "🇮🇳"),
        CurrencyInfo("CAD", "Canadian Dollar", "C$", "🇨🇦"),
        CurrencyInfo("AUD", "Australian Dollar", "A$", "🇦🇺"),
        CurrencyInfo("AED", "UAE Dirham", "AED", "🇦🇪"),
        CurrencyInfo("SGD", "Singapore Dollar", "S$", "🇸🇬"),
        CurrencyInfo("CHF", "Swiss Franc", "CHF", "🇨🇭"),
        CurrencyInfo("JPY", "Japanese Yen", "¥", "🇯🇵"),
        CurrencyInfo("SAR", "Saudi Riyal", "SAR", "🇸🇦"),
        CurrencyInfo("NZD", "New Zealand Dollar", "NZ$", "🇳🇿"),
        CurrencyInfo("BRL", "Brazilian Real", "R$", "🇧🇷"),
        CurrencyInfo("MXN", "Mexican Peso", "Mex$", "🇲🇽"),
        CurrencyInfo("SEK", "Swedish Krona", "kr", "🇸🇪"),
        CurrencyInfo("PLN", "Polish Zloty", "zł", "🇵🇱"),
        CurrencyInfo("THB", "Thai Baht", "฿", "🇹🇭"),
        CurrencyInfo("MYR", "Malaysian Ringgit", "RM", "🇲🇾"),
        CurrencyInfo("PHP", "Philippine Peso", "₱", "🇵🇭"),
        CurrencyInfo("IDR", "Indonesian Rupiah", "Rp", "🇮🇩"),
        CurrencyInfo("VND", "Vietnamese Dong", "₫", "🇻🇳"),
        CurrencyInfo("KRW", "South Korean Won", "₩", "🇰🇷"),
        CurrencyInfo("TRY", "Turkish Lira", "₺", "🇹🇷"),
        CurrencyInfo("ZAR", "South African Rand", "R", "🇿🇦"),
        CurrencyInfo("NGN", "Nigerian Naira", "₦", "🇳🇬"),
        CurrencyInfo("EGP", "Egyptian Pound", "E£", "🇪🇬"),
        CurrencyInfo("KES", "Kenyan Shilling", "KSh", "🇰🇪")
    )

    fun getSymbolForCurrency(code: String): String {
        return supportedCurrencies.firstOrNull { it.code.equals(code, ignoreCase = true) }?.symbol
            ?: supportedCountries.firstOrNull { it.defaultCurrency.equals(code, ignoreCase = true) }?.currencySymbol
            ?: "$"
    }

    fun getCountry(code: String): CountryInfo? {
        return supportedCountries.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }

    fun getFlagForCountry(code: String): String {
        return supportedCountries.firstOrNull { it.code.equals(code, ignoreCase = true) }?.flagEmoji
            ?: supportedCurrencies.firstOrNull { it.code.equals(code, ignoreCase = true) }?.flagEmoji
            ?: "🌐"
    }

    fun getFlagForCurrency(code: String): String {
        return supportedCurrencies.firstOrNull { it.code.equals(code, ignoreCase = true) }?.flagEmoji
            ?: supportedCountries.firstOrNull { it.defaultCurrency.equals(code, ignoreCase = true) }?.flagEmoji
            ?: "🌐"
    }

    fun formatMoney(amount: Double, currencyCode: String): String {
        val sym = getSymbolForCurrency(currencyCode)
        return java.lang.String.format(java.util.Locale.US, "%s%,.2f", sym, amount)
    }
}

