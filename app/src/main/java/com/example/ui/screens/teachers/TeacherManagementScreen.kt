package com.example.ui.screens.teachers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Teacher
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.screens.MadrasaViewModel
import com.example.util.BanglaFormatHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherManagementScreen(
    viewModel: MadrasaViewModel
) {
    val teachers by viewModel.teachers.collectAsState()
    val searchQuery by viewModel.teacherSearchQuery.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingTeacher by remember { mutableStateOf<Teacher?>(null) }
    var teacherToDelete by remember { mutableStateOf<Teacher?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTeacher = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_teacher_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "নতুন শিক্ষক যোগ")
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
                text = "শিক্ষক ও কর্মকর্তা ব্যবস্থাপনা",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setTeacherSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_search_input"),
                placeholder = { Text("শিক্ষকের নাম, বিষয় বা পদবী দিয়ে খুঁজুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setTeacherSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (teachers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "কোন শিক্ষক যোগ করা হয়নি। নিচে + বাটনে ক্লিক করুন।" else "কোন শিক্ষক পাওয়া যায়নি",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(teachers, key = { it.id }) { teacher ->
                        TeacherCard(
                            teacher = teacher,
                            onEdit = {
                                editingTeacher = teacher
                                showAddEditDialog = true
                            },
                            onDelete = { teacherToDelete = teacher }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        TeacherAddEditDialog(
            teacher = editingTeacher,
            onDismiss = { showAddEditDialog = false },
            onSave = { updated ->
                if (editingTeacher == null) {
                    viewModel.addTeacher(updated)
                } else {
                    viewModel.updateTeacher(updated)
                }
                showAddEditDialog = false
            }
        )
    }

    // Confirm Delete Dialog
    teacherToDelete?.let { teacher ->
        ConfirmDeleteDialog(
            title = "শিক্ষক মুছে ফেলা",
            message = "আপনি কি নিশ্চিত যে '${teacher.name}' কে মুছে ফেলতে চান?",
            onConfirm = {
                viewModel.deleteTeacher(teacher)
                teacherToDelete = null
            },
            onDismiss = { teacherToDelete = null }
        )
    }
}

@Composable
fun TeacherCard(
    teacher: Teacher,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("teacher_card_${teacher.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = teacher.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (teacher.designation.isNotBlank()) {
                    Text(
                        text = teacher.designation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (teacher.subject.isNotBlank()) {
                    Text(
                        text = "পাঠদানের বিষয়: ${teacher.subject}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (teacher.phone.isNotBlank()) {
                    Text(
                        text = "মোবাইল: ${BanglaFormatHelper.toBangla(teacher.phone)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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

@Composable
fun TeacherAddEditDialog(
    teacher: Teacher?,
    onDismiss: () -> Unit,
    onSave: (Teacher) -> Unit
) {
    var name by remember { mutableStateOf(teacher?.name ?: "") }
    var fatherName by remember { mutableStateOf(teacher?.fatherName ?: "") }
    var phone by remember { mutableStateOf(teacher?.phone ?: "") }
    var subject by remember { mutableStateOf(teacher?.subject ?: "") }
    var designation by remember { mutableStateOf(teacher?.designation ?: "সহকারী শিক্ষক") }
    var joiningDate by remember { mutableStateOf(teacher?.joiningDate ?: "") }
    var address by remember { mutableStateOf(teacher?.address ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (teacher == null) "নতুন শিক্ষক যোগ" else "শিক্ষকের তথ্য সম্পাদনা") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("শিক্ষকের নাম *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("teacher_name_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("দায়িত্ব / পদবী (যেমন: মুহাদ্দিস, হিফজ শিক্ষক)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("পাঠদানের বিষয় (যেমন: কুরআন মাজীদ, হাদিস)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("পিতার নাম") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = joiningDate,
                    onValueChange = { joiningDate = it },
                    label = { Text("যোগদানের তারিখ (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val updated = teacher?.copy(
                            name = name.trim(),
                            fatherName = fatherName.trim(),
                            phone = phone.trim(),
                            subject = subject.trim(),
                            designation = designation.trim(),
                            joiningDate = joiningDate.trim(),
                            address = address.trim()
                        ) ?: Teacher(
                            name = name.trim(),
                            fatherName = fatherName.trim(),
                            phone = phone.trim(),
                            subject = subject.trim(),
                            designation = designation.trim(),
                            joiningDate = joiningDate.trim(),
                            address = address.trim()
                        )
                        onSave(updated)
                    }
                },
                modifier = Modifier.testTag("save_teacher_button")
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
