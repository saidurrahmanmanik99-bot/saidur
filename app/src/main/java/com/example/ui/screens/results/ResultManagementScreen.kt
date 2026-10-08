package com.example.ui.screens.results

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import com.example.data.model.Exam
import com.example.data.model.Jamat
import com.example.data.model.Student
import com.example.data.model.StudentMark
import com.example.data.model.Subject
import com.example.ui.screens.MadrasaViewModel
import com.example.util.BanglaFormatHelper
import com.example.util.DocumentHtmlGenerator
import com.example.util.PrintPdfHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultManagementScreen(
    viewModel: MadrasaViewModel
) {
    val context = LocalContext.current
    val exams by viewModel.exams.collectAsState()
    val jamats by viewModel.jamats.collectAsState()
    val allStudents by viewModel.students.collectAsState()
    val allSubjects by viewModel.subjects.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var selectedExam by remember { mutableStateOf<Exam?>(null) }
    var selectedJamat by remember { mutableStateOf<Jamat?>(null) }

    // Auto-select first exam and jamat if available
    LaunchedEffect(exams) {
        if (selectedExam == null && exams.isNotEmpty()) {
            selectedExam = exams.first()
        }
    }
    LaunchedEffect(jamats) {
        if (selectedJamat == null && jamats.isNotEmpty()) {
            selectedJamat = jamats.first()
        }
    }

    val currentExam = selectedExam
    val currentJamat = selectedJamat

    // Observe marks for selected exam
    val marksList by produceState<List<StudentMark>>(initialValue = emptyList(), key1 = currentExam?.id) {
        if (currentExam != null) {
            viewModel.getMarksForExam(currentExam.id).collect { value = it }
        } else {
            value = emptyList()
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: জামাত ফলাফল ও মেধা, 1: নম্বর এন্ট্রি

    // Modal state for Single Student Marks Entry
    var studentForMarkEntry by remember { mutableStateOf<Student?>(null) }
    // Modal state for Single Student Result Sheet Preview
    var studentForResultPreview by remember { mutableStateOf<StudentResultSummary?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("result_management_screen")
    ) {
        // Exam & Jamat Pickers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Exam Selector
            var expandedExam by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedExam,
                onExpandedChange = { expandedExam = !expandedExam },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = currentExam?.name ?: "পরীক্ষা নির্বাচন",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("পরীক্ষা") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedExam) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedExam,
                    onDismissRequest = { expandedExam = false }
                ) {
                    exams.forEach { ex ->
                        DropdownMenuItem(
                            text = { Text(ex.name) },
                            onClick = {
                                selectedExam = ex
                                expandedExam = false
                            }
                        )
                    }
                }
            }

            // Jamat Selector
            var expandedJamat by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedJamat,
                onExpandedChange = { expandedJamat = !expandedJamat },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = currentJamat?.name ?: "জামাত নির্বাচন",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("জামাত") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJamat) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedJamat,
                    onDismissRequest = { expandedJamat = false }
                ) {
                    jamats.forEach { j ->
                        DropdownMenuItem(
                            text = { Text(j.name) },
                            onClick = {
                                selectedJamat = j
                                expandedJamat = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs: 0 -> সামগ্রিক ফলাফল ও মেধা, 1 -> নম্বর এন্ট্রি
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("ফলাফল ও মেধাক্রম") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("নম্বর এন্ট্রি (Marks)") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (currentExam == null || currentJamat == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("পরীক্ষা ও জামাত নির্বাচন করুন", color = Color.Gray)
            }
        } else {
            val studentsInJamat = allStudents.filter { it.jamatId == currentJamat.id }
            val subjectsInJamat = allSubjects.filter { it.jamatId == currentJamat.id }

            if (subjectsInJamat.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "সতর্কতা: '${currentJamat.name}' জামাতে কোন বিষয় তৈরি করা হয়নি! জামাত মেনু থেকে বিষয় ও পূর্ণমান যোগ করুন।",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            val resultSummaries = remember(currentExam.id, currentJamat.id, studentsInJamat, subjectsInJamat, marksList) {
                viewModel.calculateResultsForJamat(
                    examId = currentExam.id,
                    jamatId = currentJamat.id,
                    studentsInJamat = studentsInJamat,
                    subjectsInJamat = subjectsInJamat,
                    allMarksInExam = marksList
                )
            }

            if (selectedTab == 0) {
                // Tab 0: Results & Merit
                ResultsOverviewView(
                    exam = currentExam,
                    jamat = currentJamat,
                    summaries = resultSummaries,
                    subjects = subjectsInJamat,
                    onViewIndividual = { summary -> studentForResultPreview = summary },
                    onPrintJamatSheet = {
                        settings?.let { s ->
                            val html = DocumentHtmlGenerator.generateJamatResultHtml(
                                settings = s,
                                exam = currentExam,
                                jamat = currentJamat,
                                summaries = resultSummaries,
                                subjects = subjectsInJamat
                            )
                            PrintPdfHelper.printHtml(context, html, "JamatResult_${currentJamat.name}")
                        }
                    }
                )
            } else {
                // Tab 1: Marks Entry
                MarksEntryView(
                    students = studentsInJamat,
                    subjects = subjectsInJamat,
                    marksList = marksList,
                    onEnterMarksForStudent = { st -> studentForMarkEntry = st }
                )
            }
        }
    }

    // Modal: Marks Entry For Student
    studentForMarkEntry?.let { student ->
        if (currentExam != null && currentJamat != null) {
            val subjectsInJamat = allSubjects.filter { it.jamatId == currentJamat.id }
            val studentMarks = marksList.filter { it.studentId == student.id && it.examId == currentExam.id }

            StudentMarksEntryDialog(
                exam = currentExam,
                student = student,
                subjects = subjectsInJamat,
                existingMarks = studentMarks,
                onDismiss = { studentForMarkEntry = null },
                onSave = { updatedMarks ->
                    viewModel.saveStudentMarks(updatedMarks)
                    studentForMarkEntry = null
                }
            )
        }
    }

    // Modal: Individual Result Sheet Preview & Print
    studentForResultPreview?.let { summary ->
        if (currentExam != null && currentJamat != null) {
            IndividualResultDialog(
                exam = currentExam,
                summary = summary,
                totalStudents = allStudents.count { it.jamatId == currentJamat.id },
                onDismiss = { studentForResultPreview = null },
                onPrint = {
                    settings?.let { s ->
                        val html = DocumentHtmlGenerator.generateSingleResultHtml(
                            settings = s,
                            exam = currentExam,
                            summary = summary,
                            totalInJamat = allStudents.count { it.jamatId == currentJamat.id }
                        )
                        PrintPdfHelper.printHtml(context, html, "Result_${summary.student.name}")
                    }
                }
            )
        }
    }
}

@Composable
fun ResultsOverviewView(
    exam: Exam,
    jamat: Jamat,
    summaries: List<StudentResultSummary>,
    subjects: List<Subject>,
    onViewIndividual: (StudentResultSummary) -> Unit,
    onPrintJamatSheet: () -> Unit
) {
    val passedCount = summaries.count { it.isPassed }
    val failedCount = summaries.count { !it.isPassed }
    val highest = summaries.maxOfOrNull { it.totalObtainedMarks } ?: 0.0
    val lowest = summaries.minOfOrNull { it.totalObtainedMarks } ?: 0.0

    Column(modifier = Modifier.fillMaxSize()) {
        // Summary Stats Row
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(label = "মোট ছাত্র", value = BanglaFormatHelper.toBangla(summaries.size))
                StatItem(label = "পাস", value = BanglaFormatHelper.toBangla(passedCount), color = Color(0xFF2E7D32))
                StatItem(label = "ফেল", value = BanglaFormatHelper.toBangla(failedCount), color = Color(0xFFC62828))
                StatItem(label = "সর্বোচ্চ", value = BanglaFormatHelper.toBangla(highest))
                StatItem(label = "সর্বনিম্ন", value = BanglaFormatHelper.toBangla(lowest))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Print Jamat Sheet Action
        Button(
            onClick = onPrintJamatSheet,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("জামাতের পূর্ণাঙ্গ ফলাফল শীট প্রিন্ট / PDF (A4)")
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (summaries.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("এই জামাতে কোন ছাত্র নেই।", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(summaries, key = { it.student.id }) { summary ->
                    StudentResultCard(
                        summary = summary,
                        onClick = { onViewIndividual(summary) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color = Color.Unspecified) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun StudentResultCard(
    summary: StudentResultSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("result_card_${summary.student.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (summary.isPassed) MaterialTheme.colorScheme.primary else Color(0xFFC62828),
                modifier = Modifier.size(46.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "মেধা",
                        fontSize = 9.sp,
                        color = Color.White
                    )
                    Text(
                        text = BanglaFormatHelper.toBangla(summary.rank),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${BanglaFormatHelper.toBangla(summary.student.rollNo)}. ${summary.student.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "প্রাপ্ত: ${BanglaFormatHelper.toBangla(summary.totalObtainedMarks)} / ${BanglaFormatHelper.toBangla(summary.totalFullMarks)}  |  ${BanglaFormatHelper.toBangla(summary.percentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "গ্রেড: ${summary.overallGrade}  |  ফলাফল: ${if (summary.isPassed) "উত্তীর্ণ (পাস)" else "অনুত্তীর্ণ (ফেল)"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (summary.isPassed) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "বিস্তারিত",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun MarksEntryView(
    students: List<Student>,
    subjects: List<Subject>,
    marksList: List<StudentMark>,
    onEnterMarksForStudent: (Student) -> Unit
) {
    if (students.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("এই জামাতে কোন ছাত্র নেই।", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(students, key = { it.id }) { student ->
                val enteredCount = marksList.count { it.studentId == student.id && it.marksObtained > 0 }
                val isComplete = subjects.isNotEmpty() && enteredCount >= subjects.size

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEnterMarksForStudent(student) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "রোল ${BanglaFormatHelper.toBangla(student.rollNo)}: ${student.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "নম্বর এন্ট্রি: ${BanglaFormatHelper.toBangla(enteredCount)} / ${BanglaFormatHelper.toBangla(subjects.size)} বিষয়",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isComplete) Color(0xFF2E7D32) else Color(0xFFC67D00)
                            )
                        }

                        Button(
                            onClick = { onEnterMarksForStudent(student) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isComplete) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isComplete) "সম্পাদনা" else "নম্বর দিন", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentMarksEntryDialog(
    exam: Exam,
    student: Student,
    subjects: List<Subject>,
    existingMarks: List<StudentMark>,
    onDismiss: () -> Unit,
    onSave: (List<StudentMark>) -> Unit
) {
    // Map of subjectId to obtained mark string
    val marksInputState = remember {
        mutableStateMapOf<Long, String>().apply {
            subjects.forEach { sub ->
                val m = existingMarks.find { it.subjectId == sub.id }
                if (m != null) {
                    put(sub.id, BanglaFormatHelper.toBangla(m.marksObtained))
                } else {
                    put(sub.id, "")
                }
            }
        }
    }

    // Dynamic Live Calculations
    var totalFull by remember { mutableDoubleStateOf(0.0) }
    var totalObtained by remember { mutableDoubleStateOf(0.0) }
    var isPassed by remember { mutableStateOf(true) }

    LaunchedEffect(marksInputState.values.toList()) {
        var fullAcc = 0.0
        var obtAcc = 0.0
        var passAcc = true

        subjects.forEach { sub ->
            val full = sub.fullMarks.toDouble()
            val pass = sub.passMarks.toDouble()
            val entered = BanglaFormatHelper.parseBanglaDouble(marksInputState[sub.id] ?: "") ?: 0.0

            fullAcc += full
            obtAcc += entered
            if (entered < pass) {
                passAcc = false
            }
        }

        totalFull = fullAcc
        totalObtained = obtAcc
        isPassed = passAcc && subjects.isNotEmpty()
    }

    val percentage = if (totalFull > 0) (totalObtained / totalFull) * 100.0 else 0.0
    val grade = BanglaFormatHelper.calculateGrade(percentage)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "নম্বর এন্ট্রি - ${student.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "রোল: ${BanglaFormatHelper.toBangla(student.rollNo)} | জামাত: ${student.jamatName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Live calculation preview box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("মোট প্রাপ্ত", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "${BanglaFormatHelper.toBangla(totalObtained)} / ${BanglaFormatHelper.toBangla(totalFull)}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("শতাংশ", style = MaterialTheme.typography.labelSmall)
                            Text("${BanglaFormatHelper.toBangla(percentage)}%", fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("গ্রেড", style = MaterialTheme.typography.labelSmall)
                            Text(grade, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ফলাফল", style = MaterialTheme.typography.labelSmall)
                            Text(
                                if (isPassed) "পাস" else "ফেল",
                                fontWeight = FontWeight.Bold,
                                color = if (isPassed) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                subjects.forEach { subject ->
                    val entered = marksInputState[subject.id] ?: ""
                    val num = BanglaFormatHelper.parseBanglaDouble(entered) ?: 0.0
                    val subPassed = num >= subject.passMarks

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = subject.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "পূর্ণমান: ${BanglaFormatHelper.toBangla(subject.fullMarks)} (পাস: ${BanglaFormatHelper.toBangla(subject.passMarks)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                if (entered.isNotBlank()) {
                                    Text(
                                        text = if (subPassed) "উত্তীর্ণ (পাস)" else "অনুত্তীর্ণ (ফেল)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (subPassed) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = entered,
                                onValueChange = { marksInputState[subject.id] = it },
                                modifier = Modifier
                                    .width(90.dp)
                                    .testTag("mark_input_${subject.id}"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = { Text("নম্বর") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val list = subjects.mapNotNull { sub ->
                        val text = marksInputState[sub.id] ?: ""
                        val num = BanglaFormatHelper.parseBanglaDouble(text) ?: 0.0
                        StudentMark(
                            examId = exam.id,
                            studentId = student.id,
                            subjectId = sub.id,
                            marksObtained = num,
                            fullMarks = sub.fullMarks.toDouble(),
                            passMarks = sub.passMarks.toDouble()
                        )
                    }
                    onSave(list)
                },
                modifier = Modifier.testTag("save_marks_button")
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

@Composable
fun IndividualResultDialog(
    exam: Exam,
    summary: StudentResultSummary,
    totalStudents: Int,
    onDismiss: () -> Unit,
    onPrint: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("ব্যক্তিগত ফলাফল পত্র", fontWeight = FontWeight.Bold)
                Text(
                    "${summary.student.name} (রোল: ${BanglaFormatHelper.toBangla(summary.student.rollNo)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Merit summary card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("মেধাক্রম (র‍্যাংক):", fontWeight = FontWeight.Bold)
                            Text(
                                "${BanglaFormatHelper.toBangla(summary.rank)} (মোট ${BanglaFormatHelper.toBangla(totalStudents)} জনের মধ্যে)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("মোট প্রাপ্ত নম্বর:", fontWeight = FontWeight.Bold)
                            Text(
                                "${BanglaFormatHelper.toBangla(summary.totalObtainedMarks)} / ${BanglaFormatHelper.toBangla(summary.totalFullMarks)} (${BanglaFormatHelper.toBangla(summary.percentage)}%)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("চূড়ান্ত ফলাফল:", fontWeight = FontWeight.Bold)
                            Text(
                                if (summary.isPassed) "উত্তীর্ণ (পাস)" else "অনুত্তীর্ণ (ফেল)",
                                fontWeight = FontWeight.Bold,
                                color = if (summary.isPassed) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text("বিষয়ভিত্তিক নম্বর:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                summary.subjectDetails.forEach { sub ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(sub.subject.name, fontWeight = FontWeight.Medium)
                                Text("পূর্ণমান: ${BanglaFormatHelper.toBangla(sub.fullMarks)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${BanglaFormatHelper.toBangla(sub.obtainedMarks)}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (sub.isPass) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                                Text(
                                    text = "${sub.grade} | ${if (sub.isPass) "পাস" else "ফেল"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (sub.isPass) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPrint,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("A4 মার্কশিট প্রিন্ট / PDF")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}
