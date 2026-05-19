package com.Sufi.zoodex.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import org.json.JSONArray
import org.json.JSONObject
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
    var inActiveTeam: Boolean = false
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
                inActiveTeam = obj.optBoolean("inActiveTeam", false)
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

    val beastTemplates = listOf(
        BeastTemplate(1, "VOLT HOUND", "ELECTR", "A tactical cybernetic canine covered in static fur discharging high green voltage.", 120, 14, 8, 18),
        BeastTemplate(2, "STORM EAGLE", "VOID", "A majestic and legendary predator of the high peaks, channeling gravity-defying void pressure.", 100, 12, 6, 22),
        BeastTemplate(3, "MAGMA GORILLA", "FIRE", "A colossal primordial volcanic ape composed of pure obsidian plates and superheated lava.", 160, 20, 15, 8),
        BeastTemplate(4, "NEON TIGER", "CYBER", "A glowing digital apex feline stalking the mainframe and composed of raw high-speed data energy flows.", 110, 16, 10, 16),
        BeastTemplate(5, "GLITCH SPECTER", "CYBER", "A phasing network anomaly flickering in compiled compiler memory buffers.", 95, 12, 8, 14),
        BeastTemplate(6, "SOLAR HAWK", "FIRE", "A high-altitude thermal raptor whose wings burn with intense nuclear solar fusion.", 105, 15, 7, 20),
        BeastTemplate(7, "VOID FLYER", "VOID", "A deep-space bat specimen that uses gravitational sonar frequencies to locate targets.", 100, 11, 7, 21),
        BeastTemplate(8, "EMERALD COBRA", "ELECTR", "A bio-engineered serpent charging green electrical voltage that releases lightning shocks.", 95, 13, 8, 15),
        BeastTemplate(9, "STREET CAT", "CYBER", "A highly common and agile urban feline integrated with neural data tracking nodes.", 90, 11, 8, 15),
        BeastTemplate(10, "STRAY DOG", "ELECTR", "A loyal local canine equipped with static-charge bark sensors and neural link nodes.", 100, 12, 10, 13),
        BeastTemplate(11, "HOUSE SPARROW", "VOID", "A tiny, extremely common local bird capable of briefly phasing through solid structures.", 80, 8, 5, 22),
        BeastTemplate(12, "COMMON CROW", "VOID", "An exceptionally intelligent scavenger bird capable of deciphering security encryption codes.", 95, 10, 7, 18),
        BeastTemplate(13, "PARK SQUIRREL", "CYBER", "A speedy rodent that stores micro-fusion power cores instead of acorns inside urban parks.", 85, 9, 6, 20),
        BeastTemplate(14, "MARKHOR", "ELECTR", "The grand national mountain goat of Pakistan, charging with massive corkscrew electrified horns.", 140, 18, 14, 14),
        BeastTemplate(15, "SNOW LEOPARD", "FIRE", "The elusive ghost of northern peaks, breathing freezing volcanic steam waves.", 130, 17, 12, 16),
        BeastTemplate(16, "HIMALAYAN IBEX", "VOID", "A magnificent mountain ibex that walks along sheer vertical cliffs using gravity-bending hooves.", 135, 15, 13, 14),
        BeastTemplate(17, "MONAL PHEASANT", "FIRE", "A radiant avian displaying blazing solar plumes, living in sub-alpine shrub forests.", 90, 10, 8, 16),
        BeastTemplate(18, "DESERT SAND-CAT", "FIRE", "A small desert feline that glides invisibly across glowing volcanic sand dunes.", 95, 12, 9, 17),
        BeastTemplate(19, "INDUS DOLPHIN", "CYBER", "A rare freshwater swimmer using high-precision spatial echolocation systems to map coordinates.", 115, 13, 11, 16),
        BeastTemplate(20, "SHELTER CORAL", "VOID", "An bio-luminescent aquatic structure feeding directly on background cosmic rays.", 150, 10, 18, 5),
        BeastTemplate(21, "PALLAS CAT", "CYBER", "An extremely fluffy high-altitude wild feline with high-resolution telephoto ocular lenses.", 110, 13, 12, 12),
        BeastTemplate(22, "GOLDEN EAGLE", "ELECTR", "A grand high-altitude raptor executing supersonic dives charged with high static voltage.", 115, 16, 9, 19)
    )

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
                // Instantly heal fainted team members to full HP!
                capturedBeasts.forEach { it.currentHp = it.maxHp }
            }
        }
        save(context)
        return true
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
