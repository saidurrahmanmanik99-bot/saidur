package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class MadrasaSettings(
    @PrimaryKey val id: Int = 1,
    val madrasaName: String = "দারুল উলুম মাদ্রাসা",
    val madrasaAddress: String = "ঢাকা, বাংলাদেশ",
    val principalName: String = "মাওলানা মুহাম্মাদ আব্দুল্লাহ",
    val phone: String = "০১৭১২-৩৪৫৬৭৮",
    val email: String = "madrasa@example.com",
    val defaultPassMarks: Int = 33,
    val defaultFullMarks: Int = 100,
    val logoBase64: String = "",
    val paperSize: String = "A4" // A4
)

@Entity(tableName = "jamats")
data class Jamat(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. নূরানী, নাজেরা, হিফজ, ইবতিদায়ী, ১ম শ্রেণি, দাওরায়ে হাদিস
    val section: String = "ক", // শাখা
    val orderIndex: Int = 0
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // ছাত্রের নাম
    val fatherName: String, // পিতার নাম
    val motherName: String = "", // মাতার নাম
    val birthDate: String = "", // জন্ম তারিখ (DD/MM/YYYY)
    val rollNo: String, // রোল নম্বর
    val jamatId: Long, // জামাত আইডি
    val jamatName: String, // জামাতের নাম (ক্যাশড)
    val section: String = "ক", // শাখা
    val phone: String = "", // মোবাইল নম্বর
    val address: String = "", // ঠিকানা
    val admissionDate: String = "", // ভর্তি তারিখ
    val photoUri: String = "", // ছবি পাথ বা Base64
    val guardianName: String = "",
    val bloodGroup: String = ""
)

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // শিক্ষকের নাম
    val fatherName: String = "", // পিতার নাম
    val phone: String = "", // মোবাইল নম্বর
    val subject: String = "", // পাঠদানের বিষয়
    val designation: String = "", // পদবী / দায়িত্ব (মুহতামিম, নাযেমে তালিমাত, ওস্তাদ)
    val joiningDate: String = "", // যোগদানের তারিখ
    val address: String = ""
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jamatId: Long, // জামাত আইডি
    val name: String, // বিষয়ের নাম (কুরআন মাজীদ, হাদীস শরীফ, ফিকহ, আরবী ব্যাকরণ, বাংলা, গণিত, ইংরেজি)
    val fullMarks: Int = 100, // পূর্ণমান
    val passMarks: Int = 33, // পাস মার্ক
    val code: String = "" // বিষয় কোড
)

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // পরীক্ষার নাম (১ম সাময়িক, ২য় সাময়িক, বার্ষিক, মাসিক ইত্যাদি)
    val hijriYear: String = "১৪৪৬", // হিজরি / সাল
    val examDate: String = "", // পরীক্ষার তারিখ
    val notes: String = "" // বিশেষ নির্দেশনা
)

@Entity(tableName = "marks")
data class StudentMark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val studentId: Long,
    val subjectId: Long,
    val marksObtained: Double, // প্রাপ্ত নম্বর
    val fullMarks: Double = 100.0,
    val passMarks: Double = 33.0
)

@Entity(tableName = "question_papers")
data class QuestionPaper(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val madrasaName: String,
    val examName: String,
    val jamatName: String,
    val subjectName: String,
    val timeAllowed: String = "২ ঘন্টা ৩০ মিনিট", // সময়
    val fullMarks: Int = 100, // পূর্ণমান
    val instructions: String = "সকল প্রশ্নের উত্তর দেওয়া আবশ্যক। ডান পাশের সংখ্যা প্রশ্নের পূর্ণমান জ্ঞাপক।", // নির্দেশনা
    val layoutColumns: Int = 1, // 1 বা 2 কলাম
    val orientation: String = "portrait", // portrait বা landscape
    val fontSizeSp: Int = 14,
    val questionsJson: String = "[]", // JSON array of questions
    val createdAt: Long = System.currentTimeMillis()
)
