package com.Sufi.zoodex.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class Beast(
    val id: Int,
    val name: String,
    var nickname: String,
    var level: Int,
    var xp: Int,
    val elementType: String, // FIRE, WATER, CYBER, VOID, ELECTR
    var strength: Int,
    var defense: Int,
    var agility: Int,
    var maxHp: Int,
    var currentHp: Int,
    var maxEnergy: Int = 100,
    var currentEnergy: Int = 100,
    var allocatedPoints: Int = 0,
    var inActiveTeam: Boolean = false,
    /** Wall-clock millis when beast can fight again after arena faint. */
    var recoverUntilMillis: Long = 0L
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("nickname", nickname)
        obj.put("level", level)
        obj.put("xp", xp)
        obj.put("elementType", elementType)
        obj.put("strength", strength)
        obj.put("defense", defense)
        obj.put("agility", agility)
        obj.put("maxHp", maxHp)
        obj.put("currentHp", currentHp)
        obj.put("maxEnergy", maxEnergy)
        obj.put("currentEnergy", currentEnergy)
        obj.put("allocatedPoints", allocatedPoints)
        obj.put("inActiveTeam", inActiveTeam)
        obj.put("recoverUntilMillis", recoverUntilMillis)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): Beast {
            return Beast(
                id = obj.getInt("id"),
                name = obj.getString("name"),
                nickname = obj.getString("nickname"),
                level = obj.getInt("level"),
                xp = obj.getInt("xp"),
                elementType = obj.getString("elementType"),
                strength = obj.getInt("strength"),
                defense = obj.getInt("defense"),
                agility = obj.getInt("agility"),
                maxHp = obj.getInt("maxHp"),
                currentHp = obj.getInt("currentHp"),
                maxEnergy = obj.optInt("maxEnergy", 100),
                currentEnergy = obj.optInt("currentEnergy", 100),
                allocatedPoints = obj.optInt("allocatedPoints", 0),
                inActiveTeam = obj.optBoolean("inActiveTeam", false),
                recoverUntilMillis = obj.optLong("recoverUntilMillis", 0L)
            )
        }
    }
}

data class BeastTemplate(
    val id: Int,
    val name: String,
    val elementType: String,
    val description: String,
    val baseHp: Int,
    val baseStrength: Int,
    val baseDefense: Int,
    val baseAgility: Int
)

data class ChatMessage(val sender: String, val text: String, val isMe: Boolean, val time: String) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("sender", sender)
        obj.put("text", text)
        obj.put("isMe", isMe)
        obj.put("time", time)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): ChatMessage {
            return ChatMessage(
                sender = obj.getString("sender"),
                text = obj.getString("text"),
                isMe = obj.getBoolean("isMe"),
                time = obj.getString("time")
            )
        }
    }
}

data class Sector(
    val id: Int,
    val name: String,
    var faction: String, // NEON_SYNDICATE, VOID_RUNNERS, IRON_VANGUARD, UNCLAIMED
    val threatLevel: Int, // 1-5
    val rewardGold: Int,
    val rewardXP: Int
)

object GameState {
    var callsign: String = ""
    var faction: String = ""
    var playerLevel: Int = 1
    var playerXP: Int = 0
    var playerGold: Int = 250
    var statPointsAvailable: Int = 3
    var xpBoostersOwned: Int = 0
    var isDarkTheme by mutableStateOf(true)
    var playerAvatar by mutableStateOf("🦊")
    var activeTerritoryBattle: ClaimedTerritory? = null

    val beastTemplates: List<BeastTemplate>
        get() = AnimalDatabase.allAnimals.map {
            BeastTemplate(
                id = it.id,
                name = it.name,
                elementType = it.elementType,
                description = it.description,
                baseHp = it.baseHp,
                baseStrength = it.baseAttack,
                baseDefense = it.baseDefense,
                baseAgility = it.baseSpeed
            )
        }

    val capturedBeasts = mutableStateListOf<Beast>()
    val sectors = mutableStateListOf<Sector>()

    val playerTeams = mutableStateListOf<List<Int>>()
    val friendChats = mutableStateMapOf<String, List<ChatMessage>>()

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val prefs = context.getSharedPreferences("zoodex_save", Context.MODE_PRIVATE)

        callsign = prefs.getString("callsign", "") ?: ""
        faction = prefs.getString("faction", "") ?: ""
        playerLevel = prefs.getInt("playerLevel", 1)
        playerXP = prefs.getInt("playerXP", 0)
        playerGold = prefs.getInt("playerGold", 250)
        statPointsAvailable = prefs.getInt("statPointsAvailable", 3)
        xpBoostersOwned = prefs.getInt("xpBoostersOwned", 0)
        isDarkTheme = prefs.getBoolean("isDarkTheme", true)
        playerAvatar = prefs.getString("playerAvatar", "🦊") ?: "🦊"

        // Load Beasts
        val beastsJsonStr = prefs.getString("beasts_list", null)
        capturedBeasts.clear()
        if (beastsJsonStr != null) {
            try {
                val array = JSONArray(beastsJsonStr)
                for (i in 0 until array.length()) {
                    capturedBeasts.add(Beast.fromJsonObject(array.getJSONObject(i)))
                }
            } catch (e: Exception) {
                loadDefaultBeasts()
            }
        } else {
            loadDefaultBeasts()
        }

        // Align loaded beast IDs with the master database to prevent ID mismatches
        capturedBeasts.forEachIndexed { index, beast ->
            val correctAnimal = AnimalDatabase.allAnimals.find { it.name.uppercase() == beast.name.uppercase() }
            if (correctAnimal != null && correctAnimal.id != beast.id) {
                capturedBeasts[index] = beast.copy(id = correctAnimal.id)
            }
        }

        ensureStarterBeastUnlocked()

        // Load Sectors
        val sectorsJsonStr = prefs.getString("sectors_list", null)
        sectors.clear()
        if (sectorsJsonStr != null) {
            try {
                val array = JSONArray(sectorsJsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    sectors.add(
                        Sector(
                            id = obj.getInt("id"),
                            name = obj.getString("name"),
                            faction = obj.getString("faction"),
                            threatLevel = obj.getInt("threatLevel"),
                            rewardGold = obj.getInt("rewardGold"),
                            rewardXP = obj.getInt("rewardXP")
                        )
                    )
                }
            } catch (e: Exception) {
                loadDefaultSectors()
            }
        } else {
            loadDefaultSectors()
        }

        // Load Teams
        val teamsJsonStr = prefs.getString("player_teams", null)
        playerTeams.clear()
        if (teamsJsonStr != null) {
            try {
                val array = JSONArray(teamsJsonStr)
                for (i in 0 until 5) {
                    val teamArray = array.getJSONArray(i)
                    val teamList = mutableListOf<Int>()
                    for (j in 0 until teamArray.length()) {
                        teamList.add(teamArray.getInt(j))
                    }
                    playerTeams.add(teamList)
                }
            } catch (e: Exception) {
                loadDefaultTeams()
            }
        } else {
            loadDefaultTeams()
        }

        // Load Chats
        val chatsJsonStr = prefs.getString("friend_chats", null)
        friendChats.clear()
        if (chatsJsonStr != null) {
            try {
                val obj = JSONObject(chatsJsonStr)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val msgArray = obj.getJSONArray(key)
                    val msgList = mutableListOf<ChatMessage>()
                    for (i in 0 until msgArray.length()) {
                        msgList.add(ChatMessage.fromJsonObject(msgArray.getJSONObject(i)))
                    }
                    friendChats[key] = msgList
                }
            } catch (e: Exception) {
                loadDefaultChats()
            }
        } else {
            loadDefaultChats()
        }

        initialized = true
    }

    private fun ensureStarterBeastUnlocked() {
        if (capturedBeasts.any { it.name.equals("STRAY DOG", ignoreCase = true) }) return
        val starter = beastTemplates.find { it.name.equals("STRAY DOG", ignoreCase = true) }
        if (starter != null) {
            capturedBeasts.add(
                Beast(
                    id = starter.id,
                    name = starter.name,
                    nickname = starter.name,
                    level = 1,
                    xp = 0,
                    elementType = starter.elementType,
                    strength = starter.baseStrength,
                    defense = starter.baseDefense,
                    agility = starter.baseAgility,
                    maxHp = starter.baseHp,
                    currentHp = starter.baseHp,
                    inActiveTeam = true
                )
            )
        }
    }

    private fun loadDefaultBeasts() {
        capturedBeasts.clear()
    }

    private fun loadDefaultSectors() {
        sectors.clear()
        sectors.addAll(
            listOf(
                Sector(1, "OUTPOST SIGMA", "UNCLAIMED", 1, 50, 40),
                Sector(2, "DATA HIGHWAY 9", "NEON_SYNDICATE", 2, 90, 75),
                Sector(3, "VOID SPHERE", "VOID_RUNNERS", 3, 140, 120),
                Sector(4, "IRON GRID-IRON", "IRON_VANGUARD", 4, 200, 180),
                Sector(5, "CORE TERMINAL", "UNCLAIMED", 5, 350, 300)
            )
        )
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences("zoodex_save", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("callsign", callsign)
        editor.putString("faction", faction)
        editor.putInt("playerLevel", playerLevel)
        editor.putInt("playerXP", playerXP)
        editor.putInt("playerGold", playerGold)
        editor.putInt("statPointsAvailable", statPointsAvailable)
        editor.putInt("xpBoostersOwned", xpBoostersOwned)
        editor.putBoolean("isDarkTheme", isDarkTheme)
        editor.putString("playerAvatar", playerAvatar)

        // Serialize Beasts
        val beastsArray = JSONArray()
        capturedBeasts.forEach { beastsArray.put(it.toJsonObject()) }
        editor.putString("beasts_list", beastsArray.toString())

        // Serialize Sectors
        val sectorsArray = JSONArray()
        sectors.forEach { sec ->
            val obj = JSONObject()
            obj.put("id", sec.id)
            obj.put("name", sec.name)
            obj.put("faction", sec.faction)
            obj.put("threatLevel", sec.threatLevel)
            obj.put("rewardGold", sec.rewardGold)
            obj.put("rewardXP", sec.rewardXP)
            sectorsArray.put(obj)
        }
        editor.putString("sectors_list", sectorsArray.toString())

        // Serialize Teams
        val teamsArray = JSONArray()
        playerTeams.forEach { team ->
            val teamArray = JSONArray()
            team.forEach { teamArray.put(it) }
            teamsArray.put(teamArray)
        }
        editor.putString("player_teams", teamsArray.toString())

        // Serialize Chats
        val chatsObj = JSONObject()
        friendChats.forEach { (friendName, messages) ->
            val msgArray = JSONArray()
            messages.forEach { msgArray.put(it.toJsonObject()) }
            chatsObj.put(friendName, msgArray)
        }
        editor.putString("friend_chats", chatsObj.toString())

        editor.apply()
    }

    fun setProfile(context: Context, newCallsign: String, newFaction: String, newAvatar: String) {
        callsign = newCallsign.uppercase()
        faction = newFaction
        playerAvatar = newAvatar
        save(context)
    }

    fun captureBeast(context: Context, name: String, elementType: String): Beast {
        val existing = capturedBeasts.find { it.name.uppercase() == name.uppercase() }
        if (existing != null) {
            addXPAndGold(context, 50, 40)
            save(context)
            return existing
        }

        val template = beastTemplates.find { it.name.uppercase() == name.uppercase() }
        val baseHp = template?.baseHp ?: 100
        val baseStrength = template?.baseStrength ?: 10
        val baseDefense = template?.baseDefense ?: 10
        val baseAgility = template?.baseAgility ?: 10
        val id = template?.id ?: ((capturedBeasts.maxOfOrNull { it.id } ?: 0) + 1)

        val newBeast = Beast(
            id = id,
            name = name.uppercase(),
            nickname = name.uppercase(),
            level = 1,
            xp = 0,
            elementType = elementType,
            strength = baseStrength,
            defense = baseDefense,
            agility = baseAgility,
            maxHp = baseHp,
            currentHp = baseHp,
            inActiveTeam = capturedBeasts.count { it.inActiveTeam } < 3
        )
        capturedBeasts.add(newBeast)
        
        // Earn some gold & XP on successful capture
        addXPAndGold(context, 50, 40)
        save(context)
        return newBeast
    }

    fun allocateStat(context: Context, beastId: Int, statType: String): Boolean {
        if (statPointsAvailable <= 0) return false
        val beast = capturedBeasts.find { it.id == beastId } ?: return false

        when (statType) {
            "STRENGTH" -> beast.strength += 2
            "DEFENSE" -> beast.defense += 2
            "AGILITY" -> beast.agility += 2
            "HP" -> {
                beast.maxHp += 15
                beast.currentHp += 15
            }
            else -> return false
        }
        beast.allocatedPoints += 1
        statPointsAvailable -= 1
        save(context)
        return true
    }

    fun toggleRoster(context: Context, beastId: Int): Boolean {
        val beast = capturedBeasts.find { it.id == beastId } ?: return false
        if (beast.inActiveTeam) {
            // Can't remove if it's the last member
            if (capturedBeasts.count { it.inActiveTeam } <= 1) return false
            beast.inActiveTeam = false
        } else {
            // Max 3 members
            if (capturedBeasts.count { it.inActiveTeam } >= 3) return false
            beast.inActiveTeam = true
        }
        save(context)
        return true
    }

    fun buyItem(context: Context, itemType: String, cost: Int): Boolean {
        if (playerGold < cost) return false
        playerGold -= cost
        when (itemType) {
            "XP_BOOSTER" -> xpBoostersOwned += 1
            "HP_REP" -> {
                capturedBeasts.forEach {
                    it.currentHp = it.maxHp
                    it.recoverUntilMillis = 0L
                }
            }
            "RECOVERY_STIM" -> {
                capturedBeasts.forEach { it.recoverUntilMillis = 0L }
            }
        }
        save(context)
        return true
    }

    fun isBeastRecovering(beast: Beast): Boolean =
        beast.recoverUntilMillis > System.currentTimeMillis()

    fun recoveryMinutesRemaining(beast: Beast): Int {
        val left = beast.recoverUntilMillis - System.currentTimeMillis()
        if (left <= 0L) return 0
        return ((left + TimeUnit.MINUTES.toMillis(1) - 1) / TimeUnit.MINUTES.toMillis(1)).toInt()
    }

    /** After arena faint: 10–30 min base; crushing hits add extra downtime. */
    fun scheduleArenaRecovery(context: Context, beastId: Int, lastHitDamage: Int, maxHp: Int) {
        val beast = capturedBeasts.find { it.id == beastId } ?: return
        val crushing = maxHp > 0 && lastHitDamage >= (maxHp * 0.55f)
        val baseMinutes = Random.nextInt(10, 31)
        val extra = if (crushing) Random.nextInt(5, 13) else 0
        beast.recoverUntilMillis = System.currentTimeMillis() +
            TimeUnit.MINUTES.toMillis((baseMinutes + extra).toLong())
        save(context)
    }

    fun useXPBooster(context: Context): Boolean {
        if (xpBoostersOwned <= 0) return false
        xpBoostersOwned -= 1
        // Give player a direct boost of 150 XP!
        addXPAndGold(context, 150, 0)
        save(context)
        return true
    }

    fun conquestSector(context: Context, sectorId: Int) {
        val index = sectors.indexOfFirst { it.id == sectorId }
        if (index == -1) return
        val current = sectors[index]
        
        // Replace with copy instance to trigger immediate Jetpack Compose recomposition!
        sectors[index] = current.copy(faction = faction)
        
        addXPAndGold(context, current.rewardXP, current.rewardGold)
        save(context)
    }

    fun addXPAndGold(context: Context, xpGained: Int, goldGained: Int) {
        playerXP += xpGained
        playerGold += goldGained

        // XP Level bounds calculation (e.g. 100 * level required to level up)
        var xpRequired = playerLevel * 120
        while (playerXP >= xpRequired) {
            playerXP -= xpRequired
            playerLevel += 1
            statPointsAvailable += 3 // 3 fresh stat points earned on level up!
            xpRequired = playerLevel * 120
        }
        save(context)
    }

    private fun loadDefaultTeams() {
        playerTeams.clear()
        playerTeams.add(listOf())
        playerTeams.add(listOf())
        playerTeams.add(listOf())
        playerTeams.add(listOf())
        playerTeams.add(listOf())
    }

    private fun loadDefaultChats() {
        friendChats.clear()
        friendChats["GHOST_99"] = listOf(
            ChatMessage("GHOST_99", "Yo, claims in the sector core are rising.", false, "13:01"),
            ChatMessage("ME", "Ready to challenge sector bosses.", true, "13:03"),
            ChatMessage("GHOST_99", "Nice. Go accept my challenge to battle in the Arena!", false, "13:04")
        )
        friendChats["CYBER_WOLF"] = listOf(
            ChatMessage("CYBER_WOLF", "Outpost Sigma is vulnerable. We need back up!", false, "10:15")
        )
        friendChats["BYTE_BLADE"] = listOf(
            ChatMessage("BYTE_BLADE", "Glitched specs are fully active. Ready to clash!", false, "Yesterday")
        )
    }

    fun saveTeam(context: Context, teamIndex: Int, beastIds: List<Int>) {
        if (teamIndex in 0 until 5) {
            playerTeams[teamIndex] = beastIds
            save(context)
        }
    }

    fun addChatMessage(context: Context, friend: String, message: ChatMessage) {
        val currentFeed = friendChats[friend] ?: listOf()
        friendChats[friend] = currentFeed + message
        save(context)
    }
}
