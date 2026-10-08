package com.example.ui.screens.results

import com.example.data.model.Student
import com.example.data.model.Subject

data class SubjectResultDetail(
    val subject: Subject,
    val fullMarks: Double,
    val passMarks: Double,
    val obtainedMarks: Double,
    val isPass: Boolean,
    val grade: String
)

data class StudentResultSummary(
    val student: Student,
    val subjectDetails: List<SubjectResultDetail>,
    val totalFullMarks: Double,
    val totalObtainedMarks: Double,
    val percentage: Double,
    val overallGrade: String,
    val isPassed: Boolean,
    var rank: Int = 0
)
