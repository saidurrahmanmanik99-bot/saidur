package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MadrasaDao {
    // Settings
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<MadrasaSettings?>

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): MadrasaSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: MadrasaSettings)

    // Jamats
    @Query("SELECT * FROM jamats ORDER BY orderIndex ASC, id ASC")
    fun getAllJamats(): Flow<List<Jamat>>

    @Query("SELECT * FROM jamats WHERE id = :id LIMIT 1")
    suspend fun getJamatById(id: Long): Jamat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJamat(jamat: Jamat): Long

    @Update
    suspend fun updateJamat(jamat: Jamat)

    @Delete
    suspend fun deleteJamat(jamat: Jamat)

    // Students
    @Query("SELECT * FROM students ORDER BY jamatId ASC, CAST(rollNo AS INTEGER) ASC, id ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE jamatId = :jamatId ORDER BY CAST(rollNo AS INTEGER) ASC, id ASC")
    fun getStudentsByJamat(jamatId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR rollNo LIKE '%' || :query || '%' OR jamatName LIKE '%' || :query || '%'")
    fun searchStudents(query: String): Flow<List<Student>>

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    // Teachers
    @Query("SELECT * FROM teachers ORDER BY id ASC")
    fun getAllTeachers(): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers WHERE id = :id LIMIT 1")
    suspend fun getTeacherById(id: Long): Teacher?

    @Query("SELECT * FROM teachers WHERE name LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%' OR designation LIKE '%' || :query || '%'")
    fun searchTeachers(query: String): Flow<List<Teacher>>

    @Query("SELECT COUNT(*) FROM teachers")
    fun getTeacherCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher): Long

    @Update
    suspend fun updateTeacher(teacher: Teacher)

    @Delete
    suspend fun deleteTeacher(teacher: Teacher)

    // Subjects
    @Query("SELECT * FROM subjects WHERE jamatId = :jamatId ORDER BY id ASC")
    fun getSubjectsByJamat(jamatId: Long): Flow<List<Subject>>

    @Query("SELECT * FROM subjects ORDER BY jamatId ASC, id ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    // Exams
    @Query("SELECT * FROM exams ORDER BY id DESC")
    fun getAllExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    suspend fun getExamById(id: Long): Exam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Update
    suspend fun updateExam(exam: Exam)

    @Delete
    suspend fun deleteExam(exam: Exam)

    // Marks
    @Query("SELECT * FROM marks WHERE examId = :examId")
    fun getMarksByExam(examId: Long): Flow<List<StudentMark>>

    @Query("SELECT * FROM marks WHERE examId = :examId AND studentId = :studentId")
    fun getMarksForStudent(examId: Long, studentId: Long): Flow<List<StudentMark>>

    @Query("SELECT * FROM marks WHERE examId = :examId AND studentId = :studentId")
    suspend fun getMarksForStudentSync(examId: Long, studentId: Long): List<StudentMark>

    @Query("SELECT * FROM marks WHERE examId = :examId AND subjectId = :subjectId")
    fun getMarksForSubject(examId: Long, subjectId: Long): Flow<List<StudentMark>>

    @Query("SELECT * FROM marks WHERE examId = :examId AND studentId = :studentId AND subjectId = :subjectId LIMIT 1")
    suspend fun getMark(examId: Long, studentId: Long, subjectId: Long): StudentMark?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMark(mark: StudentMark)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarks(marks: List<StudentMark>)

    @Query("DELETE FROM marks WHERE examId = :examId AND studentId = :studentId")
    suspend fun deleteMarksForStudent(examId: Long, studentId: Long)

    // Question Papers
    @Query("SELECT * FROM question_papers ORDER BY createdAt DESC")
    fun getAllQuestionPapers(): Flow<List<QuestionPaper>>

    @Query("SELECT * FROM question_papers WHERE id = :id LIMIT 1")
    suspend fun getQuestionPaperById(id: Long): QuestionPaper?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionPaper(paper: QuestionPaper): Long

    @Update
    suspend fun updateQuestionPaper(paper: QuestionPaper)

    @Delete
    suspend fun deleteQuestionPaper(paper: QuestionPaper)

    // Backup & Restore helpers (query all raw lists)
    @Query("SELECT * FROM settings")
    suspend fun getAllSettingsSync(): List<MadrasaSettings>

    @Query("SELECT * FROM jamats")
    suspend fun getAllJamatsSync(): List<Jamat>

    @Query("SELECT * FROM students")
    suspend fun getAllStudentsSync(): List<Student>

    @Query("SELECT * FROM teachers")
    suspend fun getAllTeachersSync(): List<Teacher>

    @Query("SELECT * FROM subjects")
    suspend fun getAllSubjectsSync(): List<Subject>

    @Query("SELECT * FROM exams")
    suspend fun getAllExamsSync(): List<Exam>

    @Query("SELECT * FROM marks")
    suspend fun getAllMarksSync(): List<StudentMark>

    @Query("SELECT * FROM question_papers")
    suspend fun getAllQuestionPapersSync(): List<QuestionPaper>

    // Bulk inserts for restore
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJamats(jamats: List<Jamat>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<Teacher>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<Exam>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionPapers(papers: List<QuestionPaper>)

    @Query("DELETE FROM jamats")
    suspend fun clearJamats()

    @Query("DELETE FROM students")
    suspend fun clearStudents()

    @Query("DELETE FROM teachers")
    suspend fun clearTeachers()

    @Query("DELETE FROM subjects")
    suspend fun clearSubjects()

    @Query("DELETE FROM exams")
    suspend fun clearExams()

    @Query("DELETE FROM marks")
    suspend fun clearMarks()

    @Query("DELETE FROM question_papers")
    suspend fun clearQuestionPapers()
}
