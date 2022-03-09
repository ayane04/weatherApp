package com.application.weatherapp

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat
import com.application.weatherapp.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEBUG_TAG = "AsyncSample"
        private const val WEATHERINFO_URL = "https://api.openweathermap.org/data/2.5/weather?lang=en"
        private const val APP_ID = "06c921750b9a82d8f5d1294e1586276f"
        private lateinit var binding: ActivityMainBinding
        private var citySelected = "Select City"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter : ArrayAdapter<*> = ArrayAdapter.createFromResource(this, R.array.cities, R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter

//        val lvCityList = findViewById<ListView>(R.id.lvCityList)
//        val from  = arrayOf("name")
//        val to = intArrayOf(android.R.id.text1)
//        val adapter = SimpleAdapter(this@MainActivity, _list, android.R.layout.simple_list_item_1, from, to)
//        lvCityList.adapter = adapter
//        lvCityList.onItemClickListener = ListItemClickListener()

    binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val spinnerParent = parent as Spinner
            val item= spinnerParent.selectedItem as String
            citySelected = parent?.getItemAtPosition(position).toString()
            citySelected?.let {
                val urlFull = "$WEATHERINFO_URL&q=$citySelected&appid=$APP_ID"
                receiveWeatherInfo(urlFull)
            }
        }
        override fun onNothingSelected(p0: AdapterView<*>?) {
        }

    @UiThread
    private fun receiveWeatherInfo(urlFull: String) {
        val handler = HandlerCompat.createAsync(mainLooper)
        val backgroundReceiver = WeatherInfoBackgroundReceiver(handler, urlFull)
        val executeService = Executors.newSingleThreadExecutor()
        executeService.submit(backgroundReceiver)
    }

    private inner class WeatherInfoBackgroundReceiver(handler: Handler, url: String): Runnable {
        private val _handler = handler
        private val _url = url

        @WorkerThread
        override fun run() {

            var result = ""
            val url = URL(_url)
            val con = url.openConnection() as? HttpURLConnection

            con?.let {
                try {
                    it.connectTimeout = 1000
                    it.readTimeout = 1000
                    it.requestMethod = "GET"
                    it.connect()
                    val stream = it.inputStream

                    result = is2String(stream)
                    stream.close()
                }
                catch(ex: SocketTimeoutException) {
                    Log.w(DEBUG_TAG, "通信タイムアウト", ex)
                }

                it.disconnect()
            }
            val postExecutor = WeatherInfoPostExecutor(result)
            _handler.post(postExecutor)
        }

        private fun is2String(stream: InputStream): String {
            val sb = StringBuilder()
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            var line = reader.readLine()
            while(line != null) {
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        }
    }

    private inner class WeatherInfoPostExecutor(result: String): Runnable {
        private val _result = result

        @UiThread
        override fun run() {

            val JSONObject = JSONObject(_result)
//                val wind = jsonObj.getJSONObject("wind")
//                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

            val rootJSON = JSONObject(_result)
            val main = JSONObject.getJSONObject("main")
            val sys = JSONObject.getJSONObject("sys")
            val wind = JSONObject.getJSONObject("wind")
            val cityName = rootJSON.getString("name")
            val coordJSON = rootJSON.getJSONObject("coord")
            val latitude = coordJSON.getString("lat")
            val longitude = coordJSON.getString("lon")
            val weatherJSONArray = rootJSON.getJSONArray("weather")
            val weatherJSON = weatherJSONArray.getJSONObject(0)
            val weather = weatherJSON.getString("description")
            val telop = "${cityName}"

            val mainObj: JSONObject = JSONObject.getJSONObject("main")
            val temp = "" + (mainObj.getDouble("temp") - 273.15f).toInt() + "℃"
            val tempMax = "Min Temp: " + (mainObj.getDouble("temp_min") - 273.15f).toInt() + "℃"
            val tempMin = "Max Temp: " + (mainObj.getDouble("temp_max") - 273.15f).toInt() + "℃"
            val pressure = main.getString("pressure")
            val humidity = main.getString("humidity")
            val sunrise: Long = sys.getLong("sunrise")
            val sunset: Long = sys.getLong("sunset")
            val windSpeed = wind.getString("speed")


            val updatedAt: Long = JSONObject.getLong("dt")
            val updatedAtText =
                "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(
                    Date(updatedAt * 1000)
                )

            val desc = "${weather} "
            val location = "Your location: ${latitude},${longitude}"
//            val tvWeatherTelop = findViewById<Spinner>(R.id.tvWeatherTelop)
            val tvWeatherDesc = findViewById<TextView>(R.id.tvWeatherDesc)
            val tvLocation = findViewById<TextView>(R.id.tvLocation)

            findViewById<TextView>(R.id.temp).text = temp.toString()
            findViewById<TextView>(R.id.temp_min).text = tempMin.toString()
            findViewById<TextView>(R.id.temp_max).text = tempMax.toString()
            findViewById<TextView>(R.id.updated_at).text = updatedAtText
            findViewById<TextView>(R.id.sunrise).text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
            findViewById<TextView>(R.id.sunset).text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
            findViewById<TextView>(R.id.wind).text = windSpeed
            findViewById<TextView>(R.id.pressure).text = pressure
            findViewById<TextView>(R.id.humidity).text = humidity

//            tvWeatherTelop.textDirection = telop
            binding.textView.text=telop
            tvWeatherDesc.text = desc
            tvLocation.text = location

        }
    }
    }

    }
//    private inner class ListItemClickListener: AdapterView.OnItemClickListener {
//        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//            val item = _list.get(position)
//            val q = item.get("q")
//            q?.let {
//                val urlFull = "$WEATHERINFO_URL&q=$q&appid=$APP_ID"
//                receiveWeatherInfo(urlFull)
//            }
//        }
//    }
}



