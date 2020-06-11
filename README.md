# ably-jmeter

An Ably JMeter Plugin supporting the creating of [Apache JMeter](https://jmeter.apache.org/) tests involving Ably clients. 

# Installation

The plugin is a standard JMeter plugin. You can download the latest version of ably-jmeter from [Github](https://github.com/ably/ably-jmeter/releases), and then copy the downloaded JAR file into the `$JMETER_HOME/lib/ext` directory. After restarting JMeter, the Ably samplers provided by this plugin are available.

## Prerequisites

This has been tested with JMeter 5.1 and above.

## Build from source code

To build the plugin binary from source, clone the repo and run `gradle build`.

# Samplers

The plugin includes 5 samplers: 

- **Ably Connect**: initiates an Ably Realtime connection with given API key. The sampled operation is the establishment (or failure) of the connection. The connection remains available for other realtime samplers.

- **Ably Disconnect**: closes a previously established Ably connection.

- **Ably Realtime Publish**: publishes a message on a named channel using an established realtime connection, via a transient publish operation. The sampled operation is the publish and acknowledgement of the message.

- **Ably Realtime Subscribe**: subscribes to a given channel via a previously established realtime connection. The sampled operation is subscription for a given time, or a given number of messages. 

- **Ably REST publish**: publishes a message on a named channel, with given Ably connection parameters. The sampled operation is the publish and acknowledgement of the message.

## Connect Sampler

### Ably connection

This section includes basic connection settings.

- **API key**: the Ably API key to use.

- **Environment**: specify a non-default Ably endpoint via the `environment` client option.

### Client options

- **ClientId**: Identification of the `clientId`, ie virtual user or JMeter thread. The default value is `client_`. If 'Add random client id suffix' is selected, the plugin will append a randomly-generated uuid as a suffix to represent the client; otherwise, the text of 'ClientId' will be used as the 'clientId' for the client.

## Realtime Publish sampler

Pub sampler reuses previously established connection (by Connect sampler) to publish a message. If the connection failed to be established, this publish sampler will  fail immediately.

### Publish options

- **Channel name**: Name of the topic that the message will be sent to.

### Payloads

**Message type**: three options exist for specifying the `data` content of messages published. 

- String: a given string value. It can also be a JMeter variable.

- Hex string: a given hex string is decoded to a binary array. It can also be a JMeter variable.

- Random string with fixed length: Refer to below screenshot. If the option is selected, then it requires user to input 'Length'. The length means the auto generated string length. Default is 1024, which means generated a 1kb size of random string.

## Realtime Subcribe Sampler

The Subscribe sampler uses a previously established connection (created by Connect sampler, in thread scope) to subscribe to messages. If the connection has not yet been created, the subscribe sampler will fail immediately. If the connection exists, but has not yet been created

### Subscribe options

- **Channel name**: a given channel name to subscribe to.

- **Sample on**: The basis on which sampling is performed. The default value is '**elapsed with specified time(ms)**', which means a sub sampler will occur every specified milli-seconds (default is 1000ms). During the 1000 ms, multiple messages could be received, and result in report is the summarized data during 1000 ms. If the value is set to 2000, then means summarized report during 2000 ms. Another option is '**number of received messages**', which means a sub sampler will occur after receiving these specified number of messages (default is 1). 

- **Debug response**: If checked, the received message will be displayed in the sampler response.

## Disconnect Sampler

This closes a previously established connection. If no connection exists in the present thread then this sampler will fail.
