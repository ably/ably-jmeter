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
public class ConnectGroupSampler extends BaseSampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = LoggerFactory.getLogger(ConnectGroupSampler.class.getCanonicalName());

	private AblyRealtime[] clients;

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		clients = (AblyRealtime[]) vars.getObject(BaseSampler.REALTIME_CLIENT_GROUP);
		if(clients != null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage("Clients already exist.");
			result.setResponseData("Failed. Clients already exist.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
		}

		int clientCount = getGroupSize();
		try {
			clients = new AblyRealtime[clientCount];
			String[] clientIds = new String[clientCount];
			for(int i = 0; i < clientCount; i++) {
				ClientOptions opts = getRealtimeClientOptions(logger);
				clients[i] = new AblyRealtime(opts);
				clientIds[i] = opts.clientId;
			}
			vars.putObject(BaseSampler.CLIENT_ID_GROUP, clientIds);
		} catch (Exception e) {
			logger.error("Failed to establish client", e);
			result.setSuccessful(false);
			result.setResponseMessage("Failed to establish client. Please check connection configuration.");
			result.setResponseData("Failed to establish client. Please check connection configuration.".getBytes());
			result.setResponseCode("502");
			return result;
		}

		try {
			ConnectResult[] connectionOutcomes = new ConnectResult[clientCount];
			result.sampleStart();
			for(int i = 0; i < clientCount; i++) {
				AblyRealtime client = clients[i];
				ConnectResult connectionOutcome = connectionOutcomes[i] = new ConnectResult();
				client.connection.on(connectionOutcome);
				client.connect();
			}

			ErrorInfo firstError = null;
			for(ConnectResult connectionOutcome : connectionOutcomes) {
				firstError = connectionOutcome.waitForResult();
				if(firstError != null) {
					break;
				}
			}
			result.sampleEnd();

			if(firstError == null) {
				vars.putObject(BaseSampler.REALTIME_CLIENT_GROUP, clients); // save connection object as thread local variable !!
				result.setSuccessful(true);
				result.setResponseData("Successful.".getBytes());
				result.setResponseMessage("Connections established.");
				result.setResponseCodeOK();
			} else {
				/* one connection failed, so abort the whole group */
				logger.error("Failed to connect; closing all connections", firstError);
				closeAllClients(logger, clients);

				result.setSuccessful(false);
				result.setResponseMessage(MessageFormat.format("Failed to establish client {0}.", firstError.message));
				result.setResponseData("Client failed. Couldn't establish connection.".getBytes());
				result.setResponseCode(String.valueOf(firstError.statusCode));
			}
		} catch (Exception e) {
			logger.error("Failed to establish client", e);
			if(result.getEndTime() == 0) { result.sampleEnd(); } //avoid re-enter sampleEnd()
			result.setSuccessful(false);
			result.setResponseMessage("Failed to establish client.");
			result.setResponseData("Client failed with exception.".getBytes());
			result.setResponseCode("502");
		}
		return result;
	}

	@Override
	public void threadFinished() {
		closeAllClients(logger, clients);
	}
}
