# ably-jmeter

An Ably JMeter Plugin supporting the creating of [Apache JMeter](https://jmeter.apache.org/) tests involving Ably clients.

## Status

This is in preview. All feedback and Pull Requests are welcomed.

This is evolving and liable to change. Class and property names and behaviour are all subject to change; anticipate any test plan using this to be invalidated by future releases.

## Installation

The plugin is a standard JMeter plugin. You can download the latest version of ably-jmeter from [Github](https://github.com/ably/ably-jmeter/releases), and then copy the downloaded JAR file into the `$JMETER_HOME/lib/ext` directory. After restarting JMeter, the Ably samplers provided by this plugin are available.

## Prerequisites

This has been tested with JMeter 5.1 and above.

## Build from source code

To build the plugin binary from source, clone the repo and run `gradle build`.

# Properties

A number of JMeter properties can be defined as User Defined Variables, and these will be used as defaults where applicable:

- `ably.environment`: a default for the `Environment` setting in the Ably samplers which, where specified, is used as the value of the `environment` `ClientOption`.

- `ably.api_key`: a default for the `API Key` setting. Where specified, this is used as the `key` `ClientOption`.

# Samplers

The plugin includes the following samplers.

## REST operations

Samplers exist that initialise an Ably REST or Realtime client.

### Ably REST Configuration

This creates a REST client based on given parameters, which is stored in a JMeter variable (ie in thread scope) and usable by other REST operations.
The sampled operation is the (trivial) client instance creation.

#### Configuration options

- **Environment**: specify a non-default Ably endpoint via the `environment` client option.

- **API key**: the Ably API key to use.

- **ClientId**: the `clientId`. Optionally a random suffix can be added to the given `clientId`.

- **Log level**: the `logLevel` passed to the ably library instance. Logging from the library goes to the default logger in the JMeter application, and therefore will be further filtered by the log level specified in JMeter itself.

#### Variables

The variables added to thread scope are:

- `ably.rest_client`: the REST client instance.

- `ably.client_id`: the `clientId` associated with the instance (including any randomly-generated part).

### Ably REST publish

This publishes a message on a named channel, with an Ably REST client instance previously established in the thread. The sampled operation is the publish and acknowledgement of the message.

### Configuration options

- **Channel name**: the name of the channel to use. Optionally, a random suffix can be added to the given name.

- **Payload**: a specified payload, or auto-generated payload of a given length, may be specified. The payload becomes the `data` part of the message.

- **Event name**: a specified `name` value for the message.

- **Encoding**: a specified `encoding` value for the message. There is no attempt to infer the `encoding` from the given payload value.

- **Timestamp**: optionally, the time at which the publish is initiated by the sampler, is added to the message in `extras.metadata.timestamp` as a number, Unix time in milliseconds.

### Variables

The variables added to thread scope are:

- `ably.channel_name`: the channel name used for the publish (including any randomly-generated part).

### Ably REST Stats

This makes a single stats request using a previously established REST client. The JSON stats response is included in the sample response data, and can be processed by any relevant postprocessor (eg JSON extractor). The sampled operation is the stats request itself. Pagination is currently not supported.

#### Configuration options

- **Start**: this specifies the `start` request param which can be a formatted date/time string, or a Unix time in milliseconds. It defaults to the `TESTSTART.MS` JMeter property.

- **End**: this specifies the `end` request param which can be a formatted date/time string, or a Unix time in milliseconds. It defaults to being unspecified, which means that stats up to the time of the call are included.

- **Limit**: this specifies the `limit` request param. It defaults to `10`.

- **Unit**: this specifies the `unit` request param which determines the granularity of the stats request. It defaults to `minute`.

- **Direction**: this specifies the `direction` request param, which determines the order in which stats results are returned. It defaults to `backwards` - ie, most recent time intervals are listed first.

#### Variables

No variables are added to thread scope.

### Ably REST History

This makes a single history request using a previously established REST client. The JSON history response is included in the sample response data, and can be processed by any relevant postprocessor (eg JSON extractor). The sampled operation is the history request itself. Pagination is currently not supported.

#### Configuration options

- **Start**: this specifies the `start` request param which can be a formatted date/time string, or a Unix time in milliseconds. It defaults to the `TESTSTART.MS` JMeter property.

- **End**: this specifies the `end` request param which can be a formatted date/time string, or a Unix time in milliseconds. It defaults to being unspecified, which means that history up to the time of the call is included.

- **Limit**: this specifies the `limit` request param. It defaults to `100`.

- **Direction**: this specifies the `direction` request param, which determines the order in which stats results are returned. It defaults to `backwards` - ie, most recent time intervals are listed first.

#### Variables

No variables are added to thread scope.

## Realtime operations

### Ably Connect

This creates a Realtime client based on given parameters, which is stored in a JMeter variable (ie in thread scope) and usable by other Realtime operations.

#### Configuration options

- **Environment**: specify a non-default Ably endpoint via the `environment` client option.

- **API key**: the Ably API key to use.

- **ClientId**: the `clientId`. Optionally a random suffix can be added to the given `clientId`.

- **Log level**: the `logLevel` passed to the ably library instance. Logging from the library goes to the default logger in the JMeter application, and therefore will be further filtered by the log level specified in JMeter itself.

#### Variables

- `ably.realtime_client`: the Realtime client instance.

### Ably Disconnect

This closes a previously established Ably connection.

### Ably Realtime publish

This publishes a message on a named channel using an established realtime connection, via a transient publish operation. The sampled operation is the publish and acknowledgement of the message.

#### Configuration options

- **Channel name**: the name of the channel to use. Optionally, a random suffix can be added to the given name.

- **Payload**: a specified payload, or auto-generated payload of a given length, may be specified. The payload becomes the `data` part of the message.

- **Event name**: a specified `name` value for the message.

- **Encoding**: a specified `encoding` value for the message. There is no attempt to infer the `encoding` from the given payload value.

- **Timestamp**: optionally, the time at which the publish is initiated by the sampler, is added to the message in `extras.metadata.timestamp` as a number, Unix time in milliseconds.

#### Variables

The variables added to thread scope are:

- `ably.channel_name`: the channel name used for the publish (including any randomly-generated part).

### Ably Realtime Subscribe

This subscribes to a given channel via a previously established realtime connection. The sampled operation is subscription for a given time, or a given number of messages. If the connection failed to be established, this publish sampler will fail immediately.

#### Configuration options

- **Channel name**: the name of the channel to use.

- **Sample on**: The basis on which sampling is performed. The default value is '**elapsed with specified time(ms)**', which means a sub sampler will occur every specified milli-seconds (default is 1000ms). During the 1000 ms, multiple messages could be received, and result in report is the summarized data during 1000 ms. If the value is set to 2000, then means summarized report during 2000 ms. Another option is '**number of received messages**', which means a sub sampler will occur after receiving these specified number of messages (default is 1). 

- **Debug response**: If checked, the received message will be displayed in the sampler response.

- **Timestamp**: if the publisher added the publish time as mesage metadata, then this sampler will determine the mean latency across the sample as report that in the sample result.

#### Variables

No variables are added to thread scope.

## Support, feedback and troubleshooting

Please visit http://support.ably.io/ for access to our knowledgebase and to ask for any assistance.

You can also view the [community reported Github issues](https://github.com/ably/ably-jmeter/issues).

## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create a new Pull Request
