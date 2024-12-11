package exemplo.taquenteaqui

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import exemplo.taquenteaqui.SecondActivity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

class MqttManager(context: Context) {
    private val brokerUrl = BuildConfig.BROKEN_URL
    private val clientId = "android_${UUID.randomUUID()}"

    // criação do cliente MQTT com persistência em memória
    private val mqttClient = MqttAndroidClient(context, brokerUrl, clientId, MemoryPersistence())
    private var TAG = "MQTT"
    private val context: Context = context // Store the context

    private var textView: TextView? = null


    // Adicione um método para configurar o TextView
    fun setTextView(textView: TextView) {
        this.textView = textView
    }


    init {
        // configura o cliente MQTT
        configureMqttClient()
    }

    // configura o callback MQTT
    private fun configureMqttClient() {
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                message?.let {
                    val mensagemRecebida = it.toString()

                    try {
                        val json = JSONObject(mensagemRecebida)
                        val latitude = json.getDouble("latitude")
                        val longitude = json.getDouble("longitude")
                        val description = json.getString("description")

                        // Atualiza o mapa
                        SecondActivity.instance?.updateMapLocation(latitude, longitude)

                        val toastMsg = "Nova denúncia: $description em ($latitude, $longitude)"
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "Erro ao processar a mensagem MQTT", e)
                    }
                }
            }


            override fun connectionLost(cause: Throwable?) {
                // callback chamado quando a conexão MQTT é perdida
                Log.d(TAG, "Conexão perdida: ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // callback chamado quando uma mensagem é entregue com sucesso
            }
        })
    }

    // função para conectar ao broker MQTT
    fun connect(context: MainActivity) {
        if (!mqttClient.isConnected) {
            val options = MqttConnectOptions()
            options.userName = BuildConfig.MQTT_USERNAME
            options.password = BuildConfig.MQTT_PASSWORD.toCharArray()

            try {
                // conecta ao broker MQTT
                mqttClient.connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        // callback chamado em caso de conexão bem-sucedida
                        Log.d(TAG, "Conexão bem-sucedida")

                        // inscreve-se em um tópico e publica uma mensagem de teste
                        subscribe("/taquenteaqui/denuncias")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        // callback chamado em caso de falha na conexão
                        Log.e(TAG, "Falha na conexão", exception)
                        exception?.printStackTrace()
                    }
                })
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        } else {
            // o cliente MQTT já está conectado
            val message = "O cliente MQTT já está conectado."
            val show = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "O cliente MQTT já está conectado.")
        }
    }

    // função para se inscrever em um tópico MQTT
    fun subscribe(topic: String, qos: Int = 1) {
        try {
            if (mqttClient.isConnected) {
                // se o cliente MQTT está conectado, inscreve-se no tópico
                mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        // callback chamado em caso de sucesso na inscrição
                        Log.d(TAG, "Inscrito em $topic")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        // callback chamado em caso de falha na inscrição
                        Log.d(TAG, "Falha ao se inscrever em $topic")
                    }
                })
            } else {
                // o cliente MQTT não está conectado
                Log.d(TAG, "O cliente MQTT não está conectado.")
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }




    // função para publicar uma mensagem em um tópico MQTT
    fun publish(topic: String,  imageBytes: ByteArray, description: String, userName: String?, latitude: String, longitude: String, dataHora: String, qos: Int = 1, retained: Boolean = false) {
        try {
            if (mqttClient.isConnected) {
                val imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                // se o cliente MQTT está conectado, publica a mensagem no tópico
                val payload = """
                    {
                        "userName": "$userName",
                        "description": "$description",
                        "image": "$imageBase64",
                        "latitude": "$latitude",
                        "longitude": "$longitude",
                        "dataHora": "$dataHora"
                    }
                """.trimIndent()

                val message = MqttMessage(payload.toByteArray())
                message.qos = qos
                message.isRetained = true

                mqttClient.publish(topic, message, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        // callback chamado em caso de sucesso na publicação
                        Log.d(TAG, "Imagem publicada com sucesso.")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        // callback chamado em caso de falha na publicação
                        Log.e(TAG, "Falha ao publicar a imagem.", exception)
                    }
                })
            } else {
                // o cliente MQTT não está conectado
                Log.d(TAG, "O cliente MQTT não está conectado.")
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    // função para desconectar do broker MQTT
    fun disconnect() {
        try {
            if (mqttClient.isConnected) {
                // se o cliente MQTT está conectado, desconecta
                mqttClient.disconnect(null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        // callback chamado em caso de sucesso na desconexão
                        Log.d(TAG, "Desconectado")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        // callback chamado em caso de falha na desconexão
                        Log.d(TAG, "Falha ao desconectar")
                    }
                })
            } else {
                // o cliente MQTT não está conectado
                Log.d(TAG, "O cliente MQTT não está conectado.")
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    // verifica se o cliente MQTT está conectado
    fun isConnected(): Boolean {
        return mqttClient.isConnected
    }
}






