package com.Sufi.zoodex.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

data class OperativeMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String = "",
    val senderCallsign: String,
    val receiverId: String = "",
    val receiverCallsign: String,
    val content: String,
    val createdAt: String = System.currentTimeMillis().toString(),
    val isRead: Boolean = false
)

data class OperativeProfile(
    val id: String,
    val callsign: String,
    val faction: String,
    val level: Int = 1,
    val online: Boolean = false,
    val lastSeen: String? = null
)

object SupabaseService {
    private const val SUPABASE_URL = "https://gicnboxddmuvacuymhwp.supabase.co/rest/v1"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdpY25ib3hkZG11dmFjdXltaHdwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkyMDI5NjAsImV4cCI6MjA5NDc3ODk2MH0.9SuUSypsX6dQ72kKujknv4SIP-mnY1bvB5eQPgyGYlw"
    private const val TAG = "SupabaseService"

    private fun getISO8601Timestamp(): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        df.timeZone = TimeZone.getTimeZone("UTC")
        return df.format(Date())
    }

    // Initialize user profile
    suspend fun initializeUserProfile(callsign: String, faction: String): Boolean {
        return try {
            val profileData = JSONObject().apply {
                put("callsign", callsign)
                put("faction", faction)
                put("level", if (GameState.playerLevel > 0) GameState.playerLevel else 1)
                put("online", true)
                put("last_seen", getISO8601Timestamp())
            }

            val result = makeRequest(
                url = "$SUPABASE_URL/operative_profiles",
                method = "POST",
                body = profileData.toString(),
                isInsert = true,
                isUpsert = true
            )
            Log.d(TAG, "Profile initialized: $result")
            result != null
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing profile: ${e.message}")
            false
        }
    }

    // Fetch friends list
    suspend fun fetchFriendsForCallsign(myCallsign: String): List<OperativeProfile> {
        return try {
            val response = makeRequest(
                url = "$SUPABASE_URL/friendships?or=(requester_callsign.eq.$myCallsign,friend_callsign.eq.$myCallsign)&status=eq.accepted",
                method = "GET"
            )
            
            val result = mutableListOf<OperativeProfile>()
            val addedCallsigns = mutableSetOf<String>()
            if (response != null) {
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val reqCallsign = obj.getString("requester_callsign")
                    val friendCallsign = obj.getString("friend_callsign")
                    val otherParty = if (reqCallsign.uppercase() == myCallsign.uppercase()) friendCallsign else reqCallsign
                    
                    if (!addedCallsigns.contains(otherParty.uppercase())) {
                        addedCallsigns.add(otherParty.uppercase())
                        val profile = fetchProfileByCallsign(otherParty)
                        if (profile != null) result.add(profile)
                    }
                }
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching friends: ${e.message}")
            emptyList()
        }
    }

    // Fetch single profile by callsign
    suspend fun fetchProfileByCallsign(callsign: String): OperativeProfile? {
        return try {
            val response = makeRequest(
                url = "$SUPABASE_URL/operative_profiles?callsign=eq.$callsign",
                method = "GET"
            )
            
            if (response != null) {
                val array = JSONArray(response)
                if (array.length() > 0) {
                    val obj = array.getJSONObject(0)
                    return OperativeProfile(
                        id = obj.getString("id"),
                        callsign = obj.getString("callsign"),
                        faction = obj.getString("faction"),
                        level = obj.optInt("level", 1),
                        online = obj.optBoolean("online", false),
                        lastSeen = obj.optString("last_seen")
                    )
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching profile: ${e.message}")
            null
        }
    }

    // Send message
    suspend fun sendMessage(
        senderCallsign: String,
        receiverCallsign: String,
        content: String
    ): Boolean {
        val s = senderCallsign.trim().uppercase()
        val r = receiverCallsign.trim().uppercase()
        return try {
            val messageData = JSONObject().apply {
                put("sender_callsign", s)
                put("receiver_callsign", r)
                put("content", content)
                put("is_read", false)
            }

            val result = makeRequest(
                url = "$SUPABASE_URL/operative_messages",
                method = "POST",
                body = messageData.toString(),
                isInsert = true
            )
            Log.d(TAG, "Message sent: $result")
            result != null
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}")
            false
        }
    }

    // Fetch messages between two users
    suspend fun fetchMessages(
        callsign1: String,
        callsign2: String,
        limit: Int = 50
    ): List<OperativeMessage> {
        val c1 = callsign1.trim().uppercase()
        val c2 = callsign2.trim().uppercase()
        if (c1.isBlank() || c2.isBlank()) {
            Log.e(TAG, "fetchMessages: callsings must not be blank")
            return emptyList()
        }
        return try {
            val response = makeRequest(
                url = "$SUPABASE_URL/operative_messages?or=(and(sender_callsign.eq.$c1,receiver_callsign.eq.$c2),and(sender_callsign.eq.$c2,receiver_callsign.eq.$c1))&order=created_at.asc&limit=$limit",
                method = "GET"
            )

            val result = mutableListOf<OperativeMessage>()
            if (response != null) {
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    result.add(
                        OperativeMessage(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            senderId = obj.optString("sender_id", ""),
                            senderCallsign = obj.getString("sender_callsign"),
                            receiverId = obj.optString("receiver_id", ""),
                            receiverCallsign = obj.getString("receiver_callsign"),
                            content = obj.getString("content"),
                            createdAt = obj.optString("created_at", ""),
                            isRead = obj.optBoolean("is_read", false)
                        )
                    )
                }
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching messages: ${e.message}")
            emptyList()
        }
    }

    // Add friend
    suspend fun addFriend(myCallsign: String, friendCallsign: String): Boolean {
        return try {
            val friendshipData = JSONObject().apply {
                put("requester_callsign", myCallsign)
                put("friend_callsign", friendCallsign)
                put("status", "pending")
            }

            val result = makeRequest(
                url = "$SUPABASE_URL/friendships",
                method = "POST",
                body = friendshipData.toString(),
                isInsert = true
            )
            Log.d(TAG, "Friend request sent: $result")
            result != null
        } catch (e: Exception) {
            Log.e(TAG, "Error adding friend: ${e.message}")
            false
        }
    }

    // Fetch all resolved friendships (accepted or rejected) for a user
    private suspend fun fetchResolvedFriendships(myCallsign: String): Set<String> {
        return try {
            val response = makeRequest(
                url = "$SUPABASE_URL/friendships?or=(requester_callsign.eq.$myCallsign,friend_callsign.eq.$myCallsign)&status=in.(accepted,rejected)",
                method = "GET"
            )
            val resolved = mutableSetOf<String>()
            if (response != null) {
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val req = obj.getString("requester_callsign")
                    val fri = obj.getString("friend_callsign")
                    resolved.add(if (req.uppercase() == myCallsign.uppercase()) fri.uppercase() else req.uppercase())
                }
            }
            resolved
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching resolved friendships: ${e.message}")
            emptySet()
        }
    }

    // Fetch pending incoming friend requests
    suspend fun fetchPendingRequests(myCallsign: String): List<OperativeProfile> {
        return try {
            val resolvedSet = fetchResolvedFriendships(myCallsign)
            
            val response = makeRequest(
                url = "$SUPABASE_URL/friendships?friend_callsign=eq.$myCallsign&status=eq.pending",
                method = "GET"
            )
            
            val result = mutableListOf<OperativeProfile>()
            if (response != null) {
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val requesterCallsign = obj.getString("requester_callsign")
                    if (!resolvedSet.contains(requesterCallsign.uppercase())) {
                        val profile = fetchProfileByCallsign(requesterCallsign)
                        if (profile != null) result.add(profile)
                    }
                }
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching pending requests: ${e.message}")
            emptyList()
        }
    }

    // Accept friend request
    suspend fun acceptFriendRequest(requesterCallsign: String, myCallsign: String): Boolean {
        return try {
            val friendshipData = JSONObject().apply {
                put("requester_callsign", myCallsign)
                put("friend_callsign", requesterCallsign)
                put("status", "accepted")
            }

            val result = makeRequest(
                url = "$SUPABASE_URL/friendships",
                method = "POST",
                body = friendshipData.toString(),
                isInsert = true
            )
            Log.d(TAG, "Friend request accepted and inserted: $result")
            result != null
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting friend request: ${e.message}")
            false
        }
    }

    // Delete/Reject friend request or friendship (Decline)
    suspend fun deleteFriendship(requesterCallsign: String, friendCallsign: String): Boolean {
        return try {
            val friendshipData = JSONObject().apply {
                put("requester_callsign", friendCallsign) // myCallsign
                put("friend_callsign", requesterCallsign) // requesterCallsign
                put("status", "rejected")
            }

            val result = makeRequest(
                url = "$SUPABASE_URL/friendships",
                method = "POST",
                body = friendshipData.toString(),
                isInsert = true
            )
            Log.d(TAG, "Friend request declined and rejected: $result")
            result != null
        } catch (e: Exception) {
            Log.e(TAG, "Error declining friendship: ${e.message}")
            false
        }
    }

    // Generic HTTP request helper
    private suspend fun makeRequest(
        url: String,
        method: String = "GET",
        body: String? = null,
        isInsert: Boolean = false,
        isUpsert: Boolean = false
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val requestUrl = URL(url)
                val connection = requestUrl.openConnection() as java.net.HttpURLConnection
                connection.apply {
                    requestMethod = method
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                    setRequestProperty("apikey", SUPABASE_KEY)
                    setRequestProperty("Content-Type", "application/json")
                    if (isUpsert) {
                        setRequestProperty("Prefer", "resolution=merge-duplicates")
                    } else {
                        setRequestProperty("Prefer", if (isInsert) "return=minimal" else "return=representation")
                    }
                }

                body?.let {
                    connection.doOutput = true
                    connection.outputStream.write(it.toByteArray())
                    connection.outputStream.flush()
                    connection.outputStream.close()
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "Response Code: $responseCode for URL: $url")

                val response = if (responseCode in 200..299) {
                    if (responseCode == 204) {
                        ""
                    } else {
                        val stream = connection.inputStream
                        val content = stream?.bufferedReader()?.readText()
                        stream?.close()
                        content
                    }
                } else {
                    val stream = connection.errorStream
                    val content = stream?.bufferedReader()?.readText()
                    stream?.close()
                    Log.e(TAG, "Request failed with code $responseCode: $content")
                    null
                }

                connection.disconnect()
                response
            } catch (e: Exception) {
                Log.e(TAG, "Request error: ${e.message}", e)
                null
            }
        }
    }

    suspend fun saveTerritoryClaim(
        callsign: String,
        lat: Double,
        lng: Double,
        radius: Double,
        faction: String
    ): Boolean {
        val statusStr = "$lat,$lng,$radius,$faction"
        val id = UUID.randomUUID().toString()
        return try {
            val claimData = JSONObject().apply {
                put("id", id)
                put("requester_callsign", "TERRITORY")
                put("friend_callsign", callsign)
                put("status", statusStr)
            }
            val result = makeRequest(
                url = "$SUPABASE_URL/friendships",
                method = "POST",
                body = claimData.toString(),
                isInsert = true
            )
            result != null
        } catch (e: Exception) {
            Log.e(TAG, "Error saving territory claim: ${e.message}")
            false
        }
    }

    suspend fun fetchTerritoryClaims(): List<ClaimedTerritory> {
        return try {
            val response = makeRequest(
                url = "$SUPABASE_URL/friendships?requester_callsign=eq.TERRITORY",
                method = "GET"
            )
            val claims = mutableListOf<ClaimedTerritory>()
            if (response != null) {
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id", UUID.randomUUID().toString())
                    val callsign = obj.getString("friend_callsign")
                    val status = obj.getString("status")
                    val createdAt = obj.optString("created_at", "")
                    
                    // Parse status: lat,lng,radius,faction
                    val parts = status.split(",")
                    if (parts.size >= 4) {
                        val lat = parts[0].toDoubleOrNull() ?: 0.0
                        val lng = parts[1].toDoubleOrNull() ?: 0.0
                        val radius = parts[2].toDoubleOrNull() ?: 50.0
                        val faction = parts[3]
                        claims.add(ClaimedTerritory(id, callsign, lat, lng, radius, faction, createdAt))
                    }
                }
            }
            claims
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching territory claims: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveOrExpandTerritoryClaim(
        callsign: String,
        lat: Double,
        lng: Double,
        radius: Double,
        faction: String,
        expandExisting: Boolean
    ): Boolean {
        return try {
            val allClaims = fetchTerritoryClaims()
            val myClaims = allClaims.filter { it.callsign.equals(callsign, ignoreCase = true) }
            val latestMine = myClaims.maxByOrNull { it.createdAt }
            val finalLat: Double
            val finalLng: Double
            val finalRadius: Double

            if (expandExisting && latestMine != null) {
                finalLat = latestMine.lat
                finalLng = latestMine.lng
                finalRadius = (latestMine.radius + (radius * 0.65)).coerceAtLeast(latestMine.radius + 12.0)
            } else {
                finalLat = lat
                finalLng = lng
                finalRadius = radius
            }

            saveTerritoryClaim(
                callsign = callsign,
                lat = finalLat,
                lng = finalLng,
                radius = finalRadius,
                faction = faction
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in saveOrExpandTerritoryClaim: ${e.message}")
            false
        }
    }

    suspend fun createBattleRequest(
        challengerCallsign: String,
        defenderCallsign: String,
        territoryId: String,
        lat: Double,
        lng: Double
    ): Boolean {
        val payload = listOf(
            challengerCallsign.trim().uppercase(),
            territoryId,
            lat.toString(),
            lng.toString(),
            getISO8601Timestamp()
        ).joinToString("|")
        return try {
            val reqData = JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("requester_callsign", "BATTLE_REQUEST")
                put("friend_callsign", defenderCallsign.trim().uppercase())
                put("status", payload)
            }
            val result = makeRequest(
                url = "$SUPABASE_URL/friendships",
                method = "POST",
                body = reqData.toString(),
                isInsert = true
            )
            result != null
        } catch (e: Exception) {
            Log.e(TAG, "Error creating battle request: ${e.message}")
            false
        }
    }
}

data class ClaimedTerritory(
    val id: String,
    val callsign: String,
    val lat: Double,
    val lng: Double,
    val radius: Double,
    val faction: String,
    val createdAt: String = ""
)

data class BattleRequest(
    val id: String,
    val challengerCallsign: String,
    val defenderCallsign: String,
    val territoryId: String,
    val lat: Double,
    val lng: Double,
    val createdAt: String = ""
)
