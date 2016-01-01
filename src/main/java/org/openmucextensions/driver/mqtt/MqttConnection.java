package org.openmucextensions.driver.mqtt;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.ByteArrayValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQTT connection to an external MQTT broker, enables to subscribe topics from the broker
 * 
 * @author Mike Pichler
 *
 */
public class MqttConnection implements Connection, MqttCallback {

	private static final Logger logger = LoggerFactory.getLogger(MqttDriver.class);
	private final MqttClient client;
	
	// OpenMUC listener
	private RecordsReceivedListener listener = null;
	private List<ChannelRecordContainer> containers = null;
	
	// properties for sending messages
	private int qos = 1;
	private boolean retained = true;
	
	/**
	 * Creates a new <code>MqttConnection</code> instance and an internal <code>MqttClient</code>.
	 * Call {@link MqttConnection#connect(String)}) afterwards to establish the connection to an
	 * external MQTT broker.
	 * 
	 * @param serverURI
	 * @param clientId
	 * @throws MqttException
	 */
	public MqttConnection(final String serverURI, String clientId) throws MqttException {

		if(clientId==null||clientId.isEmpty()) clientId = MqttClient.generateClientId();
		
		client = new MqttClient(serverURI, clientId);
		client.setCallback(this);
	}
	
	public void connect(final Map<String, String> properties) throws MqttSecurityException, MqttException {		
				
		if(properties.containsKey("qos")) {
			try {
				int qos = Integer.parseInt(properties.get("qos"));
				if(qos>-1&&qos<3) this.qos = qos;
				else logger.warn("Invalid qos property value: {} (must be between 0 and 2)", qos);
			} catch (NumberFormatException e) {
				logger.warn("Property qos cannot be parsed as integer (is {})", properties.get("qos")) ;
			}
		}
		
		if(properties.containsKey("retained")) this.retained = Boolean.parseBoolean(properties.get("retained"));
		
		MqttConnectOptions options = getOptions(properties);
		client.connect(options);
		logger.debug("Connected to MQTT broker {} with client id {}", client.getServerURI(), client.getClientId());
	}
	
	@Override
	public List<ChannelScanInfo> scanForChannels(String settings)
			throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ConnectionException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
			throws UnsupportedOperationException, ConnectionException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void startListening(List<ChannelRecordContainer> containers, RecordsReceivedListener listener)
			throws UnsupportedOperationException, ConnectionException {
		
		if(containers == null) return;
		
		if(!client.isConnected()) throw new ConnectionException("Client is not connected to MQTT broker");
		
		// according to the OpenMUC specification, the new subscription list replaces the old one
        removeSubscriptions();
        
		this.containers = containers;
        this.listener = listener;
        
        String[] topics = new String[containers.size()];
        
        for(int index = 0; index < containers.size(); index++) {
        	topics[index] = containers.get(index).getChannelAddress();
        }
        
        try {
			client.subscribe(topics);
			logger.debug("Subscribed to {} topic(s) at MQTT broker {}", topics.length, client.getServerURI());
		} catch (MqttException e) {
			throw new ConnectionException("Error while trying to subscribe to multiple topics", e);
		}
	}
	
	private void removeSubscriptions() {
		if(containers != null) {
			for (ChannelRecordContainer channelRecordContainer : containers) {
				try {
					client.unsubscribe(channelRecordContainer.getChannelAddress());
				} catch (MqttException ignore) { }
			}
		}
	}

	@Override
	public Object write(List<ChannelValueContainer> containers, Object containerListHandle)
			throws UnsupportedOperationException, ConnectionException {
		
		for (ChannelValueContainer channelValueContainer : containers) {
			
			if (channelValueContainer.getValue() != null) {
				String content = channelValueContainer.getValue().asString();
				String topic = channelValueContainer.getChannelAddress();
				MqttMessage message = new MqttMessage(content.getBytes());
				message.setQos(qos);
				message.setRetained(retained);
				try {
					client.publish(topic, message);
					channelValueContainer.setFlag(Flag.VALID);
				} catch (MqttException e) {
					throw new ConnectionException("Error while trying to send message over MQTT", e);
				} 
			} else {
				channelValueContainer.setFlag(Flag.CANNOT_WRITE_NULL_VALUE);
			}
		}
		
		return null;
	}

	@Override
	public void disconnect() {
		if(client.isConnected())
			try {
				client.disconnect();
				logger.debug("Disconnected client {} from MQTT broker {}", client.getClientId(), client.getServerURI());
			} catch (MqttException e) {
				logger.warn("Error while trying to disconnect from MQTT broker: {}", e.getMessage());
			}
	}

	@Override
	public void connectionLost(Throwable cause) {
		logger.error("Connection to MQTT broker {} lost: {}", client.getServerURI(), cause.getMessage());
		if(listener != null) listener.connectionInterrupted("mqtt", this);
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
		long timestamp = new Date().getTime();
		
		for (ChannelRecordContainer channelRecordContainer : containers) {
			if(channelRecordContainer.getChannelAddress().equals(topic)) {
				channelRecordContainer.setRecord(new Record(new ByteArrayValue(message.getPayload()), timestamp));
				break;
			}
		}
		
		if(listener != null) listener.newRecords(containers);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// this callback will not be used
	}
	
	private MqttConnectOptions getOptions(final Map<String, String> properties) {
		
		MqttConnectOptions options = new MqttConnectOptions();

		for (String key : properties.keySet()) {
			
			String value = properties.get(key);
			
			if(key.equals("keepAliveInterval")) options.setKeepAliveInterval(Integer.parseInt(value));
			if(key.equals("userName")) options.setUserName(value);
			if(key.equals("password")) options.setPassword(value.toCharArray());
			if(key.equals("cleanSession")) options.setCleanSession(Boolean.parseBoolean(value));
			if(key.equals("connectionTimeout")) options.setConnectionTimeout(Integer.parseInt(value));
			
		}
		
		return options;
	}
}
