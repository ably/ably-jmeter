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
 * A sampler that establishes a group of Ably realtime connections and stores the
 * client instances in thread scope
 */
public class ConnectGroupSampler extends BaseSampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = LoggerFactory.getLogger(ConnectGroupSampler.class.getCanonicalName());

	private AblyRealtime[] clients;

	public ConnectGroupSampler() {
		super(logger);
	}

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		clients = (AblyRealtime[]) vars.getObject(BaseSampler.REALTIME_CLIENT_GROUP);
		if(clients != null) {
			return fillFailedResult(result, "Clients already exist", 500);
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
			return fillFailedResult(result, "Failed to establish client" + e.getMessage(), 500);
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

			if(firstError == null) {
				vars.putObject(BaseSampler.REALTIME_CLIENT_GROUP, clients); // save connection object as thread local variable !!
				return fillOKResult(result);
			} else {
				/* one connection failed, so abort the whole group */
				logger.error("Failed to connect; closing all connections", firstError);
				closeAllClients(logger, clients);
				return fillFailedResult(result, firstError);
			}
		} catch (Exception e) {
			logger.error("Failed to establish client", e);
			return fillFailedResult(result, "Failed to establish client" + e.getMessage(), 500);
		}
	}

	@Override
	public void threadFinished() {
		closeAllClients(logger, clients);
	}
}
