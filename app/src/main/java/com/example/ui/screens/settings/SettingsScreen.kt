package com.example.ui.screens.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.MadrasaSettings
import com.example.ui.screens.MadrasaViewModel
import com.example.util.BanglaFormatHelper
import com.example.util.PrintPdfHelper
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun SettingsScreen(
    viewModel: MadrasaViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingsState by viewModel.settings.collectAsState()

    var madrasaName by remember(settingsState) { mutableStateOf(settingsState?.madrasaName ?: "দারুল উলুম মাদ্রাসা") }
    var madrasaAddress by remember(settingsState) { mutableStateOf(settingsState?.madrasaAddress ?: "ঢাকা, বাংলাদেশ") }
    var principalName by remember(settingsState) { mutableStateOf(settingsState?.principalName ?: "মুহতামিম সাহেব") }
    var phone by remember(settingsState) { mutableStateOf(settingsState?.phone ?: "") }
    var email by remember(settingsState) { mutableStateOf(settingsState?.email ?: "") }
    var defaultPassMarks by remember(settingsState) { mutableStateOf(settingsState?.defaultPassMarks?.toString() ?: "33") }
    var defaultFullMarks by remember(settingsState) { mutableStateOf(settingsState?.defaultFullMarks?.toString() ?: "100") }

    // Backup restore status message
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessStatus by remember { mutableStateOf(true) }

    // File picker launcher for Restore/Import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                    val jsonString = reader.readText()
                    reader.close()
                    val success = viewModel.importBackupJson(jsonString)
                    if (success) {
                        statusMessage = "ডাটা সফলভাবে রিস্টোর হয়েছে!"
                        isSuccessStatus = true
                        Toast.makeText(context, "ডাটা সফলভাবে রিস্টোর হয়েছে!", Toast.LENGTH_SHORT).show()
                    } else {
                        statusMessage = "ডাটা ফাইলটি সঠিক নয় বা ত্রুটি হয়েছে।"
                        isSuccessStatus = false
                    }
                } catch (e: Exception) {
                    statusMessage = "রিস্টোর ব্যর্থ হয়েছে: ${e.localizedMessage}"
                    isSuccessStatus = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = "মাদ্রাসা ও সিস্টেম সেটিংস",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "প্রতিষ্ঠানের তথ্য, প্রিন্ট সেটিংস এবং অফলাইন ডাটা ব্যাকআপ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Status banner if any
        statusMessage?.let { msg ->
            Surface(
                color = if (isSuccessStatus) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSuccessStatus) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isSuccessStatus) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = msg, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Madrasa Profile Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "মাদ্রাসার মৌলিক তথ্য",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = madrasaName,
                    onValueChange = { madrasaName = it },
                    label = { Text("মাদ্রাসার নাম *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_madrasa_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = madrasaAddress,
                    onValueChange = { madrasaAddress = it },
                    label = { Text("মাদ্রাসার ঠিকানা *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = principalName,
                    onValueChange = { principalName = it },
                    label = { Text("মুহতামিম / প্রিন্সিপালের নাম *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("মোবাইল নম্বর") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("ইমেইল") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = defaultFullMarks,
                        onValueChange = { defaultFullMarks = it },
                        label = { Text("ডিফল্ট পূর্ণমান") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = defaultPassMarks,
                        onValueChange = { defaultPassMarks = it },
                        label = { Text("ডিফল্ট পাস মার্ক") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Button(
                    onClick = {
                        val fullM = BanglaFormatHelper.parseBanglaInt(defaultFullMarks) ?: 100
                        val passM = BanglaFormatHelper.parseBanglaInt(defaultPassMarks) ?: 33
                        val updated = (settingsState ?: MadrasaSettings()).copy(
                            madrasaName = madrasaName.trim(),
                            madrasaAddress = madrasaAddress.trim(),
                            principalName = principalName.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            defaultFullMarks = fullM,
                            defaultPassMarks = passM
                        )
                        viewModel.saveSettings(updated)
                        Toast.makeText(context, "সেটিংস সফলভাবে সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_settings_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("তথ্য সংরক্ষণ করুন")
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Data Backup & Restore Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অফলাইন ডাটা ব্যাকআপ ও রিস্টোর",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "যেহেতু এই অ্যাপটি সম্পূর্ণ অফলাইন, আপনার মাদ্রাসার ছাত্র, শিক্ষক, পরীক্ষা ও ফলাফলের সকল ডাটা একটি ফাইলে ব্যাকআপ করে অন্য ফোনে সহজে ট্রান্সফার করতে পারবেন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Export / Backup Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val json = viewModel.exportBackupJson()
                                    val fileName = "Madrasa_Backup_${System.currentTimeMillis()}.json"
                                    PrintPdfHelper.shareTextFile(context, json, fileName)
                                    statusMessage = "ব্যাকআপ ফাইল তৈরি হয়েছে।"
                                    isSuccessStatus = true
                                } catch (e: Exception) {
                                    statusMessage = "ব্যাকআপ তৈরিতে ত্রুটি: ${e.localizedMessage}"
                                    isSuccessStatus = false
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("backup_data_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ব্যাকআপ নিন")
                    }

                    // Restore / Import Button
                    OutlinedButton(
                        onClick = {
                            importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("restore_data_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("রিস্টোর করুন")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
