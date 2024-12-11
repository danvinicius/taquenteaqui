package exemplo.taquenteaqui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DenunciaDetailsActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var txtDescription: TextView
    private lateinit var txtUserName: TextView
    private lateinit var txtDateTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_denuncia_details)

        imageView = findViewById(R.id.imageView)
        txtDescription = findViewById(R.id.txtDescription)
        txtUserName = findViewById(R.id.txtUserName)
        txtDateTime = findViewById(R.id.txtDateTime)

        // Receber os dados da Intent
        val description = intent.getStringExtra("description")
        val imageBytes = intent.getByteArrayExtra("image")
        val userName = intent.getStringExtra("userName")
        val dateTime = intent.getStringExtra("dateTime")

        // Exibir os dados
        txtDescription.text = description
        txtUserName.text = "Denunciado por: $userName"
        txtDateTime.text = "Data/Hora: $dateTime"

        imageBytes?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            imageView.setImageBitmap(bitmap)
        }
    }
}
