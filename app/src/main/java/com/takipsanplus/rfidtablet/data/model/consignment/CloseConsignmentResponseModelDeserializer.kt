package com.takipsanplus.rfidtablet.data.model.consignment

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * API sometimes returns `data` as a JSON array, sometimes as a single object.
 * Default Gson cannot map both to [CloseConsignmentResponseModel].
 */
class CloseConsignmentResponseModelDeserializer : JsonDeserializer<CloseConsignmentResponseModel> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): CloseConsignmentResponseModel {
        val root = json?.asJsonObject ?: throw JsonParseException("closeConsignment: expected JSON object")
        val dataEl = root.get("data")
        val data: List<ConsignmentData> = when {
            dataEl == null || dataEl.isJsonNull -> emptyList()
            dataEl.isJsonArray -> {
                val listType = object : TypeToken<List<ConsignmentData>>() {}.type
                context.deserialize<List<ConsignmentData>>(dataEl, listType) ?: emptyList()
            }
            dataEl.isJsonObject -> listOf(context.deserialize(dataEl, ConsignmentData::class.java))
            else -> emptyList()
        }
        val errorMessage = stringOrEmpty(root, "errorMessage", "error_message")
        val status = stringOrEmpty(root, "status")
        return CloseConsignmentResponseModel(
            data = data,
            errorMessage = errorMessage,
            status = status
        )
    }

    private fun stringOrEmpty(root: JsonObject, vararg keys: String): String {
        for (k in keys) {
            val el = root.get(k) ?: continue
            if (!el.isJsonNull && el.isJsonPrimitive) return el.asString
        }
        return ""
    }
}
