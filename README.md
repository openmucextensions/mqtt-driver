# MQTT Driver
OpenMUC driver for reading/writing values via MQTT. The bundle uses the [Eclipse Paho library](http://www.eclipse.org/paho/) internally. The following functionality is provided:

* Connecting to multiple MQTT brokers in one driver instance
* Optional TLS encryption of the broker connection
* Subscription to multiple topics
* Notification of OpenMUC framework listeners, if a new message arrives
* Sending messages by writing to a topic (represented by a channel)
* Outgoing messages will be persisted to a file for later delivery, if the broker connection has been lost
* Heartbeat signal (ping messages) to monitor broker connection

## Driver configuration
A OpenMUC device represents a connection to an external MQTT broker. The **device address** therefore is used as the broker's address. The address of the broker to connect to is specified as a URI. Two types of connection are supported `tcp://` for a TCP connection and `ssl://` for a TCP connection secured by SSL/TLS. For example:

```
tcp://localhost:1883
ssl://localhost:8883
```

If the port is not specified, it will default to 1883 for tcp:// URIs, and 8883 for ssl:// URIs. OpenMUC's device settings string can be used to pass some optional connection properties:

* `clientId` the client id that will be used when connecting to the external broker. If not set, a randomly created id will be used
* `keepAliveInterval` this value, measured in seconds, enables the client to detect if the broker is no longer available. If no message will be sent or received during this interval, the client sends a small "ping" message to ensure the broker is still reachable. The default value is 60 seconds
* `userName` the used name that will be used when connecting to the external broker
* `password` the password that will be used when connecting to the external broker
* `cleanSession` this flag sets whether the client and server should remember state across restarts and reconnects. Possible values are `true` and `false`, the default is `true`
* `connectionTimeout` this value, measured in seconds, defines the maximum time interval the client will wait for the network connection to the MQTT server to be established. The default timeout is 30 seconds
* `qos` the [quality of service level](http://www.hivemq.com/blog/mqtt-essentials-part-6-mqtt-quality-of-service-levels) used for sending a message (driver write operation) between 0 and 2. The default level is 1 (message will be delivered at least once)
* `retained` if set to `true`, the last message sent for a specific topic will be retained by the broker. Possible values are `true` and `false`, the default is `true`

Note that all property keys are **case-sensitive**. Keys and the corresponding values must have the format `key=value`, leading and trailing whitespace will be ignored. Multiple properties must be separated using a comma. The following example shows a valid settings string:

```
clientId=MyClientId,keepAliveInterval=120,cleanSession=false
```
Any unknown keys in the settings string will be ignored. As all properties are optional, an empty settings string is also valid.

## Applying security
MQTT allows authentication both on application and transportation layer. To use application layer authentication, simply set the `userName` and `password` property in OpenMUC's setting string (see Driver configuration above). Note that the user name and password will be sent **in plain-text** over the network if no transport encryption has been applied. Depending on the broker implementation, the client id may also be used for authentication together with the provided user name and password.

To apply transport encryption and server authentication, the broker must provide a trusted certificate. If using a self-signed certificate, it must be added to the trusted certificates on the client system. Trusted certificates by default are located in the keystore `JAVA_HOME/jre/lib/security/cacerts`. To find out the JAVA_HOME directory on UNIX based systems, execute the following command:

```
/usr/libexec/java_home
```

Afterwards, add the certificate to the truststore like shown [in this article](http://magicmonster.com/kb/prg/java/ssl/pkix_path_building_failed.html). If adding the certificate to the `cacerts` truststore doesn't work, try to also add it to the `jssecacerts` truststore in the same directory (worked for me).

## Useful tools and links
This section provides a collection of useful tools and links for working with MQTT:

* For testing, public available MQTT brokers may be useful. The test server provided by [mosquitto.org](http://test.mosquitto.org/) provides ports for unencrypted, encrypted and websocket-based connections
* HiveMQ provides an [online websocket client](http://www.hivemq.com/demos/websocket-client/) which can send and receive MQTT messages
* [This article](http://magicmonster.com/kb/prg/java/ssl/pkix_path_building_failed.html) shows how to add a broker certificate to the local truststore at the client system
* HiveMQ also provides a great tutorial explaining the [MQTT essentials](http://www.hivemq.com/blog/mqtt-essentials/) and some [MQTT security fundamentals](http://www.hivemq.com/blog/mqtt-security-fundamentals/)
