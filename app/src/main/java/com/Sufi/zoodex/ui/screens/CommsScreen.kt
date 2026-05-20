package com.Sufi.zoodex.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Sufi.zoodex.data.*
import com.Sufi.zoodex.ui.theme.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.*
import kotlinx.coroutines.isActive
import android.util.Log
import android.content.Context

@Composable
fun CommsScreen(onBack: () -> Unit, onLaunchBattle: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("MESSAGES", "FRIENDS")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State management
    val friends = remember { mutableStateListOf<OperativeProfile>() }
    val pendingRequests = remember { mutableStateListOf<OperativeProfile>() }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFriend by remember { mutableStateOf<OperativeProfile?>(null) }

    fun refreshFriendsList() {
        scope.launch {
            try {
                val loadedFriends = SupabaseService.fetchFriendsForCallsign(GameState.callsign)
                friends.clear()
                friends.addAll(loadedFriends)
                
                val loadedPending = SupabaseService.fetchPendingRequests(GameState.callsign)
                pendingRequests.clear()
                pendingRequests.addAll(loadedPending)
                Log.d("CommsScreen", "Manually refreshed friends list")
            } catch (e: Exception) {
                Log.e("CommsScreen", "Error manually refreshing friends: ${e.message}")
            }
        }
    }

    // Load and poll friends and requests on launch and periodically
    LaunchedEffect(Unit) {
        GameState.init(context)
        
        // Ensure our own profile is registered in Supabase
        withContext(Dispatchers.IO) {
            try {
                SupabaseService.initializeUserProfile(GameState.callsign, GameState.faction)
            } catch (e: Exception) {
                Log.e("CommsScreen", "Failed to register profile: ${e.message}")
            }
        }

        isLoading = true
        var isFirst = true
        while (true) {
            try {
                val loadedFriends = SupabaseService.fetchFriendsForCallsign(GameState.callsign)
                val loadedPending = SupabaseService.fetchPendingRequests(GameState.callsign)
                
                // Compare and update lists to avoid unnecessary UI redraws
                if (isFirst || loadedFriends.size != friends.size || loadedFriends.map { it.callsign } != friends.map { it.callsign }) {
                    friends.clear()
                    friends.addAll(loadedFriends)
                }
                if (isFirst || loadedPending.size != pendingRequests.size || loadedPending.map { it.callsign } != pendingRequests.map { it.callsign }) {
                    pendingRequests.clear()
                    pendingRequests.addAll(loadedPending)
                }
            } catch (e: Exception) {
                Log.e("CommsScreen", "Error polling friends/requests: ${e.message}")
            }
            isLoading = false
            isFirst = false
            delay(3500)
        }
    }

    if (selectedFriend != null) {
        DirectMessagesView(
            friend = selectedFriend!!,
            onBack = { selectedFriend = null }
        )
    } else {
        Column(
            Modifier
                .fillMaxSize()
                .background(ObsidianBlack)
        ) {
            // Header
            ScreenHeader(title = "COMMS CENTER", onBack = onBack)

            // Tab selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .background(GlassSurface, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (selected) Color.White.copy(0.08f) else Color.Transparent)
                            .border(
                                1.dp, 
                                if (selected) Color.White.copy(0.06f) else Color.Transparent, 
                                RoundedCornerShape(9.dp)
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) TextPrimary else TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Content
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> MessagesTab(friends, { selectedFriend = it })
                    1 -> FriendsTab(
                        friends = friends,
                        pendingRequests = pendingRequests,
                        onRefresh = { refreshFriendsList() },
                        onSelectFriend = { selectedFriend = it }
                    )
                }
            }
        }
    }
}

@Composable
fun MessagesTab(
    friends: List<OperativeProfile>,
    onSelectFriend: (OperativeProfile) -> Unit
) {
    if (friends.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "📡 NO ACTIVE CHANNELS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Add friends from the FRIENDS tab to start messaging",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(friends) { friend ->
                FriendChatPreview(friend = friend, onClick = { onSelectFriend(friend) })
            }
        }
    }
}

@Composable
fun FriendChatPreview(friend: OperativeProfile, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = GlassSurface,
        border = BorderStroke(1.dp, Color.White.copy(0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Online Status Indicator
            Box(
                Modifier
                    .size(12.dp)
                    .background(
                        if (friend.online) AppleGreen else TextTertiary, 
                        CircleShape
                    )
            )
            Spacer(Modifier.width(12.dp))

            // Friend Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.callsign, 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = friend.faction.replace("_", " "), 
                    style = MaterialTheme.typography.labelMedium, 
                    fontSize = 10.sp, 
                    color = CyberBlueEnd
                )
            }

            Spacer(Modifier.width(8.dp))

            // Status badge
            Text(
                text = if (friend.online) "🟢" else "⚫", 
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun FriendsTab(
    friends: List<OperativeProfile>,
    pendingRequests: List<OperativeProfile>,
    onRefresh: () -> Unit,
    onSelectFriend: (OperativeProfile) -> Unit
) {
    var addFriendCode by remember { mutableStateOf("") }
    var isAdding by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val myCode = "ZOODEX-" + GameState.callsign.uppercase() + "-" + GameState.faction.take(3).uppercase()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // My Code Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberGradient, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "YOUR OPERATIVE CODE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ObsidianBlack,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = myCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ObsidianBlack,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "📋 Share with other operatives to connect",
                        style = MaterialTheme.typography.labelSmall,
                        color = ObsidianBlack.copy(0.7f),
                        fontSize = 8.sp
                    )
                }
            }
        }

        // Add Friend Section
        item {
            Column {
                Text(
                    text = "ADD NEW OPERATIVE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = addFriendCode,
                        onValueChange = { addFriendCode = it.uppercase() },
                        placeholder = { Text("e.g. ZOODEX-CALLSIGN-FAC", color = TextTertiary, fontSize = 11.sp) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 12.sp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleBlue,
                            unfocusedBorderColor = Color.White.copy(0.08f),
                            focusedContainerColor = GlassSurface,
                            unfocusedContainerColor = GlassSurface,
                            cursorColor = AppleBlue
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                if (addFriendCode.isNotBlank()) {
                                    isAdding = true
                                    try {
                                        val parts = addFriendCode.split("-")
                                        val callsignFromCode = parts.getOrNull(1) ?: ""
                                        
                                        if (callsignFromCode.isBlank()) {
                                            statusMessage = "✗ Invalid code format"
                                        } else if (callsignFromCode.uppercase() == GameState.callsign.uppercase()) {
                                            statusMessage = "✗ Cannot add yourself"
                                        } else {
                                            val success = SupabaseService.addFriend(GameState.callsign, callsignFromCode)
                                            statusMessage = if (success) "✓ Friend request sent!" else "✗ Failed to add friend"
                                            if (success) {
                                                onRefresh()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        statusMessage = "✗ Error: ${e.message}"
                                    }
                                    isAdding = false
                                    addFriendCode = ""
                                    delay(2500)
                                    statusMessage = ""
                                }
                            }
                        },
                        enabled = !isAdding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppleBlue, 
                            contentColor = ObsidianBlack,
                            disabledContainerColor = TextTertiary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(if (isAdding) "..." else "ADD", fontWeight = FontWeight.Bold)
                    }
                }

                if (statusMessage.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(statusMessage, fontSize = 10.sp, color = if (statusMessage.contains("✓")) AppleGreen else AppleRed, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Pending Incoming Requests Section
        if (pendingRequests.isNotEmpty()) {
            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "INCOMING PENDING REQUESTS [${pendingRequests.size}]",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppleBlue,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                )
            }

            items(pendingRequests) { requester ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GlassSurface,
                    border = BorderStroke(1.dp, AppleBlue.copy(0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = requester.callsign,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = requester.faction.replace("_", " "),
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val success = SupabaseService.acceptFriendRequest(requester.callsign, GameState.callsign)
                                        if (success) {
                                            onRefresh()
                                            onSelectFriend(requester)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppleGreen, contentColor = ObsidianBlack),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("ACCEPT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Button(
                                onClick = {
                                    scope.launch {
                                        val success = SupabaseService.deleteFriendship(requester.callsign, GameState.callsign)
                                        if (success) {
                                            onRefresh()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppleRed, contentColor = TextPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("DECLINE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Friends List
        if (friends.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "SECURED ALLIES [${friends.size}]",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                )
            }

            items(friends) { friend ->
                FriendChatPreview(friend = friend, onClick = { onSelectFriend(friend) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessagesView(friend: OperativeProfile, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { GameState.init(context) }
    
    var messages by remember { mutableStateOf<List<OperativeMessage>>(emptyList()) }
    var currentText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberLazyListState()
    val myCallsign = remember {
        GameState.callsign.ifBlank {
            val prefs = context.getSharedPreferences("zoodex_save", Context.MODE_PRIVATE)
            prefs.getString("callsign", "") ?: ""
        }.uppercase()
    }

    // Load and poll messages on launch and periodically
    LaunchedEffect(friend.callsign) {
        isLoading = true
        var isFirst = true
        while (isActive) {
            try {
                if (myCallsign.isBlank()) {
                    loadError = "Your profile callsign is missing. Re-open setup/profile first."
                    isLoading = false
                    delay(2500)
                    continue
                }
                val loadedMessages = withTimeoutOrNull(10000) {
                    SupabaseService.fetchMessages(myCallsign, friend.callsign.uppercase())
                } ?: emptyList()
                // Update only if counts or last messages differ
                if (loadedMessages.size != messages.size || loadedMessages.lastOrNull()?.id != messages.lastOrNull()?.id) {
                    messages = loadedMessages
                    if (isFirst && messages.isNotEmpty()) {
                        scope.launch {
                            delay(100) // Small delay to let UI render before scroll
                            try {
                                scrollState.animateScrollToItem(messages.size - 1)
                            } catch (se: Exception) {
                                Log.e("DirectMessages", "Error scrolling: ${se.message}")
                            }
                        }
                    }
                }
                loadError = null
            } catch (e: Exception) {
                Log.e("DirectMessages", "Error polling messages: ${e.message}")
                loadError = "Message sync error. Retrying..."
            }
            isLoading = false
            isFirst = false
            delay(2500)
        }
    }

    // Auto-scroll on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                try {
                    scrollState.animateScrollToItem(messages.size - 1)
                } catch (se: Exception) {
                    Log.e("DirectMessages", "Error auto-scrolling: ${se.message}")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // Chat Header
        Box(
            Modifier
                .fillMaxWidth()
                .background(GlassSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = onBack,
                    colors = ButtonDefaults.textButtonColors(contentColor = AppleBlue)
                ) {
                    Text("← BACK", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        friend.callsign, 
                        style = MaterialTheme.typography.labelLarge, 
                        color = TextPrimary, 
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (friend.online) "🟢 ONLINE" else "⚫ OFFLINE", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontSize = 9.sp, 
                            color = if (friend.online) AppleGreen else TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
            }
        }

        Divider(color = Color.White.copy(0.06f), thickness = 0.5.dp)

        // Messages List
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppleBlue)
            }
        } else if (loadError != null && messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    loadError ?: "Unable to load messages",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppleRed,
                    textAlign = TextAlign.Center
                )
            }
        } else if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No messages yet. Start the conversation!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isMyMessage = msg.senderCallsign == GameState.callsign
                    ChatBubble(message = msg, isMyMessage = isMyMessage)
                }
            }
        }

        Divider(color = Color.White.copy(0.06f), thickness = 0.5.dp)

        // Message Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ObsidianBlack)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = currentText,
                onValueChange = { currentText = it },
                placeholder = { Text("Type a message...", color = TextTertiary, fontSize = 12.sp) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberBlueStart,
                    unfocusedBorderColor = Color.White.copy(0.08f),
                    focusedContainerColor = GlassSurface,
                    unfocusedContainerColor = GlassSurface,
                    cursorColor = CyberBlueStart
                ),
                modifier = Modifier.weight(1f),
                enabled = !isSending
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .let { 
                        if (isSending) it.background(TextTertiary) else it.background(CyberGradient)
                    }
                    .clickable(enabled = !isSending && currentText.isNotBlank()) {
                        scope.launch {
                            if (currentText.isNotBlank() && !isSending) {
                                isSending = true
                                try {
                                    val success = SupabaseService.sendMessage(
                                        myCallsign,
                                        friend.callsign.uppercase(),
                                        currentText
                                    )
                                    if (success) {
                                        val newMessage = OperativeMessage(
                                            senderCallsign = myCallsign,
                                            receiverCallsign = friend.callsign.uppercase(),
                                            content = currentText,
                                            isRead = false
                                        )
                                        messages = messages + newMessage
                                        currentText = ""
                                        Log.d("DirectMessages", "Message sent successfully")
                                    }
                                } catch (e: Exception) {
                                    Log.e("DirectMessages", "Error sending message: ${e.message}")
                                }
                                isSending = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("→", fontSize = 18.sp, color = ObsidianBlack, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ChatBubble(message: OperativeMessage, isMyMessage: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    ) {
        if (!isMyMessage) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.05f))
                    .border(1.dp, CyberBlueEnd.copy(0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderCallsign.take(1),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyberBlueEnd,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 250.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .let { 
                        if (isMyMessage) it.background(CyberGradient) else it.background(GlassSurface)
                    }
                    .border(
                        1.dp,
                        if (isMyMessage) CyberBlueStart.copy(0.3f) else Color.White.copy(0.06f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    color = if (isMyMessage) ObsidianBlack else TextPrimary
                )
            }
        }

        if (isMyMessage) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.05f))
                    .border(1.dp, CyberBlueStart.copy(0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = GameState.callsign.take(1),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyberBlueStart,
                    fontSize = 12.sp
                )
            }
        }
    }
}


