package com.maropost.taxicab.asynctasks

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import android.os.AsyncTask
import com.maropost.taxicab.utils.DirectionsJSONParser


 class ParserTask(private val parserCallbacks: ParserCallbacks) : AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {

    // Parsing the data in non-ui thread
    override fun doInBackground(vararg jsonData: String): List<List<HashMap<String, String>>>? {

        val jObject: JSONObject
        var routes: List<List<HashMap<String, String>>>? = null

        try {
            jObject = JSONObject(jsonData[0])
            val parser = DirectionsJSONParser()

            // Starts parsing data
            routes = parser.parse(jObject)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return routes
    }

    // Executes in UI thread, after the parsing process
    override fun onPostExecute(result: List<List<HashMap<String, String>>>) {
        var points: ArrayList<LatLng>? = null
        var lineOptions: PolylineOptions? = null
        val markerOptions = MarkerOptions()

        // Traversing through all the routes
        for (i in result.indices) {
            points = ArrayList()
            lineOptions = PolylineOptions()

            // Fetching i-th route
            val path = result[i]

            // Fetching all the points in i-th route
            for (j in path.indices) {
                val point = path[j]

                val lat = java.lang.Double.parseDouble(point["lat"]!!)
                val lng = java.lang.Double.parseDouble(point["lng"]!!)
                val position = LatLng(lat, lng)

                points.add(position)
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points)
            lineOptions.width(2f)
            lineOptions.color(Color.RED)
        }

        // Drawing polyline in the Google Map for the i-th route
        parserCallbacks.onLineOptionsObtained(lineOptions)
    }

     interface ParserCallbacks{
         fun onLineOptionsObtained(lineOptions: PolylineOptions?)
     }
}