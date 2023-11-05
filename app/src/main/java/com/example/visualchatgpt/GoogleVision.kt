package com.example.visualchatgpt

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.ImageView
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer

object GoogleVision {

    private const val TAG = "VisualChatGPT: GOOGLE_VISION"

    fun getTextFromImage(mContext: Context, mImageView: ImageView) : String? {

        if (mImageView.drawable == null) {
            Log.d(TAG, "Image view drawable is null")
            return null
        }

        val imgBitmap = (mImageView.drawable as BitmapDrawable).bitmap

        val textRecognizer = TextRecognizer.Builder(mContext).build()

        val frames = Frame.Builder().setBitmap(imgBitmap).build()

        val items = textRecognizer.detect(frames)

        var textExtracted = ""

        for (i in 0 until items.size()) {
            textExtracted += items.valueAt(i).value
        }

        return textExtracted

    }

}