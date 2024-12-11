package exemplo.taquenteaqui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.util.Log
import kotlin.math.pow
import kotlin.math.sqrt

class AccelerometerManager(
    context: Context,
    private val fallDetectionListener: FallDetectionListener,
    private val locationManager: LocationManager,
    private val mqttManager: MqttManager
) : SensorEventListener {

    interface FallDetectionListener {
        fun onFallDetected(latitude: Double, longitude: Double)
    }

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var fallDetected: Boolean = false
    private var lastAcceleration: Double = 0.0
    private var lastTime: Long = System.currentTimeMillis()
    private val handler = Handler()

    init {
        if (accelerometer == null) {
            throw UnsupportedOperationException("Acelerômetro não está disponível no dispositivo.")
        }
    }

    fun startListening() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stopListening() {
        Log.d("AccelerometerManager", "Parando a escuta do acelerômetro")
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Não estamos preocupados com mudanças na precisão do sensor neste exemplo
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == accelerometer) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = (currentTime - lastTime) / 1000.0 // Delta de tempo em segundos

            // cálculo da aceleração usando a fórmula Euclidiana
            val acceleration =
                sqrt(event.values[0].toDouble().pow(2.0) + event.values[1].toDouble().pow(2.0) + event.values[2].toDouble().pow(2.0))

            // calcular a mudança na velocidade usando a aceleração
            val deltaVelocity = (acceleration + lastAcceleration) / 2 * deltaTime

            // atualizar a última aceleração e o último tempo
            lastAcceleration = acceleration
            lastTime = currentTime

            Log.d("AccelerometerManager", "Aceleração X: ${event.values[0]}, Y: ${event.values[1]}, Z: ${event.values[2]}")

            // chamada ao método de detecção de queda
            detectFall(deltaVelocity, acceleration, AppGlobals.minha_latitude, AppGlobals.minha_longitude)
        }
    }

    // método privado para a lógica de detecção de queda
    private fun detectFall(deltaVelocity: Double, acceleration: Double, latitude: Double, longitude: Double) {
        // implementar a lógica de detecção de queda, ainda não tá pronto, precisa de ajuste sobre a aceleração, a parada do nada
        //e coisas do tipo.
        // deltaVelocity < -4.0 && acceleration > 10.0
        if (acceleration > 20.0) {
            // Verificar se a queda já foi detectada
            if (!fallDetected) {
                // Chamar o callback de detecção de queda
                fallDetectionListener.onFallDetected(latitude, longitude)

                // Adicionar a mensagem de log dentro do AccelerometerManager
                Log.d("FallDetection", "Queda detectada!")

                // Chamar o método getLastLocation da classe LocationManager
                locationManager.getLastLocation()
                //SecondActivity.updateMapLocation()

                // Adicionar a lógica para publicar a mensagem MQTT aqui
                publishFallMessage()

                // Defina o sinalizador de queda como verdadeiro
                fallDetected = true

                // Parar a escuta do acelerômetro após a detecção da queda
                stopListening()

            }
        }
    }

    private fun publishFallMessage() {
        // Verificar se a mensagem já foi publicada
        if (!fallDetected) {
            // Substitua "seu_topico" pelo tópico MQTT desejado
            val topic = "/taquenteaqui/denuncias"
            val message = "Queda detectada! Nome: ${AppGlobals.userName} Latitude: ${AppGlobals.minha_latitude} Longitude: ${AppGlobals.minha_longitude} "

            // Verificar se o cliente MQTT está conectado antes de tentar publicar
            if (mqttManager.isConnected()) {
                // Publicar a mensagem MQTT

                Log.d("MQTT", "Mensagem publicada com sucesso no tópico $topic")
            } else {
                Log.e("MQTT", "Erro: Cliente MQTT não está conectado. A mensagem não foi publicada.")
            }

            // Ajustar fallDetected aqui para garantir que a lógica de detecção de queda só seja acionada uma vez
            fallDetected = true
        }
    }
}
