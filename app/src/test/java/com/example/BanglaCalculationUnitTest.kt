package com.example

import com.example.util.BanglaFormatHelper
import org.junit.Assert.*
import org.junit.Test

class BanglaCalculationUnitTest {

    @Test
    fun testBanglaDigitConversion() {
        assertEquals("১২৩৪৫৬৭৮৯০", BanglaFormatHelper.toBangla("1234567890"))
        assertEquals("১০০", BanglaFormatHelper.toBangla(100))
        assertEquals("৩৩", BanglaFormatHelper.toBangla(33.0))
        assertEquals("৮৫.৫", BanglaFormatHelper.toBangla(85.5))
    }

    @Test
    fun testBanglaDigitParsing() {
        assertEquals(100.0, BanglaFormatHelper.parseBanglaDouble("১০০") ?: 0.0, 0.001)
        assertEquals(33.5, BanglaFormatHelper.parseBanglaDouble("৩৩.৫") ?: 0.0, 0.001)
        assertEquals(100, BanglaFormatHelper.parseBanglaInt("১০০") ?: 0)
        assertEquals(33, BanglaFormatHelper.parseBanglaInt("৩৩") ?: 0)
    }

    @Test
    fun testGradeCalculation() {
        assertEquals("মুমতায (A+)", BanglaFormatHelper.calculateGrade(85.0))
        assertEquals("জায়্যিদ জিদ্দান (A)", BanglaFormatHelper.calculateGrade(75.0))
        assertEquals("জায়্যিদ (A-)", BanglaFormatHelper.calculateGrade(65.0))
        assertEquals("মাকবুল (B)", BanglaFormatHelper.calculateGrade(55.0))
        assertEquals("রাসিব (ফেল)", BanglaFormatHelper.calculateGrade(30.0))
    }

    @Test
    fun testPassFailCalculation() {
        assertTrue(BanglaFormatHelper.calculateIsPass(33.0, 33.0))
        assertTrue(BanglaFormatHelper.calculateIsPass(80.0, 33.0))
        assertFalse(BanglaFormatHelper.calculateIsPass(32.9, 33.0))
    }
}
