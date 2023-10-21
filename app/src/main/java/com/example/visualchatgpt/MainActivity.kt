package com.example.visualchatgpt

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer

class MainActivity : AppCompatActivity() {

    private val RESULT_LOAD_IMAGE = 1

    private lateinit var imageView: ImageView

    private lateinit var textView: TextView

    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageSrc)
        textView = findViewById(R.id.imageText)
        button = findViewById(R.id.genTextButton)

        button.setOnClickListener {
            detectTextFromImage()
        }

        imageView.setOnClickListener {
            pickImageFromGallery()
        }

    }

    private fun pickImageFromGallery() {
        val intent = Intent (
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(intent, RESULT_LOAD_IMAGE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            val selectedImage = data.data
            val filePathCol = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(
                selectedImage!!,
                filePathCol,
                null, null, null
            )
            cursor?.moveToFirst()
            val colIdx = cursor?.getColumnIndex(filePathCol[0])
            val picPath = cursor?.getString(colIdx!!)
            cursor?.close()

            imageView.setImageBitmap(BitmapFactory.decodeFile(picPath))
        }
    }


    private fun detectTextFromImage(){

        val imgBitmap = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.sample_image)

        val textRecognizer = TextRecognizer.Builder(applicationContext).build()

        if (!textRecognizer.isOperational) {
            Toast.makeText(this, "Text Recognizer not initialized", Toast.LENGTH_LONG)
            return
        }

        val frames = Frame.Builder().setBitmap(imgBitmap).build()

        val items = textRecognizer.detect(frames)

        var textExtracted: String = ""

        for (i in 0 until items.size()) {
            textExtracted += items.valueAt(i).value
        }

        textView.text = textExtracted

    }

}