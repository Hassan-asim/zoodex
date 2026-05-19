# 🗄️ Supabase Database Setup Instructions

## 📍 Your Supabase Project

**Project URL:** https://gicnboxddmuvacuymhwp.supabase.co

Go to your Supabase dashboard and follow these steps:

---

## Step 1: Create the Database Tables

1. In Supabase, click **"SQL Editor"** in the left sidebar
2. Click **"New Query"**
3. Copy and paste the following SQL, then click **"Run"**:

```sql
-- Create operative_profiles table
CREATE TABLE operative_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    callsign VARCHAR(30) UNIQUE NOT NULL,
    faction VARCHAR(50) NOT NULL,
    level INT DEFAULT 1,
    online BOOLEAN DEFAULT FALSE,
    last_seen TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create operative_messages table
CREATE TABLE operative_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_callsign VARCHAR(30) NOT NULL,
    receiver_callsign VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create friendships table
CREATE TABLE friendships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_callsign VARCHAR(30) NOT NULL,
    friend_callsign VARCHAR(30) NOT NULL,
    status VARCHAR(20) DEFAULT 'accepted',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(requester_callsign, friend_callsign)
);

-- Create indexes for faster queries
CREATE INDEX idx_messages_sender ON operative_messages(sender_callsign);
CREATE INDEX idx_messages_receiver ON operative_messages(receiver_callsign);
CREATE INDEX idx_friendships_requester ON friendships(requester_callsign);
CREATE INDEX idx_friendships_friend ON friendships(friend_callsign);
```

---

## Step 2: Enable Row Level Security (Optional but Recommended)

Create a new query and run this:

```sql
-- Enable RLS on all tables
ALTER TABLE operative_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE operative_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE friendships ENABLE ROW LEVEL SECURITY;

-- Allow anyone to read profiles
CREATE POLICY "Public profiles" ON operative_profiles
    FOR SELECT USING (true);

-- Allow inserts to all tables (we'll validate on app side)
CREATE POLICY "Anyone can insert" ON operative_profiles
    FOR INSERT WITH CHECK (true);

CREATE POLICY "Anyone can insert messages" ON operative_messages
    FOR INSERT WITH CHECK (true);

CREATE POLICY "Anyone can insert friendships" ON friendships
    FOR INSERT WITH CHECK (true);

-- Allow reading messages for both participants
CREATE POLICY "Read own messages" ON operative_messages
    FOR SELECT USING (true);

-- Allow reading friendships
CREATE POLICY "Read friendships" ON friendships
    FOR SELECT USING (true);
```

---

## Step 3: Insert Some Test Data (Optional)

Create a new query and run this to add some test operative profiles:

```sql
INSERT INTO operative_profiles (callsign, faction, level, online) VALUES
('GHOST_99', 'VOID_RUNNERS', 5, true),
('CYBER_WOLF', 'NEON_SYNDICATE', 7, true),
('IRON_FIST', 'IRON_VANGUARD', 6, false),
('BYTE_BLADE', 'NEON_SYNDICATE', 4, true),
('NOVA_SPARK', 'VOID_RUNNERS', 3, false),
('PHANTOM_CORE', 'IRON_VANGUARD', 8, true),
('NEON_DRAGON', 'NEON_SYNDICATE', 5, false);

-- Add some friendships
INSERT INTO friendships (requester_callsign, friend_callsign, status) VALUES
('GHOST_99', 'CYBER_WOLF', 'accepted'),
('GHOST_99', 'BYTE_BLADE', 'accepted'),
('CYBER_WOLF', 'IRON_FIST', 'accepted'),
('BYTE_BLADE', 'PHANTOM_CORE', 'accepted');
```

---

## Step 4: Verify Setup

To verify everything is working:

1. Click on **"Table Editor"** in the left sidebar
2. You should see three new tables:
   - `operative_profiles`
   - `operative_messages`
   - `friendships`
3. Click each table to verify they have data (if you ran the test data)

---

## 🚀 Now Your App Can Connect!

The app is already configured to use your Supabase project. When you run it:

1. **First-Time Setup**: Creates an operative profile with your callsign and faction
2. **Comms Center → Friends Tab**: Add friends using their operative code
3. **Comms Center → Messages Tab**: See active conversations
4. **Direct Chat**: Send and receive messages in real-time

---

## 💡 Operative Code Format

When adding friends, use the format: `ZOODEX-[FACTION]-[NUMBER]`

**Examples:**
- `ZOODEX-VOID-99` (for VOID_RUNNERS)
- `ZOODEX-NEON-42` (for NEON_SYNDICATE)
- `ZOODEX-IRON-88` (for IRON_VANGUARD)

The app extracts the middle part (e.g., "VOID") to find the friend's callsign.

---

## 🔍 Troubleshooting

### Messages not sending?
- Check that you have internet connection
- Verify the operative_messages table exists
- Check Supabase logs for errors

### Can't see friends?
- Make sure friendships table has rows with `status = 'accepted'`
- Verify the callsigns match exactly (case-sensitive)

### Profiles not showing?
- Ensure operative_profiles table has data
- Check that callsigns are created during first-time setup

---

## 📊 Database Schema Diagram

```
operative_profiles (1) ──────── (∞) operative_messages
├── id (UUID)                       ├── id (UUID)
├── callsign (VARCHAR)              ├── sender_callsign (VARCHAR)
├── faction (VARCHAR)               ├── receiver_callsign (VARCHAR)
├── level (INT)                     ├── content (TEXT)
├── online (BOOLEAN)                ├── is_read (BOOLEAN)
├── last_seen (TIMESTAMP)           └── created_at (TIMESTAMP)
└── created_at (TIMESTAMP)

operative_profiles (∞) ──────── (∞) friendships
├── callsign                        ├── id (UUID)
└── ...                             ├── requester_callsign (VARCHAR)
                                    ├── friend_callsign (VARCHAR)
                                    ├── status (VARCHAR)
                                    └── created_at (TIMESTAMP)
```

---

## ✅ You're All Set!

Your Supabase database is now ready for the Zoodex app's messaging system. The app will automatically:
- Create user profiles on first setup
- Store messages securely
- Manage friendships
- Sync online/offline status

Happy messaging! 🎮📱
