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

	public ConnectSampler() {
		super(logger);
	}

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		client = (AblyRealtime) vars.getObject(BaseSampler.REALTIME_CLIENT);
		if(client != null) {
			return fillFailedResult(result, "Client already exists", 500);
		}

		ClientOptions opts = getRealtimeClientOptions(logger);
		try {
			vars.putObject(BaseSampler.CLIENT_ID, opts.clientId);
			client = new AblyRealtime(opts);
		} catch (Exception e) {
			logger.error("Failed to establish client " + client, e);
			return fillFailedResult(result, "Failed to establish client" + e.getMessage(), 500);
		}

		try {
			ConnectResult connectionOutcome = new ConnectResult();
			client.connection.on(connectionOutcome);

			result.sampleStart();
			client.connect();
			ErrorInfo error = connectionOutcome.waitForResult();

			if(error == null) {
				vars.putObject(BaseSampler.REALTIME_CLIENT, client); // save connection object as thread local variable !!
				return fillOKResult(result);
			} else {
				return fillFailedResult(result, error);
			}
		} catch (Exception e) {
			logger.error("Failed to establish client " + client, e);
			return fillFailedResult(result, "Failed to establish client" + e.getMessage(), 500);
		}
	}

	@Override
	public void threadFinished() {
		closeClient(logger, client);
	}
}
