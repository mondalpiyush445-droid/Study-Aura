package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.AppSettingsEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.components.CgpaCalculatorDialog

@Composable
fun ProfileSettingsScreen(
    userProfile: UserProfileEntity?,
    appSettings: AppSettingsEntity?,
    onUpdateProfile: (name: String, dept: String, uniId: String, currentGpa: Double, targetGpa: Double) -> Unit,
    onUpdateAvatar: (String?) -> Unit = {},
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleBiometricLock: (Boolean, String) -> Unit,
    onLockAppNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nameInput by remember(userProfile) { mutableStateOf(userProfile?.fullName ?: "Alex Scholar") }
    var deptInput by remember(userProfile) { mutableStateOf(userProfile?.department ?: "Computer Science") }
    var idInput by remember(userProfile) { mutableStateOf(userProfile?.universityId ?: "CS-2024-8891") }
    var currentGpaInput by remember(userProfile) { mutableStateOf((userProfile?.currentGpa ?: 3.88).toString()) }
    var targetGpaInput by remember(userProfile) { mutableStateOf((userProfile?.targetGpa ?: 3.95).toString()) }

    var pinCodeInput by remember(appSettings) { mutableStateOf(appSettings?.pinCode ?: "1234") }
    var saveStatusMsg by remember { mutableStateOf<String?>(null) }
    var showCgpaCalculator by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onUpdateAvatar(it.toString()) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profile & App Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Profile Identity Card with Upload Personal Photo
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { photoPicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userProfile?.avatarUri.isNullOrBlank()) {
                            AsyncImage(
                                model = userProfile?.avatarUri,
                                contentDescription = "Personal Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Upload Badge Overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Upload Photo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = nameInput,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$deptInput • ID: $idInput",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap avatar photo to upload personal picture",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deptInput,
                    onValueChange = { deptInput = it },
                    label = { Text("Department / Major") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_dept_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = idInput,
                    onValueChange = { idInput = it },
                    label = { Text("University ID Number") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_id_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentGpaInput,
                        onValueChange = { currentGpaInput = it },
                        label = { Text("Current GPA") },
                        modifier = Modifier.weight(1f).testTag("profile_gpa_input")
                    )
                    OutlinedTextField(
                        value = targetGpaInput,
                        onValueChange = { targetGpaInput = it },
                        label = { Text("Target GPA") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showCgpaCalculator = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Calculate, contentDescription = "CGPA Calculator")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open CGPA Calculator Tool")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val cGpa = currentGpaInput.toDoubleOrNull() ?: 3.88
                        val tGpa = targetGpaInput.toDoubleOrNull() ?: 3.95
                        onUpdateProfile(nameInput, deptInput, idInput, cGpa, tGpa)
                        saveStatusMsg = "Profile successfully updated offline!"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_profile_btn")
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Profile")
                }

                saveStatusMsg?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Appearance & Preferences
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = "Dark Mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dark Mode Option",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Reduce eye strain during evening study sessions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = appSettings?.isDarkMode ?: false,
                        onCheckedChange = { onToggleDarkMode(it) },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        // Security & Biometric Lock
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Biometric Lock",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Biometric Security Lock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Lock confidential academic records behind fingerprint / face or PIN.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = appSettings?.isBiometricEnabled ?: false,
                        onCheckedChange = { enabled ->
                            onToggleBiometricLock(enabled, pinCodeInput)
                        },
                        modifier = Modifier.testTag("biometric_lock_switch")
                    )
                }

                if (appSettings?.isBiometricEnabled == true) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pinCodeInput,
                        onValueChange = { pinCodeInput = it },
                        label = { Text("Fallback Passcode PIN (Default: 1234)") },
                        modifier = Modifier.fillMaxWidth().testTag("pin_code_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onLockAppNow,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("lock_app_now_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lock Application Now")
                    }
                }
            }
        }

        // Local Storage & Privacy Mandate Badge
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "Local Storage",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Local Storage & Privacy Guaranteed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• All student records, credentials, notes, and exam schedules are strictly persisted in an encrypted local Room SQLite database.\n" +
                            "• Zero external servers or social media integrations used.\n" +
                            "• Lightweight, fast, and fully accessible offline for maximum privacy and performance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showCgpaCalculator) {
        CgpaCalculatorDialog(
            initialCgpa = userProfile?.currentGpa ?: 3.88,
            onDismiss = { showCgpaCalculator = false },
            onApplyToProfile = { newCgpa ->
                currentGpaInput = newCgpa.toString()
                val tGpa = targetGpaInput.toDoubleOrNull() ?: 3.95
                onUpdateProfile(nameInput, deptInput, idInput, newCgpa, tGpa)
                saveStatusMsg = "Calculated CGPA ($newCgpa) saved to profile!"
            }
        )
    }
}
