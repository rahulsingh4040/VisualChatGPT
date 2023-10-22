package com.example.visualchatgpt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer

class MainActivity : AppCompatActivity() {

    private val RESULT_LOAD_IMAGE = 1

    private val RESULT_CAPTURE_IMAGE = 2

    private lateinit var imageView: ImageView

    private lateinit var textView: TextView

    private lateinit var button: Button

    private lateinit var picPath: String

    private lateinit var selectImgBtn: Button

    private lateinit var cpyBtn: ImageButton

    private lateinit var openCamera: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageSrc)
        textView = findViewById(R.id.imageText)
        button = findViewById(R.id.genTextButton)
        cpyBtn = findViewById(R.id.cpyBtn)
        selectImgBtn = findViewById(R.id.selectImageButton)
        openCamera = findViewById(R.id.selectCameraButton)

        button.setOnClickListener {
            detectTextFromImage()
        }

        selectImgBtn.setOnClickListener {
            pickImageFromGallery()
        }

        cpyBtn.setOnClickListener {
            copyTextToClipBoard()
        }

        openCamera.setOnClickListener {
            openCameraAndPickImage()
        }

    }

    private fun openCameraAndPickImage() {
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePicture, RESULT_CAPTURE_IMAGE)
    }

    private fun copyTextToClipBoard() {
        val txt = textView.text
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Image Data", txt)
        clipboard.setPrimaryClip(clipData)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
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
            picPath = cursor?.getString(colIdx!!)!!
            cursor.close()

            imageView.setImageBitmap(BitmapFactory.decodeFile(picPath))
        }

        if (requestCode == RESULT_CAPTURE_IMAGE && resultCode == RESULT_OK && data != null) {
            imageView.setImageBitmap(data.extras?.get("data") as Bitmap)
        }
    }


    private fun detectTextFromImage(){

        if (!this::picPath.isInitialized) {
            return
        }

        val imgBitmap = (imageView.drawable as BitmapDrawable).bitmap

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