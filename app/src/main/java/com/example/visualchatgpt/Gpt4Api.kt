package com.example.visualchatgpt

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

object Gpt4Api {

    private const val TAG = "VisualChatGPT: GPT4API"

    fun getResponse(mContext: Context, query: String): String? {

        if (query == "Extracted Text Here") {
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

        val postRequest = object: JsonObjectRequest(
            Method.POST, Const.url, jsonObject,
            Response.Listener { response ->
                val stringText = response.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                outputString = stringText

            },
            Response.ErrorListener {
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

        Volley.newRequestQueue(mContext).add(postRequest)

        return outputString
    }

}