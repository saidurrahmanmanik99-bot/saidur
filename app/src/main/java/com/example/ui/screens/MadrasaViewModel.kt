package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.MadrasaRepository
import com.example.ui.screens.results.StudentResultSummary
import com.example.ui.screens.results.SubjectResultDetail
import com.example.util.BanglaFormatHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String) {
    HOME("হোম"),
    STUDENTS("ছাত্র"),
    TEACHERS("শিক্ষক"),
    JAMATS("জামাত"),
    EXAMS("পরীক্ষা"),
    RESULTS("ফলাফল"),
    QUESTIONS("প্রশ্নপত্র"),
    DOCUMENTS("ডকুমেন্ট"),
    SETTINGS("সেটিংস")
}

class MadrasaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MadrasaRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MadrasaRepository(db.madrasaDao())
    }

    // Navigation state
    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    // Settings
    val settings: StateFlow<MadrasaSettings?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveSettings(settings: MadrasaSettings) {
        viewModelScope.launch { repository.saveSettings(settings) }
    }

    // Jamats
    val jamats: StateFlow<List<Jamat>> = repository.allJamats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addJamat(name: String, section: String = "ক") {
        viewModelScope.launch {
            repository.insertJamat(Jamat(name = name, section = section))
        }
    }

    fun updateJamat(jamat: Jamat) {
        viewModelScope.launch { repository.updateJamat(jamat) }
    }

    fun deleteJamat(jamat: Jamat) {
        viewModelScope.launch { repository.deleteJamat(jamat) }
    }

    // Students
    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    private val _selectedJamatFilter = MutableStateFlow<Long?>(null)
    val selectedJamatFilter: StateFlow<Long?> = _selectedJamatFilter.asStateFlow()

    fun setStudentSearchQuery(query: String) {
        _studentSearchQuery.value = query
    }

    fun setSelectedJamatFilter(jamatId: Long?) {
        _selectedJamatFilter.value = jamatId
    }

    val students: StateFlow<List<Student>> = combine(
        repository.allStudents,
        _studentSearchQuery,
        _selectedJamatFilter
    ) { list, query, jamatId ->
        var filtered = list
        if (jamatId != null) {
            filtered = filtered.filter { it.jamatId == jamatId }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(q) ||
                        it.rollNo.contains(q) ||
                        it.jamatName.lowercase().contains(q) ||
                        it.phone.contains(q)
            }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStudentCount: StateFlow<Int> = repository.studentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addStudent(student: Student) {
        viewModelScope.launch { repository.insertStudent(student) }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch { repository.updateStudent(student) }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch { repository.deleteStudent(student) }
    }

    // Teachers
    private val _teacherSearchQuery = MutableStateFlow("")
    val teacherSearchQuery: StateFlow<String> = _teacherSearchQuery.asStateFlow()

    fun setTeacherSearchQuery(query: String) {
        _teacherSearchQuery.value = query
    }

    val teachers: StateFlow<List<Teacher>> = combine(
        repository.allTeachers,
        _teacherSearchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else {
            val q = query.trim().lowercase()
            list.filter {
                it.name.lowercase().contains(q) ||
                        it.subject.lowercase().contains(q) ||
                        it.designation.lowercase().contains(q) ||
                        it.phone.contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTeacherCount: StateFlow<Int> = repository.teacherCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addTeacher(teacher: Teacher) {
        viewModelScope.launch { repository.insertTeacher(teacher) }
    }

    fun updateTeacher(teacher: Teacher) {
        viewModelScope.launch { repository.updateTeacher(teacher) }
    }

    fun deleteTeacher(teacher: Teacher) {
        viewModelScope.launch { repository.deleteTeacher(teacher) }
    }

    // Subjects
    val subjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSubject(subject: Subject) {
        viewModelScope.launch { repository.insertSubject(subject) }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch { repository.updateSubject(subject) }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch { repository.deleteSubject(subject) }
    }

    // Exams
    val exams: StateFlow<List<Exam>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExam(exam: Exam) {
        viewModelScope.launch { repository.insertExam(exam) }
    }

    fun updateExam(exam: Exam) {
        viewModelScope.launch { repository.updateExam(exam) }
    }

    fun deleteExam(exam: Exam) {
        viewModelScope.launch { repository.deleteExam(exam) }
    }

    // Marks Management
    fun getMarksForExam(examId: Long): Flow<List<StudentMark>> = repository.getMarksByExam(examId)

    fun saveStudentMark(mark: StudentMark) {
        viewModelScope.launch { repository.saveMark(mark) }
    }

    fun saveStudentMarks(marks: List<StudentMark>) {
        viewModelScope.launch { repository.saveMarks(marks) }
    }

    // Result Summaries Calculation
    fun calculateResultsForJamat(
        examId: Long,
        jamatId: Long,
        studentsInJamat: List<Student>,
        subjectsInJamat: List<Subject>,
        allMarksInExam: List<StudentMark>
    ): List<StudentResultSummary> {
        val marksByStudent = allMarksInExam.filter { it.examId == examId }
            .groupBy { it.studentId }

        val list = studentsInJamat.map { student ->
            val studentMarks = marksByStudent[student.id] ?: emptyList()
            var totalFull = 0.0
            var totalObtained = 0.0
            var allPassed = true

            val subjectDetails = subjectsInJamat.map { subject ->
                val markEntry = studentMarks.find { it.subjectId == subject.id }
                val obtained = markEntry?.marksObtained ?: 0.0
                val full = if (markEntry != null && markEntry.fullMarks > 0) markEntry.fullMarks else subject.fullMarks.toDouble()
                val pass = if (markEntry != null && markEntry.passMarks > 0) markEntry.passMarks else subject.passMarks.toDouble()

                val isPass = obtained >= pass
                if (!isPass) allPassed = false

                val pct = if (full > 0) (obtained / full) * 100.0 else 0.0
                val grade = BanglaFormatHelper.calculateGrade(pct, (pass / full) * 100.0)

                totalFull += full
                totalObtained += obtained

                SubjectResultDetail(
                    subject = subject,
                    fullMarks = full,
                    passMarks = pass,
                    obtainedMarks = obtained,
                    isPass = isPass,
                    grade = grade
                )
            }

            val overallPercentage = if (totalFull > 0) (totalObtained / totalFull) * 100.0 else 0.0
            val overallGrade = if (subjectsInJamat.isNotEmpty()) {
                if (!allPassed) "ফেল" else BanglaFormatHelper.calculateGrade(overallPercentage)
            } else "—"

            val isPassed = allPassed && subjectsInJamat.isNotEmpty()

            StudentResultSummary(
                student = student,
                subjectDetails = subjectDetails,
                totalFullMarks = totalFull,
                totalObtainedMarks = totalObtained,
                percentage = overallPercentage,
                overallGrade = overallGrade,
                isPassed = isPassed
            )
        }

        // Calculate merit order (rank)
        // Order by isPassed desc, totalObtainedMarks desc
        val sorted = list.sortedWith(
            compareByDescending<StudentResultSummary> { it.isPassed }
                .thenByDescending { it.totalObtainedMarks }
        )

        sorted.forEachIndexed { index, item ->
            item.rank = index + 1
        }

        return sorted
    }

    // Question Papers
    val questionPapers: StateFlow<List<QuestionPaper>> = repository.allQuestionPapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addQuestionPaper(paper: QuestionPaper) {
        viewModelScope.launch { repository.insertQuestionPaper(paper) }
    }

    fun updateQuestionPaper(paper: QuestionPaper) {
        viewModelScope.launch { repository.updateQuestionPaper(paper) }
    }

    fun deleteQuestionPaper(paper: QuestionPaper) {
        viewModelScope.launch { repository.deleteQuestionPaper(paper) }
    }

    // Backup & Restore
    suspend fun exportBackupJson(): String = repository.exportBackupJson()
    suspend fun importBackupJson(json: String): Boolean = repository.importBackupJson(json)
}
