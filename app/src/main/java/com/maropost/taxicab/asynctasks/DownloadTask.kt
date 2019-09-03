package com.maropost.taxicab.asynctasks

import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.maps.model.PolylineOptions
import com.maropost.taxicab.utils.InputReader


class DownloadTask(private val downloadTaskCallbacks:DownloadTaskCallbacks) : AsyncTask<String, Void, String>(),ParserTask.ParserCallbacks {

    // Downloading data in non-ui thread
    override fun doInBackground(vararg url: String): String {

        // For storing data from web service
        var data = ""
        try {
            // Fetching the data from web service
            val inputReader = InputReader()
            data = inputReader.downloadUrl(url[0])
        } catch (e: Exception) {
            Log.d("Background Task", e.toString())
        }
        return data
    }

    // Executes in UI thread, after the execution of doInBackground()
    override fun onPostExecute(result: String) {
        super.onPostExecute(result)

        val parserTask = ParserTask(this)
        // Invokes the thread for parsing the JSON data
        parserTask.execute(result)
    }

    override fun onLineOptionsObtained(lineOptions: PolylineOptions?) {
        downloadTaskCallbacks.onLineOptionsObtained(lineOptions)
    }

    interface DownloadTaskCallbacks{
        fun onLineOptionsObtained(lineOptions: PolylineOptions?)
    }
}