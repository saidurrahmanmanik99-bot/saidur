package com.example.ui.screens.jamats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.example.data.model.Jamat
import com.example.data.model.Subject
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.screens.MadrasaViewModel
import com.example.util.BanglaFormatHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JamatManagementScreen(
    viewModel: MadrasaViewModel
) {
    val jamats by viewModel.jamats.collectAsState()
    val allStudents by viewModel.students.collectAsState()
    val allSubjects by viewModel.subjects.collectAsState()

    var showAddJamatDialog by remember { mutableStateOf(false) }
    var editingJamat by remember { mutableStateOf<Jamat?>(null) }
    var jamatToDelete by remember { mutableStateOf<Jamat?>(null) }

    // Selected jamat for managing its subjects & student list
    var selectedJamatForDetails by remember { mutableStateOf<Jamat?>(null) }

    // Subject Add/Edit
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }
    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingJamat = null
                    showAddJamatDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_jamat_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "নতুন জামাত যোগ")
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
                text = "জামাত ও শ্রেণি ব্যবস্থাপনা",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "প্রতিটি জামাতের বিষয়, পূর্ণমান এবং ছাত্র সংখ্যা পরিচালনা করুন",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (jamats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "কোন জামাত যোগ করা হয়নি। নিচে + বাটনে ক্লিক করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(jamats, key = { it.id }) { jamat ->
                        val studentCount = allStudents.count { it.jamatId == jamat.id }
                        val subjectCount = allSubjects.count { it.jamatId == jamat.id }

                        JamatCard(
                            jamat = jamat,
                            studentCount = studentCount,
                            subjectCount = subjectCount,
                            onClick = { selectedJamatForDetails = jamat },
                            onEdit = {
                                editingJamat = jamat
                                showAddJamatDialog = true
                            },
                            onDelete = { jamatToDelete = jamat }
                        )
                    }
                }
            }
        }
    }

    // Jamat Details Sheet / Dialog (Subjects + Students in Jamat)
    selectedJamatForDetails?.let { jamat ->
        val subjectsInJamat = allSubjects.filter { it.jamatId == jamat.id }
        val studentsInJamat = allStudents.filter { it.jamatId == jamat.id }

        AlertDialog(
            onDismissRequest = { selectedJamatForDetails = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${jamat.name} (শাখা: ${jamat.section})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Subjects Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বিষয়সমূহ (${BanglaFormatHelper.toBangla(subjectsInJamat.size)} টি)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FilledTonalButton(
                            onClick = {
                                editingSubject = null
                                showAddSubjectDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("বিষয় যোগ", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (subjectsInJamat.isEmpty()) {
                        Text(
                            text = "কোন বিষয় যোগ করা হয়নি। উপরের বাটনে ক্লিক করে বিষয় যোগ করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp)
                        ) {
                            subjectsInJamat.forEach { subject ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = subject.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "পূর্ণমান: ${BanglaFormatHelper.toBangla(subject.fullMarks)} | পাস: ${BanglaFormatHelper.toBangla(subject.passMarks)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    editingSubject = subject
                                                    showAddSubjectDialog = true
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = { subjectToDelete = subject },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    // Students in Jamat
                    Text(
                        text = "এই জামাতের ছাত্র তালিকা (${BanglaFormatHelper.toBangla(studentsInJamat.size)} জন)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (studentsInJamat.isEmpty()) {
                        Text(
                            text = "এই জামাতে এখনো কোন ছাত্র ভর্তি করা হয়নি।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp)
                        ) {
                            studentsInJamat.take(10).forEach { st ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${BanglaFormatHelper.toBangla(st.rollNo)}. ${st.name}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = BanglaFormatHelper.toBangla(st.phone),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedJamatForDetails = null }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // Add / Edit Jamat Dialog
    if (showAddJamatDialog) {
        var name by remember { mutableStateOf(editingJamat?.name ?: "") }
        var section by remember { mutableStateOf(editingJamat?.section ?: "ক") }

        AlertDialog(
            onDismissRequest = { showAddJamatDialog = false },
            title = { Text(if (editingJamat == null) "নতুন জামাত যোগ" else "জামাতের নাম পরিবর্তন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("জামাতের নাম * (যেমন: হিফজুল কুরআন)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("jamat_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("শাখা (যেমন: ক, খ)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            if (editingJamat == null) {
                                viewModel.addJamat(name.trim(), section.trim().ifBlank { "ক" })
                            } else {
                                viewModel.updateJamat(editingJamat!!.copy(name = name.trim(), section = section.trim().ifBlank { "ক" }))
                            }
                            showAddJamatDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_jamat_button")
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddJamatDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Add / Edit Subject Dialog
    if (showAddSubjectDialog && selectedJamatForDetails != null) {
        val currentJamat = selectedJamatForDetails!!
        var subjectName by remember { mutableStateOf(editingSubject?.name ?: "") }
        var fullMarksText by remember { mutableStateOf(editingSubject?.fullMarks?.toString() ?: "100") }
        var passMarksText by remember { mutableStateOf(editingSubject?.passMarks?.toString() ?: "33") }

        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text(if (editingSubject == null) "নতুন বিষয় যোগ" else "বিষয় সম্পাদনা") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("বিষয়ের নাম * (যেমন: কুরআন মাজীদ)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subject_name_input"),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fullMarksText,
                            onValueChange = { fullMarksText = it },
                            label = { Text("পূর্ণমান") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = passMarksText,
                            onValueChange = { passMarksText = it },
                            label = { Text("পাস মার্ক") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subjectName.isNotBlank()) {
                            val fMarks = BanglaFormatHelper.parseBanglaInt(fullMarksText) ?: 100
                            val pMarks = BanglaFormatHelper.parseBanglaInt(passMarksText) ?: 33
                            if (editingSubject == null) {
                                viewModel.addSubject(
                                    Subject(
                                        jamatId = currentJamat.id,
                                        name = subjectName.trim(),
                                        fullMarks = fMarks,
                                        passMarks = pMarks
                                    )
                                )
                            } else {
                                viewModel.updateSubject(
                                    editingSubject!!.copy(
                                        name = subjectName.trim(),
                                        fullMarks = fMarks,
                                        passMarks = pMarks
                                    )
                                )
                            }
                            showAddSubjectDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_subject_button")
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Confirm Delete Jamat Dialog
    jamatToDelete?.let { jamat ->
        ConfirmDeleteDialog(
            title = "জামাত মুছে ফেলা",
            message = "আপনি কি নিশ্চিত যে '${jamat.name}' জামাত মুছে ফেলতে চান?",
            onConfirm = {
                viewModel.deleteJamat(jamat)
                jamatToDelete = null
                if (selectedJamatForDetails?.id == jamat.id) {
                    selectedJamatForDetails = null
                }
            },
            onDismiss = { jamatToDelete = null }
        )
    }

    // Confirm Delete Subject Dialog
    subjectToDelete?.let { subject ->
        ConfirmDeleteDialog(
            title = "বিষয় মুছে ফেলা",
            message = "আপনি কি নিশ্চিত যে '${subject.name}' বিষয়টি মুছে ফেলতে চান?",
            onConfirm = {
                viewModel.deleteSubject(subject)
                subjectToDelete = null
            },
            onDismiss = { subjectToDelete = null }
        )
    }
}

@Composable
fun JamatCard(
    jamat: Jamat,
    studentCount: Int,
    subjectCount: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("jamat_card_${jamat.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Class,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = jamat.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "শাখা: ${jamat.section}  |  ছাত্র: ${BanglaFormatHelper.toBangla(studentCount)} জন  |  বিষয়: ${BanglaFormatHelper.toBangla(subjectCount)} টি",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
