package com.Sufi.zoodex.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.Sufi.zoodex.data.ClaimedTerritory
import com.Sufi.zoodex.data.GameState
import com.Sufi.zoodex.data.SupabaseService
import com.Sufi.zoodex.ui.theme.*
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.json.JSONArray
import org.json.JSONObject

private const val TONER_STYLE = "https://tiles.stadiamaps.com/styles/stamen_toner.json"
/** Detailed streets / POI (OpenFreeMap) — primary map for gameplay. */
private const val MAP_STYLE_DETAILED = "https://tiles.openfreemap.org/styles/liberty"
private const val FALLBACK_STYLE = "https://demotiles.maplibre.org/style.json"
private const val TAG_MAP = "TerritoryMap"

// Haversine distance in metres between two lat/lng points
private fun haversineMetres(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2).let { it * it } +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2).let { it * it }
    return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

// Check if a point is inside a polygon using ray-casting
private fun pointInPolygon(lat: Double, lon: Double, polygon: List<Pair<Double, Double>>): Boolean {
    if (polygon.size < 3) return false
    var inside = false
    var j = polygon.size - 1
    for (i in polygon.indices) {
        val xi = polygon[i].second; val yi = polygon[i].first
        val xj = polygon[j].second; val yj = polygon[j].first
        if ((yi > lat) != (yj > lat) && lon < (xj - xi) * (lat - yi) / (yj - yi) + xi) {
            inside = !inside
        }
        j = i
    }
    return inside
}

// Build a GeoJSON polygon Feature from lat/lng list
private fun buildPolygonFeature(points: List<Pair<Double, Double>>): String {
    val coords = JSONArray()
    val ring = JSONArray()
    points.forEach { (lat, lng) -> ring.put(JSONArray().put(lng).put(lat)) }
    // close the ring
    if (points.isNotEmpty()) ring.put(JSONArray().put(points.first().second).put(points.first().first))
    coords.put(ring)
    val geometry = JSONObject().apply {
        put("type", "Polygon")
        put("coordinates", coords)
    }
    return JSONObject().apply {
        put("type", "Feature")
        put("geometry", geometry)
        put("properties", JSONObject())
    }.toString()
}

// Build a GeoJSON FeatureCollection for all claimed territories
private fun buildTerritoriesFeatureCollection(
    claims: List<ClaimedTerritory>,
    myCallsign: String
): String {
    val features = JSONArray()
    claims.forEach { claim ->
        // Each territory stored as lat,lng,radius → expand to a circle-ish polygon
        val segments = 32
        val ring = JSONArray()
        for (i in 0 until segments) {
            val angle = (i.toDouble() / segments) * 2 * Math.PI
            val dLat = (claim.radius / 111320.0) * Math.cos(angle)
            val dLng = (claim.radius / (111320.0 * Math.cos(Math.toRadians(claim.lat)))) * Math.sin(angle)
            ring.put(JSONArray().put(claim.lng + dLng).put(claim.lat + dLat))
        }
        // close ring
        val firstAngle = 0.0
        val dLat0 = (claim.radius / 111320.0) * Math.cos(firstAngle)
        val dLng0 = (claim.radius / (111320.0 * Math.cos(Math.toRadians(claim.lat)))) * Math.sin(firstAngle)
        ring.put(JSONArray().put(claim.lng + dLng0).put(claim.lat + dLat0))

        val coords = JSONArray().put(ring)
        val feature = JSONObject().apply {
            put("type", "Feature")
            put("geometry", JSONObject().apply {
                put("type", "Polygon")
                put("coordinates", coords)
            })
            put("properties", JSONObject().apply {
                put("callsign", claim.callsign)
                put("faction", claim.faction)
                put("isOwn", claim.callsign == myCallsign)
            })
        }
        features.put(feature)
    }
    return JSONObject().apply {
        put("type", "FeatureCollection")
        put("features", features)
    }.toString()
}

@Composable
fun MapScreen(onBack: () -> Unit, onBattle: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── Permission state ──────────────────────────────────────────────────────
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // ── State ─────────────────────────────────────────────────────────────────
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var mapReady by remember { mutableStateOf(false) }
    var isTracking by remember { mutableStateOf(false) }
    var currentLat by remember { mutableStateOf(0.0) }
    var currentLng by remember { mutableStateOf(0.0) }
    var distanceMetres by remember { mutableStateOf(0.0) }
    val walkedPath = remember { mutableStateListOf<Pair<Double, Double>>() }
    var claimedTerritories by remember { mutableStateOf<List<ClaimedTerritory>>(emptyList()) }
    var statusMsg by remember { mutableStateOf("GPS READY — TAP START TO CLAIM TERRITORY") }
    var showBattleDialog by remember { mutableStateOf<ClaimedTerritory?>(null) }
    var pathSaved by remember { mutableStateOf(false) }
    var startedInsideOwnTerritory by remember { mutableStateOf(false) }
    val sentBattleRequests = remember { mutableStateListOf<String>() }

    val myCallsign = remember {
        GameState.callsign.ifBlank {
            context.getSharedPreferences("zoodex_save", Context.MODE_PRIVATE)
                .getString("callsign", "OPERATIVE") ?: "OPERATIVE"
        }
    }
    val myFaction = remember {
        GameState.faction.ifBlank {
            context.getSharedPreferences("zoodex_save", Context.MODE_PRIVATE)
                .getString("faction", "UNCLAIMED") ?: "UNCLAIMED"
        }
    }

    // ── GPS location client ───────────────────────────────────────────────────
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val newLat = loc.latitude
                val newLng = loc.longitude

                // Distance since last point
                if (walkedPath.isNotEmpty() && isTracking) {
                    val last = walkedPath.last()
                    val d = haversineMetres(last.first, last.second, newLat, newLng)
                    if (d >= 3.0) { // only add if moved >3m
                        walkedPath.add(Pair(newLat, newLng))
                        distanceMetres += d
                    }
                }
                currentLat = newLat
                currentLng = newLng

                // Pan camera
                mapLibreMap?.animateCamera(
                    CameraUpdateFactory.newLatLng(LatLng(newLat, newLng)), 500
                )

                // Check if current position overlaps any claimed rival territory
                claimedTerritories.firstOrNull { claim ->
                    claim.callsign != myCallsign &&
                    haversineMetres(newLat, newLng, claim.lat, claim.lng) < claim.radius
                }?.let { rivalClaim ->
                    if (showBattleDialog == null) {
                        showBattleDialog = rivalClaim
                    }
                    if (!sentBattleRequests.contains(rivalClaim.id)) {
                        sentBattleRequests.add(rivalClaim.id)
                        scope.launch(Dispatchers.IO) {
                            SupabaseService.createBattleRequest(
                                challengerCallsign = myCallsign,
                                defenderCallsign = rivalClaim.callsign,
                                territoryId = rivalClaim.id,
                                lat = newLat,
                                lng = newLng
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Load territories from Supabase on launch ──────────────────────────────
    LaunchedEffect(Unit) {
        MapLibre.getInstance(context)
        if (!hasPermission) {
            permLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
        scope.launch(Dispatchers.IO) {
            try {
                val claims = SupabaseService.fetchTerritoryClaims()
                withContext(Dispatchers.Main) { claimedTerritories = claims }
            } catch (e: Exception) {
                Log.e(TAG_MAP, "Error fetching territories: ${e.message}")
            }
        }
    }

    // ── Draw territories on map whenever ready or territories change ──────────
    LaunchedEffect(mapReady, claimedTerritories) {
        if (!mapReady) return@LaunchedEffect
        val map = mapLibreMap ?: return@LaunchedEffect
        val style = map.style ?: return@LaunchedEffect
        try {
            val geoJson = buildTerritoriesFeatureCollection(claimedTerritories, myCallsign)
            val existingSource = style.getSource("territories-source") as? GeoJsonSource
            if (existingSource != null) {
                existingSource.setGeoJson(geoJson)
            } else {
                style.addSource(GeoJsonSource("territories-source", geoJson))
                style.addLayer(FillLayer("territories-fill", "territories-source").apply {
                    setProperties(
                        PropertyFactory.fillColor(
                            org.maplibre.android.style.expressions.Expression.match(
                                org.maplibre.android.style.expressions.Expression.get("isOwn"),
                                org.maplibre.android.style.expressions.Expression.literal(true),
                                org.maplibre.android.style.expressions.Expression.literal("#00FFCC"),
                                org.maplibre.android.style.expressions.Expression.literal("#FF3366")
                            )
                        ),
                        PropertyFactory.fillOpacity(0.25f)
                    )
                })
                style.addLayer(LineLayer("territories-line", "territories-source").apply {
                    setProperties(
                        PropertyFactory.lineColor(
                            org.maplibre.android.style.expressions.Expression.match(
                                org.maplibre.android.style.expressions.Expression.get("isOwn"),
                                org.maplibre.android.style.expressions.Expression.literal(true),
                                org.maplibre.android.style.expressions.Expression.literal("#00FFCC"),
                                org.maplibre.android.style.expressions.Expression.literal("#FF3366")
                            )
                        ),
                        PropertyFactory.lineWidth(2.5f)
                    )
                })
            }
        } catch (e: Exception) {
            Log.e(TAG_MAP, "Error drawing territories: ${e.message}")
        }
    }

    // ── Draw live walk path on map ────────────────────────────────────────────
    LaunchedEffect(walkedPath.size) {
        if (!mapReady || walkedPath.size < 2) return@LaunchedEffect
        val map = mapLibreMap ?: return@LaunchedEffect
        val style = map.style ?: return@LaunchedEffect
        try {
            val lineCoords = JSONArray().apply {
                walkedPath.forEach { (lat, lng) -> put(JSONArray().put(lng).put(lat)) }
            }
            val lineGeo = JSONObject().apply {
                put("type", "Feature")
                put("geometry", JSONObject().apply {
                    put("type", "LineString")
                    put("coordinates", lineCoords)
                })
                put("properties", JSONObject())
            }.toString()

            val existingLine = style.getSource("walk-path-source") as? GeoJsonSource
            if (existingLine != null) {
                existingLine.setGeoJson(lineGeo)
            } else {
                style.addSource(GeoJsonSource("walk-path-source", lineGeo))
                style.addLayer(LineLayer("walk-path-line", "walk-path-source").apply {
                    setProperties(
                        PropertyFactory.lineColor("#00E5FF"),
                        PropertyFactory.lineWidth(4f),
                        PropertyFactory.lineOpacity(0.9f)
                    )
                })
            }
        } catch (e: Exception) {
            Log.e(TAG_MAP, "Walk path draw error: ${e.message}")
        }
    }

    // ── Main UI ───────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize().background(ObsidianBlack)) {

        // MapView via AndroidView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapLibre.getInstance(ctx)
                MapView(ctx).apply {
                    onCreate(null)
                    getMapAsync { mapLibre ->
                        // MapLibre Android SDK version in this project does not expose
                        // addOnDidFailLoadingMapListener; use a guaranteed-render fallback style.
                        mapLibre.setStyle(MAP_STYLE_DETAILED) { _ ->
                            mapLibreMap = mapLibre
                            mapReady = true
                            mapLibre.uiSettings.isAttributionEnabled = false
                            mapLibre.uiSettings.isLogoEnabled = false
                            statusMsg = "MAP READY — TAP START TO CLAIM"
                        }
                        // Default zoom
                        mapLibre.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(LatLng(30.3753, 69.3451), 5.0)
                        )
                    }
                }
            },
            update = { mv -> mv.onStart() }
        )

        // Top header bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianBlack.copy(alpha = 0.85f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Back
                    Text(
                        "← BACK",
                        color = AppleBlue,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.clickable { onBack() }
                    )
                    Text(
                        "⚡ TERRITORY CONQUEST",
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 13.sp
                    )
                    // Stats
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${(distanceMetres).toInt()}m walked",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp
                        )
                        Text(
                            "${claimedTerritories.count { it.callsign == myCallsign }} zones",
                            color = AppleGreen,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // Status ticker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeonCyan.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusMsg,
                    color = NeonCyan,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        // Bottom control panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(ObsidianBlack.copy(alpha = 0.88f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // GPS info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (currentLat != 0.0) "📍 ${String.format("%.5f", currentLat)}, ${String.format("%.5f", currentLng)}" else "📍 Acquiring GPS...",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp
                )
                Text(
                    text = "${walkedPath.size} GPS points",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp
                )
            }

            // Start / Stop controls
            if (!hasPermission) {
                Button(
                    onClick = {
                        permLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleOrange),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("GRANT GPS PERMISSION", fontWeight = FontWeight.ExtraBold, color = ObsidianBlack)
                }
            } else if (!isTracking) {
                Button(
                    onClick = {
                        if (!hasPermission) return@Button
                        walkedPath.clear()
                        distanceMetres = 0.0
                        pathSaved = false
                        isTracking = true
                        startedInsideOwnTerritory = claimedTerritories.any { claim ->
                            claim.callsign == myCallsign &&
                                haversineMetres(currentLat, currentLng, claim.lat, claim.lng) <= claim.radius
                        }
                        if (currentLat != 0.0 && currentLng != 0.0) {
                            walkedPath.add(Pair(currentLat, currentLng))
                        }
                        statusMsg = "🛰️ GPS TRACKING ACTIVE — WALK YOUR TERRITORY"
                        startGpsTracking(fusedLocationClient, locationCallback)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (walkedPath.size >= 3 && pathSaved) "🛰️ START NEW TERRITORY" else "▶ START TRACKING",
                        fontWeight = FontWeight.ExtraBold,
                        color = ObsidianBlack,
                        fontSize = 14.sp
                    )
                }
            } else {
                // STOP button
                Button(
                    onClick = {
                        isTracking = false
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                        scope.launch {
                            if (walkedPath.size >= 2 && distanceMetres >= 12.0) {
                                statusMsg = "⏳ PROCESSING TERRITORY CLAIM..."
                                val lat = walkedPath.map { it.first }.average()
                                val lng = walkedPath.map { it.second }.average()
                                val radius = (distanceMetres / (2 * Math.PI)).coerceAtLeast(18.0)
                                val ok = withContext(Dispatchers.IO) {
                                    SupabaseService.saveOrExpandTerritoryClaim(
                                        callsign = myCallsign,
                                        lat = lat,
                                        lng = lng,
                                        radius = radius,
                                        faction = myFaction,
                                        expandExisting = startedInsideOwnTerritory
                                    )
                                }
                                if (ok) {
                                    pathSaved = true
                                    val updated = withContext(Dispatchers.IO) { SupabaseService.fetchTerritoryClaims() }
                                    claimedTerritories = updated
                                    statusMsg = if (startedInsideOwnTerritory) {
                                        "✅ TERRITORY EXPANDED (${distanceMetres.toInt()}m tracked)"
                                    } else {
                                        "✅ NEW TERRITORY CLAIMED (${distanceMetres.toInt()}m tracked)"
                                    }
                                } else {
                                    statusMsg = "❌ CLAIM SAVE FAILED — CHECK CONNECTION"
                                }
                            } else {
                                statusMsg = "⏹ TOO FEW POINTS — WALK MORE NEXT TIME"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        "⏹ STOP TRACKING  (${walkedPath.size} pts • ${(distanceMetres).toInt()}m)",
                        fontWeight = FontWeight.ExtraBold,
                        color = ObsidianBlack,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // ── Battle Challenge Dialog ───────────────────────────────────────────
        showBattleDialog?.let { rival ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBlack.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .background(Color(0xFF0E0E18), RoundedCornerShape(20.dp))
                        .border(1.dp, NeonRed.copy(0.5f), RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("⚔️", fontSize = 42.sp)
                    Text(
                        "RIVAL TERRITORY DETECTED",
                        color = NeonRed,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "You have entered ${rival.callsign}'s claimed zone!\nDefeat them to take over this territory.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp
                    )
                    Text(
                        "Faction: ${rival.faction.replace("_", " ")}",
                        color = NeonViolet,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showBattleDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.08f)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("RETREAT", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                GameState.activeTerritoryBattle = rival
                                showBattleDialog = null
                                onBattle()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⚔️ BATTLE!", color = ObsidianBlack, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun startGpsTracking(
    client: FusedLocationProviderClient,
    callback: LocationCallback
) {
    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
        .setMinUpdateDistanceMeters(3f)
        .build()
    client.requestLocationUpdates(request, callback, Looper.getMainLooper())
}
