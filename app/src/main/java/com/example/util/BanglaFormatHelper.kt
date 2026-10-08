package com.example.util

import com.example.data.model.*
import java.util.Locale

object BanglaFormatHelper {
    private val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBangla(number: Any?): String {
        if (number == null) return ""
        val str = when (number) {
            is Double -> if (number % 1.0 == 0.0) number.toInt().toString() else String.format(Locale.US, "%.1f", number)
            is Float -> if (number % 1.0f == 0.0f) number.toInt().toString() else String.format(Locale.US, "%.1f", number)
            else -> number.toString()
        }
        var result = str
        for (i in englishDigits.indices) {
            result = result.replace(englishDigits[i], banglaDigits[i])
        }
        return result
    }

    fun parseBanglaDouble(text: String): Double? {
        var str = text.trim()
        for (i in banglaDigits.indices) {
            str = str.replace(banglaDigits[i], englishDigits[i])
        }
        return str.toDoubleOrNull()
    }

    fun parseBanglaInt(text: String): Int? {
        var str = text.trim()
        for (i in banglaDigits.indices) {
            str = str.replace(banglaDigits[i], englishDigits[i])
        }
        return str.toIntOrNull()
    }

    /**
     * Standard Islamic/Madrasa Grading Calculation:
     * 80-100: মুমতায (A+ / ممتاز)
     * 70-79: জায়্যিদ জিদ্দান (A / جيد جداً)
     * 60-69: জায়্যিদ (A- / جيد)
     * 50-59: মাকবুল (B / مقبول)
     * 33-49: পাস (C/D)
     * <33: রাসিব / অকৃতকার্য (F / ফেল)
     */
    fun calculateGrade(percentage: Double, passPercentage: Double = 33.0): String {
        return when {
            percentage < passPercentage -> "রাসিব (ফেল)"
            percentage >= 80.0 -> "মুমতায (A+)"
            percentage >= 70.0 -> "জায়্যিদ জিদ্দান (A)"
            percentage >= 60.0 -> "জায়্যিদ (A-)"
            percentage >= 50.0 -> "মাকবুল (B)"
            percentage >= 40.0 -> "মাকবুল (C)"
            else -> "উত্তীর্ণ (পাস)"
        }
    }

    fun calculateIsPass(percentage: Double, passPercentage: Double = 33.0): Boolean {
        return percentage >= passPercentage
    }
}
