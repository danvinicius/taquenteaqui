package exemplo.taquenteaqui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Inicialize os componentes da tela
        val imageView = findViewById<ImageView>(R.id.detailImageView)
        val txtDescription = findViewById<TextView>(R.id.detailDescription)
        val txtDateTime = findViewById<TextView>(R.id.detailDateTime)
        val txtUsername = findViewById<TextView>(R.id.detailUsername)
        val txtCoordinates = findViewById<TextView>(R.id.detailCoordinates)

        // Receber os dados da intent
        val imageBytes = intent.getByteArrayExtra("image")
        val description = intent.getStringExtra("description")
        val dateTime = intent.getStringExtra("dateTime")
        val username = intent.getStringExtra("username")
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        // Configurar os elementos da tela
        imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes?.size ?: 0))
        txtDescription.text = description
        txtDateTime.text = dateTime
        txtUsername.text = username

        // Exibir as coordenadas de latitude e longitude
        val coordinatesText = "Latitude: $latitude, Longitude: $longitude"
        txtCoordinates.text = coordinatesText
    }

    // Função para voltar para a SecondActivity
    fun onBackPressed(view: View) {
        // Voltar para a SecondActivity
        super.onBackPressed()
    }
}
