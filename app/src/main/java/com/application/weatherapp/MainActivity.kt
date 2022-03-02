package com.application.weatherapp

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Spinner
import android.widget.ArrayAdapter
import com.application.weatherapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    val CITY: String="Tokyo, JP"
//            Array<String> = arrayOf("Tokyo,JP", "California, US", "London, UK")
    val API: String = "06c921750b9a82d8f5d1294e1586276f"

//    private lateinit var latestUpdateTitleTextView: TextView
//    private lateinit var latestUpdateTextView: TextView
    private lateinit var binding: ActivityMainBinding

//    private var codeSelected = "Tokyo, JP"

    private var citySelected = "Tokyo, JP"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val sharedPreferences = getSharedPreferences("weather app", Context.MODE_PRIVATE)

        val adapter : ArrayAdapter<*> = ArrayAdapter.createFromResource(this, R.array.cities, R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {


            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val spinnerParent = parent as Spinner

                // ここ？
                val item = spinnerParent.selectedItem as String
                citySelected = parent?.getItemAtPosition(position).toString()
                    //???
//                    States().getStatesMap()[parent?.getItemAtPosition(position)].toString()

//                updateDataAndVisualizations(sharedPreferences, false)

//                binding.textView.text = item

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        weatherTask().execute()

    }

    inner class weatherTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            /* Showing the ProgressBar, Making the main design GONE */
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API").readText(
                    Charsets.UTF_8
                )
            }catch (e: Exception){
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: "+ SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt*1000))
                val temp = main.getString("temp")+"°C"
                val tempMin = "Min Temp: " + main.getString("temp_min")+"°C"
                val tempMax = "Max Temp: " + main.getString("temp_max")+"°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")

                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")

                val address = jsonObj.getString("name")+", "+sys.getString("country")

                /* Populating extracted data into our views */
                findViewById<TextView>(R.id.textView).text = address
                findViewById<TextView>(R.id.updated_at).text =  updatedAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity
//                latestUpdateTitleTextView = findViewById(R.id.tv_latest_update_title)
//                latestUpdateTextView = findViewById(R.id.tv_latest_update)

                /* Views populated, Hiding the loader, Showing the main design */
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

            } catch (e: Exception) {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }
        }

    }
//    private fun updateDataAndVisualizations(sharedPreferences: SharedPreferences, calledFromCardView: Boolean) {
//        setCardText(sharedPreferences)
//    }

//    private fun setCardText(sharedPreferences: SharedPreferences) {
//        // Set Cards subtitle text
//        val latestUpdateTileText = "$codeSelected"
//        latestUpdateTitleTextView.text = latestUpdateTileText
//
//        // Format selected state from "New York" to "NEWYORK" or "All States" to "US"
//        val CITY = if (citySelected == "All States") "US" else citySelected.uppercase(Locale.ROOT)
//            .replace(" ", "")
//
//        // Set last updated text
//        latestUpdateTextView.text = sharedPreferences.getString("${CITY}_UPDATED", "Unknown")

        // Set total numbers
//        .text = sharedPreferences.getString("${CITY}_INFECTED", "Unknown")

        // Get new numbers
//        val newInfected = sharedPreferences.getString("${CITY}_NEW_INFECTED", "0")
//        val newVaccinated = sharedPreferences.getString("${state}_NEW_VACCINATED", "0")
//        val newDeaths = sharedPreferences.getString("${state}_NEW_DEATHS", "0")

        // Format new numbers
//        val formattedNewInfected = if (newInfected!!.replace(",", "").toInt() <= 0) "No Changes" else "+ $newInfected"
//        val formattedNewVaccinated = if (newVaccinated!!.replace(",", "").toInt() <= 0) "No Changes" else "+ $newVaccinated"
//        val formattedNewDeaths = if (newDeaths!!.replace(",", "").toInt() <= 0) "No Changes" else "+ $newDeaths"

        // Set new numbers
//        newInfectedTextView.text = formattedNewInfected
//        newVaccinatedTextView.text = formattedNewVaccinated
//        newDeathsTextView.text = formattedNewDeaths
    }


