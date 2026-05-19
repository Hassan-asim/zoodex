package com.Sufi.zoodex.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
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

    // Initialize user profile
    suspend fun initializeUserProfile(callsign: String, faction: String): Boolean {
        return try {
            val profileData = JSONObject().apply {
                put("callsign", callsign)
                put("faction", faction)
                put("level", if (GameState.playerLevel > 0) GameState.playerLevel else 1)
                put("online", true)
                put("last_seen", System.currentTimeMillis())
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
            if (response != null) {
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val reqCallsign = obj.getString("requester_callsign")
                    val friendCallsign = obj.getString("friend_callsign")
                    val otherParty = if (reqCallsign.uppercase() == myCallsign.uppercase()) friendCallsign else reqCallsign
                    val profile = fetchProfileByCallsign(otherParty)
                    if (profile != null) result.add(profile)
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
        return try {
            val messageData = JSONObject().apply {
                put("sender_callsign", senderCallsign)
                put("receiver_callsign", receiverCallsign)
                put("content", content)
                put("created_at", System.currentTimeMillis())
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
        return try {
            val response = makeRequest(
                url = "$SUPABASE_URL/operative_messages?or=(and(sender_callsign.eq.$callsign1,receiver_callsign.eq.$callsign2),and(sender_callsign.eq.$callsign2,receiver_callsign.eq.$callsign1))&order=created_at.asc&limit=$limit",
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
                put("created_at", System.currentTimeMillis())
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

    // Fetch pending incoming friend requests
    suspend fun fetchPendingRequests(myCallsign: String): List<OperativeProfile> {
        return try {
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
                    val profile = fetchProfileByCallsign(requesterCallsign)
                    if (profile != null) result.add(profile)
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
            val updateData = JSONObject().apply {
                put("status", "accepted")
            }
            
            val result = makeRequest(
                url = "$SUPABASE_URL/friendships?requester_callsign=eq.$requesterCallsign&friend_callsign=eq.$myCallsign",
                method = "PATCH",
                body = updateData.toString()
            )
            Log.d(TAG, "Friend request accepted: $result")
            result != null
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting friend request: ${e.message}")
            false
        }
    }

    // Delete/Reject friend request or friendship
    suspend fun deleteFriendship(requesterCallsign: String, friendCallsign: String): Boolean {
        return try {
            val result = makeRequest(
                url = "$SUPABASE_URL/friendships?or=(and(requester_callsign.eq.$requesterCallsign,friend_callsign.eq.$friendCallsign),and(requester_callsign.eq.$friendCallsign,friend_callsign.eq.$requesterCallsign))",
                method = "DELETE"
            )
            Log.d(TAG, "Friendship deleted/rejected: $result")
            result != null
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting friendship: ${e.message}")
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
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "Response Code: $responseCode for URL: $url")

                val stream = if (responseCode == 200 || responseCode == 201) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = stream?.bufferedReader()?.readText()
                stream?.close()
                connection.disconnect()

                response
            } catch (e: Exception) {
                Log.e(TAG, "Request error: ${e.message}", e)
                null
            }
        }
    }
}
