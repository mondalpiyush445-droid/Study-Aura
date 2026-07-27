package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.CertificateEntity
import com.example.ui.components.CertificateCard

@Composable
fun CertificatesScreen(
    certificates: List<CertificateEntity>,
    onAddCertificate: (title: String, issuer: String, dateMillis: Long, credId: String, category: String, skills: String) -> Unit,
    onDeleteCertificate: (CertificateEntity) -> Unit,
    onAttachImage: (CertificateEntity, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Professional Credentials & Certificates",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Verified student certifications, course honors, and uploaded credential photos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (certificates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No certificates stored yet. Tap '+' to record your credentials.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(certificates) { cert ->
                        CertificateCard(
                            certificate = cert,
                            onDelete = { onDeleteCertificate(cert) },
                            onAttachImage = { imageUri -> onAttachImage(cert, imageUri) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_certificate_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Credential")
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var issuer by remember { mutableStateOf("") }
        var credId by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Professional") }
        var skills by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Certificate Credential") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Certificate / Honor Title") },
                        modifier = Modifier.fillMaxWidth().testTag("add_cert_title_input")
                    )
                    OutlinedTextField(
                        value = issuer,
                        onValueChange = { issuer = it },
                        label = { Text("Issuing Organization / University") },
                        modifier = Modifier.fillMaxWidth().testTag("add_cert_issuer_input")
                    )
                    OutlinedTextField(
                        value = credId,
                        onValueChange = { credId = it },
                        label = { Text("Credential ID / License Number") },
                        modifier = Modifier.fillMaxWidth().testTag("add_cert_id_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = category == "Professional",
                            onClick = { category = "Professional" },
                            label = { Text("Professional") }
                        )
                        FilterChip(
                            selected = category == "Academic",
                            onClick = { category = "Academic" },
                            label = { Text("Academic") }
                        )
                        FilterChip(
                            selected = category == "Workshop",
                            onClick = { category = "Workshop" },
                            label = { Text("Workshop") }
                        )
                    }
                    OutlinedTextField(
                        value = skills,
                        onValueChange = { skills = it },
                        label = { Text("Skills / Keywords (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (title.isNotBlank() && issuer.isNotBlank()) {
                            onAddCertificate(
                                title,
                                issuer,
                                System.currentTimeMillis(),
                                if (credId.isBlank()) "CRED-" + (1000..9999).random() else credId,
                                category,
                                skills
                            )
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_add_cert_btn")
                ) {
                    Text("Save Credential")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
