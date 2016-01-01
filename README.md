# MQTT Driver
OpenMUC driver for reading/writing values via MQTT

The following definition comes from the MQTT 3.1.1 specification:
> MQTT is a Client Server publish/subscribe messaging transport protocol. It is light weight, open, simple, and designed so as to be easy to implement. These characteristics make it ideal for use in many situations, including constrained environments such as for communication in Machine to Machine (M2M) and Internet of Things (IoT) contexts where a small code footprint is required and/or network bandwidth is at a premium.

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

Note that all property keys are **case-sensitive**. Keys and the corresponding values must have the format `key=value`, leading and trailing whitespace will be ignored. Multiple properties must be separated using a comma. The following example shows a valid settings string:

```
clientId=MyClientId,keepAliveInterval=120,cleanSession=false
```
Any unknown keys in the settings string will be ignored. As all properties are optional, an empty settings string is also valid.
