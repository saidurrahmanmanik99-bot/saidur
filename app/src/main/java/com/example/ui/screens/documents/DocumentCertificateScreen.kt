package com.example.ui.screens.documents

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.example.data.model.Student
import com.example.ui.screens.MadrasaViewModel
import com.example.util.BanglaFormatHelper
import com.example.util.DocumentHtmlGenerator
import com.example.util.PrintPdfHelper

enum class DocumentType(val title: String, val code: String) {
    ID_CARD("ছাত্র পরিচয়পত্র (Student ID Card)", "id_card"),
    TESTIMONIAL("প্রশংসাপত্র (Testimonial)", "testimonial"),
    CHARACTER("প্রত্যয়নপত্র (Character Certificate)", "character"),
    TRANSFER("ছাড়পত্র / টিসি (Transfer Certificate)", "transfer")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentCertificateScreen(
    viewModel: MadrasaViewModel
) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var selectedDocType by remember { mutableStateOf(DocumentType.TESTIMONIAL) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var extraNote by remember { mutableStateOf("") }
    var studentSearch by remember { mutableStateOf("") }

    val filteredStudents = remember(students, studentSearch) {
        if (studentSearch.isBlank()) students
        else {
            val q = studentSearch.trim().lowercase()
            students.filter { it.name.lowercase().contains(q) || it.rollNo.contains(q) || it.jamatName.lowercase().contains(q) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("document_certificate_screen")
    ) {
        Text(
            text = "সনদপত্র ও প্রত্যয়ন জেনারেটর",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "ছাত্র পরিচয়পত্র, প্রশংসাপত্র, প্রত্যয়নপত্র ও ছাড়পত্র তৈরি ও সরাসরি A4 প্রিন্ট / PDF সংরক্ষণ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Document Type Selection Cards
        Text("১. সনদের ধরন নির্বাচন করুন:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            DocumentType.values().forEach { docType ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedDocType = docType },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedDocType == docType) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDocType == docType,
                            onClick = { selectedDocType = docType }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = docType.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedDocType == docType) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Student Selection
        Text("২. ছাত্র নির্বাচন করুন:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = studentSearch,
            onValueChange = { studentSearch = it },
            placeholder = { Text("নাম বা রোল দিয়ে ছাত্র খুঁজুন...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Student selector box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 140.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            if (filteredStudents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("কোন ছাত্র পাওয়া যায়নি", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredStudents, key = { it.id }) { student ->
                        val isSelected = selectedStudent?.id == student.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStudent = student },
                            color = if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${student.name} (রোল: ${BanglaFormatHelper.toBangla(student.rollNo)})",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = student.jamatName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Extra note input
        OutlinedTextField(
            value = extraNote,
            onValueChange = { extraNote = it },
            label = { Text("বিশেষ মন্তব্য / ছাড়পত্রের কারণ (ঐচ্ছিক)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Generate / Print Button
        Button(
            onClick = {
                val currentStudent = selectedStudent
                val currentSettings = settings
                if (currentStudent != null && currentSettings != null) {
                    val html = DocumentHtmlGenerator.generateCertificateHtml(
                        type = selectedDocType.code,
                        student = currentStudent,
                        settings = currentSettings,
                        extraNote = extraNote
                    )
                    PrintPdfHelper.printHtml(
                        context,
                        html,
                        "${selectedDocType.code}_${currentStudent.name}"
                    )
                }
            },
            enabled = selectedStudent != null && settings != null,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("print_document_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (selectedStudent != null)
                    "${selectedDocType.title.split(" ")[0]} প্রিন্ট / PDF সংরক্ষণ"
                else
                    "প্রথমে ছাত্র নির্বাচন করুন",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
