package paho;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Subscriber implements MqttCallback {

	public static String serverURI = "tcp://iot.eclipse.org:1883";
	public static String clientId = "MQTTTestSubscriber13122015";
	
	public static void main(String[] args) throws MqttException {
		new Subscriber().startListening();
	}

	public void startListening() throws MqttException {
		
		String[] topics = {"9cb2bc6d-565f-470f-a556-046868374d87/VoltageChannel", "9cb2bc6d-565f-470f-a556-046868374d87/CurrentChannel"};
		
		MqttClient client = new MqttClient(serverURI, clientId);
		client.connect();
		client.setCallback(this);
		client.subscribe(topics);
		
		System.out.println("Waiting for messages...");
	}
	
	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage message)
			throws Exception {
		System.out.println("Received message from topic " + topic + ": " + message.toString());
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		
	}
	
}
