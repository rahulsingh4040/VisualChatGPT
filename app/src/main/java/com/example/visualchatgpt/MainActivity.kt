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
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import org.json.JSONObject

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

    private lateinit var aiBtn: Button

    private lateinit var developerLink: TextView

    private lateinit var progressBar: ProgressBar

    private var API_KEY = "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"

    private var url = "https://api.openai.com/v1/completions"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageSrc)
        textView = findViewById(R.id.imageText)
        button = findViewById(R.id.genTextButton)
        cpyBtn = findViewById(R.id.cpyBtn)
        selectImgBtn = findViewById(R.id.selectImageButton)
        openCamera = findViewById(R.id.selectCameraButton)
        developerLink = findViewById(R.id.developerLink)
        progressBar = findViewById(R.id.progressBar)
        aiBtn = findViewById(R.id.aiBtn)

        developerLink.setOnClickListener {
            val uri = Uri.parse("https://www.linkedin.com/in/rahulsingh4040/")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

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

        aiBtn.setOnClickListener {
            getResponse(textView.text.toString())
        }

    }

    private fun getResponse(query: String) {
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)

        val jsonObject = JSONObject()

        jsonObject.put("model", "text-davinci-003")
        jsonObject.put("prompt", query)
        jsonObject.put("temperature", 0)
        jsonObject.put("max_tokens", 100)
        jsonObject.put("top_p", 1)
        jsonObject.put("frequency_penalty", 0.0)
        jsonObject.put("presence_penalty", 0.0)

        val postRequest = object: JsonObjectRequest(Method.POST, url, jsonObject,
            Response.Listener { response ->  
                val responseMsg = response.getJSONArray("choices").getJSONObject(0).
                getString("text")

                textView.text = responseMsg
            },
            Response.ErrorListener {
                Log.d("ASDF", "Error: ${it.message}")
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json"
                params["Authorization"] = API_KEY
                return params
            }
        }

        postRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            override fun retry(error: VolleyError?) {

            }

        }

        queue.add(postRequest)
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

        val imgBitmap = (imageView.drawable as BitmapDrawable).bitmap

        val textRecognizer = TextRecognizer.Builder(applicationContext).build()

        if (!textRecognizer.isOperational) {
            Toast.makeText(this, "Text Recognizer not initialized", Toast.LENGTH_LONG)
            return
        }

        progressBar.visibility = View.VISIBLE

        val frames = Frame.Builder().setBitmap(imgBitmap).build()

        val items = textRecognizer.detect(frames)

        var textExtracted: String = ""

        for (i in 0 until items.size()) {
            textExtracted += items.valueAt(i).value
        }

        Handler(Looper.getMainLooper()).postDelayed ({
            progressBar.visibility = View.GONE
            textView.text = textExtracted
        }, 1000)

    }

}