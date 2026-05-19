# 🎖️ OPERATIVE CODES - Friend Connection Guide

## How to Use Operative Codes

Your operative code is your **unique identifier** in Zoodex. Share it with friends so they can add you!

### Format:
```
ZOODEX-[FACTION]-[NUMBER]
```

### Example Codes:

#### 🌌 VOID_RUNNERS
- ZOODEX-VOID-01
- ZOODEX-VOID-02
- ZOODEX-VOID-99
- ZOODEX-VOID-42
- ZOODEX-VOID-88

#### 💜 NEON_SYNDICATE
- ZOODEX-NEON-01
- ZOODEX-NEON-42
- ZOODEX-NEON-77
- ZOODEX-NEON-99
- ZOODEX-NEON-55

#### 🔴 IRON_VANGUARD
- ZOODEX-IRON-01
- ZOODEX-IRON-88
- ZOODEX-IRON-45
- ZOODEX-IRON-72
- ZOODEX-IRON-33

---

## 📱 How to Add a Friend

### Steps:

1. **Open COMMS CENTER**
   - From home screen, tap "COMMS CENTER"

2. **Go to FRIENDS Tab**
   - Tap the "FRIENDS" button

3. **Find "ADD NEW OPERATIVE" Section**
   - You'll see your operative code at the top
   - Below that is a text field for adding friends

4. **Enter Friend's Operative Code**
   - Example: `ZOODEX-NEON-42`
   - Code is **NOT case-sensitive**
   - Can include spaces or dashes

5. **Tap "ADD" Button**
   - System extracts callsign from code
   - Sends friend request to Supabase
   - Friend appears in list after acceptance

6. **Start Chatting!**
   - Go to MESSAGES tab
   - Tap friend's name
   - Type and send messages

---

## 🎮 Test It Out!

### Recommended Test Setup:

**Device 1:**
- Operative Code: `ZOODEX-NEON-01`
- Callsign: CYBER_WOLF
- Faction: NEON_SYNDICATE

**Device 2:**
- Operative Code: `ZOODEX-VOID-99`
- Callsign: GHOST_99
- Faction: VOID_RUNNERS

### Test Conversation:
1. Device 1 opens Comms Center → Friends Tab
2. Device 1 enters: `ZOODEX-VOID-99`
3. Device 1 taps ADD
4. Device 2 should see Device 1 in friends list
5. Device 1 goes to Messages Tab
6. Device 1 taps GHOST_99 to chat
7. Device 1 types: "Hello from CYBER_WOLF!"
8. Device 2 receives message in real-time!

---

## 🔍 Understanding the Code

### Format Breakdown:
```
ZOODEX - [FACTION] - [NUMBER]
  ↓         ↓          ↓
  Brand    Faction   Unique ID
```

**FACTION Options:**
- `VOID` = VOID_RUNNERS (🌌 Void/Purple)
- `NEON` = NEON_SYNDICATE (💜 Neon/Cyan)
- `IRON` = IRON_VANGUARD (🔴 Iron/Red)

**NUMBER:**
- Randomly generated (01-99)
- Changes per user
- Makes each code unique

---

## 💡 Tips for Sharing

### Best Ways to Share Your Code:
- 📱 Text it to friends
- 💬 Share on Discord/WhatsApp
- 🎮 Post in gaming communities
- 📋 Screenshot and share on social media
- 💌 Email it to teammates

### Example Share Message:
```
Hey! Join me in Zoodex! 🎮
My operative code: ZOODEX-NEON-42
Add me and let's chat! 💬
```

---

## ❓ Troubleshooting

### Code Rejected - "Friend Not Found"
- ✅ Check spelling (VOID, NEON, or IRON)
- ✅ Verify dashes and spacing
- ✅ Make sure friend has completed first-time setup
- ✅ Wait a few seconds and try again

### Can't See Friend in Messages
- ✅ Refresh the app (close and reopen)
- ✅ Check internet connection
- ✅ Verify friend accepted the request
- ✅ Check Supabase dashboard for data

### Can't Send Messages
- ✅ Ensure you have internet
- ✅ Check Supabase is accessible
- ✅ Verify both users are in friendships table
- ✅ Restart the app

---

## 🌍 Global Operative Directory

### Pre-Generated Test Operatives:

You can test messaging with these pre-created accounts:

| Callsign | Code | Faction | Level | Status |
|----------|------|---------|-------|--------|
| GHOST_99 | ZOODEX-VOID-99 | VOID_RUNNERS | 5 | 🟢 Online |
| CYBER_WOLF | ZOODEX-NEON-42 | NEON_SYNDICATE | 7 | 🟢 Online |
| IRON_FIST | ZOODEX-IRON-01 | IRON_VANGUARD | 6 | ⚫ Offline |
| BYTE_BLADE | ZOODEX-NEON-77 | NEON_SYNDICATE | 4 | 🟢 Online |
| PHANTOM_CORE | ZOODEX-IRON-88 | IRON_VANGUARD | 8 | 🟢 Online |

---

## 📊 Operative Statistics

### Status Indicators:
- 🟢 **GREEN** = Online now
- ⚫ **GRAY** = Offline / Last seen: [time]
- 💛 **YELLOW** = Away (coming soon)

### Faction Colors:
- 🌌 **VOID_RUNNERS** = Purple/Violet vibes
- 💜 **NEON_SYNDICATE** = Cyan/Neon blue
- 🔴 **IRON_VANGUARD** = Red/Orange tones

---

## 🎯 Game Tips

### Before Adding Friends:
1. Decide your Faction wisely (affects your operative code)
2. Choose your Callsign carefully (it's your identity!)
3. The code displays your faction and level

### After Adding Friends:
1. Start conversations to build reputation
2. Coordinate strategies for territory wars (coming soon)
3. Form teams for arena battles (coming soon)
4. Share captured animals and tips

---

## 📚 More Features Coming Soon

With the operative code system, future features will include:
- ✅ Direct messaging (NOW AVAILABLE!)
- 📅 Friend requests & acceptance
- 🎁 Send battle challenges
- 👥 Create squads/teams
- 🏆 Leaderboards by faction
- 🎮 Matchmaking for arena battles

---

## 🚀 You're Ready!

Now that you understand operative codes, you can:
1. ✅ Share your code with friends
2. ✅ Add friends using their codes
3. ✅ Start real-time messaging
4. ✅ Build your operative network

**Your App is Live!** 🎮📱

---

**Questions?** Check the main documentation or review the code comments in `CommsScreen.kt` and `SupabaseService.kt`.
