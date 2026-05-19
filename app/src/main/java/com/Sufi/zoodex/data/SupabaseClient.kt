package com.Sufi.zoodex.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.UUID

object SupabaseClient {
    private const val SUPABASE_URL = "https://gicnboxddmuvacuymhwp.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdpY25ib3hkZG11dmFjdXltaHdwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkyMDI5NjAsImV4cCI6MjA5NDc3ODk2MH0.9SuUSypsX6dQ72kKujknv4SIP-mnY1bvB5eQPgyGYlw"
    private const val TAG = "SupabaseClient"

    suspend fun getAnimals(): List<AnimalData> = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL("$SUPABASE_URL/rest/v1/animals?select=*")
            val connection = url.openConnection()
            connection.setRequestProperty("apikey", SUPABASE_KEY)
            connection.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val animals = mutableListOf<AnimalData>()
            
            val jsonArray = JSONArray(response)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                animals.add(
                    AnimalData(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        elementType = obj.getString("element_type"),
                        description = obj.getString("description"),
                        baseHp = obj.getInt("base_hp"),
                        baseAttack = obj.getInt("base_attack"),
                        baseDefense = obj.getInt("base_defense"),
                        baseSpeed = obj.getInt("base_speed"),
                        iconUrl = obj.optString("icon_url", ""),
                        encyclopediaClass = obj.optString("encyclopedia_class", "COMMON")
                    )
                )
            }
            animals
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching animals: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveCaptureRecord(
        playerId: String,
        animalId: Int,
        imagePath: String,
        callsign: String
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL("$SUPABASE_URL/rest/v1/player_captures")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("apikey", SUPABASE_KEY)
            connection.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
            connection.doOutput = true

            val json = JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("player_id", playerId)
                put("animal_id", animalId)
                put("capture_date", System.currentTimeMillis())
                put("image_path", imagePath)
                put("player_callsign", callsign)
            }

            connection.outputStream.write(json.toString().toByteArray())
            connection.outputStream.close()

            val responseCode = (connection as java.net.HttpURLConnection).responseCode
            responseCode in 200..299
        } catch (e: Exception) {
            Log.e(TAG, "Error saving capture record: ${e.message}")
            false
        }
    }

    suspend fun getPlayerCaptures(playerId: String): List<CaptureRecord> = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL("$SUPABASE_URL/rest/v1/player_captures?player_id=eq.$playerId")
            val connection = url.openConnection()
            connection.setRequestProperty("apikey", SUPABASE_KEY)
            connection.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val records = mutableListOf<CaptureRecord>()

            val jsonArray = JSONArray(response)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                records.add(
                    CaptureRecord(
                        id = obj.getString("id"),
                        playerId = obj.getString("player_id"),
                        animalId = obj.getInt("animal_id"),
                        captureDateMillis = obj.getLong("capture_date"),
                        imagePath = obj.getString("image_path")
                    )
                )
            }
            records
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching capture records: ${e.message}")
            emptyList()
        }
    }
}

data class AnimalData(
    val id: Int,
    val name: String,
    val elementType: String,
    val description: String,
    val baseHp: Int,
    val baseAttack: Int,
    val baseDefense: Int,
    val baseSpeed: Int,
    val iconUrl: String,
    val encyclopediaClass: String
)

data class CaptureRecord(
    val id: String,
    val playerId: String,
    val animalId: Int,
    val captureDateMillis: Long,
    val imagePath: String
)
