package com.example.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object PrintPdfHelper {

    /**
     * Prints or saves as PDF an HTML document using the standard Android Print framework.
     * Android's Print Spooler natively supports "Save as PDF" as a printer destination!
     */
    fun printHtml(context: Context, htmlContent: String, jobTitle: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager != null) {
                    val printAdapter: PrintDocumentAdapter = webView.createPrintDocumentAdapter(jobTitle)
                    val attributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                    printManager.print(jobTitle, printAdapter, attributes)
                } else {
                    Toast.makeText(context, "প্রিন্ট সেবা উপলব্ধ নেই", Toast.LENGTH_SHORT).show()
                }
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html; charset=UTF-8", "UTF-8", null)
    }

    /**
     * Share backup JSON file or text
     */
    fun shareTextFile(context: Context, content: String, filename: String) {
        try {
            val file = File(context.cacheDir, filename)
            file.writeText(content, Charsets.UTF_8)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "ব্যাকআপ ফাইল সংরক্ষণ বা শেয়ার করুন"))
        } catch (e: Exception) {
            // Fallback plain share
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
            }
            context.startActivity(Intent.createChooser(intent, "ব্যাকআপ শেয়ার করুন"))
        }
    }
}
