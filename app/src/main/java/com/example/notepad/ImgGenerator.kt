package com.example.notepad.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.pdf.PdfDocument
import android.util.Log
import com.example.notepad.R
import com.example.notepad.data.Note
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object ImgGenerator {
    fun generateImg(context: Context, notes: List<Note>, title: String = "RubicHub Notes"): File {

       val bitmap = generateBitmap(context, notes, title)

        // Save file
        val file = File(context.getExternalFilesDir(null), "notes_output.jpg")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        return file
    }


    fun generatePdf(context: Context, notes: List<Note>, title: String = "RubicHub Notes"): File {
        val bitmap = generateBitmap(context,notes, title )
        val document = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawBitmap( bitmap,0f, 0f, null)
        document.finishPage(page)

        val file = File(context.getExternalFilesDir(null), "notes_output.pdf")
        FileOutputStream(file).use {
            document.writeTo(it)
        }
        document.close()

        return file
    }

    fun generateBitmap (context : Context, notes: List<Note>, title: String = "RubicHub Notes", forPrinting : Boolean = false ): Bitmap{
        val width = 576
        val lineHeight = 40f
        val baseHeight = 200f
        val columnPadding = 10f

        val contentHeight = notes.size * lineHeight
        val height = (baseHeight + contentHeight + 100f).toInt()
        val isUrdu = Locale.getDefault().language == "ur"
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 30f
            isFakeBoldText = true
        }

        var yPos = 40f

        if(forPrinting){
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Darzee Book",width/2f, yPos+30f, paint)
            yPos+=100f
        }
        else{
            // Logo
            BitmapFactory.decodeResource(context.resources, R.drawable.logo)?.let {
                val logo = Bitmap.createScaledBitmap(it, 150, 120, false)
                canvas.drawBitmap(logo, (width - logo.width) / 2f, yPos, null)
                yPos += logo.height + 40f
            }
        }



        // Title
        val titleX = (width - paint.measureText(title)) / 2f
        canvas.drawText(title, titleX, yPos, paint)
        yPos += 60f

        // Table Headers
        paint.textSize = 20f
        paint.isFakeBoldText = true
        if (isUrdu){

            paint.textAlign = Paint.Align.RIGHT

            val colWidth = (width - columnPadding * 2) / 2f
            val descHeaderX = columnPadding + colWidth // Left side
            val titleHeaderX = width - columnPadding   // Right side

            val title_label = context.getString(R.string.title_label)
            val dec_label = context.getString(R.string.desc_label)
            val titleOffset = 60f
            canvas.drawText(title_label, titleHeaderX-titleOffset, yPos, paint)
            canvas.drawText(dec_label, descHeaderX, yPos, paint)

            yPos += lineHeight

            // Table Content
            paint.textSize = 18f
            paint.isFakeBoldText = false

            notes.forEach {
                val trimmedDesc = it.description.take(40)
                canvas.drawText(it.title, titleHeaderX-titleOffset, yPos, paint)
                canvas.drawText(trimmedDesc, descHeaderX, yPos, paint)
                yPos += lineHeight
            }

            yPos = 40f


        } else {
            val colWidth = (width - columnPadding * 2) / 2f
            val titleHeaderX = (width - (colWidth * 2 + columnPadding)) / 2f
            val descHeaderX = titleHeaderX + colWidth + columnPadding
            val titleOffset1 = 60f
            val title_label = context.getString(R.string.title_label)
            val dec_label = context.getString(R.string.desc_label)
            canvas.drawText(title_label, titleHeaderX+titleOffset1 , yPos, paint)
            canvas.drawText(dec_label, descHeaderX, yPos, paint)

            yPos += lineHeight

            // Table Content
            paint.textSize = 18f
            paint.isFakeBoldText = false

            notes.forEach {
                val trimmedDesc = it.description.take(40)
                val titleOffset = 60f
                canvas.drawText(it.title, titleHeaderX+titleOffset, yPos, paint)
                canvas.drawText(trimmedDesc, descHeaderX, yPos, paint)
                yPos += lineHeight
            }
            yPos = 40f
        }


        return bitmap

    }


}
