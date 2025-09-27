package com.example.stressease

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import java.io.File

class PdfViewer: AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.pdfviewer) // or activity_pdf_viewer if renamed

            val pdfView: PDFView = findViewById(R.id.pdfView)

            val filePath = intent.getStringExtra("pdfPath")
            if (filePath != null) {
                val file = File(filePath)
                if (file.exists()) {
                    pdfView.fromFile(file)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .defaultPage(0)
                        .enableAnnotationRendering(true)
                        .spacing(10)
                        .load()
                }
            }
        }
    }

