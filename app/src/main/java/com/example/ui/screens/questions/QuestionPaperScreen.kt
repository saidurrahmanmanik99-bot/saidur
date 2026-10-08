package com.example.ui.screens.questions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.model.QuestionPaper
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.screens.MadrasaViewModel
import com.example.util.BanglaFormatHelper
import com.example.util.DocumentHtmlGenerator
import com.example.util.PrintPdfHelper
import org.json.JSONArray
import org.json.JSONObject

data class QuestionItem(
    var number: String,
    var text: String,
    var marks: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionPaperScreen(
    viewModel: MadrasaViewModel
) {
    val context = LocalContext.current
    val papers by viewModel.questionPapers.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val jamats by viewModel.jamats.collectAsState()

    var showCreateEditDialog by remember { mutableStateOf(false) }
    var editingPaper by remember { mutableStateOf<QuestionPaper?>(null) }
    var paperToDelete by remember { mutableStateOf<QuestionPaper?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingPaper = null
                    showCreateEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_question_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "নতুন প্রশ্নপত্র তৈরি")
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
                text = "পরীক্ষার প্রশ্নপত্র জেনারেটর",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "বাংলা, আরবি ও উর্দু ভাষায় এক বা দুই কলাম বিশিষ্ট A4 প্রশ্নপত্র তৈরি ও প্রিন্ট করুন",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (papers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "কোন প্রশ্নপত্র তৈরি করা হয়নি। নিচে + বাটনে ক্লিক করে তৈরি করুন।",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(papers, key = { it.id }) { paper ->
                        QuestionPaperCard(
                            paper = paper,
                            onPrint = {
                                settings?.let { s ->
                                    val html = DocumentHtmlGenerator.generateQuestionPaperHtml(paper, s)
                                    PrintPdfHelper.printHtml(context, html, "Question_${paper.examName}_${paper.subjectName}")
                                }
                            },
                            onEdit = {
                                editingPaper = paper
                                showCreateEditDialog = true
                            },
                            onDelete = { paperToDelete = paper }
                        )
                    }
                }
            }
        }
    }

    // Create / Edit Dialog
    if (showCreateEditDialog) {
        QuestionPaperEditorDialog(
            paper = editingPaper,
            defaultMadrasaName = settings?.madrasaName ?: "",
            jamats = jamats.map { it.name },
            onDismiss = { showCreateEditDialog = false },
            onSave = { updated ->
                if (editingPaper == null) {
                    viewModel.addQuestionPaper(updated)
                } else {
                    viewModel.updateQuestionPaper(updated)
                }
                showCreateEditDialog = false
            }
        )
    }

    // Confirm Delete
    paperToDelete?.let { paper ->
        ConfirmDeleteDialog(
            title = "প্রশ্নপত্র মুছে ফেলা",
            message = "আপনি কি নিশ্চিত যে '${paper.examName} - ${paper.subjectName}' প্রশ্নপত্রটি মুছে ফেলতে চান?",
            onConfirm = {
                viewModel.deleteQuestionPaper(paper)
                paperToDelete = null
            },
            onDismiss = { paperToDelete = null }
        )
    }
}

@Composable
fun QuestionPaperCard(
    paper: QuestionPaper,
    onPrint: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card_${paper.id}"),
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
                        text = "${paper.examName} - ${paper.subjectName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "জামাত: ${paper.jamatName}  |  সময়: ${paper.timeAllowed}  |  পূর্ণমান: ${BanglaFormatHelper.toBangla(paper.fullMarks)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "লেআউট: ${if (paper.layoutColumns == 2) "২ কলাম" else "১ কলাম"} (${if (paper.orientation == "landscape") "ল্যান্ডস্কেপ" else "পোর্ট্রেট"})",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
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

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onPrint,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("A4 প্রশ্নপত্র প্রিন্ট / PDF সংরক্ষণ")
            }
        }
    }
}

@Composable
fun QuestionPaperEditorDialog(
    paper: QuestionPaper?,
    defaultMadrasaName: String,
    jamats: List<String>,
    onDismiss: () -> Unit,
    onSave: (QuestionPaper) -> Unit
) {
    var madrasaName by remember { mutableStateOf(paper?.madrasaName ?: defaultMadrasaName) }
    var examName by remember { mutableStateOf(paper?.examName ?: "১ম সাময়িক পরীক্ষা - ১৪৪৬ হি.") }
    var jamatName by remember { mutableStateOf(paper?.jamatName ?: (jamats.firstOrNull() ?: "নূরানী")) }
    var subjectName by remember { mutableStateOf(paper?.subjectName ?: "কুরআন মাজীদ ও তাজবীদ") }
    var timeAllowed by remember { mutableStateOf(paper?.timeAllowed ?: "২ ঘন্টা ৩০ মিনিট") }
    var fullMarksText by remember { mutableStateOf(paper?.fullMarks?.toString() ?: "100") }
    var instructions by remember {
        mutableStateOf(
            paper?.instructions ?: "সকল প্রশ্নের উত্তর দেওয়া আবশ্যক। ডান পাশের সংখ্যা প্রশ্নের পূর্ণমান জ্ঞাপক।"
        )
    }
    var layoutColumns by remember { mutableIntStateOf(paper?.layoutColumns ?: 1) }
    var orientation by remember { mutableStateOf(paper?.orientation ?: "portrait") }
    var fontSizeSp by remember { mutableIntStateOf(paper?.fontSizeSp ?: 14) }

    // Questions list
    val questions = remember {
        mutableStateListOf<QuestionItem>().apply {
            if (paper != null && paper.questionsJson.isNotBlank()) {
                try {
                    val arr = JSONArray(paper.questionsJson)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        add(
                            QuestionItem(
                                number = obj.optString("number", "${i + 1}।"),
                                text = obj.optString("text", ""),
                                marks = obj.optString("marks", "")
                            )
                        )
                    }
                } catch (e: Exception) {
                    add(QuestionItem("১।", "", "১০"))
                }
            } else {
                add(QuestionItem("১।", "প্রশ্ন লিখুন (বাংলা / Arabic / Urdu)...", "১০"))
                add(QuestionItem("২।", "", "১০"))
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (paper == null) "নতুন প্রশ্নপত্র তৈরি" else "প্রশ্নপত্র সম্পাদনা", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = examName,
                    onValueChange = { examName = it },
                    label = { Text("পরীক্ষার নাম *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = jamatName,
                        onValueChange = { jamatName = it },
                        label = { Text("জামাত *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("বিষয় *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = timeAllowed,
                        onValueChange = { timeAllowed = it },
                        label = { Text("সময়") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fullMarksText,
                        onValueChange = { fullMarksText = it },
                        label = { Text("পূর্ণমান") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("পরীক্ষার্থীদের জন্য নির্দেশনা") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                // Layout Options: Columns & Orientation
                Text("লেআউট ও প্রিন্ট সেটিংস:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = layoutColumns == 1,
                        onClick = { layoutColumns = 1 },
                        label = { Text("১ কলাম") }
                    )
                    FilterChip(
                        selected = layoutColumns == 2,
                        onClick = { layoutColumns = 2 },
                        label = { Text("২ কলাম") }
                    )
                    FilterChip(
                        selected = orientation == "portrait",
                        onClick = { orientation = "portrait" },
                        label = { Text("পোর্ট্রেট") }
                    )
                    FilterChip(
                        selected = orientation == "landscape",
                        onClick = { orientation = "landscape" },
                        label = { Text("ল্যান্ডস্কেপ") }
                    )
                }

                // Font size slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ফন্ট সাইজ: ${fontSizeSp}sp", style = MaterialTheme.typography.bodySmall)
                    Row {
                        IconButton(onClick = { if (fontSizeSp > 10) fontSizeSp-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        IconButton(onClick = { if (fontSizeSp < 22) fontSizeSp++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Questions Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("প্রশ্নসমূহ (${questions.size} টি):", fontWeight = FontWeight.Bold)
                    TextButton(
                        onClick = {
                            val nextNum = "${BanglaFormatHelper.toBangla(questions.size + 1)}।"
                            questions.add(QuestionItem(nextNum, "", "১০"))
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("প্রশ্ন যোগ")
                    }
                }

                questions.forEachIndexed { index, q ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = q.number,
                                    onValueChange = { q.number = it },
                                    label = { Text("নম্বর") },
                                    modifier = Modifier.width(65.dp),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = q.marks,
                                    onValueChange = { q.marks = it },
                                    label = { Text("মান") },
                                    modifier = Modifier.width(65.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { if (questions.size > 1) questions.removeAt(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = q.text,
                                onValueChange = { q.text = it },
                                placeholder = { Text("প্রশ্ন লিখুন... (বাংলা, العربية, اردو)") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (examName.isNotBlank() && subjectName.isNotBlank()) {
                        val arr = JSONArray()
                        questions.forEach { q ->
                            val obj = JSONObject().apply {
                                put("number", q.number)
                                put("text", q.text)
                                put("marks", q.marks)
                            }
                            arr.put(obj)
                        }

                        val fMarks = BanglaFormatHelper.parseBanglaInt(fullMarksText) ?: 100
                        val updated = paper?.copy(
                            madrasaName = madrasaName.trim(),
                            examName = examName.trim(),
                            jamatName = jamatName.trim(),
                            subjectName = subjectName.trim(),
                            timeAllowed = timeAllowed.trim(),
                            fullMarks = fMarks,
                            instructions = instructions.trim(),
                            layoutColumns = layoutColumns,
                            orientation = orientation,
                            fontSizeSp = fontSizeSp,
                            questionsJson = arr.toString()
                        ) ?: QuestionPaper(
                            madrasaName = madrasaName.trim(),
                            examName = examName.trim(),
                            jamatName = jamatName.trim(),
                            subjectName = subjectName.trim(),
                            timeAllowed = timeAllowed.trim(),
                            fullMarks = fMarks,
                            instructions = instructions.trim(),
                            layoutColumns = layoutColumns,
                            orientation = orientation,
                            fontSizeSp = fontSizeSp,
                            questionsJson = arr.toString()
                        )
                        onSave(updated)
                    }
                },
                modifier = Modifier.testTag("save_question_paper_button")
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
