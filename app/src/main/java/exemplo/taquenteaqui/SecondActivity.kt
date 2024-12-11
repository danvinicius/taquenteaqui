package exemplo.taquenteaqui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.MapView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import exemplo.taquenteaqui.AppGlobals.mqttManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SecondActivity : AppCompatActivity(), LocationManager.LocationCallback, OnMapReadyCallback {

    private lateinit var locationManager: LocationManager
    private lateinit var mapView: MapView
    private lateinit var imageView: ImageView
    private lateinit var edtDescription: EditText
    private lateinit var btnCapture: Button
    private lateinit var btnSend: Button

    private var capturedImage: Bitmap? = null
    private val REQUEST_CAMERA_PERMISSION = 100
    private val REQUEST_IMAGE_CAPTURE = 101

    private lateinit var googleMap: GoogleMap
    private var mapViewInitialized = false
    private var currentLocationMarker: Marker? = null

    // Adicione uma variável para armazenar a última localização
    private var lastKnownLocation: LatLng? = null

    private var isSensorStarted = false

    companion object {
        @Volatile
        internal var instance: SecondActivity? = null

        @JvmStatic
        fun getInstance(): SecondActivity? {
            return instance
        }

        @JvmStatic
        fun setInstance(activity: SecondActivity?) {
            instance = activity
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        SecondActivity.setInstance(this)

        // Inicializar a classe LocationManager
        locationManager = LocationManager(this, this)

        // Solicitar permissão de localização
        locationManager.requestLocationPermission()

        // Inicialize o MapView
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this) // Mantenha esta linha aqui

        imageView = findViewById(R.id.imageView)
        edtDescription = findViewById(R.id.edtDescription)
        btnCapture = findViewById(R.id.btnCapture)
        btnSend = findViewById(R.id.btnSend)

        // Solicita permissão à câmera
        requestCameraPermission()

        btnCapture.setOnClickListener {
            openCamera()
        }

        btnSend.setOnClickListener {
            sendPhoto()
        }

    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        mapViewInitialized = true


        Log.d("MapReady", "Mapa está pronto - sendo chamado")


        // Verificar se a permissão de localização está concedida
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permissão concedida, obter a última localização
            locationManager.getLastLocation()

            // Verificar se há uma última localização conhecida
            lastKnownLocation?.let {


                // Se houver, chame updateMapLocation
                // FOI O ÚLTIMO A SAIR: updateMapLocation(it.latitude, it.longitude)
                // Limpe a última localização conhecida após usá-la
                lastKnownLocation = null
            }
        } else {
            // Permissão não concedida, solicitar permissão (isso deveria ser tratado anteriormente)
            Log.e("SecondActivity", "Location permission not granted.")
        }
    }

    private fun obterDataHoraAtual(): String {
        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formato.format(Date())
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            capturedImage = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(capturedImage)
        }
    }

    private fun sendPhoto() {
        val description = edtDescription.text.toString()

        if (capturedImage != null && description.isNotBlank()) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            capturedImage!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()

            val dataHora = obterDataHoraAtual()

            // Publica a imagem e descrição
            AppGlobals.mqttManager?.publish(
                "/taquenteaqui/denuncias",
                imageBytes,
                description,
                AppGlobals.userName,
                AppGlobals.minha_latitude.toString(),
                AppGlobals.minha_longitude.toString(),
                dataHora
            )

            // Limpar os campos após o envio da denúncia
            edtDescription.text.clear()  // Limpa a descrição
            imageView.setImageBitmap(null)  // Limpa a imagem capturada

            // Limpar a variável de imagem capturada
            capturedImage = null

            Toast.makeText(this, "Denúncia enviada!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Preencha a descrição e capture uma foto.", Toast.LENGTH_SHORT)
                .show()
        }
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()

        isSensorStarted = false
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onLocationPermissionGranted() {
        Log.d("SecondActivity", "Location permission granted in SecondActivity")

        // Verificar se a permissão de localização está concedida
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permissão concedida, obter a última localização
            locationManager.getLastLocation()
        } else {
            // Permissão não concedida, solicitar permissão (isso deveria ser tratado anteriormente)
            Log.e("SecondActivity", "Location permission not granted.")
        }
    }


    fun updateMapLocation(latitude: Double, longitude: Double) {
        Log.d("MapUpdate", "UpdateMapLocation chamado com latitude: $latitude, longitude: $longitude")
        if (!mapViewInitialized) {
            Log.d("MapUpdate", "MapView não está inicializado")

            // Se o mapa não estiver inicializado, armazene a última localização conhecida
            lastKnownLocation = LatLng(latitude, longitude)
            // Aguarde a inicialização do mapa antes de tentar atualizar a localização
            return
        }

        val currentLocation = LatLng(latitude, longitude)

        if (currentLocationMarker == null) {
            Log.d("MapUpdate", "Criando novo marcador")
            // Se o marcador ainda não foi criado, crie-o e adicione ao mapa
            val markerOptions = MarkerOptions().position(currentLocation).title("Sua Localização Atual")

            // Configurar o marcador para ser uma bolinha azul
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            currentLocationMarker = googleMap.addMarker(markerOptions)
        } else {
            Log.d("MapUpdate", "Atualizando posição do marcador")
            // Se o marcador já existe, apenas atualize sua posição
            currentLocationMarker?.position = currentLocation
        }

        // Mova a câmera para a nova localização
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
        // Opcional: ajuste o nível de zoom
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f))
    }



    var TAG = "DFSDF"
    fun processMqttMessage(mensagemRecebida: String) {
        Log.d(TAG, "Chegou no secundy: $mensagemRecebida")

        val newClickableText = TextView(this)

        newClickableText.text = mensagemRecebida

        // Adicione o novo TextView ao layout
        val layout = findViewById<LinearLayout>(R.id.linear_layout_container)
        layout.addView(newClickableText)


        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // Limpe o texto após 20 segundos
            layout.removeView(newClickableText)
        }, 20000)

        // Adicione um OnClickListener ao texto clicável - ITALIA
        newClickableText.setOnClickListener {
            // Analise a mensagem recebida para extrair latitude e longitude
            val message =  newClickableText.text.toString()

            // Encontrar o valor após "Latitude: " e "Longitude: "
            val latitudePattern = Regex("Latitude: ([-+]?\\d+\\.?\\d*)")
            val longitudePattern = Regex("Longitude: ([-+]?\\d+\\.?\\d*)")

            val latitudeMatch = latitudePattern.find(message)
            val longitudeMatch = longitudePattern.find(message)

            val novaLatitude = latitudeMatch?.groupValues?.get(1)?.toDoubleOrNull()
            val novaLongitude = longitudeMatch?.groupValues?.get(1)?.toDoubleOrNull()

            AppGlobals.minha_latitude = novaLatitude ?: 0.0
            AppGlobals.minha_longitude = novaLongitude ?: 0.0

            if (novaLatitude != null && novaLongitude != null) {
                Log.d("FallDetection", "Novos valores - Latitude: $novaLatitude, Longitude: $novaLongitude")
                Log.d("updateMapLocation", "Valores finais - minha_latitude: ${AppGlobals.minha_latitude}, minha_longitude: ${AppGlobals.minha_longitude}")
            } else {
                Log.e("FallDetection", "Valores de Latitude ou Longitude não encontrados ou não são válidos")
            }

            // Chame a função updateMapLocation com os valores seguros
            updateMapLocation(AppGlobals.minha_latitude, AppGlobals.minha_longitude)

        }
    }


}