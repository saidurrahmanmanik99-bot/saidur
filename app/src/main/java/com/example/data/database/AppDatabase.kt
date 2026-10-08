package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.MadrasaDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MadrasaSettings::class,
        Jamat::class,
        Student::class,
        Teacher::class,
        Subject::class,
        Exam::class,
        StudentMark::class,
        QuestionPaper::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun madrasaDao(): MadrasaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "madrasa_offline.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default initial data for smooth first run
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).madrasaDao()
                                seedInitialData(dao)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(dao: MadrasaDao) {
            // Default Settings
            dao.insertOrUpdateSettings(
                MadrasaSettings(
                    id = 1,
                    madrasaName = "দারুল উলুম ইসলামিয়া মাদ্রাসা",
                    madrasaAddress = "মিরপুর, ঢাকা - ১২১৬",
                    principalName = "মুফতী মুহাম্মদ আব্দুল্লাহ কাসেমী",
                    phone = "০১৭০০-০০০০০০",
                    email = "info@darululoom.edu.bd",
                    defaultPassMarks = 33,
                    defaultFullMarks = 100
                )
            )

            // Default Jamats
            val j1 = dao.insertJamat(Jamat(name = "নূরানী", section = "ক", orderIndex = 1))
            val j2 = dao.insertJamat(Jamat(name = "নাজেরা", section = "ক", orderIndex = 2))
            val j3 = dao.insertJamat(Jamat(name = "হিফজুল কুরআন", section = "ক", orderIndex = 3))
            val j4 = dao.insertJamat(Jamat(name = "ইবতিদায়ী (১ম শ্রেণি)", section = "ক", orderIndex = 4))
            val j5 = dao.insertJamat(Jamat(name = "মিজান (৩য় শ্রেণি)", section = "ক", orderIndex = 5))

            // Default Subjects for Jamats
            dao.insertSubject(Subject(jamatId = j1, name = "কায়দা ও আমপারা", fullMarks = 100, passMarks = 33))
            dao.insertSubject(Subject(jamatId = j1, name = "দীনিয়াত ও দোয়া", fullMarks = 100, passMarks = 33))
            dao.insertSubject(Subject(jamatId = j1, name = "বাংলা ও অংক", fullMarks = 100, passMarks = 33))

            dao.insertSubject(Subject(jamatId = j2, name = "কুরআন তেলাওয়াত (নাজেরা)", fullMarks = 100, passMarks = 33))
            dao.insertSubject(Subject(jamatId = j2, name = "তাজবীদ ও মাসায়েল", fullMarks = 100, passMarks = 33))
            dao.insertSubject(Subject(jamatId = j2, name = "সাধারণ বিষয়", fullMarks = 100, passMarks = 33))

            dao.insertSubject(Subject(jamatId = j4, name = "কুরআন মাজীদ ও তাজবীদ", fullMarks = 100, passMarks = 33))
            dao.insertSubject(Subject(jamatId = j4, name = "আকাইদ ও ফিকাহ", fullMarks = 100, passMarks = 33))
            dao.insertSubject(Subject(jamatId = j4, name = "আরবি ব্যাকরণ", fullMarks = 100, passMarks = 33))
            dao.insertSubject(Subject(jamatId = j4, name = "বাংলা ও সাহিত্য", fullMarks = 100, passMarks = 33))
            dao.insertSubject(Subject(jamatId = j4, name = "গণিত", fullMarks = 100, passMarks = 33))

            // Sample Students
            val s1 = dao.insertStudent(
                Student(
                    name = "আব্দুর রহমান",
                    fatherName = "মুহাম্মদ রফিকুল ইসলাম",
                    motherName = "ফাতেমা বেগম",
                    birthDate = "১২/০৫/২০১৪",
                    rollNo = "০১",
                    jamatId = j4,
                    jamatName = "ইবতিদায়ী (১ম শ্রেণি)",
                    section = "ক",
                    phone = "০১৮১১-১১২২৩৩",
                    address = "মিরপুর-১০, ঢাকা",
                    admissionDate = "০১/০১/২০২৪"
                )
            )
            val s2 = dao.insertStudent(
                Student(
                    name = "মুহাম্মাদ হাসিবুর রহমান",
                    fatherName = "মাওলানা আব্দুল হাই",
                    motherName = "মরিয়ম আক্তার",
                    birthDate = "০৫/০৮/২০১৩",
                    rollNo = "০২",
                    jamatId = j4,
                    jamatName = "ইবতিদায়ী (১ম শ্রেণি)",
                    section = "ক",
                    phone = "০১৯২২-৩৩৪৪৫৫",
                    address = "পল্লবী, মিরপুর, ঢাকা",
                    admissionDate = "০১/০১/২০২৪"
                )
            )
            val s3 = dao.insertStudent(
                Student(
                    name = "মুহাম্মাদ সালমান ফারসী",
                    fatherName = "জসিম উদ্দিন",
                    motherName = "রাবেয়া বসরী",
                    birthDate = "২০/১১/২০১৫",
                    rollNo = "০১",
                    jamatId = j1,
                    jamatName = "নূরানী",
                    section = "ক",
                    phone = "০১৭৩৩-৪৫৫৫৬৬",
                    address = "কাফরুল, ঢাকা",
                    admissionDate = "১৫/০১/২০২৪"
                )
            )

            // Sample Teachers
            dao.insertTeacher(
                Teacher(
                    name = "মাওলানা আহমাদুল্লাহ",
                    fatherName = "কারী আব্দুল বাতেন",
                    phone = "০১৭৭৭-৮৮৮৯৯৯",
                    subject = "কুরআন মাজীদ ও তাজবীদ",
                    designation = "সিনিয়র শিক্ষক (মুহাদ্দিস)",
                    joiningDate = "০১/০৩/২০২০",
                    address = "ঢাকা"
                )
            )
            dao.insertTeacher(
                Teacher(
                    name = "হাফেজ মাওলানা জুবায়ের হোসেন",
                    fatherName = "মুহাম্মদ সোলায়মান",
                    phone = "০১৮৮৮-৯৯৯০০১",
                    subject = "হিফজুল কুরআন",
                    designation = "হিফজ শিক্ষক",
                    joiningDate = "১৫/০৬/২০২১",
                    address = "ঢাকা"
                )
            )

            // Default Exam
            val examId = dao.insertExam(
                Exam(
                    name = "১ম সাময়িক পরীক্ষা",
                    hijriYear = "১৪৪৬ হিজরি / ২০২৪ খ্রি.",
                    examDate = "১৫/০৮/২০২৪",
                    notes = "সকল পরীক্ষা সকাল ৯:০০ টা থেকে শুরু হবে।"
                )
            )
            dao.insertExam(
                Exam(
                    name = "বার্ষিক পরীক্ষা",
                    hijriYear = "১৪৪৬ হিজরি / ২০২৪ খ্রি.",
                    examDate = "২০/১১/২০২৪",
                    notes = "বার্ষিক চূড়ান্ত মূল্যায়ন ও সনদ বিতরণ।"
                )
            )

            // Sample Marks for s1 and s2 in j4
            val subjectsJ4 = dao.getAllSubjectsSync().filter { it.jamatId == j4 }
            if (subjectsJ4.isNotEmpty()) {
                val marks1 = listOf(
                    StudentMark(examId = examId, studentId = s1, subjectId = subjectsJ4[0].id, marksObtained = 88.0),
                    StudentMark(examId = examId, studentId = s1, subjectId = subjectsJ4.getOrNull(1)?.id ?: 0, marksObtained = 82.0),
                    StudentMark(examId = examId, studentId = s1, subjectId = subjectsJ4.getOrNull(2)?.id ?: 0, marksObtained = 91.0),
                    StudentMark(examId = examId, studentId = s1, subjectId = subjectsJ4.getOrNull(3)?.id ?: 0, marksObtained = 78.0),
                    StudentMark(examId = examId, studentId = s1, subjectId = subjectsJ4.getOrNull(4)?.id ?: 0, marksObtained = 85.0)
                ).filter { it.subjectId > 0 }
                dao.insertMarks(marks1)

                val marks2 = listOf(
                    StudentMark(examId = examId, studentId = s2, subjectId = subjectsJ4[0].id, marksObtained = 75.0),
                    StudentMark(examId = examId, studentId = s2, subjectId = subjectsJ4.getOrNull(1)?.id ?: 0, marksObtained = 79.0),
                    StudentMark(examId = examId, studentId = s2, subjectId = subjectsJ4.getOrNull(2)?.id ?: 0, marksObtained = 84.0),
                    StudentMark(examId = examId, studentId = s2, subjectId = subjectsJ4.getOrNull(3)?.id ?: 0, marksObtained = 70.0),
                    StudentMark(examId = examId, studentId = s2, subjectId = subjectsJ4.getOrNull(4)?.id ?: 0, marksObtained = 88.0)
                ).filter { it.subjectId > 0 }
                dao.insertMarks(marks2)
            }
        }
    }
}
