package com.example.networkanalyzer

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.Spanned
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.util.*

class NetworkInfoActivity : AppCompatActivity() {

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiListView: ListView
    private lateinit var barChart: BarChart
    private val wifiScanResults = mutableListOf<ScanResult>()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_info)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiListView = findViewById(R.id.wifiListView)
        barChart = findViewById(R.id.barChart)

        setupBarChart()
        try {
            if (hasLocationPermission()) {
                startWifiScan()
            } else {
                requestLocationPermission()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the exception, show an error message, or perform recovery actions.
        }
    }

    private fun hasLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
    }

    private fun setupBarChart() {
        // Initialize the BarChart
        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)
        barChart.description.isEnabled = false
        barChart.setMaxVisibleValueCount(60)
        barChart.setPinchZoom(false)
        barChart.setDrawGridBackground(false)
    }

    private fun startWifiScan() {
        val wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                wifiScanResults.clear()
                wifiScanResults.addAll(wifiManager.scanResults.distinctBy { it.SSID })
                displayWifiListAndGraph()
            }
        }

        registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        handler.postDelayed({
            wifiManager.startScan()
            handler.postDelayed({ unregisterReceiver(wifiReceiver) }, 5000)
        }, 1000)
    }

    private fun displayWifiListAndGraph() {
        // Assign distinct colors to networks
        val networkColors = generateDistinctColors(wifiScanResults.size)

        // Create a list of BarEntry objects for the signal strengths with distinct colors
        val entries = wifiScanResults.mapIndexed { index, result ->
            val level = result.level
            BarEntry(index.toFloat(), level.toFloat(), networkColors[index])
        }

        // Create a BarDataSet with the signal strengths and distinct colors
        val dataSet = BarDataSet(entries, "Signal Strength (dBm)")
        dataSet.colors = networkColors.toIntArray().toList() // Ensure graph colors match the list colors

        // Add data to the BarChart
        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.invalidate()

        // Create a list of Spanned objects with HTML-formatted text
        val wifiNetworks = wifiScanResults.mapIndexed { index, result ->
            val colorHex = convertColorToHexString(networkColors[index])
            val formattedText = "<font color='$colorHex'>${result.SSID}</font> (${result.level} dBm)"
            Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY) as Spanned
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, wifiNetworks)
        wifiListView.adapter = adapter
    }

    private fun generateDistinctColors(count: Int): List<Int> {
        val colors = mutableListOf<Int>()
        val random = Random()
        for (i in 0 until count) {
            val color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256))
            colors.add(color)
        }
        return colors
    }

    private fun convertColorToHexString(color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }
}
