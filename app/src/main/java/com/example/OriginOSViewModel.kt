package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class OriginIslandType {
    NORMAL,
    MUSIC,
    NFC_SCANNING,
    NFC_BROADCASTING,
    PRIVATE_SECURE,
    CHARGING
}

data class NfcCard(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // "Transit", "Keycard", "GymPass"
    val uid: String,
    val colorHex: Long,
    val isEmulated: Boolean = false
)

data class PrivateNote(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: String
)

class OriginOSViewModel : ViewModel() {

    // Lockscreen and system states
    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _lockscreenFlipped = MutableStateFlow(false)
    val lockscreenFlipped: StateFlow<Boolean> = _lockscreenFlipped.asStateFlow()

    // Control Centre states
    private val _controlCenterVisible = MutableStateFlow(false)
    val controlCenterVisible: StateFlow<Boolean> = _controlCenterVisible.asStateFlow()

    private val _nfcEnabled = MutableStateFlow(true)
    val nfcEnabled: StateFlow<Boolean> = _nfcEnabled.asStateFlow()

    private val _brightness = MutableStateFlow(0.7f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    private val _volume = MutableStateFlow(0.5f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _wifiEnabled = MutableStateFlow(true)
    val wifiEnabled: StateFlow<Boolean> = _wifiEnabled.asStateFlow()

    private val _bluetoothEnabled = MutableStateFlow(true)
    val bluetoothEnabled: StateFlow<Boolean> = _bluetoothEnabled.asStateFlow()

    private val _flashlightEnabled = MutableStateFlow(false)
    val flashlightEnabled: StateFlow<Boolean> = _flashlightEnabled.asStateFlow()

    private val _batterySaver = MutableStateFlow(false)
    val batterySaver: StateFlow<Boolean> = _batterySaver.asStateFlow()

    // Desktop Customization
    private val _desktopBackgroundIdx = MutableStateFlow(0)
    val desktopBackgroundIdx: StateFlow<Int> = _desktopBackgroundIdx.asStateFlow()

    // Origin Island States
    private val _islandType = MutableStateFlow(OriginIslandType.NORMAL)
    val islandType: StateFlow<OriginIslandType> = _islandType.asStateFlow()

    private val _islandMessage = MutableStateFlow("OriginOS 6 Ultimate Active")
    val islandMessage: StateFlow<String> = _islandMessage.asStateFlow()

    // NFC states
    private val _nfcCards = MutableStateFlow(
        listOf(
            NfcCard(name = "Shenzhen Metro Pass", type = "Transit", uid = "8A:92:C3:FF", colorHex = 0xFF10B981),
            NfcCard(name = "Heckmann Smart Key", type = "Keycard", uid = "F4:B1:39:0C", colorHex = 0xFF0EA5E9),
            NfcCard(name = "SuperGym Member Card", type = "GymPass", uid = "2D:F5:6C:BB", colorHex = 0xFFF43F5E)
        )
    )
    val nfcCards: StateFlow<List<NfcCard>> = _nfcCards.asStateFlow()

    private val _nfcScanning = MutableStateFlow(false)
    val nfcScanning: StateFlow<Boolean> = _nfcScanning.asStateFlow()

    private val _scannedCardInfo = MutableStateFlow<String?>(null)
    val scannedCardInfo: StateFlow<String?> = _scannedCardInfo.asStateFlow()

    private val _broadcastingCard = MutableStateFlow<NfcCard?>(null)
    val broadcastingCard: StateFlow<NfcCard?> = _broadcastingCard.asStateFlow()

    // Private Space states
    private val _privatePin = MutableStateFlow("1234")
    val privatePin: StateFlow<String> = _privatePin.asStateFlow()

    private val _enteredPin = MutableStateFlow("")
    val enteredPin: StateFlow<String> = _enteredPin.asStateFlow()

    private val _isPrivateSpaceUnlocked = MutableStateFlow(false)
    val isPrivateSpaceUnlocked: StateFlow<Boolean> = _isPrivateSpaceUnlocked.asStateFlow()

    private val _isPrivateSpaceSetup = MutableStateFlow(true) // already configured
    val isPrivateSpaceSetup: StateFlow<Boolean> = _isPrivateSpaceSetup.asStateFlow()

    private val _showPinError = MutableStateFlow(false)
    val showPinError: StateFlow<Boolean> = _showPinError.asStateFlow()

    private val _privateNotes = MutableStateFlow(
        listOf(
            PrivateNote(
                title = "Secret Wallet Seed",
                content = "crystal forest anchor pattern dynamic gravity engine smooth transition canvas",
                timestamp = "2026-06-04"
            ),
            PrivateNote(
                title = "OriginOS Code Name",
                content = "The codename for OS 6 is 'Alkaid'. Emphasize 120Hz smooth state machines everywhere.",
                timestamp = "2026-06-05"
            )
        )
    )
    val privateNotes: StateFlow<List<PrivateNote>> = _privateNotes.asStateFlow()

    // Music Media Player demo state
    private val _isPlayingMusic = MutableStateFlow(false)
    val isPlayingMusic: StateFlow<Boolean> = _isPlayingMusic.asStateFlow()

    private val _activeTrackName = MutableStateFlow("Alkaid - OriginOS Ambient Theme")
    val activeTrackName: StateFlow<String> = _activeTrackName.asStateFlow()

    // Functions
    fun setLocked(locked: Boolean) {
        _isLocked.value = locked
        if (locked) {
            // Re-lock clears private space auth for security
            _isPrivateSpaceUnlocked.value = false
            _enteredPin.value = ""
            setIsland(OriginIslandType.NORMAL, "Device Locked")
        } else {
            setIsland(OriginIslandType.CHARGING, "Unlocked • Welcome back")
        }
    }

    fun toggleLockscreenFlip() {
        _lockscreenFlipped.value = !_lockscreenFlipped.value
    }

    fun toggleControlCenter() {
        _controlCenterVisible.value = !_controlCenterVisible.value
    }

    fun setControlCenterVisible(visible: Boolean) {
        _controlCenterVisible.value = visible
    }

    fun toggleNfc() {
        _nfcEnabled.value = !_nfcEnabled.value
        if (!_nfcEnabled.value) {
            stopBroadcastingNfc()
        }
        setIsland(
            OriginIslandType.NORMAL,
            if (_nfcEnabled.value) "NFC Radio: ON" else "NFC Radio: OFF"
        )
    }

    fun setWifiEnabled(enabled: Boolean) {
        _wifiEnabled.value = enabled
    }

    fun setBluetoothEnabled(enabled: Boolean) {
        _bluetoothEnabled.value = enabled
        if (enabled) {
            setIsland(OriginIslandType.NORMAL, "Bluetooth Active")
        }
    }

    fun toggleFlashlight() {
        _flashlightEnabled.value = !_flashlightEnabled.value
    }

    fun toggleBatterySaver() {
        _batterySaver.value = !_batterySaver.value
    }

    fun changeDesktopBackground() {
        _desktopBackgroundIdx.value = (_desktopBackgroundIdx.value + 1) % 4
    }

    fun setBrightness(value: Float) {
        _brightness.value = value
    }

    fun setVolume(value: Float) {
        _volume.value = value
    }

    // Origin Island helpers
    fun setIsland(type: OriginIslandType, message: String) {
        _islandType.value = type
        _islandMessage.value = message
    }

    // Music control (Flashes in Island)
    fun togglePlayMusic() {
        _isPlayingMusic.value = !_isPlayingMusic.value
        if (_isPlayingMusic.value) {
            setIsland(OriginIslandType.MUSIC, "Playing: Alkaid Theme 🎵")
        } else {
            setIsland(OriginIslandType.NORMAL, "Music Paused")
        }
    }

    // NFC Card scanner / Virtualizer
    fun startNfcScanning() {
        if (!_nfcEnabled.value) {
            setIsland(OriginIslandType.NORMAL, "Enable NFC first in Control Center!")
            return
        }
        _nfcScanning.value = true
        _scannedCardInfo.value = null
        setIsland(OriginIslandType.NFC_SCANNING, "Ready to Scan RFID/NFC Card...")
    }

    fun simulateNfcCardScanned() {
        if (!_nfcScanning.value) return
        _nfcScanning.value = false
        val customUids = listOf("B9:4C:D8:1E", "F3:A0:29:C1", "5E:92:E4:70")
        val customNames = listOf("Office Secure Pass", "Apartment RFID Fob", "Campus Smart Card")
        val randomIdx = (customNames.indices).random()

        val scannedName = customNames[randomIdx]
        val scannedUid = customUids[randomIdx]

        _scannedCardInfo.value = "Flipped: $scannedName ($scannedUid)"
        
        // Add to cards if doesn't already exist
        if (_nfcCards.value.none { it.uid == scannedUid }) {
            val newCard = NfcCard(
                name = scannedName,
                type = "Keycard",
                uid = scannedUid,
                colorHex = listOf(0xFF8B5CF6, 0xFF3B82F6, 0xFFF59E0B).random()
            )
            _nfcCards.value = _nfcCards.value + newCard
        }

        setIsland(OriginIslandType.NFC_SCANNING, "Scanned: $scannedName 🎉")
    }

    fun cancelNfcScanning() {
        _nfcScanning.value = false
        setIsland(OriginIslandType.NORMAL, "NFC Ready")
    }

    fun startBroadcastingCard(card: NfcCard) {
        if (!_nfcEnabled.value) {
            setIsland(OriginIslandType.NORMAL, "Enable NFC first!")
            return
        }
        _broadcastingCard.value = card
        setIsland(OriginIslandType.NFC_BROADCASTING, "Broadcasting ${card.name} (${card.type})...")
    }

    fun stopBroadcastingNfc() {
        _broadcastingCard.value = null
        setIsland(OriginIslandType.NORMAL, "NFC Idle")
    }

    // Private Space Pin logic
    fun enterPinDigit(digit: Char) {
        if (_enteredPin.value.length < 4) {
            _enteredPin.value += digit
            if (_enteredPin.value.length == 4) {
                verifyPrivatePin()
            }
        }
    }

    fun backspacePin() {
        if (_enteredPin.value.isNotEmpty()) {
            _enteredPin.value = _enteredPin.value.dropLast(1)
        }
    }

    fun verifyPrivatePin() {
        if (_enteredPin.value == _privatePin.value) {
            _isPrivateSpaceUnlocked.value = true
            _showPinError.value = false
            setIsland(OriginIslandType.PRIVATE_SECURE, "Vault Unlocked Safely")
        } else {
            _showPinError.value = true
            _enteredPin.value = "" // Clear entry
            setIsland(OriginIslandType.NORMAL, "Wrong PIN. Decryption Failed.")
        }
    }

    fun lockPrivateSpace() {
        _isPrivateSpaceUnlocked.value = false
        _enteredPin.value = ""
        setIsland(OriginIslandType.NORMAL, "Private Space Locked")
    }

    fun addPrivateNote(title: String, content: String) {
        if (title.isBlank() || content.isBlank()) return
        val newNote = PrivateNote(
            title = title,
            content = content,
            timestamp = "2026-06-05"
        )
        _privateNotes.value = listOf(newNote) + _privateNotes.value
    }

    fun deletePrivateNote(id: String) {
        _privateNotes.value = _privateNotes.value.filter { it.id != id }
    }
}
