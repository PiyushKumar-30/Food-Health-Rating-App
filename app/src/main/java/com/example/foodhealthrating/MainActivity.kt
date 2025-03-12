package com.example.foodhealthrating

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var tvHealthScore: TextView
    private lateinit var tvNutritionalInfo: TextView

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var processingBarcode = false
    private var lastScannedBarcode: String? = null

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "MainActivity"
        // Replace with your actual USDA API key
        private const val USDA_API_KEY = "YOUR_USDA_API_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.preview_view)
        tvHealthScore = findViewById(R.id.tvHealthScore)
        tvNutritionalInfo = findViewById(R.id.tvNutritionalInfo)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImageProxy(imageProxy)
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                Log.e(TAG, "Camera binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        // Avoid processing if already handling a barcode.
        if (processingBarcode) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcodeValue = barcodes[0].rawValue
                    if (barcodeValue.isNullOrEmpty()) {
                        imageProxy.close()
                        return@addOnSuccessListener
                    }
                    // Ignore if this barcode is the same as the last scanned one.
                    if (barcodeValue == lastScannedBarcode) {
                        imageProxy.close()
                        return@addOnSuccessListener
                    }
                    // New barcode detected—set state and process.
                    processingBarcode = true
                    lastScannedBarcode = barcodeValue
                    Log.d(TAG, "Barcode detected: $barcodeValue")
                    fetchNutritionalInfoOpenFoodFacts(barcodeValue)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Barcode scanning failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun fetchNutritionalInfoOpenFoodFacts(barcode: String) {
        val url = "https://in.openfoodfacts.org/api/v0/product/$barcode.json"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Open Food Facts request failed", e)
                fetchNutritionalInfoUSDA(barcode)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseData = it.body?.string()
                    // If the response is not as expected, fallback to USDA.
                    if (!it.isSuccessful || responseData.isNullOrEmpty() || !responseData.contains("\"product\"")) {
                        fetchNutritionalInfoUSDA(barcode)
                        return
                    }
                    parseAndDisplayNutritionalData(responseData)
                }
            }
        })
    }

    private fun fetchNutritionalInfoUSDA(barcode: String) {
        val url = "https://api.nal.usda.gov/fdc/v1/foods/search?api_key=$USDA_API_KEY&query=$barcode&dataType=Branded"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "USDA request failed", e)
                displayNoDataFoundMessage()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseData = it.body?.string()
                    if (!it.isSuccessful || responseData.isNullOrEmpty() || !responseData.contains("\"foods\"")) {
                        displayNoDataFoundMessage()
                        return
                    }
                    parseAndDisplayNutritionalData(responseData)
                }
            }
        })
    }

    private fun parseAndDisplayNutritionalData(responseData: String?) {
        responseData?.let {
            try {
                val jsonObject = JSONObject(it)
                var sugar = 0.0
                var fat = 0.0
                var sodium = 0.0
                var fiber = 0.0
                var protein = 0.0

                if (jsonObject.has("product")) {
                    val product = jsonObject.optJSONObject("product") ?: run {
                        displayNoDataFoundMessage()
                        return
                    }
                    val nutriments = product.optJSONObject("nutriments") ?: run {
                        displayNoDataFoundMessage()
                        return
                    }
                    sugar = nutriments.optDouble("sugars_100g", 0.0)
                    fat = nutriments.optDouble("fat_100g", 0.0)
                    sodium = nutriments.optDouble("sodium_100g", 0.0) * 1000
                    fiber = nutriments.optDouble("fiber_100g", 0.0)
                    protein = nutriments.optDouble("proteins_100g", 0.0)
                } else if (jsonObject.has("foods")) {
                    val foodsArray: JSONArray = jsonObject.getJSONArray("foods")
                    if (foodsArray.length() == 0) {
                        displayNoDataFoundMessage()
                        return
                    }
                    val food = foodsArray.getJSONObject(0)
                    val nutrientsArray: JSONArray = food.optJSONArray("foodNutrients") ?: run {
                        displayNoDataFoundMessage()
                        return
                    }
                    for (i in 0 until nutrientsArray.length()) {
                        val nutrient = nutrientsArray.getJSONObject(i)
                        val nutrientId = nutrient.optInt("nutrientId", -1)
                        val value = nutrient.optDouble("value", 0.0)
                        when (nutrientId) {
                            269 -> sugar = value
                            1004 -> fat = value
                            1093 -> sodium = value * 1000
                            1079 -> fiber = value
                            1003 -> protein = value
                        }
                    }
                } else {
                    displayNoDataFoundMessage()
                    return
                }

                val healthScore = computeHealthScore(sugar, fat, sodium, fiber, protein)
                runOnUiThread {
                    tvHealthScore.text = "Health Score: $healthScore / 10"
                    tvNutritionalInfo.text = """
                        Nutritional Info:
                        - Sugar: $sugar g
                        - Fat: $fat g
                        - Sodium: $sodium mg
                        - Fiber: $fiber g
                        - Protein: $protein g
                    """.trimIndent()
                }
                resetScanningAfterDelay()
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing nutritional data", e)
                displayNoDataFoundMessage()
            }
        }
    }

    private fun resetScanningAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            processingBarcode = false
            lastScannedBarcode = null  // Allow the same barcode to be processed again after the delay.
        }, 2000)
    }

    private fun computeHealthScore(
        sugar: Double,
        fat: Double,
        sodium: Double,
        fiber: Double,
        protein: Double
    ): Int {
        var score = 0
        if (sugar < 10) score += 2
        if (fat < 5) score += 2
        if (sodium < 200) score += 2
        if (fiber >= 3) score += 2
        if (protein >= 5) score += 2
        return score.coerceAtMost(10)
    }

    private fun displayNoDataFoundMessage() {
        runOnUiThread {
            tvNutritionalInfo.text = "No data found"
            tvHealthScore.text = ""
            Toast.makeText(this@MainActivity, "Product information not available", Toast.LENGTH_SHORT).show()
        }
        resetScanningAfterDelay()
    }
}
