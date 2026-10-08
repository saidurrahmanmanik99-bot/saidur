package com.example.data.repository

import com.example.data.dao.MadrasaDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class MadrasaRepository(private val dao: MadrasaDao) {
    // Settings
    val settings: Flow<MadrasaSettings?> = dao.getSettings()
    suspend fun getSettingsOnce(): MadrasaSettings? = dao.getSettingsOnce()
    suspend fun saveSettings(settings: MadrasaSettings) = dao.insertOrUpdateSettings(settings)

    // Jamats
    val allJamats: Flow<List<Jamat>> = dao.getAllJamats()
    suspend fun insertJamat(jamat: Jamat) = dao.insertJamat(jamat)
    suspend fun updateJamat(jamat: Jamat) = dao.updateJamat(jamat)
    suspend fun deleteJamat(jamat: Jamat) = dao.deleteJamat(jamat)

    // Students
    val allStudents: Flow<List<Student>> = dao.getAllStudents()
    fun getStudentsByJamat(jamatId: Long): Flow<List<Student>> = dao.getStudentsByJamat(jamatId)
    suspend fun getStudentById(id: Long) = dao.getStudentById(id)
    fun searchStudents(query: String): Flow<List<Student>> = dao.searchStudents(query)
    val studentCount: Flow<Int> = dao.getStudentCount()
    suspend fun insertStudent(student: Student) = dao.insertStudent(student)
    suspend fun updateStudent(student: Student) = dao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = dao.deleteStudent(student)

    // Teachers
    val allTeachers: Flow<List<Teacher>> = dao.getAllTeachers()
    suspend fun getTeacherById(id: Long) = dao.getTeacherById(id)
    fun searchTeachers(query: String): Flow<List<Teacher>> = dao.searchTeachers(query)
    val teacherCount: Flow<Int> = dao.getTeacherCount()
    suspend fun insertTeacher(teacher: Teacher) = dao.insertTeacher(teacher)
    suspend fun updateTeacher(teacher: Teacher) = dao.updateTeacher(teacher)
    suspend fun deleteTeacher(teacher: Teacher) = dao.deleteTeacher(teacher)

    // Subjects
    val allSubjects: Flow<List<Subject>> = dao.getAllSubjects()
    fun getSubjectsByJamat(jamatId: Long): Flow<List<Subject>> = dao.getSubjectsByJamat(jamatId)
    suspend fun insertSubject(subject: Subject) = dao.insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = dao.updateSubject(subject)
    suspend fun deleteSubject(subject: Subject) = dao.deleteSubject(subject)

    // Exams
    val allExams: Flow<List<Exam>> = dao.getAllExams()
    suspend fun getExamById(id: Long) = dao.getExamById(id)
    suspend fun insertExam(exam: Exam) = dao.insertExam(exam)
    suspend fun updateExam(exam: Exam) = dao.updateExam(exam)
    suspend fun deleteExam(exam: Exam) = dao.deleteExam(exam)

    // Marks
    fun getMarksByExam(examId: Long): Flow<List<StudentMark>> = dao.getMarksByExam(examId)
    fun getMarksForStudent(examId: Long, studentId: Long): Flow<List<StudentMark>> = dao.getMarksForStudent(examId, studentId)
    suspend fun getMarksForStudentSync(examId: Long, studentId: Long) = dao.getMarksForStudentSync(examId, studentId)
    suspend fun saveMark(mark: StudentMark) = dao.insertOrUpdateMark(mark)
    suspend fun saveMarks(marks: List<StudentMark>) = dao.insertMarks(marks)
    suspend fun deleteMarksForStudent(examId: Long, studentId: Long) = dao.deleteMarksForStudent(examId, studentId)

    // Question Papers
    val allQuestionPapers: Flow<List<QuestionPaper>> = dao.getAllQuestionPapers()
    suspend fun getQuestionPaperById(id: Long) = dao.getQuestionPaperById(id)
    suspend fun insertQuestionPaper(paper: QuestionPaper) = dao.insertQuestionPaper(paper)
    suspend fun updateQuestionPaper(paper: QuestionPaper) = dao.updateQuestionPaper(paper)
    suspend fun deleteQuestionPaper(paper: QuestionPaper) = dao.deleteQuestionPaper(paper)

    // Backup & Restore
    suspend fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val settingsList = dao.getAllSettingsSync()
        if (settingsList.isNotEmpty()) {
            val s = settingsList[0]
            val sObj = JSONObject().apply {
                put("madrasaName", s.madrasaName)
                put("madrasaAddress", s.madrasaAddress)
                put("principalName", s.principalName)
                put("phone", s.phone)
                put("email", s.email)
                put("defaultPassMarks", s.defaultPassMarks)
                put("defaultFullMarks", s.defaultFullMarks)
            }
            root.put("settings", sObj)
        }

        // Jamats
        val jamatsArr = JSONArray()
        dao.getAllJamatsSync().forEach {
            jamatsArr.put(JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("section", it.section)
                put("orderIndex", it.orderIndex)
            })
        }
        root.put("jamats", jamatsArr)

        // Students
        val studentsArr = JSONArray()
        dao.getAllStudentsSync().forEach {
            studentsArr.put(JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("fatherName", it.fatherName)
                put("motherName", it.motherName)
                put("birthDate", it.birthDate)
                put("rollNo", it.rollNo)
                put("jamatId", it.jamatId)
                put("jamatName", it.jamatName)
                put("section", it.section)
                put("phone", it.phone)
                put("address", it.address)
                put("admissionDate", it.admissionDate)
            })
        }
        root.put("students", studentsArr)

        // Teachers
        val teachersArr = JSONArray()
        dao.getAllTeachersSync().forEach {
            teachersArr.put(JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("fatherName", it.fatherName)
                put("phone", it.phone)
                put("subject", it.subject)
                put("designation", it.designation)
                put("joiningDate", it.joiningDate)
                put("address", it.address)
            })
        }
        root.put("teachers", teachersArr)

        // Subjects
        val subjectsArr = JSONArray()
        dao.getAllSubjectsSync().forEach {
            subjectsArr.put(JSONObject().apply {
                put("id", it.id)
                put("jamatId", it.jamatId)
                put("name", it.name)
                put("fullMarks", it.fullMarks)
                put("passMarks", it.passMarks)
                put("code", it.code)
            })
        }
        root.put("subjects", subjectsArr)

        // Exams
        val examsArr = JSONArray()
        dao.getAllExamsSync().forEach {
            examsArr.put(JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("hijriYear", it.hijriYear)
                put("examDate", it.examDate)
                put("notes", it.notes)
            })
        }
        root.put("exams", examsArr)

        // Marks
        val marksArr = JSONArray()
        dao.getAllMarksSync().forEach {
            marksArr.put(JSONObject().apply {
                put("id", it.id)
                put("examId", it.examId)
                put("studentId", it.studentId)
                put("subjectId", it.subjectId)
                put("marksObtained", it.marksObtained)
                put("fullMarks", it.fullMarks)
                put("passMarks", it.passMarks)
            })
        }
        root.put("marks", marksArr)

        // Question Papers
        val qpArr = JSONArray()
        dao.getAllQuestionPapersSync().forEach {
            qpArr.put(JSONObject().apply {
                put("id", it.id)
                put("madrasaName", it.madrasaName)
                put("examName", it.examName)
                put("jamatName", it.jamatName)
                put("subjectName", it.subjectName)
                put("timeAllowed", it.timeAllowed)
                put("fullMarks", it.fullMarks)
                put("instructions", it.instructions)
                put("layoutColumns", it.layoutColumns)
                put("orientation", it.orientation)
                put("fontSizeSp", it.fontSizeSp)
                put("questionsJson", it.questionsJson)
            })
        }
        root.put("question_papers", qpArr)

        return root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)

            if (root.has("settings")) {
                val sObj = root.getJSONObject("settings")
                val s = MadrasaSettings(
                    id = 1,
                    madrasaName = sObj.optString("madrasaName", "দারুল উলুম মাদ্রাসা"),
                    madrasaAddress = sObj.optString("madrasaAddress", ""),
                    principalName = sObj.optString("principalName", ""),
                    phone = sObj.optString("phone", ""),
                    email = sObj.optString("email", ""),
                    defaultPassMarks = sObj.optInt("defaultPassMarks", 33),
                    defaultFullMarks = sObj.optInt("defaultFullMarks", 100)
                )
                dao.insertOrUpdateSettings(s)
            }

            if (root.has("jamats")) {
                val arr = root.getJSONArray("jamats")
                val list = mutableListOf<Jamat>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        Jamat(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            section = obj.optString("section", "ক"),
                            orderIndex = obj.optInt("orderIndex", 0)
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    dao.clearJamats()
                    dao.insertJamats(list)
                }
            }

            if (root.has("students")) {
                val arr = root.getJSONArray("students")
                val list = mutableListOf<Student>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        Student(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            fatherName = obj.optString("fatherName", ""),
                            motherName = obj.optString("motherName", ""),
                            birthDate = obj.optString("birthDate", ""),
                            rollNo = obj.optString("rollNo", "1"),
                            jamatId = obj.optLong("jamatId", 0),
                            jamatName = obj.optString("jamatName", ""),
                            section = obj.optString("section", "ক"),
                            phone = obj.optString("phone", ""),
                            address = obj.optString("address", ""),
                            admissionDate = obj.optString("admissionDate", "")
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    dao.clearStudents()
                    dao.insertStudents(list)
                }
            }

            if (root.has("teachers")) {
                val arr = root.getJSONArray("teachers")
                val list = mutableListOf<Teacher>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        Teacher(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            fatherName = obj.optString("fatherName", ""),
                            phone = obj.optString("phone", ""),
                            subject = obj.optString("subject", ""),
                            designation = obj.optString("designation", ""),
                            joiningDate = obj.optString("joiningDate", ""),
                            address = obj.optString("address", "")
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    dao.clearTeachers()
                    dao.insertTeachers(list)
                }
            }

            if (root.has("subjects")) {
                val arr = root.getJSONArray("subjects")
                val list = mutableListOf<Subject>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        Subject(
                            id = obj.optLong("id", 0),
                            jamatId = obj.optLong("jamatId", 0),
                            name = obj.getString("name"),
                            fullMarks = obj.optInt("fullMarks", 100),
                            passMarks = obj.optInt("passMarks", 33),
                            code = obj.optString("code", "")
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    dao.clearSubjects()
                    dao.insertSubjects(list)
                }
            }

            if (root.has("exams")) {
                val arr = root.getJSONArray("exams")
                val list = mutableListOf<Exam>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        Exam(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            hijriYear = obj.optString("hijriYear", ""),
                            examDate = obj.optString("examDate", ""),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    dao.clearExams()
                    dao.insertExams(list)
                }
            }

            if (root.has("marks")) {
                val arr = root.getJSONArray("marks")
                val list = mutableListOf<StudentMark>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        StudentMark(
                            id = obj.optLong("id", 0),
                            examId = obj.optLong("examId", 0),
                            studentId = obj.optLong("studentId", 0),
                            subjectId = obj.optLong("subjectId", 0),
                            marksObtained = obj.optDouble("marksObtained", 0.0),
                            fullMarks = obj.optDouble("fullMarks", 100.0),
                            passMarks = obj.optDouble("passMarks", 33.0)
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    dao.clearMarks()
                    dao.insertMarks(list)
                }
            }

            if (root.has("question_papers")) {
                val arr = root.getJSONArray("question_papers")
                val list = mutableListOf<QuestionPaper>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        QuestionPaper(
                            id = obj.optLong("id", 0),
                            madrasaName = obj.optString("madrasaName", ""),
                            examName = obj.optString("examName", ""),
                            jamatName = obj.optString("jamatName", ""),
                            subjectName = obj.optString("subjectName", ""),
                            timeAllowed = obj.optString("timeAllowed", ""),
                            fullMarks = obj.optInt("fullMarks", 100),
                            instructions = obj.optString("instructions", ""),
                            layoutColumns = obj.optInt("layoutColumns", 1),
                            orientation = obj.optString("orientation", "portrait"),
                            fontSizeSp = obj.optInt("fontSizeSp", 14),
                            questionsJson = obj.optString("questionsJson", "[]")
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    dao.clearQuestionPapers()
                    dao.insertQuestionPapers(list)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
