package com.example.ui.screens.students

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Jamat
import com.example.data.model.Student
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.screens.MadrasaViewModel
import com.example.util.BanglaFormatHelper
import com.example.util.DocumentHtmlGenerator
import com.example.util.PrintPdfHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagementScreen(
    viewModel: MadrasaViewModel
) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsState()
    val jamats by viewModel.jamats.collectAsState()
    val searchQuery by viewModel.studentSearchQuery.collectAsState()
    val selectedJamatId by viewModel.selectedJamatFilter.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }
    var studentForDetail by remember { mutableStateOf<Student?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingStudent = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_student_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "নতুন ছাত্র যোগ")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setStudentSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_search_input"),
                placeholder = { Text("নাম, রোল, জামাত বা মোবাইল দিয়ে খুঁজুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setStudentSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Jamat filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedJamatId == null,
                        onClick = { viewModel.setSelectedJamatFilter(null) },
                        label = { Text("সকল জামাত (${BanglaFormatHelper.toBangla(students.size)})") }
                    )
                }
                items(jamats) { jamat ->
                    FilterChip(
                        selected = selectedJamatId == jamat.id,
                        onClick = { viewModel.setSelectedJamatFilter(jamat.id) },
                        label = { Text(jamat.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Student List
            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "কোন ছাত্রের তথ্য নেই। নিচে + বাটনে ক্লিক করে ছাত্র যোগ করুন।" else "কোন ছাত্র পাওয়া যায়নি",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(students, key = { it.id }) { student ->
                        StudentCard(
                            student = student,
                            onClick = { studentForDetail = student },
                            onEdit = {
                                editingStudent = student
                                showAddEditDialog = true
                            },
                            onDelete = { studentToDelete = student },
                            onPrint = {
                                settings?.let { s ->
                                    val html = DocumentHtmlGenerator.generateCertificateHtml("id_card", student, s)
                                    PrintPdfHelper.printHtml(context, html, "Student_${student.name}_ID")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog
    studentForDetail?.let { student ->
        StudentDetailDialog(
            student = student,
            onDismiss = { studentForDetail = null },
            onPrintIdCard = {
                settings?.let { s ->
                    val html = DocumentHtmlGenerator.generateCertificateHtml("id_card", student, s)
                    PrintPdfHelper.printHtml(context, html, "Student_${student.name}_ID")
                }
            },
            onEdit = {
                studentForDetail = null
                editingStudent = student
                showAddEditDialog = true
            }
        )
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        StudentAddEditDialog(
            student = editingStudent,
            jamats = jamats,
            onDismiss = { showAddEditDialog = false },
            onSave = { updated ->
                if (editingStudent == null) {
                    viewModel.addStudent(updated)
                } else {
                    viewModel.updateStudent(updated)
                }
                showAddEditDialog = false
            }
        )
    }

    // Confirm Delete Dialog
    studentToDelete?.let { student ->
        ConfirmDeleteDialog(
            title = "ছাত্র মুছে ফেলা",
            message = "আপনি কি নিশ্চিত যে '${student.name}' (রোল: ${BanglaFormatHelper.toBangla(student.rollNo)}) কে মুছে ফেলতে চান?",
            onConfirm = {
                viewModel.deleteStudent(student)
                studentToDelete = null
            },
            onDismiss = { studentToDelete = null }
        )
    }
}

@Composable
fun StudentCard(
    student: Student,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPrint: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("student_card_${student.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar badge with Roll
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "রোল",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = BanglaFormatHelper.toBangla(student.rollNo),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "জামাত: ${student.jamatName} (শাখা: ${student.section})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (student.fatherName.isNotBlank()) {
                    Text(
                        text = "পিতা: ${student.fatherName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (student.phone.isNotBlank()) {
                    Text(
                        text = "ফোন: ${BanglaFormatHelper.toBangla(student.phone)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Actions
            IconButton(onClick = onPrint, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = "প্রিন্ট",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "সম্পাদনা",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "মুছে ফেলুন",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun StudentDetailDialog(
    student: Student,
    onDismiss: () -> Unit,
    onPrintIdCard: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ছাত্রের বিস্তারিত প্রোফাইল", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileField("ছাত্রের নাম", student.name)
                ProfileField("রোল নম্বর", BanglaFormatHelper.toBangla(student.rollNo))
                ProfileField("জামাত / শ্রেণি", student.jamatName)
                ProfileField("শাখা", student.section)
                ProfileField("পিতার নাম", student.fatherName)
                ProfileField("মাতার নাম", student.motherName)
                ProfileField("জন্ম তারিখ", BanglaFormatHelper.toBangla(student.birthDate))
                ProfileField("মোবাইল নম্বর", BanglaFormatHelper.toBangla(student.phone))
                ProfileField("ঠিকানা", student.address)
                ProfileField("ভর্তি তারিখ", BanglaFormatHelper.toBangla(student.admissionDate))
            }
        },
        confirmButton = {
            Button(
                onClick = onPrintIdCard,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("পরিচয়পত্র প্রিন্ট")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onEdit) {
                    Text("সম্পাদনা")
                }
                TextButton(onClick = onDismiss) {
                    Text("বন্ধ করুন")
                }
            }
        }
    )
}

@Composable
fun ProfileField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAddEditDialog(
    student: Student?,
    jamats: List<Jamat>,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var fatherName by remember { mutableStateOf(student?.fatherName ?: "") }
    var motherName by remember { mutableStateOf(student?.motherName ?: "") }
    var birthDate by remember { mutableStateOf(student?.birthDate ?: "") }
    var rollNo by remember { mutableStateOf(student?.rollNo ?: "") }
    var selectedJamat by remember { mutableStateOf(jamats.find { it.id == student?.jamatId } ?: jamats.firstOrNull()) }
    var section by remember { mutableStateOf(student?.section ?: "ক") }
    var phone by remember { mutableStateOf(student?.phone ?: "") }
    var address by remember { mutableStateOf(student?.address ?: "") }
    var admissionDate by remember { mutableStateOf(student?.admissionDate ?: "") }

    var expandedJamatDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (student == null) "নতুন ছাত্র যোগ" else "ছাত্রের তথ্য সম্পাদনা")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ছাত্রের নাম *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_name_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = rollNo,
                        onValueChange = { rollNo = it },
                        label = { Text("রোল নম্বর *") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("student_roll_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("শাখা") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Jamat selector
                ExposedDropdownMenuBox(
                    expanded = expandedJamatDropdown,
                    onExpandedChange = { expandedJamatDropdown = !expandedJamatDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedJamat?.name ?: "জামাত নির্বাচন করুন",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("জামাত / শ্রেণি *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJamatDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedJamatDropdown,
                        onDismissRequest = { expandedJamatDropdown = false }
                    ) {
                        jamats.forEach { jamat ->
                            DropdownMenuItem(
                                text = { Text(jamat.name) },
                                onClick = {
                                    selectedJamat = jamat
                                    expandedJamatDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("পিতার নাম") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = motherName,
                    onValueChange = { motherName = it },
                    label = { Text("মাতার নাম") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("জন্ম তারিখ (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = admissionDate,
                    onValueChange = { admissionDate = it },
                    label = { Text("ভর্তি তারিখ (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val currentJamat = selectedJamat
                        val jamatId = currentJamat?.id ?: 0L
                        val jamatName = currentJamat?.name ?: "অনির্দিষ্ট"
                        val updated = student?.copy(
                            name = name.trim(),
                            fatherName = fatherName.trim(),
                            motherName = motherName.trim(),
                            birthDate = birthDate.trim(),
                            rollNo = rollNo.trim().ifBlank { "1" },
                            jamatId = jamatId,
                            jamatName = jamatName,
                            section = section.trim().ifBlank { "ক" },
                            phone = phone.trim(),
                            address = address.trim(),
                            admissionDate = admissionDate.trim()
                        ) ?: Student(
                            name = name.trim(),
                            fatherName = fatherName.trim(),
                            motherName = motherName.trim(),
                            birthDate = birthDate.trim(),
                            rollNo = rollNo.trim().ifBlank { "1" },
                            jamatId = jamatId,
                            jamatName = jamatName,
                            section = section.trim().ifBlank { "ক" },
                            phone = phone.trim(),
                            address = address.trim(),
                            admissionDate = admissionDate.trim()
                        )
                        onSave(updated)
                    }
                },
                modifier = Modifier.testTag("save_student_button")
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
