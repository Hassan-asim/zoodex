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
import android.util.Log

@Composable
fun CommsScreen(onBack: () -> Unit, onLaunchBattle: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("MESSAGES", "FRIENDS")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State management
    val friends = remember { mutableStateListOf<OperativeProfile>() }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFriend by remember { mutableStateOf<OperativeProfile?>(null) }

    // Load friends on launch
    LaunchedEffect(Unit) {
        GameState.init(context)
        scope.launch {
            isLoading = true
            try {
                val loadedFriends = SupabaseService.fetchFriendsForCallsign(GameState.callsign)
                friends.clear()
                friends.addAll(loadedFriends)
                Log.d("CommsScreen", "Loaded ${friends.size} friends")
            } catch (e: Exception) {
                Log.e("CommsScreen", "Error loading friends: ${e.message}")
            }
            isLoading = false
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
                    1 -> FriendsTab(friends, { selectedFriend = it })
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
    onSelectFriend: (OperativeProfile) -> Unit
) {
    var addFriendCode by remember { mutableStateOf("") }
    var isAdding by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val myCode = "ZOODEX-" + GameState.faction.take(3).uppercase() + "-${GameState.playerLevel}9B"

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
                        text = "📋 Tap to copy & share with other operatives",
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
                        placeholder = { Text("e.g. ZOODEX-NEO-229B", color = TextTertiary, fontSize = 11.sp) },
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
                                        val callsignFromCode = addFriendCode.split("-").getOrNull(1) ?: ""
                                        val success = SupabaseService.addFriend(GameState.callsign, callsignFromCode)
                                        statusMessage = if (success) "✓ Friend request sent!" else "✗ Failed to add friend"
                                    } catch (e: Exception) {
                                        statusMessage = "✗ Error: ${e.message}"
                                    }
                                    isAdding = false
                                    addFriendCode = ""
                                    delay(2000)
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
    
    var messages by remember { mutableStateOf<List<OperativeMessage>>(emptyList()) }
    var currentText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    // Load messages on launch
    LaunchedEffect(friend.id) {
        scope.launch {
            isLoading = true
            try {
                val loadedMessages = SupabaseService.fetchMessages(GameState.callsign, friend.callsign)
                messages = loadedMessages
                Log.d("DirectMessages", "Loaded ${messages.size} messages")
                if (messages.isNotEmpty()) {
                    scrollState.animateScrollToItem(messages.size - 1)
                }
            } catch (e: Exception) {
                Log.e("DirectMessages", "Error loading messages: ${e.message}")
            }
            isLoading = false
        }
    }

    // Auto-scroll on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
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
                                        GameState.callsign,
                                        friend.callsign,
                                        currentText
                                    )
                                    if (success) {
                                        val newMessage = OperativeMessage(
                                            senderCallsign = GameState.callsign,
                                            receiverCallsign = friend.callsign,
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


