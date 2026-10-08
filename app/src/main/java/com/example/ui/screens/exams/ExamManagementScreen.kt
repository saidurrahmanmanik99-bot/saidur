package com.example.ui.screens.exams

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Exam
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.screens.MadrasaViewModel
import com.example.ui.screens.ScreenTab
import com.example.util.BanglaFormatHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamManagementScreen(
    viewModel: MadrasaViewModel,
    onNavigateToResults: () -> Unit
) {
    val exams by viewModel.exams.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingExam by remember { mutableStateOf<Exam?>(null) }
    var examToDelete by remember { mutableStateOf<Exam?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingExam = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_exam_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "নতুন পরীক্ষা যোগ")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "পরীক্ষা ব্যবস্থাপনা",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "সাময়িক, বার্ষিক ও মাসিক পরীক্ষা তৈরি ও সময়সূচী নির্ধারণ করুন",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (exams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "কোন পরীক্ষা তৈরি করা হয়নি। নিচে + বাটনে ক্লিক করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(exams, key = { it.id }) { exam ->
                        ExamCard(
                            exam = exam,
                            onResultClick = onNavigateToResults,
                            onEdit = {
                                editingExam = exam
                                showAddEditDialog = true
                            },
                            onDelete = { examToDelete = exam }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Exam Dialog
    if (showAddEditDialog) {
        ExamAddEditDialog(
            exam = editingExam,
            onDismiss = { showAddEditDialog = false },
            onSave = { updated ->
                if (editingExam == null) {
                    viewModel.addExam(updated)
                } else {
                    viewModel.updateExam(updated)
                }
                showAddEditDialog = false
            }
        )
    }

    // Confirm Delete Dialog
    examToDelete?.let { exam ->
        ConfirmDeleteDialog(
            title = "পরীক্ষা মুছে ফেলা",
            message = "আপনি কি নিশ্চিত যে '${exam.name}' মুছে ফেলতে চান?",
            onConfirm = {
                viewModel.deleteExam(exam)
                examToDelete = null
            },
            onDismiss = { examToDelete = null }
        )
    }
}

@Composable
fun ExamCard(
    exam: Exam,
    onResultClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exam_card_${exam.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exam.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "সাল/হিজরি: ${exam.hijriYear}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (exam.examDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "তারিখ: ${BanglaFormatHelper.toBangla(exam.examDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (exam.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "নির্দেশনা: ${exam.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onResultClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Grade, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("নম্বর এন্ট্রি ও ফলাফল দেখুন")
            }
        }
    }
}

@Composable
fun ExamAddEditDialog(
    exam: Exam?,
    onDismiss: () -> Unit,
    onSave: (Exam) -> Unit
) {
    var name by remember { mutableStateOf(exam?.name ?: "১ম সাময়িক পরীক্ষা") }
    var hijriYear by remember { mutableStateOf(exam?.hijriYear ?: "১৪৪৬ হিজরি / ২০২৪-২৫") }
    var examDate by remember { mutableStateOf(exam?.examDate ?: "") }
    var notes by remember { mutableStateOf(exam?.notes ?: "") }

    val presetExamNames = listOf("১ম সাময়িক পরীক্ষা", "২য় সাময়িক পরীক্ষা", "বার্ষিক পরীক্ষা", "মাসিক পরীক্ষা", "বিশেষ পরীক্ষা")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (exam == null) "নতুন পরীক্ষা তৈরি" else "পরীক্ষার তথ্য সম্পাদনা") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Preset chips
                Text("দ্রুত নাম নির্বাচন করুন:", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    presetExamNames.take(3).forEach { pName ->
                        FilterChip(
                            selected = name == pName,
                            onClick = { name = pName },
                            label = { Text(pName) }
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("পরীক্ষার নাম *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exam_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = hijriYear,
                    onValueChange = { hijriYear = it },
                    label = { Text("হিজরি / শিক্ষাবর্ষ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = examDate,
                    onValueChange = { examDate = it },
                    label = { Text("পরীক্ষার তারিখ (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("বিশেষ নির্দেশনা") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val updated = exam?.copy(
                            name = name.trim(),
                            hijriYear = hijriYear.trim(),
                            examDate = examDate.trim(),
                            notes = notes.trim()
                        ) ?: Exam(
                            name = name.trim(),
                            hijriYear = hijriYear.trim(),
                            examDate = examDate.trim(),
                            notes = notes.trim()
                        )
                        onSave(updated)
                    }
                },
                modifier = Modifier.testTag("save_exam_button")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
