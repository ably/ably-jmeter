package io.ably.jmeter.samplers;

import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.types.ClientOptions;
import io.ably.lib.types.ErrorInfo;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * A sampler that establishes an Ably realtime connection and stores the
 * client instance in thread scope
 */
public class ConnectSampler extends BaseSampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = LoggerFactory.getLogger(ConnectSampler.class.getCanonicalName());

	private AblyRealtime client;

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		client = (AblyRealtime) vars.getObject(BaseSampler.REALTIME_CLIENT);
		if(client != null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Client {0} already exists.", client));
			result.setResponseData("Failed. Client already exists.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
		}

		ClientOptions opts = getRealtimeClientOptions(logger);
		try {
			vars.putObject(BaseSampler.CLIENT_ID, opts.clientId);
			client = new AblyRealtime(opts);
		} catch (Exception e) {
			logger.error("Failed to establish client " + client, e);
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Failed to establish client {0}. Please check connection configuration.", client));
			result.setResponseData("Failed to establish client. Please check connection configuration.".getBytes());
			result.setResponseCode("502");
			return result;
		}

		try {
			ConnectResult connectionOutcome = new ConnectResult();
			client.connection.on(connectionOutcome);

			result.sampleStart();
			client.connect();
			ErrorInfo error = connectionOutcome.waitForResult();
			result.sampleEnd();

			if(error == null) {
				vars.putObject(BaseSampler.REALTIME_CLIENT, client); // save connection object as thread local variable !!
				result.setSuccessful(true);
				result.setResponseData("Successful.".getBytes());
				result.setResponseMessage(MessageFormat.format("Connection {0} established.", client));
				result.setResponseCodeOK();
			} else {
				result.setSuccessful(false);
				result.setResponseMessage(MessageFormat.format("Failed to establish client {0}.", error.message));
				result.setResponseData(MessageFormat.format("Client [{0}] failed. Couldn't establish connection.",
						opts.clientId).getBytes());
				result.setResponseCode(String.valueOf(error.statusCode));
			}
		} catch (Exception e) {
			logger.error("Failed to establish client " + client, e);
			if(result.getEndTime() == 0) { result.sampleEnd(); } //avoid re-enter sampleEnd()
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Failed to establish client {0}.", client));
			result.setResponseData(MessageFormat.format("Client [{0}] failed with exception.", opts.clientId).getBytes());
			result.setResponseCode("502");
		}
		return result;
	}

	@Override
	public void threadFinished() {
		closeClient(logger, client);
	}
}
