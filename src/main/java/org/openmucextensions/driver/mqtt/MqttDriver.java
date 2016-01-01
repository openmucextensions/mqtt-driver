package org.openmucextensions.driver.mqtt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enables to subscribe to MQTT topics provided by an external broker
 * 
 * @author Mike Pichler
 *
 */
public class MqttDriver implements DriverService {

	private final static Logger logger = LoggerFactory.getLogger(MqttDriver.class);
	
	private final static DriverInfo driverInfo = new DriverInfo("mqtt", // id
			// description
			"MQTT communication protocol driver",
			// device address
			"External MQTT broker URI, e.g. tcp://iot.eclipse.org:1883",
			// settings
			"See documentation to get a list of possible properties",
			// channel address
			"MQTT topic",
			// device scan parameters
			"Device scan is not supported");
	
	protected void activate(ComponentContext context) {
		logger.info("MQTT driver activated");
	}
	
	protected void deactivate(ComponentContext context) {
		logger.info("MQTT driver deactivated");
	}
	
	@Override
	public DriverInfo getInfo() {
		return driverInfo;
	}

	@Override
	public void scanForDevices(String settings, DriverDeviceScanListener listener)
			throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ScanInterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void interruptDeviceScan() throws UnsupportedOperationException {
		// device scan is not supported
	}

	@Override
	public Connection connect(String deviceAddress, String settings)
			throws ArgumentSyntaxException, ConnectionException {
		
		if(deviceAddress == null) throw new ArgumentSyntaxException("Device address is null");
		
		Map<String, String> properties = parseSettings(settings); // method is null-save
		
		try {
			
			MqttConnection connection = new MqttConnection(deviceAddress, properties.get("clientId"));
			connection.connect(properties);
			return connection;
			
		} catch (MqttException e) {
			throw new ConnectionException("Error while trying to connect to MQTT broker " + deviceAddress, e);
		}
	}
	
	/**
	 * Parses a settings (properties) string and returns a map containing all valid
	 * key/value combinations. Key/value pairs must have the format [key=value] and must be
	 * separated with a comma.<p>
	 * If the settings string is <code>null</code> or empty, an empty map will be returned.
	 * 
	 * @param settings the settings string
	 * @return a map containing all valid key/value combinations
	 */
	protected Map<String, String> parseSettings(final String settings) {
		
		Map<String, String> result = new HashMap<>();
		
		if (settings != null && !settings.isEmpty()) {
			String[] properties = settings.trim().split(",");
			for (String property : properties) {

				String[] tokens = property.trim().split("=");
				if (tokens.length == 2) {
					String key = tokens[0].trim();
					String value = tokens[1].trim();
					result.put(key, value);
				}
			} 
		}
		
		return result;
	}
}
