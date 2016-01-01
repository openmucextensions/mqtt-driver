package org.openmucextensions.driver.mqtt;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class TestMqttDriver {

	private MqttDriver connection;
	
	@Before
	public void setUp() throws Exception {
		connection = new MqttDriver();
	}

	@Test
	public void testParseSettings() {
		
		String settings = "property1=value1, property2 = value2 , property3=  value3, invalidproperty, invalid=";
		Map<String, String> properties = connection.parseSettings(settings);
		
		assertThat(properties.size(), is(3));
		assertThat(properties.get("property1"), is("value1"));
		assertThat(properties.get("property2"), is("value2"));
		assertThat(properties.get("property3"), is("value3"));
	}
	
	@Test
	public void testEmptySettings() {
		
		Map<String, String> properties = connection.parseSettings(null);
		assertNotNull(properties);
		assertThat(properties.size(), is(0));
		
		properties = connection.parseSettings("");
		assertNotNull(properties);
		assertThat(properties.size(), is(0));	
	}

}
