package com.takipsanplus.rfidtablet.presentation.shipment

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.takipsanplus.rfidtablet.R

/**
 * QR ham metninden (04 model, 05 quality, …) alanları çıkarır.
 * Öncelik: JSON `{"04":3230,"05":385,...}` biçimi (okuyucunun döndürdüğü ham metin).
 * API’ye gönderilecek değer ayrı tutulur; bu sadece gösterim içindir.
 */
object ShipmentQrPayloadParser {

    /** Tabloda gösterilecek sıra ve anlamları. */
    val DISPLAY_AI_ORDER = listOf("04", "05", "06", "07", "13")

    private val aiInParens = Regex("""\((\d{2})\)""")

    fun labelResForAi(ai: String): Int? = when (ai) {
        "04" -> R.string.shipment_qr_field_model
        "05" -> R.string.shipment_qr_field_quality
        "06" -> R.string.shipment_qr_field_color
        "07" -> R.string.shipment_qr_field_size
        "13" -> R.string.shipment_qr_field_order_code
        else -> null
    }

    fun extractAiFields(raw: String): Map<String, String> {
        val s = raw.trim()
        if (s.isEmpty()) return emptyMap()

        parseJsonObjectQr(s)?.let { full ->
            return linkedMapOf<String, String>().apply {
                DISPLAY_AI_ORDER.forEach { ai ->
                    put(ai, full[normalizeJsonKey(ai)].orEmpty())
                }
            }
        }

        val paren = parseParenAis(s)
        if (paren.isNotEmpty()) return paren
        val kv = parseKeyValueDelimited(s)
        if (kv.isNotEmpty()) return kv
        val bare = parseBareAiPrefixChain(s)
        if (bare.isNotEmpty()) return bare
        return parseGroupSeparatorSegments(s)
    }

    /**
     * `{"00":1,"04":3230,"13":"39060-W/1"}` — anahtarlar rakam stringi, iki haneye normalize.
     */
    private fun parseJsonObjectQr(raw: String): Map<String, String>? {
        val t = raw.trim()
        if (!t.startsWith('{')) return null
        return try {
            val el = JsonParser.parseString(t)
            if (!el.isJsonObject) return null
            val obj = el.asJsonObject
            buildMap {
                for (e in obj.entrySet()) {
                    val nk = normalizeJsonKey(e.key)
                    put(nk, jsonElementToDisplayString(e.value))
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    /** "4" → "04"; "04" → "04"; "13" → "13" */
    private fun normalizeJsonKey(key: String): String {
        val k = key.trim()
        if (k.matches(Regex("""^\d{1,2}$"""))) return k.padStart(2, '0')
        return k
    }

    private fun jsonElementToDisplayString(e: JsonElement): String {
        when {
            e.isJsonNull -> return ""
            e.isJsonArray -> return e.asJsonArray.joinElements()
            e.isJsonObject -> return e.asJsonObject.toString()
            e.isJsonPrimitive -> {
                val p = e.asJsonPrimitive
                return when {
                    p.isString -> p.asString
                    p.isBoolean -> p.asBoolean.toString()
                    p.isNumber -> numberToPlainString(p)
                    else -> p.toString()
                }
            }
            else -> return e.toString()
        }
    }

    private fun JsonArray.joinElements(): String =
        map { jsonElementToDisplayString(it) }.joinToString(", ")

    private fun numberToPlainString(p: JsonPrimitive): String {
        val n = p.asNumber
        return try {
            val d = n.toDouble()
            if (d.isFinite() && d == kotlin.math.floor(d) && kotlin.math.abs(d) <= Long.MAX_VALUE) {
                d.toLong().toString()
            } else {
                n.toString()
            }
        } catch (_: Exception) {
            n.toString()
        }
    }

    private fun parseParenAis(raw: String): Map<String, String> {
        val matches = aiInParens.findAll(raw).toList()
        if (matches.isEmpty()) return emptyMap()
        val map = linkedMapOf<String, String>()
        for (i in matches.indices) {
            val ai = matches[i].groupValues[1]
            val valueStart = matches[i].range.last + 1
            val valueEnd = if (i + 1 < matches.size) matches[i + 1].range.first else raw.length
            val value = raw.substring(valueStart, valueEnd).trim()
            map[ai] = value
        }
        return map
    }

    /** Örnek: `04:abc;05:def` veya `04=abc|05=def` */
    private fun parseKeyValueDelimited(raw: String): Map<String, String> {
        val r = Regex("""(?:^|[|;])(\d{2})\s*[:=]\s*([^|;]+)""")
        val map = linkedMapOf<String, String>()
        r.findAll(raw).forEach { map[it.groupValues[1]] = it.groupValues[2].trim() }
        return map
    }

    /**
     * Parantez yok: `04`deger`05`deger… sırası (değer içinde sonraki AI kodu geçmemeli).
     */
    private fun parseBareAiPrefixChain(raw: String): Map<String, String> {
        val known = DISPLAY_AI_ORDER.toSet()
        val map = linkedMapOf<String, String>()
        var i = 0
        val r = raw
        while (i <= r.length - 2) {
            val ai = r.substring(i, i + 2)
            if (!ai.all { it.isDigit() } || ai !in known) {
                return if (map.isEmpty()) emptyMap() else map
            }
            i += 2
            val nextAt = known.asSequence()
                .filter { it != ai }
                .mapNotNull { cand ->
                    val j = r.indexOf(cand, i)
                    if (j >= i) j else null
                }
                .minOrNull() ?: r.length
            map[ai] = r.substring(i, nextAt).trim()
            i = nextAt
        }
        return map
    }

    /** GS1 grup ayırıcı (\u001d) ile gelen segmentler: `04` + değer */
    private fun parseGroupSeparatorSegments(raw: String): Map<String, String> {
        val gs = '\u001d'
        if (!raw.contains(gs)) return emptyMap()
        val map = linkedMapOf<String, String>()
        for (seg in raw.split(gs)) {
            val t = seg.trim()
            if (t.length < 3) continue
            val ai = t.take(2)
            if (!ai.all { it.isDigit() }) continue
            map[ai] = t.drop(2).trim()
        }
        return map
    }
}
