package com.example.pencatatankeuangan.data

import com.example.pencatatankeuangan.model.ShoppingItem
import org.json.JSONArray
import org.json.JSONObject

class ShoppingRepository {

    // READ
    suspend fun getAll(): List<ShoppingItem> {
        val userId = SupabaseRest.requireUserId()

        val text = SupabaseRest.request(
            method = "GET",
            path = "/rest/v1/shopping_items?select=*&user_id=eq.$userId&order=id.asc"
        )

        val array = JSONArray(text)

        return List(array.length()) { index ->
            val item = array.getJSONObject(index)

            ShoppingItem(
                id = item.getLong("id"),
                productName = item.getString("product_name"),
                quantity = item.optInt("quantity", 1),
                priority = item.optString("priority", "Sedang"),
                isPurchased = item.optBoolean("is_purchased", false),
                note = if (item.isNull("note")) "" else item.optString("note")
            )
        }
    }

    // CREATE
    suspend fun add(item: ShoppingItem) {
        val body = toJson(item).put("user_id", SupabaseRest.requireUserId())

        SupabaseRest.request(
            method = "POST",
            path = "/rest/v1/shopping_items",
            body = body
        )
    }

    // UPDATE
    suspend fun update(item: ShoppingItem) {
        val userId = SupabaseRest.requireUserId()

        SupabaseRest.request(
            method = "PATCH",
            path = "/rest/v1/shopping_items?id=eq.${item.id}&user_id=eq.$userId",
            body = toJson(item)
        )
    }

    // DELETE
    suspend fun delete(id: Long) {
        val userId = SupabaseRest.requireUserId()

        SupabaseRest.request(
            method = "DELETE",
            path = "/rest/v1/shopping_items?id=eq.$id&user_id=eq.$userId"
        )
    }

    private fun toJson(item: ShoppingItem) = JSONObject().apply {
        put("product_name", item.productName)
        put("quantity", item.quantity)
        put("priority", item.priority)
        put("is_purchased", item.isPurchased)
        put("note", item.note)
    }
}
