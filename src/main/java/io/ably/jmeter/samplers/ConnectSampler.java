package io.ably.jmeter.samplers;

import io.ably.jmeter.Util;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.ConnectionStateListener.ConnectionStateChange;
import io.ably.lib.types.ClientOptions;
import io.ably.lib.util.Log;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A sampler that establishes an Ably realtime connection and stores the
 * client instance in thread scope
 */
public class ConnectSampler extends AbstractAblySampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = Logger.getLogger(ConnectSampler.class.getCanonicalName());

	private transient AblyRealtime client;

	private static class ConnectResult {
		private ConnectionStateChange state;
		private synchronized void setState(ConnectionStateChange state) {
			this.state = state;
			notify();
		}
		private synchronized ConnectionStateChange waitForState() throws InterruptedException {
			wait();
			return state;
		}
	}

	@Override
	public SampleResult sample(Entry entry) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		logger.log(Level.FINE, "sample");
		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		client = (AblyRealtime) vars.getObject(AbstractAblySampler.CLIENT);
		if (client != null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Client {0} already exists.", client));
			result.setResponseData("Failed. Client already exists.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
		}

		ClientOptions opts = new ClientOptions();
		String clientId = getClientIdPrefix();
		try {
			if(isClientIdSuffix()) {
				clientId = Util.generateClientId(clientId);
			}

			String env = getEnvironment();
			if(env != null && !env.equals("")) {
				opts.environment = env;
			}
			opts.key = getApiKey();
			opts.clientId = clientId;
			opts.autoConnect = false;
			opts.useTokenAuth = false;
			client = new AblyRealtime(opts);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to establish client " + client , e);
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Failed to establish client {0}. Please check connection configuration.", client));
			result.setResponseData("Failed to establish client. Please check connection configuration.".getBytes());
			result.setResponseCode("502");
			return result;
		}

		try {
			final ConnectResult connectionOutcome = new ConnectResult();

			client.connection.on(state -> {
				switch(state.current) {
					case connected:
					case failed:
					case suspended:
						connectionOutcome.setState(state);
						break;
					default:
						/* ignore */
				}
			}
			);

			result.sampleStart();
			client.connect();
			ConnectionStateChange state = connectionOutcome.waitForState();
			result.sampleEnd();

			switch(state.current) {
				case connected:
					vars.putObject(AbstractAblySampler.CLIENT, client); // save connection object as thread local variable !!
					result.setSuccessful(true);
					result.setResponseData("Successful.".getBytes());
					result.setResponseMessage(MessageFormat.format("Connection {0} established.", client));
					result.setResponseCodeOK();
					break;
				case failed:
				case suspended:
					result.sampleEnd();
					result.setSuccessful(false);
					result.setResponseMessage(MessageFormat.format("Failed to establish client {0}.", state.reason.message));
					result.setResponseData(MessageFormat.format("Client [{0}] failed. Couldn't establish connection.",
							clientId).getBytes());
					result.setResponseCode(String.valueOf(state.reason.statusCode));
					break;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to establish client " + client , e);
			if (result.getEndTime() == 0) { result.sampleEnd(); } //avoid re-enter sampleEnd()
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Failed to establish client {0}.", client));
			result.setResponseData(MessageFormat.format("Client [{0}] failed with exception.", clientId).getBytes());
			result.setResponseCode("502");
		}
		return result;
	}
}
