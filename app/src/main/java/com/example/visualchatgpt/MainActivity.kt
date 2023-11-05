package com.example.visualchatgpt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.visualchatgpt.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(mBinding.root)

        setOnClickListeners()

    }

    private fun setOnClickListeners() {
        mBinding.developerLink.setOnClickListener {
            val uri = Uri.parse(Const.developerURL)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        mBinding.genTextButton.setOnClickListener {
            var textFromImage: String?
            GlobalScope.launch (Dispatchers.Default) {
                textFromImage = GoogleVision.getTextFromImage(applicationContext, mBinding.imageSrc)
                withContext(Dispatchers.Main) {
                    if (textFromImage != null) mBinding.imageText.text = textFromImage
                }
            }
        }

        mBinding.selectImageButton.setOnClickListener {
            pickImageFromGallery()
        }

        mBinding.cpyBtn.setOnClickListener {
            copyTextToClipBoard()
        }

        mBinding.selectCameraButton.setOnClickListener {
            openCameraAndPickImage()
        }

        mBinding.aiBtn.setOnClickListener {
            GlobalScope.launch (Dispatchers.IO) {
                val outputString = Gpt4Api.getResponse(applicationContext, mBinding.imageText.text.toString())
                withContext(Dispatchers.Main) {
                    if (outputString != null) mBinding.imageText.text = outputString
                }
            }
        }
    }

    private fun getResponse(query: String): String? {

        Log.d(TAG, "Awaiting response from GPT4")

        if (query == "Extracted Text Here") {
            Toast.makeText(this, "No text found to search", Toast.LENGTH_LONG).show()
            return null
        }

        var outputString: String? = null

        val jsonObject = JSONObject()

        jsonObject.put("model", "gpt-3.5-turbo")

        val jsonArrayMessage = JSONArray()
        val jsonObjectMessage = JSONObject()
        jsonObjectMessage.put("role", "user")
        jsonObjectMessage.put("content", query)
        jsonArrayMessage.put(jsonObjectMessage)

        jsonObject.put("messages", jsonArrayMessage)

        val postRequest = object: JsonObjectRequest(Method.POST, Const.url, jsonObject,
            Response.Listener { response ->
                val stringText = response.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                Log.d(TAG, "Response Msg: $stringText")
                mBinding.progressBar.visibility = View.GONE
                outputString = stringText

            },
            Response.ErrorListener {
                Toast.makeText(this, "API error please contact developer", Toast.LENGTH_LONG).show()
                Log.d(TAG, "Error: ${it.stackTrace[0]}")
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json"
                params["Authorization"] = "Bearer ${Const.API_KEY}"
                return params
            }

            override fun parseNetworkError(volleyError: VolleyError?): VolleyError {
                return super.parseNetworkError(volleyError)
            }
        }

        val intTimeoutPeriod = 60000 // 60 seconds timeout duration defined

        val retryPolicy: RetryPolicy = DefaultRetryPolicy(
            intTimeoutPeriod,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        postRequest.retryPolicy = retryPolicy

        Volley.newRequestQueue(applicationContext).add(postRequest)
        mBinding.progressBar.visibility = View.VISIBLE

        return outputString
    }
    
    private fun openCameraAndPickImage() {
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePicture, Const.RESULT_CAPTURE_IMAGE)
    }

    private fun copyTextToClipBoard() {
        val txt = mBinding.imageText.text
        if (txt == "") {
            Toast.makeText(this, "No text found to copy", Toast.LENGTH_LONG).show()
            return
        }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Image Data", txt)
        clipboard.setPrimaryClip(clipData)
    }


    private fun pickImageFromGallery() {
        val intent = Intent (
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(intent, Const.RESULT_LOAD_IMAGE)
    }

    @Deprecated("Deprecated API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Const.RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            val selectedImage = data.data
            val filePathCol = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(
                selectedImage!!,
                filePathCol,
                null, null, null
            )
            cursor?.moveToFirst()
            val colIdx = cursor?.getColumnIndex(filePathCol[0])
            val picPath = cursor?.getString(colIdx!!)!!
            cursor.close()

            mBinding.imageSrc.setImageBitmap(BitmapFactory.decodeFile(picPath))
        }

        if (requestCode == Const.RESULT_CAPTURE_IMAGE && resultCode == RESULT_OK && data != null) {
            mBinding.imageSrc.setImageBitmap(data.extras?.get("data") as Bitmap)
        }
    }
    
    companion object {
        private const val TAG = "VisualChatGPT: MainActivity" 
    }

}