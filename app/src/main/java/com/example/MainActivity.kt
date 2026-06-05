package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: OriginOSViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OriginOSApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun OriginOSApp(viewModel: OriginOSViewModel) {
    val isLocked by viewModel.isLocked.collectAsState()
    val backgroundIdx by viewModel.desktopBackgroundIdx.collectAsState()

    // Base background gradient matching selected theme
    val activeGradient = remember(backgroundIdx) {
        when (backgroundIdx) {
            0 -> Brush.verticalGradient(listOf(Color(0xFF0F0C1B), Color(0xFF201335), Color(0xFF06040A))) // Nebula
            1 -> Brush.verticalGradient(listOf(Color(0xFF011627), Color(0xFF013A63), Color(0xFF010204))) // Aqua
            2 -> Brush.verticalGradient(listOf(Color(0xFF1C0A00), Color(0xFF401600), Color(0xFF0C0300))) // Solar
            else -> Brush.verticalGradient(listOf(Color(0xFF0B1411), Color(0xFF14241E), Color(0xFF020403))) // Cyber Matrix
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(activeGradient)
    ) {
        // High fidelity particle/orbital elements standard of OriginOS design
        OriginOSAmbientBackdrop(backgroundIdx = backgroundIdx)

        // 3D Flip Lock Screen and Desktop transitions
        AnimatedContent(
            targetState = isLocked,
            transitionSpec = {
                // Customized ultra-smooth elastic scale-and-depth transition
                (scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialScale = 0.85f
                ) + fadeIn(animationSpec = tween(400))).togetherWith(
                    scaleOut(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        targetScale = 1.15f
                    ) + fadeOut(animationSpec = tween(300))
                )
            },
            label = "LockScreenTransition"
        ) { locked ->
            if (locked) {
                // 3D Flip Clock Lock Screen layout
                OriginLockScreen(
                    viewModel = viewModel,
                    onUnlock = { viewModel.setLocked(false) }
                )
            } else {
                // Launcher Workspace containing Island, Widgets, Pages & Control Center
                OriginLauncherWorkspace(viewModel = viewModel)
            }
        }
    }
}

// Background animated ambient designs
@Composable
fun OriginOSAmbientBackdrop(backgroundIdx: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "BackdropOrbits")
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Orbits"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScalePulse"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .scale(scalePulse)
            .blur(80.dp)
    ) {
        val width = size.width
        val height = size.height

        val particleColor = when (backgroundIdx) {
            0 -> Color(0xFF9333EA) // Nebula Purple
            1 -> Color(0xFF06B6D4) // Aqua Blue
            2 -> Color(0xFFEA580C) // Solar Gold
            else -> Color(0xFF10B981) // Cyber Green
        }

        val secondColor = when (backgroundIdx) {
            0 -> Color(0xFFEC4899)
            1 -> Color(0xFF3B82F6)
            2 -> Color(0xFFFBBF24)
            else -> Color(0xFF059669)
        }

        // Draw dynamic rotating gradient circles to create deep visual atmosphere
        val radian = Math.toRadians(orbitRotation.toDouble())
        val offsetX = (Math.cos(radian) * width * 0.15).toFloat()
        val offsetY = (Math.sin(radian) * height * 0.1).toFloat()

        drawCircle(
            color = particleColor.copy(alpha = 0.22f),
            radius = width * 0.35f,
            center = Offset(width * 0.4f + offsetX, height * 0.3f + offsetY)
        )

        drawCircle(
            color = secondColor.copy(alpha = 0.18f),
            radius = width * 0.45f,
            center = Offset(width * 0.62f - offsetX, height * 0.72f - offsetY)
        )
    }
}

// ==========================================
// 1. FLIP CARD LOCK SCREEN
// ==========================================
@Composable
fun OriginLockScreen(
    viewModel: OriginOSViewModel,
    onUnlock: () -> Unit
) {
    val systemFlipped by viewModel.lockscreenFlipped.collectAsState()
    val isPrivateUnlocked by viewModel.isPrivateSpaceUnlocked.collectAsState()

    // Format current time/date dynamically
    var timeString by remember { mutableStateOf("10:08") }
    var dateString by remember { mutableStateOf("Friday, June 5") }

    LaunchedEffect(Unit) {
        while (true) {
            val calendar = Calendar.getInstance()
            timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
            dateString = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(calendar.time)
            delay(1000)
        }
    }

    // Spring flip rotation animation
    val cardRotation by animateFloatAsState(
        targetValue = if (systemFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CardFlipAngle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Elegant top heading
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(
                    text = "ORIGIN OS • ATOMIC CARD",
                    fontSize = 11.sp,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Encrypted Lock",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SECURE SANDBOX ACTIVE",
                        fontSize = 9.sp,
                        letterSpacing = 1.5.sp,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                }
            }

            // Interactive 3D physical card widget
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .aspectRatio(0.68f)
                    .graphicsLayer {
                        rotationY = cardRotation
                        cameraDistance = 14 * density // depth index
                    }
                    .shadow(32.dp, RoundedCornerShape(28.dp), clip = false)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.toggleLockscreenFlip()
                    }
                    .testTag("lockscreen_flip_card")
            ) {
                if (cardRotation <= 90f) {
                    // FRONT OF LOCK CARD
                    LockCardFront(timeString = timeString, dateString = dateString)
                } else {
                    // BACK OF LOCK CARD
                    LockCardBack(isPrivateUnlocked = isPrivateUnlocked)
                }
            }

            // Animated interactive biometric unlock slider trigger
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = if (systemFlipped) "Tap card to flip back" else "Swipe up or hold scan to unlock",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Interactive Biometric Indicator button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF2563EB).copy(alpha = 0.4f), Color.Transparent)
                            ),
                            CircleShape
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clip(CircleShape)
                        .clickable { onUnlock() }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { onUnlock() },
                                onTap = { onUnlock() }
                            )
                        }
                        .testTag("unlock_biometric_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulse circles
                    val infiniteTransition = rememberInfiniteTransition(label = "FingerprintPulse")
                    val pulseRadius by infiniteTransition.animateFloat(
                        initialValue = 0.7f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1800, easing = EaseOutQuad),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "Pulse"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(pulseRadius)
                            .border(1.dp, Color.Cyan.copy(alpha = (1.1f - pulseRadius).coerceIn(0f, 1f)), CircleShape)
                    )

                    Icon(
                        imageVector = Icons.Rounded.Fingerprint,
                        contentDescription = "Scan Fingerprint to Unlock",
                        tint = Color.Cyan,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LockCardFront(timeString: String, dateString: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.03f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ORIGIN OS 6",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
                // Atomic cloud icon
                Icon(
                    imageVector = Icons.Rounded.Cloud,
                    contentDescription = "Cloud Info Widget",
                    tint = Color.Cyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Big Atomic Clock Numbers
            Column {
                Text(
                    text = timeString,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-1).sp,
                    color = Color.White
                )
                Text(
                    text = dateString,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // Nested widgets on lockscreen representing active cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Widget 1: Steps Atomic Core
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsRun,
                            contentDescription = "Steps",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "7,458",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "STEPS TODAY",
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Widget 2: Energy charging standard status card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Rounded.BatteryChargingFull,
                            contentDescription = "Battery Status",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "88%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "SUPER FLASH",
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LockCardBack(isPrivateUnlocked: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // Rotate back texture so it renders straight
                rotationY = 180f
            }
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1E1E2E).copy(alpha = 0.9f), Color(0xFF11111B).copy(alpha = 0.9f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ATOMIC SYSTEM CONFIG",
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Cyan
                )
                Icon(
                    imageVector = Icons.Rounded.VerifiedUser,
                    contentDescription = "Verified Identity",
                    tint = Color.Cyan,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Lock screen configuration details
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ConfigRow(label = "Origin Engine 6.0", valStr = "v6.0.4 - STABLE")
                ConfigRow(label = "Atomic Screen Latency", valStr = "2.4ms (90Hz)")
                ConfigRow(label = "GPU Rendering Type", valStr = "Crystalline")
                ConfigRow(
                    label = "Private Vault Lock",
                    valStr = if (isPrivateUnlocked) "UNLOCKED" else "LOCKED",
                    isWarning = !isPrivateUnlocked
                )
            }

            // Quick launch shortcut details inside cards
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Nfc,
                        contentDescription = "Transit Pass",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Origin NFC Card Stack",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Transit & Smart Locks Ready",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigRow(label: String, valStr: String, isWarning: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
        Text(
            text = valStr,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isWarning) Color(0xFFF43F5E) else Color.White
        )
    }
}


// ==========================================
// 2. UNLOCKED WORKSPACE LAUNCHER
// ==========================================
@Composable
fun OriginLauncherWorkspace(viewModel: OriginOSViewModel) {
    val showControlCenter by viewModel.controlCenterVisible.collectAsState()
    val islandType by viewModel.islandType.collectAsState()
    val isPrivateUnlocked by viewModel.isPrivateSpaceUnlocked.collectAsState()

    var activeView by remember { mutableStateOf<String>("desktop") } // desktop, nfc_manager, private_space

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP AREA containing dynamic Origin Island
            Spacer(modifier = Modifier.height(12.dp))
            OriginIslandComponent(viewModel = viewModel)

            Spacer(modifier = Modifier.height(14.dp))

            // MAIN WORKSPACE INTERACTIVE PORTION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                // Load active subscreen view based on state
                AnimatedContent(
                    targetState = activeView,
                    transitionSpec = {
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialOffsetX = { if (targetState == "desktop") -it else it }
                        ) + fadeIn()).togetherWith(
                            slideOutHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                targetOffsetX = { if (targetState == "desktop") it else -it }
                            ) + fadeOut()
                        )
                    },
                    label = "LauncherNavigationTransition"
                ) { viewName ->
                    when (viewName) {
                        "desktop" -> LauncherDesktopGrid(
                            viewModel = viewModel,
                            onGoToNfc = { activeView = "nfc_manager" },
                            onGoToPrivate = {
                                if (isPrivateUnlocked) {
                                    activeView = "private_space"
                                } else {
                                    viewModel.setIsland(OriginIslandType.NORMAL, "Please unlock with PIN widget below")
                                }
                            }
                        )
                        "nfc_manager" -> NfcDashboardView(
                            viewModel = viewModel,
                            onBackToDesktop = { activeView = "desktop" }
                        )
                        "private_space" -> PrivateSpaceView(
                            viewModel = viewModel,
                            onBackToDesktop = { activeView = "desktop" }
                        )
                    }
                }
            }

            // GORGEOUS BOTTOM NAVIGATION PANEL
            OriginBottomNavBar(
                activeView = activeView,
                onSelectView = { targetView ->
                    if (targetView == "private_space" && !isPrivateUnlocked) {
                        viewModel.setIsland(OriginIslandType.NORMAL, "Enter Secret PIN code to access vault")
                    } else {
                        activeView = targetView
                    }
                },
                isPrivateUnlocked = isPrivateUnlocked
            )
        }

        // SLIDING GLASSMORPHIC CONTROL CENTER DRAWER OVERLAY
        AnimatedVisibility(
            visible = showControlCenter,
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                initialOffsetY = { -it }
            ) + fadeIn(),
            exit = slideOutVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                targetOffsetY = { -it }
            ) + fadeOut()
        ) {
            ControlCenterDrawer(viewModel = viewModel)
        }
    }
}


// ==========================================
// 3. ORIGIN ISLAND (DYNAMIC NOTIFICATION PILL)
// ==========================================
@Composable
fun OriginIslandComponent(viewModel: OriginOSViewModel) {
    val islandType by viewModel.islandType.collectAsState()
    val islandMessage by viewModel.islandMessage.collectAsState()
    val isPlayingMusic by viewModel.isPlayingMusic.collectAsState()
    val activeTrack by viewModel.activeTrackName.collectAsState()

    // Interactive expansion toggles
    var isManuallyExpanded by remember { mutableStateOf(false) }

    val contentHeight: Dp
    val contentWidthFraction: Float
    val glowColor: Color

    when (islandType) {
        OriginIslandType.NORMAL -> {
            contentHeight = if (isManuallyExpanded) 120.dp else 40.dp
            contentWidthFraction = if (isManuallyExpanded) 0.9f else 0.50f
            glowColor = Color(0xFF9333EA).copy(alpha = 0.2f)
        }
        OriginIslandType.MUSIC -> {
            contentHeight = if (isManuallyExpanded) 150.dp else 56.dp
            contentWidthFraction = if (isManuallyExpanded) 0.95f else 0.88f
            glowColor = Color(0xFFF43F5E).copy(alpha = 0.4f)
        }
        OriginIslandType.NFC_SCANNING -> {
            contentHeight = 84.dp
            contentWidthFraction = 0.90f
            glowColor = Color(0xFF06B6D4).copy(alpha = 0.5f)
        }
        OriginIslandType.NFC_BROADCASTING -> {
            contentHeight = 88.dp
            contentWidthFraction = 0.92f
            glowColor = Color(0xFF10B981).copy(alpha = 0.5f)
        }
        OriginIslandType.PRIVATE_SECURE -> {
            contentHeight = if (isManuallyExpanded) 130.dp else 44.dp
            contentWidthFraction = if (isManuallyExpanded) 0.92f else 0.65f
            glowColor = Color(0xFFEF4444).copy(alpha = 0.4f)
        }
        OriginIslandType.CHARGING -> {
            contentHeight = 44.dp
            contentWidthFraction = 0.72f
            glowColor = Color(0xFF10B981).copy(alpha = 0.6f)
        }
    }

    val animatedHeight by animateDpAsState(
        targetValue = contentHeight,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "IslandHeight"
    )

    val animatedWidthFraction by animateFloatAsState(
        targetValue = contentWidthFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "IslandWidth"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(animatedWidthFraction)
            .height(animatedHeight)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clip(RoundedCornerShape(26.dp))
            .background(Color.Black)
            .border(1.2.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(26.dp))
            .clickable { isManuallyExpanded = !isManuallyExpanded }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .testTag("dynamic_origin_island"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            when (islandType) {
                // A. NORMAL STATE
                OriginIslandType.NORMAL -> {
                    if (isManuallyExpanded) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SYSTEM ATOMIC STATUS", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Icon(Icons.Rounded.Info, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                "OriginOS 6 Ultimate Hub: Perfect fluid simulation of futuristic smartphone design keys.",
                                fontSize = 12.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text("Tap to collapse", fontSize = 9.sp, color = Color.Gray)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(Color(0xFF8B5CF6), CircleShape)
                            )
                            Text(
                                text = islandMessage,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // B. MUSIC STATE
                OriginIslandType.MUSIC -> {
                    if (isManuallyExpanded) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Cover Art
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            Brush.sweepGradient(
                                                listOf(Color.Red, Color.Magenta, Color.Blue, Color.Red)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Color.White)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(activeTrack, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("OriginOS Ambient Sync", fontSize = 10.sp, color = Color.Gray)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    IconButton(
                                        onClick = { viewModel.togglePlayMusic() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingMusic) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                                            contentDescription = "Play/Pause",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            // Dynamic animated music viz lines
                            MusicOscillatorVisualizer()
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = "Active Media Player",
                                tint = Color(0xFFF43F5E),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Now Playing: $activeTrack",
                                fontSize = 11.5.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            MusicTinyVisualizerLines()
                        }
                    }
                }

                // C. NFC SCANNING STATE
                OriginIslandType.NFC_SCANNING -> {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color.Cyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("NFC Scanner Ready", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Ready to read tag RFID...", fontSize = 9.sp, color = Color.LightGray)
                        }
                        Button(
                            onClick = { viewModel.simulateNfcCardScanned() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("MOCK RFID SCAN", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // D. NFC BROADCASTING STATE
                OriginIslandType.NFC_BROADCASTING -> {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Nfc,
                            contentDescription = "Active broadcast",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            val activeCard by viewModel.broadcastingCard.collectAsState()
                            Text("Broadcasting NFC ID", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(activeCard?.name ?: "RFID Token Stack", fontSize = 9.sp, color = Color.LightGray)
                        }
                        Button(
                            onClick = { viewModel.stopBroadcastingNfc() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("STOP", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // E. PRIVATE SECURE DECRYPTED
                OriginIslandType.PRIVATE_SECURE -> {
                    if (isManuallyExpanded) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("DECRYPTED PRIVATE STORAGE", fontSize = 10.sp, color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold)
                                Icon(Icons.Rounded.LockOpen, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(16.dp))
                            }
                            Text(
                                "Sensitive session active. Do not leave unattended.",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            Button(
                                onClick = { viewModel.lockPrivateSpace() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("SECURE BLOCK NOW", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = "Active Session",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Vault Unlocked Safely",
                                fontSize = 11.5.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = "Secure",
                                tint = Color.Green,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // F. CHARGING
                OriginIslandType.CHARGING -> {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.FlashOn, contentDescription = "FlashCharge", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Text("120W Flash Charge", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text("88%", fontSize = 13.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MusicTinyVisualizerLines() {
    val infiniteTransition = rememberInfiniteTransition(label = "TinyViz")
    val animFactor1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(animation = tween(400, easing = EaseInOutCubic), repeatMode = RepeatMode.Reverse),
        label = "tiny1"
    )
    val animFactor2 by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(animation = tween(500, easing = EaseInOutCubic), repeatMode = RepeatMode.Reverse),
        label = "tiny2"
    )
    val animFactor3 by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(animation = tween(350, easing = EaseInOutCubic), repeatMode = RepeatMode.Reverse),
        label = "tiny3"
    )

    Row(
        modifier = Modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(modifier = Modifier.width(2.dp).fillMaxHeight(animFactor1).background(Color(0xFFF43F5E), RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.width(2.dp).fillMaxHeight(animFactor2).background(Color(0xFFF43F5E), RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.width(2.dp).fillMaxHeight(animFactor3).background(Color(0xFFF43F5E), RoundedCornerShape(1.dp)))
    }
}

@Composable
fun MusicOscillatorVisualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "Oscillator")
    val sizeRatio by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
        label = "osc"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
    ) {
        val width = size.width
        val height = size.height

        val p = androidx.compose.ui.graphics.Path()
        p.moveTo(0f, height / 2)

        val steps = 40
        val ampValue = height * 0.4f * sizeRatio

        for (i in 0..steps) {
            val x = (width / steps) * i
            val rad = (i * Math.PI * 4 / steps)
            val y = height / 2 + Math.sin(rad).toFloat() * ampValue
            p.lineTo(x, y)
        }

        drawPath(
            path = p,
            color = Color(0xFFF43F5E),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}


// ==========================================
// 4. MAIN WORKSPACE / LAUNCHER DESKTOP GRID
// ==========================================
@Composable
fun LauncherDesktopGrid(
    viewModel: OriginOSViewModel,
    onGoToNfc: () -> Unit,
    onGoToPrivate: () -> Unit
) {
    val batterySaver by viewModel.batterySaver.collectAsState()
    val isPrivateUnlocked by viewModel.isPrivateSpaceUnlocked.collectAsState()
    val isPlayingMusic by viewModel.isPlayingMusic.collectAsState()
    val wifiEnabled by viewModel.wifiEnabled.collectAsState()
    val bluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
    val nfcEnabled by viewModel.nfcEnabled.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // QUICK SYSTEM BANNER/PULLDOWN INDICATOR
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { viewModel.toggleControlCenter() }
                    .padding(12.dp)
                    .testTag("pulldown_bar_control_center")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.DashboardCustomize, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        Text("Active Desktop: Drag Down for Control Center", fontSize = 11.sp, color = Color.LightGray)
                    }
                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, tint = Color.LightGray)
                }
            }
        }

        // TILE BLOCK 1: Flipcard Locking trigger & Media Widget
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // LOCK SCREEN CARD RETRIGGER
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.1f)
                        .shadow(8.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                            )
                        )
                        .clickable { viewModel.setLocked(true) }
                        .padding(14.dp)
                        .testTag("lock_trigger_widget")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SettingsBackupRestore,
                                contentDescription = "Lock icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("3D FLIP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Column {
                            Text("Lock Screen", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Atomic Card Flip", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }

                // MUSIC ATOMIC WIDGET
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.1f)
                        .shadow(8.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFF43F5E), Color(0xFFE11D48))
                            )
                        )
                        .clickable { viewModel.togglePlayMusic() }
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPlayingMusic) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                                contentDescription = "Audio State",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = "Music",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text("Atomic Music", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPlayingMusic) "Playing Album" else "Tap to Play",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // TILE BLOCK 2: NFC Virtual Card and Private Space Entry Keypad
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // NFC TRANSIT DECK CONTROL
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(130.dp)
                        .shadow(8.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.07f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
                        .clickable { onGoToNfc() }
                        .padding(14.dp)
                        .testTag("nfc_widget_card")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Nfc,
                                contentDescription = "NFC Dashboard",
                                tint = if (nfcEnabled) Color(0xFF10B981) else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "NFC PASS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (nfcEnabled) Color(0xFF10B981) else Color.Gray
                            )
                        }

                        Column {
                            Text("Origin NFC Hub", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Active Broadcast & Sim", fontSize = 11.sp, color = Color.LightGray)
                        }
                    }
                }

                // SECRET VAULT PIN TRIGGER WIDGET
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .height(130.dp)
                        .shadow(8.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.07f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
                        .clickable {
                            if (isPrivateUnlocked) {
                                onGoToPrivate()
                            } else {
                                showPinDialog = true
                            }
                        }
                        .padding(14.dp)
                        .testTag("private_space_widget_pin")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPrivateUnlocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                contentDescription = "Private Sandbox Vault",
                                tint = if (isPrivateUnlocked) Color.Green else Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isPrivateUnlocked) Color.Green else Color(0xFFEF4444),
                                        CircleShape
                                    )
                            )
                        }

                        Column {
                            Text("Private Space", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPrivateUnlocked) "TAP TO VISIT" else "TAP TO DEC PIN",
                                fontSize = 10.sp,
                                color = if (isPrivateUnlocked) Color.Green else Color.LightGray
                            )
                        }
                    }
                }
            }
        }

        // TILE BLOCK 3: OriginOS Desktop Customizer Control Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ORIGIN ENGINE CONFIG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Icon(Icons.Rounded.Palette, contentDescription = null, tint = Color.Cyan, modifier = Modifier.size(16.dp))
                    }

                    Text(
                        "OriginOS 6 introduces Desktop Space personalization. Change dynamic particle grids, toggle active states and fluid physics.",
                        fontSize = 11.sp, color = Color.LightGray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.changeDesktopBackground() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Swap Aesthetic Grid", fontSize = 11.sp, color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.setIsland(OriginIslandType.CHARGING, "120W Flash Charge Initialized") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mock Charge", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Passcode Pad dialog for secure Private Space Entry on first visit
    if (showPinDialog) {
        Dialog(onDismissRequest = { showPinDialog = false }) {
            PrivateSpacePinKeypad(
                viewModel = viewModel,
                onSuccess = {
                    showPinDialog = false
                    onGoToPrivate()
                },
                onCancel = { showPinDialog = false }
            )
        }
    }
}


// ==========================================
// 5. CONTROL CENTRE (SLIDING GLASSMORPH PANEL)
// ==========================================
@Composable
fun ControlCenterDrawer(viewModel: OriginOSViewModel) {
    val wifiEnabled by viewModel.wifiEnabled.collectAsState()
    val bluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
    val nfcEnabled by viewModel.nfcEnabled.collectAsState()
    val flashlightEnabled by viewModel.flashlightEnabled.collectAsState()
    val batterySaver by viewModel.batterySaver.collectAsState()
    val brightness by viewModel.brightness.collectAsState()
    val volume by viewModel.volume.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)) // backdrop dim
            .clickable { viewModel.setControlCenterVisible(false) } // click outline to dismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f) // slides down from top
                .shadow(24.dp, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1E2E), Color(0xFF11111B))
                    )
                )
                .border(
                    1.2.dp,
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .clickable(enabled = true, onClick = {}) // prevent clickthrough
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CONTROL CENTRE",
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Text("OriginOS 6 Matrix Panel", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                IconButton(
                    onClick = { viewModel.setControlCenterVisible(false) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // BIG MAIN CONNECTIVITY TILES (WIFI + BLUETOOTH)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Wifi
                BigTile(
                    title = "Wi-Fi Hub",
                    subtitle = if (wifiEnabled) "Alkaid_5G_Secure" else "Disabled Network",
                    icon = Icons.Rounded.Wifi,
                    isActive = wifiEnabled,
                    onClick = { viewModel.setWifiEnabled(!wifiEnabled) }
                )

                // Bluetooth
                BigTile(
                    title = "Bluetooth Core",
                    subtitle = if (bluetoothEnabled) "Vivo_Air_Pro" else "Power Off",
                    icon = Icons.Rounded.Bluetooth,
                    isActive = bluetoothEnabled,
                    onClick = { viewModel.setBluetoothEnabled(!bluetoothEnabled) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // TACTILE SLIDERS PANEL (VOLUME + BRIGHTNESS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Brightness Slider Custom OriginOS Style
                TactileSliderCard(
                    modifier = Modifier.weight(1f),
                    title = "Brightness",
                    value = brightness,
                    onValueChange = { viewModel.setBrightness(it) },
                    icon = Icons.Rounded.LightMode
                )

                // Volume Slider Custom OriginOS Style
                TactileSliderCard(
                    modifier = Modifier.weight(1f),
                    title = "Volume Stream",
                    value = volume,
                    onValueChange = { viewModel.setVolume(it) },
                    icon = Icons.Rounded.VolumeUp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SMALL TOGGLES MATRIX GRID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CircleToggle(
                    icon = Icons.Rounded.Nfc,
                    label = "NFC Radio",
                    isActive = nfcEnabled,
                    onClick = { viewModel.toggleNfc() }
                )

                CircleToggle(
                    icon = Icons.Rounded.FlashlightOn,
                    label = "Flashlight",
                    isActive = flashlightEnabled,
                    onClick = { viewModel.toggleFlashlight() }
                )

                CircleToggle(
                    icon = Icons.Rounded.BatterySaver,
                    label = "Battery Saver",
                    isActive = batterySaver,
                    onClick = { viewModel.toggleBatterySaver() }
                )

                CircleToggle(
                    icon = Icons.Rounded.ScreenRotation,
                    label = "Auto Rotate",
                    isActive = true,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Simulated active elements (like flash trigger simulator if turned on)
            if (flashlightEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF08A).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = Color.Yellow)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Active Flashlight Simulator Radiating LED", color = Color.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sliding handle bar decoration
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun RowScope.BigTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isActive) Color(0xFF0284C7).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f)
            )
            .border(
                1.3.dp,
                if (isActive) Color(0xFF0284C7) else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isActive) Color(0xFF0284C7) else Color.White.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) Color.White else Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, fontSize = 9.sp, color = Color.LightGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun TactileSliderCard(
    modifier: Modifier = Modifier,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector
) {
    Box(
        modifier = modifier
            .height(124.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }

            // Real-time custom slider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .pointerInput(value) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            val widthPx = size.width
                            val step = dragAmount / widthPx
                            val newValue = (value + step).coerceIn(0f, 1f)
                            onValueChange(newValue)
                        }
                    }
            ) {
                // Progress Fill
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(value)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                            )
                        )
                )

                // Grab visual circle indicator
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${(value * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CircleToggle(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.08f)
                )
                .border(
                    1.2.dp,
                    if (isActive) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.15f),
                    CircleShape
                )
                .clickable { onClick() }
                .testTag("control_center_toggle_${label.lowercase().replace(" ", "_")}"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else Color.LightGray,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = label,
            fontSize = 9.5.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
    }
}


// ==========================================
// 6. NFC CARD MANAGER AND SCANNER VIEW
// ==========================================
@Composable
fun NfcDashboardView(
    viewModel: OriginOSViewModel,
    onBackToDesktop: () -> Unit
) {
    val nfcEnabled by viewModel.nfcEnabled.collectAsState()
    val nfcCards by viewModel.nfcCards.collectAsState()
    val isScanning by viewModel.nfcScanning.collectAsState()
    val scannedInfo by viewModel.scannedCardInfo.collectAsState()
    val activeBroadcastCard by viewModel.broadcastingCard.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Heading with Back option
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBackToDesktop,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Origin NFC Stack", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Switch(
                checked = nfcEnabled,
                onCheckedChange = { viewModel.toggleNfc() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF10B981),
                    checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.3f)
                ),
                modifier = Modifier.testTag("nfc_switch")
            )
        }

        if (!nfcEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Rounded.Nfc, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(54.dp))
                    Text("NFC Hardware is Disabled", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "Please flip the switcher at the top right to enable cellular virtualized radio antennas.",
                        fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Main content active
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("NFC Tag Scanner Radar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Cyan)

                    Text(
                        "Verify and read RFID tags by running scanner. Align magnetic cards to the back sensor.",
                        fontSize = 11.sp, color = Color.LightGray
                    )

                    if (isScanning) {
                        // Dynamic Radar Sweep Wave animation
                        NfcRadarSweepWave()

                        Button(
                            onClick = { viewModel.cancelNfcScanning() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("CANCEL SCANNING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.startNfcScanning() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Radar, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RUN TAG DISCOVERY SCAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    scannedInfo?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text(it, fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Cards deck list
            Text("VIRTUALIZED CARD WALLET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.5.sp)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(nfcCards) { card ->
                    val isEmulatingNow = activeBroadcastCard?.id == card.id

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(card.colorHex).copy(alpha = 0.2f))
                            .border(
                                1.5.dp,
                                if (isEmulatingNow) Color(card.colorHex) else Color(card.colorHex).copy(alpha = 0.5f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = when (card.type) {
                                            "Transit" -> Icons.Rounded.Subway
                                            "Keycard" -> Icons.Rounded.VpnKey
                                            else -> Icons.Rounded.FitnessCenter
                                        },
                                        contentDescription = null,
                                        tint = Color(card.colorHex),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(card.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Text("RFID UID: ${card.uid} • TYPE: ${card.type}", fontSize = 10.sp, color = Color.LightGray)
                            }

                            if (isEmulatingNow) {
                                Button(
                                    onClick = { viewModel.stopBroadcastingNfc() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("OFF", fontSize = 10.sp, color = Color.White)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.startBroadcastingCard(card) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(card.colorHex)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("EMULATE", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NfcRadarSweepWave() {
    val infiniteTransition = rememberInfiniteTransition(label = "Radar")
    val radarScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(80.dp)) {
            drawCircle(
                color = Color.Cyan.copy(alpha = (1.1f - radarScale).coerceIn(0f, 1f)),
                radius = size.width * radarScale,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color.Cyan.copy(alpha = 0.2f),
                radius = size.width * 0.4f
            )
        }
        Icon(Icons.Rounded.Nfc, contentDescription = null, tint = Color.Cyan, modifier = Modifier.size(32.dp))
    }
}


// ==========================================
// 7. PRIVATE SPACE SECURE CORNER (VAULT CODE)
// ==========================================
@Composable
fun PrivateSpacePinKeypad(
    viewModel: OriginOSViewModel,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val enteredPin by viewModel.enteredPin.collectAsState()
    val showPinError by viewModel.showPinError.collectAsState()
    val isUnlocked by viewModel.isPrivateSpaceUnlocked.collectAsState()

    LaunchedEffect(isUnlocked) {
        if (isUnlocked) {
            onSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF181825))
            .border(1.2.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ENTER PRIVATE KEYPIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { idx ->
                    val isFilled = idx < enteredPin.length
                    val color = if (showPinError) Color.Red else if (isFilled) Color.Green else Color.White.copy(alpha = 0.2f)
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(color, CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                }
            }

            if (showPinError) {
                Text("PIN authentication failed. Try again ('1234')", color = Color.Red, fontSize = 10.sp)
            } else {
                Text("Default development PIN passcode: 1234", color = Color.Cyan.copy(alpha = 0.7f), fontSize = 10.sp)
            }

            // Secure Keypad layout 3x4
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val keys = listOf(
                    listOf('1', '2', '3'),
                    listOf('4', '5', '6'),
                    listOf('7', '8', '9'),
                    listOf('C', '0', '⌫')
                )

                keys.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowKeys.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.8f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .clickable {
                                        when (key) {
                                            'C' -> viewModel.backspacePin() // backspace acts same here or clear
                                            '⌫' -> viewModel.backspacePin()
                                            else -> viewModel.enterPinDigit(key)
                                        }
                                    }
                                    .testTag("keypad_digit_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 7B. INNER VAULT PAGE VIEW
@Composable
fun PrivateSpaceView(
    viewModel: OriginOSViewModel,
    onBackToDesktop: () -> Unit
) {
    val privateNotes by viewModel.privateNotes.collectAsState()
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Safe Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBackToDesktop,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Private Space Core", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            IconButton(
                onClick = {
                    viewModel.lockPrivateSpace()
                    onBackToDesktop()
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFEF4444).copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Rounded.Lock, contentDescription = "Lock", tint = Color(0xFFEF4444))
            }
        }

        // Warning bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEF4444).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                Text("Session decrypted. All local files unzipped in hidden partition.", color = Color(0xFFF87171), fontSize = 10.5.sp)
            }
        }

        // Mock confidential documents block
        Text("SECURE DIGITAL VAULT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.5.sp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Encrypted Media Locker / Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("3 Files stored", fontSize = 10.sp, color = Color.Cyan)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VaultFileBadge(name = "id_card_scan.jpg", type = "IMG")
                    VaultFileBadge(name = "ledger_backup.txt", type = "TXT")
                    VaultFileBadge(name = "payroll_details.pdf", type = "PDF")
                }
            }
        }

        // Dynamic hidden ledger/notes adder
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Store Private Note or Password", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)

                OutlinedTextField(
                    value = newNoteTitle,
                    onValueChange = { newNoteTitle = it },
                    placeholder = { Text("Secret Title", color = Color.Gray, fontSize = 11.sp) },
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Cyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("private_note_title")
                )

                OutlinedTextField(
                    value = newNoteContent,
                    onValueChange = { newNoteContent = it },
                    placeholder = { Text("Encrypted content...", color = Color.Gray, fontSize = 11.sp) },
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Cyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("private_note_content")
                )

                Button(
                    onClick = {
                        viewModel.addPrivateNote(newNoteTitle, newNoteContent)
                        newNoteTitle = ""
                        newNoteContent = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("add_private_note_button"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("SAVE TO ENCRYPTED VAULT", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Display current notes
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(privateNotes) { note ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(note.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            IconButton(
                                onClick = { viewModel.deletePrivateNote(note.id) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(note.content, fontSize = 11.sp, color = Color.LightGray)
                        Text(note.timestamp, fontSize = 8.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.VaultFileBadge(name: String, type: String) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = when (type) {
                    "IMG" -> Icons.Rounded.Image
                    "PDF" -> Icons.Rounded.PictureAsPdf
                    else -> Icons.Rounded.Article
                },
                contentDescription = null,
                tint = Color.Cyan,
                modifier = Modifier.size(14.dp)
            )
            Text(
                name,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


// ==========================================
// 8. COHESIVE SYSTEM BOTTOM NAVBAR STYLE
// ==========================================
@Composable
fun OriginBottomNavBar(
    activeView: String,
    onSelectView: (String) -> Unit,
    isPrivateUnlocked: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color.Transparent)
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Outlined.Home,
                activeIcon = Icons.Filled.Home,
                label = "Desktop",
                isActive = activeView == "desktop",
                onClick = { onSelectView("desktop") }
            )

            NavBarItem(
                icon = Icons.Outlined.Nfc,
                activeIcon = Icons.Filled.Nfc,
                label = "NFC Stack",
                isActive = activeView == "nfc_manager",
                onClick = { onSelectView("nfc_manager") }
            )

            NavBarItem(
                icon = if (isPrivateUnlocked) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
                activeIcon = if (isPrivateUnlocked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                label = "Secure Vault",
                isActive = activeView == "private_space",
                onClick = { onSelectView("private_space") }
            )
        }
    }
}

@Composable
fun RowScope.NavBarItem(
    icon: ImageVector,
    activeIcon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "NavBarIconScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .testTag("nav_item_${label.lowercase().replace(" ", "_")}")
    ) {
        Icon(
            imageVector = if (isActive) activeIcon else icon,
            contentDescription = label,
            tint = if (isActive) Color.Cyan else Color.LightGray,
            modifier = Modifier
                .size(22.dp)
                .scale(animatedScale)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) Color.Cyan else Color.LightGray
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
