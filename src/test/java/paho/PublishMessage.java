package paho;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class PublishMessage {

	public static void main(String[] args) throws MqttException {
		
		String serverURI = "tcp://iot.eclipse.org:1883";
		String clientId = "MQTTTestClient13122015";
		MemoryPersistence persistence = new MemoryPersistence();
		
		MqttClient client = new MqttClient(serverURI, clientId, persistence);
		
		MqttConnectOptions options = new MqttConnectOptions();
		options.setCleanSession(true);
		
		client.connect(options);
		System.out.println("Connected to broker " + serverURI);
		
		String message = "Hello MQTT world";
		String topic = "13122015";
		
		MqttMessage mqttMessage = new MqttMessage(message.getBytes());
		mqttMessage.setQos(1);
		
		client.publish(topic, mqttMessage);
		System.out.println("Message " + message + " published");
		client.disconnect();
		
	}

}
