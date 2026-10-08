package com.example.util

import com.example.data.model.*
import com.example.ui.screens.results.StudentResultSummary
import org.json.JSONArray

object DocumentHtmlGenerator {

    /**
     * Professional A4 Result Sheet for single student
     */
    fun generateSingleResultHtml(
        settings: MadrasaSettings,
        exam: Exam,
        summary: StudentResultSummary,
        totalInJamat: Int
    ): String {
        val rows = summary.subjectDetails.joinToString("\n") { item ->
            """
            <tr>
                <td style="text-align: left; padding: 8px;">${item.subject.name}</td>
                <td style="text-align: center; padding: 8px;">${BanglaFormatHelper.toBangla(item.fullMarks)}</td>
                <td style="text-align: center; padding: 8px;">${BanglaFormatHelper.toBangla(item.passMarks)}</td>
                <td style="text-align: center; padding: 8px; font-weight: bold; color: ${if (item.isPass) "#1b5e20" else "#b71c1c"};">
                    ${BanglaFormatHelper.toBangla(item.obtainedMarks)}
                </td>
                <td style="text-align: center; padding: 8px;">${item.grade}</td>
                <td style="text-align: center; padding: 8px; font-weight: bold; color: ${if (item.isPass) "#1b5e20" else "#b71c1c"};">
                    ${if (item.isPass) "পাস" else "ফেল"}
                </td>
            </tr>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html lang="bn">
        <head>
            <meta charset="UTF-8">
            <title>ফলাফল পত্র - ${summary.student.name}</title>
            <style>
                @page { size: A4 portrait; margin: 15mm; }
                body {
                    font-family: 'SolaimanLipi', 'Kalpurush', 'Nikosh', 'Arial', sans-serif;
                    margin: 0; padding: 10px; color: #222; line-height: 1.5;
                }
                .container {
                    border: 3px double #004D40;
                    padding: 24px;
                    border-radius: 8px;
                    background: #fff;
                }
                .header { text-align: center; border-bottom: 2px solid #004D40; padding-bottom: 12px; margin-bottom: 16px; }
                .title { font-size: 24px; font-weight: bold; color: #004D40; margin: 0; }
                .subtitle { font-size: 14px; color: #555; margin: 4px 0; }
                .exam-title {
                    display: inline-block;
                    background: #004D40; color: #fff;
                    font-size: 16px; font-weight: bold;
                    padding: 6px 20px; border-radius: 20px; margin-top: 8px;
                }
                .info-table { width: 100%; border-collapse: collapse; margin-bottom: 16px; font-size: 14px; }
                .info-table td { padding: 4px 8px; }
                .info-label { font-weight: bold; color: #004D40; width: 15%; }
                .marks-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; font-size: 14px; }
                .marks-table th { background: #E0F2F1; color: #004D40; border: 1px solid #80CBC4; padding: 8px; font-weight: bold; }
                .marks-table td { border: 1px solid #B2DFDB; }
                .summary-box {
                    display: flex; justify-content: space-between;
                    background: #F1F8E9; border: 1px solid #C8E6C9;
                    padding: 12px 16px; border-radius: 6px; margin-bottom: 24px; font-size: 14px;
                }
                .summary-item { font-weight: bold; }
                .signatures {
                    margin-top: 40px; display: flex; justify-content: space-between; padding: 0 20px;
                }
                .sig-line {
                    border-top: 1px dashed #444; width: 160px; text-align: center; font-size: 13px; padding-top: 6px;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <div style="font-size: 12px; color: #004D40; font-weight: bold;">بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ</div>
                    <div class="title">${settings.madrasaName}</div>
                    <div class="subtitle">${settings.madrasaAddress}</div>
                    <div class="exam-title">${exam.name} - ${exam.hijriYear}</div>
                </div>

                <table class="info-table">
                    <tr>
                        <td class="info-label">ছাত্রের নাম:</td>
                        <td><strong>${summary.student.name}</strong></td>
                        <td class="info-label">রোল নম্বর:</td>
                        <td><strong>${BanglaFormatHelper.toBangla(summary.student.rollNo)}</strong></td>
                    </tr>
                    <tr>
                        <td class="info-label">পিতার নাম:</td>
                        <td>${summary.student.fatherName}</td>
                        <td class="info-label">জামাত / শ্রেণি:</td>
                        <td>${summary.student.jamatName} (শাখা: ${summary.student.section})</td>
                    </tr>
                    <tr>
                        <td class="info-label">ভর্তি আইডি:</td>
                        <td>#${BanglaFormatHelper.toBangla(summary.student.id)}</td>
                        <td class="info-label">মোবাইল নম্বর:</td>
                        <td>${BanglaFormatHelper.toBangla(summary.student.phone)}</td>
                    </tr>
                </table>

                <table class="marks-table">
                    <thead>
                        <tr>
                            <th>বিষয়ের নাম</th>
                            <th style="width: 12%;">পূর্ণমান</th>
                            <th style="width: 12%;">পাস মার্ক</th>
                            <th style="width: 14%;">প্রাপ্ত নম্বর</th>
                            <th style="width: 15%;">গ্রেড</th>
                            <th style="width: 12%;">ফলাফল</th>
                        </tr>
                    </thead>
                    <tbody>
                        $rows
                    </tbody>
                </table>

                <table style="width: 100%; border: 1px solid #80CBC4; background: #E0F2F1; margin-bottom: 30px; font-size: 14px; border-radius: 4px;">
                    <tr>
                        <td style="padding: 10px; text-align: center;"><strong>সর্বমোট পূর্ণমান:</strong> ${BanglaFormatHelper.toBangla(summary.totalFullMarks)}</td>
                        <td style="padding: 10px; text-align: center;"><strong>সর্বমোট প্রাপ্ত:</strong> ${BanglaFormatHelper.toBangla(summary.totalObtainedMarks)}</td>
                        <td style="padding: 10px; text-align: center;"><strong>গড় / শতাংশ:</strong> ${BanglaFormatHelper.toBangla(summary.percentage)}%</td>
                        <td style="padding: 10px; text-align: center;"><strong>চূড়ান্ত গ্রেড:</strong> <span style="font-size: 15px; color: #004D40; font-weight: bold;">${summary.overallGrade}</span></td>
                    </tr>
                    <tr>
                        <td colspan="2" style="padding: 10px; text-align: center; border-top: 1px solid #80CBC4;">
                            <strong>চূড়ান্ত ফলাফল:</strong> <span style="font-size: 16px; font-weight: bold; color: ${if (summary.isPassed) "#1b5e20" else "#b71c1c"};">${if (summary.isPassed) "উত্তীর্ণ (পাস)" else "অনুত্তীর্ণ (ফেল)"}</span>
                        </td>
                        <td colspan="2" style="padding: 10px; text-align: center; border-top: 1px solid #80CBC4;">
                            <strong>মেধাক্রম (র‍্যাংক):</strong> <span style="font-size: 16px; font-weight: bold; color: #004D40;">${BanglaFormatHelper.toBangla(summary.rank)} (মোট ${BanglaFormatHelper.toBangla(totalInJamat)} জনের মধ্যে)</span>
                        </td>
                    </tr>
                </table>

                <table style="width: 100%; margin-top: 50px;">
                    <tr>
                        <td style="width: 33%; text-align: center;">
                            <div style="border-top: 1px dashed #333; width: 140px; margin: 0 auto; padding-top: 5px; font-size: 13px;">অভিভাবকের স্বাক্ষর</div>
                        </td>
                        <td style="width: 33%; text-align: center;">
                            <div style="border-top: 1px dashed #333; width: 140px; margin: 0 auto; padding-top: 5px; font-size: 13px;">শ্রেণি শিক্ষকের স্বাক্ষর</div>
                        </td>
                        <td style="width: 33%; text-align: center;">
                            <div style="border-top: 1px dashed #333; width: 150px; margin: 0 auto; padding-top: 5px; font-size: 13px;">মুহতামিম / অধ্যক্ষের স্বাক্ষর<br><span style="font-size: 11px; color: #666;">${settings.principalName}</span></div>
                        </td>
                    </tr>
                </table>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Professional A4 Jamat-wide Tabulation / Result Sheet
     */
    fun generateJamatResultHtml(
        settings: MadrasaSettings,
        exam: Exam,
        jamat: Jamat,
        summaries: List<StudentResultSummary>,
        subjects: List<Subject>
    ): String {
        val subjectHeaders = subjects.joinToString("") {
            "<th style='padding: 6px 4px; font-size: 11px;'>${it.name}<br>(${BanglaFormatHelper.toBangla(it.fullMarks)})</th>"
        }

        val rows = summaries.joinToString("\n") { s ->
            val markTds = subjects.joinToString("") { sub ->
                val m = s.subjectDetails.find { it.subject.id == sub.id }
                val score = m?.obtainedMarks?.let { BanglaFormatHelper.toBangla(it) } ?: "-"
                "<td style='text-align: center; padding: 6px 4px; font-size: 12px;'>$score</td>"
            }
            """
            <tr>
                <td style="text-align: center; padding: 6px; font-weight: bold;">${BanglaFormatHelper.toBangla(s.rank)}</td>
                <td style="text-align: center; padding: 6px;">${BanglaFormatHelper.toBangla(s.student.rollNo)}</td>
                <td style="text-align: left; padding: 6px; font-weight: 500;">${s.student.name}</td>
                $markTds
                <td style="text-align: center; padding: 6px; font-weight: bold;">${BanglaFormatHelper.toBangla(s.totalObtainedMarks)}</td>
                <td style="text-align: center; padding: 6px;">${BanglaFormatHelper.toBangla(s.percentage)}%</td>
                <td style="text-align: center; padding: 6px; font-size: 11px;">${s.overallGrade}</td>
                <td style="text-align: center; padding: 6px; font-weight: bold; color: ${if (s.isPassed) "#1b5e20" else "#b71c1c"};">
                    ${if (s.isPassed) "পাস" else "ফেল"}
                </td>
            </tr>
            """.trimIndent()
        }

        val passedCount = summaries.count { it.isPassed }
        val failedCount = summaries.count { !it.isPassed }
        val highest = summaries.maxOfOrNull { it.totalObtainedMarks } ?: 0.0
        val lowest = summaries.minOfOrNull { it.totalObtainedMarks } ?: 0.0

        return """
        <!DOCTYPE html>
        <html lang="bn">
        <head>
            <meta charset="UTF-8">
            <title>জামাত ফলাফল শীট - ${jamat.name}</title>
            <style>
                @page { size: A4 landscape; margin: 10mm; }
                body {
                    font-family: 'SolaimanLipi', 'Kalpurush', 'Nikosh', 'Arial', sans-serif;
                    margin: 0; padding: 5px; color: #222; font-size: 12px;
                }
                .container { border: 2px solid #004D40; padding: 15px; background: #fff; }
                .header { text-align: center; border-bottom: 2px solid #004D40; padding-bottom: 8px; margin-bottom: 12px; }
                .title { font-size: 20px; font-weight: bold; color: #004D40; margin: 0; }
                .subtitle { font-size: 13px; color: #555; margin: 2px 0; }
                .badge {
                    display: inline-block; background: #004D40; color: #fff;
                    font-size: 14px; font-weight: bold; padding: 4px 16px; border-radius: 12px; margin-top: 6px;
                }
                .sheet-table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
                .sheet-table th { background: #E0F2F1; color: #004D40; border: 1px solid #80CBC4; font-weight: bold; }
                .sheet-table td { border: 1px solid #B2DFDB; }
                .stats-bar {
                    display: flex; justify-content: space-around;
                    background: #E8F5E9; border: 1px solid #A5D6A7;
                    padding: 8px; font-size: 13px; font-weight: bold; margin-bottom: 20px;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <div style="font-size: 11px; color: #004D40; font-weight: bold;">بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ</div>
                    <div class="title">${settings.madrasaName}</div>
                    <div class="subtitle">${settings.madrasaAddress}</div>
                    <div class="badge">${exam.name} - ${exam.hijriYear} | জামাত: ${jamat.name} (শাখা: ${jamat.section})</div>
                </div>

                <div class="stats-bar">
                    <span>মোট ছাত্র: ${BanglaFormatHelper.toBangla(summaries.size)} জন</span>
                    <span style="color: #2e7d32;">পাস: ${BanglaFormatHelper.toBangla(passedCount)} জন</span>
                    <span style="color: #c62828;">ফেল: ${BanglaFormatHelper.toBangla(failedCount)} জন</span>
                    <span>সর্বোচ্চ নম্বর: ${BanglaFormatHelper.toBangla(highest)}</span>
                    <span>সর্বনিম্ন নম্বর: ${BanglaFormatHelper.toBangla(lowest)}</span>
                </div>

                <table class="sheet-table">
                    <thead>
                        <tr>
                            <th style="width: 4%;">মেধা</th>
                            <th style="width: 5%;">রোল</th>
                            <th style="width: 18%; text-align: left; padding-left: 6px;">ছাত্রের নাম</th>
                            $subjectHeaders
                            <th style="width: 7%;">মোট</th>
                            <th style="width: 7%;">শতাংশ</th>
                            <th style="width: 9%;">গ্রেড</th>
                            <th style="width: 6%;">ফলাফল</th>
                        </tr>
                    </thead>
                    <tbody>
                        $rows
                    </tbody>
                </table>

                <table style="width: 100%; margin-top: 35px;">
                    <tr>
                        <td style="width: 33%; text-align: center;">
                            <div style="border-top: 1px dashed #333; width: 140px; margin: 0 auto; padding-top: 5px;">যাচাইকারীর স্বাক্ষর</div>
                        </td>
                        <td style="width: 33%; text-align: center;">
                            <div style="border-top: 1px dashed #333; width: 140px; margin: 0 auto; padding-top: 5px;">নাযেমে তা'লীমাত</div>
                        </td>
                        <td style="width: 33%; text-align: center;">
                            <div style="border-top: 1px dashed #333; width: 160px; margin: 0 auto; padding-top: 5px;">মুহতামিম / অধ্যক্ষের স্বাক্ষর<br><span style="font-size: 10px; color: #666;">${settings.principalName}</span></div>
                        </td>
                    </tr>
                </table>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Question Paper A4 generator (1 or 2 columns, portrait or landscape, font size)
     */
    fun generateQuestionPaperHtml(paper: QuestionPaper, settings: MadrasaSettings): String {
        val qArray = try {
            JSONArray(paper.questionsJson)
        } catch (e: Exception) {
            JSONArray()
        }

        val qList = mutableListOf<Triple<String, String, String>>() // number, questionText, marks
        for (i in 0 until qArray.length()) {
            val obj = qArray.optJSONObject(i)
            if (obj != null) {
                val num = obj.optString("number", "${i + 1}।")
                val text = obj.optString("text", "")
                val marks = obj.optString("marks", "")
                qList.add(Triple(num, text, marks))
            }
        }

        val questionsHtml = qList.joinToString("\n") { (num, text, marks) ->
            """
            <div class="question-row">
                <div class="q-num">$num</div>
                <div class="q-text">${text.replace("\n", "<br>")}</div>
                ${if (marks.isNotBlank()) "<div class='q-marks'>[ $marks ]</div>" else ""}
            </div>
            """.trimIndent()
        }

        val cols = if (paper.layoutColumns == 2) 2 else 1
        val orientation = if (paper.orientation == "landscape") "landscape" else "portrait"
        val fontSize = paper.fontSizeSp.coerceIn(10, 24)

        return """
        <!DOCTYPE html>
        <html lang="bn">
        <head>
            <meta charset="UTF-8">
            <title>${paper.examName} - ${paper.subjectName}</title>
            <style>
                @page { size: A4 $orientation; margin: 15mm; }
                body {
                    font-family: 'SolaimanLipi', 'Kalpurush', 'Amiri', 'Jameel Noori Nastaleeq', 'Arial', sans-serif;
                    margin: 0; padding: 10px; color: #111; font-size: ${fontSize}px; line-height: 1.6;
                }
                .header { text-align: center; border-bottom: 2px solid #000; padding-bottom: 8px; margin-bottom: 12px; }
                .bismillah { font-size: ${fontSize + 2}px; font-weight: bold; margin-bottom: 4px; font-family: 'Amiri', serif; }
                .madrasa-name { font-size: ${fontSize + 8}px; font-weight: bold; margin-bottom: 2px; }
                .exam-name { font-size: ${fontSize + 3}px; font-weight: bold; margin-bottom: 4px; }
                .meta-table { width: 100%; border-collapse: collapse; margin-bottom: 6px; font-size: ${fontSize}px; }
                .meta-table td { padding: 2px 4px; }
                .instructions {
                    font-style: italic; font-size: ${fontSize - 1}px; border-top: 1px dashed #666;
                    border-bottom: 1px dashed #666; padding: 4px 6px; margin-bottom: 14px; text-align: center;
                }
                .question-container {
                    column-count: $cols;
                    column-gap: 24px;
                    column-rule: ${if (cols > 1) "1px solid #ccc" else "none"};
                }
                .question-row {
                    display: flex;
                    align-items: flex-start;
                    margin-bottom: 14px;
                    break-inside: avoid;
                }
                .q-num {
                    font-weight: bold;
                    min-width: 32px;
                }
                .q-text {
                    flex-grow: 1;
                }
                .q-marks {
                    font-weight: bold;
                    margin-left: 10px;
                    white-space: nowrap;
                }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="bismillah">بِسْمِ اللهِ الرَّحْمٰনِ الرَّحِيْمِ</div>
                <div class="madrasa-name">${paper.madrasaName.ifBlank { settings.madrasaName }}</div>
                <div class="exam-name">${paper.examName}</div>
                <table class="meta-table">
                    <tr>
                        <td style="text-align: left; width: 33%;"><strong>জামাত:</strong> ${paper.jamatName}</td>
                        <td style="text-align: center; width: 34%;"><strong>বিষয়:</strong> ${paper.subjectName}</td>
                        <td style="text-align: right; width: 33%;"><strong>সময়:</strong> ${paper.timeAllowed}</td>
                    </tr>
                    <tr>
                        <td style="text-align: left;"></td>
                        <td style="text-align: center;"></td>
                        <td style="text-align: right;"><strong>পূর্ণমান:</strong> ${BanglaFormatHelper.toBangla(paper.fullMarks)}</td>
                    </tr>
                </table>
                ${if (paper.instructions.isNotBlank()) "<div class='instructions'>[ ${paper.instructions} ]</div>" else ""}
            </div>

            <div class="question-container">
                $questionsHtml
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Certificates / ID Card Generator
     */
    fun generateCertificateHtml(
        type: String, // "id_card", "testimonial" (প্রশংসাপত্র), "character" (প্রত্যয়নপত্র), "transfer" (ছাড়পত্র)
        student: Student,
        settings: MadrasaSettings,
        extraNote: String = ""
    ): String {
        return when (type) {
            "id_card" -> generateIdCardHtml(student, settings)
            "testimonial" -> generateTestimonialHtml(student, settings, extraNote)
            "character" -> generateCharacterCertificateHtml(student, settings, extraNote)
            "transfer" -> generateTransferCertificateHtml(student, settings, extraNote)
            else -> generateTestimonialHtml(student, settings, extraNote)
        }
    }

    private fun generateIdCardHtml(student: Student, settings: MadrasaSettings): String {
        return """
        <!DOCTYPE html>
        <html lang="bn">
        <head>
            <meta charset="UTF-8">
            <title>ছাত্র পরিচয়পত্র - ${student.name}</title>
            <style>
                @page { size: A4 portrait; margin: 15mm; }
                body {
                    font-family: 'SolaimanLipi', 'Kalpurush', 'Arial', sans-serif;
                    display: flex; justify-content: center; align-items: center; min-height: 80vh;
                }
                .card {
                    width: 340px; height: 500px; border: 2px solid #004D40; border-radius: 12px;
                    box-shadow: 0 4px 10px rgba(0,0,0,0.15); overflow: hidden; background: #fff;
                    display: flex; flex-direction: column; text-align: center;
                }
                .card-header {
                    background: #004D40; color: #fff; padding: 12px 8px;
                }
                .madrasa-name { font-size: 16px; font-weight: bold; }
                .card-badge {
                    background: #FFD54F; color: #004D40; font-size: 12px; font-weight: bold;
                    display: inline-block; padding: 2px 10px; border-radius: 8px; margin-top: 4px;
                }
                .photo-box {
                    width: 90px; height: 110px; border: 2px dashed #004D40; margin: 12px auto 8px;
                    display: flex; align-items: center; justify-content: center; font-size: 11px; color: #666;
                    background: #E0F2F1; border-radius: 6px;
                }
                .info-table { width: 90%; margin: 0 auto; font-size: 13px; text-align: left; }
                .info-table td { padding: 3px 4px; }
                .label { font-weight: bold; color: #004D40; width: 38%; }
                .card-footer {
                    margin-top: auto; padding: 10px; border-top: 1px dashed #004D40;
                    display: flex; justify-content: space-between; font-size: 11px;
                }
            </style>
        </head>
        <body>
            <div class="card">
                <div class="card-header">
                    <div style="font-size: 11px;">بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ</div>
                    <div class="madrasa-name">${settings.madrasaName}</div>
                    <div style="font-size: 10px; color: #E0F2F1;">${settings.madrasaAddress}</div>
                    <div class="card-badge">ছাত্র পরিচয়পত্র</div>
                </div>

                <div class="photo-box">ছবির স্থান</div>

                <div style="font-size: 16px; font-weight: bold; color: #004D40; margin-bottom: 6px;">
                    ${student.name}
                </div>

                <table class="info-table">
                    <tr><td class="label">রোল নম্বর:</td><td><strong>${BanglaFormatHelper.toBangla(student.rollNo)}</strong></td></tr>
                    <tr><td class="label">জামাত / শ্রেণি:</td><td>${student.jamatName}</td></tr>
                    <tr><td class="label">শাখা:</td><td>${student.section}</td></tr>
                    <tr><td class="label">পিতার নাম:</td><td>${student.fatherName}</td></tr>
                    <tr><td class="label">মোবাইল নম্বর:</td><td>${BanglaFormatHelper.toBangla(student.phone)}</td></tr>
                    <tr><td class="label">ভর্তি আইডি:</td><td>#${BanglaFormatHelper.toBangla(student.id)}</td></tr>
                </table>

                <div class="card-footer">
                    <span>ছাত্রের স্বাক্ষর</span>
                    <span style="font-weight: bold;">অধ্যক্ষের স্বাক্ষর</span>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun generateTestimonialHtml(student: Student, settings: MadrasaSettings, note: String): String {
        return """
        <!DOCTYPE html>
        <html lang="bn">
        <head>
            <meta charset="UTF-8">
            <title>প্রশংসাপত্র - ${student.name}</title>
            <style>
                @page { size: A4 landscape; margin: 15mm; }
                body {
                    font-family: 'SolaimanLipi', 'Kalpurush', 'Arial', sans-serif;
                    padding: 10px; color: #222;
                }
                .border-outer {
                    border: 4px double #004D40; padding: 30px; border-radius: 12px;
                    min-height: 85vh; display: flex; flex-direction: column; justify-content: space-between;
                }
                .header { text-align: center; border-bottom: 2px solid #004D40; padding-bottom: 12px; }
                .title { font-size: 28px; font-weight: bold; color: #004D40; }
                .cert-type {
                    display: inline-block; background: #004D40; color: #fff;
                    font-size: 20px; font-weight: bold; padding: 6px 28px; border-radius: 20px; margin-top: 12px;
                }
                .content { font-size: 18px; line-height: 2.2; text-align: justify; margin: 30px 10px; }
                .highlight { font-weight: bold; color: #004D40; text-decoration: underline; }
                .footer { display: flex; justify-content: space-between; margin-top: 50px; padding: 0 40px; }
            </style>
        </head>
        <body>
            <div class="border-outer">
                <div class="header">
                    <div style="font-size: 14px; color: #004D40; font-weight: bold;">بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ</div>
                    <div class="title">${settings.madrasaName}</div>
                    <div style="font-size: 14px; color: #555;">${settings.madrasaAddress} | ফোন: ${BanglaFormatHelper.toBangla(settings.phone)}</div>
                    <div class="cert-type">প্রশংসাপত্র (Certificate of Appreciation)</div>
                </div>

                <div class="content">
                    এই মর্মে প্রশংসাপত্র প্রদান করা যাচ্ছে যে, <span class="highlight">${student.name}</span>,
                    পিতা: <span class="highlight">${student.fatherName}</span>,
                    মাতা: <span class="highlight">${student.motherName.ifBlank { "—" }}</span>,
                    ঠিকানা: <span class="highlight">${student.address.ifBlank { "—" }}</span>।
                    তিনি অত্র মাদ্রাসার <span class="highlight">${student.jamatName}</span> জামাতের একজন নিয়মিত ছাত্র ছিলেন (রোল: ${BanglaFormatHelper.toBangla(student.rollNo)})।
                    অত্র মাদ্রাসায় অধ্যয়নকালীন সময়ে তাহার চরিত্র প্রশংসনীয়, স্বভাব সৎ ও আচার-ব্যবহার উত্তম ছিল। মাদ্রাসা বিরোধী কোনো কার্যকলাপে তাহার সংশ্লিষ্টতা পরিলক্ষিত হয় নাই।
                    ${if (note.isNotBlank()) "<br>বিশেষ মন্তব্য: $note" else ""}
                    <br>আমরা তাহার সার্বিক ইহকালীন ও পরকালীন সাফল্য এবং উজ্জ্বল ভবিষ্যৎ কামনা করি।
                </div>

                <div class="footer">
                    <div style="text-align: center; border-top: 1px dashed #333; width: 160px; padding-top: 6px;">নাযেমে তা'লীমাত</div>
                    <div style="text-align: center; border-top: 1px dashed #333; width: 200px; padding-top: 6px;">
                        মুহতামিম / প্রিন্সিপাল<br><span style="font-size: 12px; color: #555;">${settings.principalName}</span>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun generateCharacterCertificateHtml(student: Student, settings: MadrasaSettings, note: String): String {
        return """
        <!DOCTYPE html>
        <html lang="bn">
        <head>
            <meta charset="UTF-8">
            <title>প্রত্যয়নপত্র - ${student.name}</title>
            <style>
                @page { size: A4 portrait; margin: 20mm; }
                body { font-family: 'SolaimanLipi', 'Kalpurush', 'Arial', sans-serif; line-height: 2.2; }
                .box { border: 3px double #004D40; padding: 30px; border-radius: 8px; }
                .header { text-align: center; border-bottom: 2px solid #004D40; padding-bottom: 12px; margin-bottom: 30px; }
            </style>
        </head>
        <body>
            <div class="box">
                <div class="header">
                    <div style="font-size: 13px; font-weight: bold; color: #004D40;">بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ</div>
                    <h2 style="margin: 4px 0; color: #004D40;">${settings.madrasaName}</h2>
                    <p style="margin: 0; color: #555; font-size: 13px;">${settings.madrasaAddress}</p>
                    <div style="margin-top: 14px; font-size: 18px; font-weight: bold; text-decoration: underline; color: #004D40;">প্রত্যয়নপত্র</div>
                </div>
                <div style="font-size: 16px; text-align: justify;">
                    এই মর্মে প্রত্যয়ন করা যাচ্ছে যে, <strong>${student.name}</strong>, পিতা: <strong>${student.fatherName}</strong>, অত্র প্রতিষ্ঠানের <strong>${student.jamatName}</strong> শ্রেণির একজন নিয়মিত ছাত্র। তাহার শ্রেণি রোল নম্বর <strong>${BanglaFormatHelper.toBangla(student.rollNo)}</strong>। তাহার স্বভাব-চরিত্র অত্যন্ত সুন্দর ও মার্জিত।
                    <br><br>
                    ${if (note.isNotBlank()) "মন্তব্য: $note<br><br>" else ""}
                    তাহার ভবিষ্যৎ জীবনের উত্তরোত্তর সফলতা ও নেক হায়াত কামনা করি।
                </div>
                <div style="margin-top: 80px; display: flex; justify-content: flex-end;">
                    <div style="text-align: center; border-top: 1px dashed #333; width: 180px; padding-top: 6px;">
                        মুহতামিম / অধ্যক্ষের স্বাক্ষর<br><span style="font-size: 12px;">${settings.principalName}</span>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun generateTransferCertificateHtml(student: Student, settings: MadrasaSettings, reason: String): String {
        return """
        <!DOCTYPE html>
        <html lang="bn">
        <head>
            <meta charset="UTF-8">
            <title>ছাড়পত্র (টিসি) - ${student.name}</title>
            <style>
                @page { size: A4 portrait; margin: 20mm; }
                body { font-family: 'SolaimanLipi', 'Kalpurush', 'Arial', sans-serif; line-height: 2.2; }
                .box { border: 3px double #004D40; padding: 30px; border-radius: 8px; }
                .header { text-align: center; border-bottom: 2px solid #004D40; padding-bottom: 12px; margin-bottom: 30px; }
            </style>
        </head>
        <body>
            <div class="box">
                <div class="header">
                    <div style="font-size: 13px; font-weight: bold; color: #004D40;">بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ</div>
                    <h2 style="margin: 4px 0; color: #004D40;">${settings.madrasaName}</h2>
                    <p style="margin: 0; color: #555; font-size: 13px;">${settings.madrasaAddress}</p>
                    <div style="margin-top: 14px; font-size: 18px; font-weight: bold; text-decoration: underline; color: #004D40;">ছাড়পত্র (Transfer Certificate)</div>
                </div>
                <div style="font-size: 16px; text-align: justify;">
                    অত্র মাদ্রাসার ছাত্র <strong>${student.name}</strong>, পিতা: <strong>${student.fatherName}</strong>, রোল: <strong>${BanglaFormatHelper.toBangla(student.rollNo)}</strong>, জামাত: <strong>${student.jamatName}</strong> কে তাহার অভিভাবকের আবেদনের প্রেক্ষিতে ছাড়পত্র প্রদান করা হইলো।
                    <br><br>
                    মাদ্রাসায় তাহার যাবতীয় পাওনাদি সম্পূর্ণ পরিশোধিত আছে। তাহার আচরণ সন্তোষজনক ছিল।
                    <br><br>
                    <strong>ছাড়পত্রের কারণ:</strong> ${if (reason.isNotBlank()) reason else "অভিভাবকের ইচ্ছানুযায়ী / স্থানান্তর"}
                </div>
                <div style="margin-top: 80px; display: flex; justify-content: space-between;">
                    <div style="text-align: center; border-top: 1px dashed #333; width: 140px; padding-top: 6px;">হিসাব রক্ষক</div>
                    <div style="text-align: center; border-top: 1px dashed #333; width: 180px; padding-top: 6px;">
                        মুহতামিম / অধ্যক্ষের স্বাক্ষর<br><span style="font-size: 12px;">${settings.principalName}</span>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}
