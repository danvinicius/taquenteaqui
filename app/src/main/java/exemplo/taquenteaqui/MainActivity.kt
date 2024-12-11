package exemplo.taquenteaqui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText


class MainActivity : AppCompatActivity() {

    // inicialização do MqttManager usando lazy initialization
    private val mqttManager: MqttManager by lazy {
        MqttManager(this)
    }

    // inicialização do Button usando lazy initialization
    private val enterButton: Button by lazy {
        findViewById(R.id.enterButton)
    }

    val usernameEditText: EditText by lazy {
        findViewById(R.id.usernameEditText)
    }

    // inicialização do AccelerometerManager usando lazy initialization
    private lateinit var accelerometerManager: AccelerometerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // configura o listener de clique para o botão
        enterButton.setOnClickListener {
            try {
                Log.d("MyApp", "Tentando conectar ao broker MQTT.")
                // verifica se o cliente MQTT já está conectado
                if (!mqttManager.isConnected()) {
                    // conectar ao broker MQTT usando o MqttManager
                    mqttManager.connect(this)
                } else {
                    val intent = Intent(this, SecondActivity::class.java)
                    startActivity(intent)

                    // o cliente MQTT já está conectado, apenas registra no log
                    Log.d("MyApp", "O cliente MQTT já está conectado.")
                }

                // Configura a instância do MqttManager em AppGlobals
                AppGlobals.mqttManager = mqttManager

                // exibe uma mensagem de boas-vindas com os dados inseridos, ainda pensando no q fazer com os dados
                // porem ainda não faz nada!!!
                val message = "Bem-vindo ao TaQuenteAqui!"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                val intent = Intent(this, SecondActivity::class.java)

                startActivity(intent)

                val username = usernameEditText.text.toString()
                // Com o nome inserido pelo usuário definir o nome de AppGlobals.userName
                AppGlobals.userName = username

            } catch (e: Exception) {
                // em caso de erro, imprime o stack trace no log
                e.printStackTrace()
                Log.e("APP", "Erro ao clicar no botão Entrar", e)
            }
        }
    }

    // método chamado ao destruir a atividade
    override fun onDestroy() {
        super.onDestroy()
        // desconectar do broker MQTT ao destruir a atividade
        mqttManager.disconnect()
    }
}



