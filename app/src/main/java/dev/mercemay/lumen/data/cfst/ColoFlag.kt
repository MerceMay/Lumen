package dev.mercemay.lumen.data.cfst

object ColoFlag {
    private val countryByColo = mapOf(
        "LAX" to "US", "SJC" to "US", "SEA" to "US", "DFW" to "US", "ORD" to "US", "IAD" to "US", "EWR" to "US", "JFK" to "US", "MIA" to "US", "ATL" to "US", "DEN" to "US", "PHX" to "US",
        "HKG" to "HK", "NRT" to "JP", "KIX" to "JP", "SIN" to "SG", "ICN" to "KR", "TPE" to "TW", "BKK" to "TH", "KUL" to "MY", "MNL" to "PH", "CGK" to "ID", "HAN" to "VN", "SGN" to "VN",
        "LHR" to "GB", "MAN" to "GB", "CDG" to "FR", "FRA" to "DE", "AMS" to "NL", "MAD" to "ES", "BCN" to "ES", "MXP" to "IT", "FCO" to "IT", "ZRH" to "CH", "ARN" to "SE", "WAW" to "PL", "VIE" to "AT", "PRG" to "CZ",
        "SYD" to "AU", "MEL" to "AU", "AKL" to "NZ",
        "YYZ" to "CA", "YVR" to "CA", "MEX" to "MX", "GRU" to "BR", "SCL" to "CL", "EZE" to "AR",
        "JNB" to "ZA", "CPT" to "ZA", "DXB" to "AE", "TLV" to "IL", "BOM" to "IN", "DEL" to "IN",
    )

    fun flagForColo(colo: String?): String {
        val country = countryByColo[colo?.uppercase().orEmpty()] ?: return Unknown
        return countryToFlag(country)
    }

    fun countryToFlag(countryCode: String?): String {
        val uppercase = countryCode?.uppercase().orEmpty()
        if (uppercase.length != 2) return Unknown
        val first = Character.codePointAt(uppercase, 0) - 'A'.code + 0x1F1E6
        val second = Character.codePointAt(uppercase, 1) - 'A'.code + 0x1F1E6
        return String(Character.toChars(first)) + String(Character.toChars(second))
    }

    const val Unknown = "Unknown"
}
