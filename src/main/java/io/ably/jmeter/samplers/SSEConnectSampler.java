package io.ably.jmeter.samplers;

import com.launchdarkly.eventsource.ConnectionErrorHandler;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import io.ably.lib.types.ErrorInfo;
import io.ably.lib.types.Message;
import io.ably.lib.util.Serialisation;
import okhttp3.HttpUrl;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sampler that establishes an SSE connection, subscribes to a given channel,
 * and waits for a subscription conditions to be met
 */
public class SSEConnectSampler extends SubscribeSampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = LoggerFactory.getLogger(SSEConnectSampler.class.getCanonicalName());

	private transient EventSource client;
	private transient SSESubscriptionHandler handler;

	public SSEConnectSampler() {
		super(logger);
	}

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		final SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		final JMeterVariables vars = JMeterContextService.getContext().getVariables();
		client = (EventSource) vars.getObject(BaseSampler.SSE_CLIENT);
		if(client != null) {
			return fillFailedResult(result, new ErrorInfo("Subscribe failed because connection is not established.", 500, 50000));
		}

		final SubscriptionCondition subCondition = new SubscriptionCondition();
		final String validateErr = subCondition.validate();
		if(validateErr != null) {
			return fillFailedResult(result, new ErrorInfo(validateErr, 500, 50000));
		}

		final String clientId = getClientId();
		vars.putObject(BaseSampler.CLIENT_ID, clientId);

		String channel = getChannelName();
		vars.putObject(BaseSampler.CHANNEL_NAME, channel);

		final String environment = getEnvironment();
		boolean isProduction = environment == null || environment.isEmpty() || environment.equals("production");
		final String host = isProduction ? "realtime.ably.io" : environment + "-realtime.ably.io";

		try {
			final HttpUrl httpUrl = new HttpUrl.Builder()
					.scheme("https")
					.host(host)
					.addPathSegment("event-stream")
					.addQueryParameter("clientId", clientId)
					.addQueryParameter("channels", channel)
					.addQueryParameter("v", "1.2")
					.addQueryParameter("enveloped", "true")
					.addQueryParameter("key", getApiKey())
					.build();

			handler = new SSESubscriptionHandler(subCondition, clientId);
			client = new EventSource.Builder(handler, httpUrl).logger(new ConnectionLogger(logger)).build();

			/* connect and wait for outcome */
			result.sampleStart();
			client.start();
			final Throwable error = handler.waitForOutcome();

			if(error != null) {
				return fillFailedResult(result, new ErrorInfo("Connect failed; exception: " + error.getMessage(), 500, 50000));
			}

			vars.putObject(BaseSampler.SSE_CLIENT, client);
			vars.putObject(BaseSampler.SSE_CLIENT_HANDLER, handler);
		} catch (Exception e) {
			logger.error("Failed to establish client " + client, e);
			return fillFailedResult(result, new ErrorInfo("Subscribe failed; exception: " + e.getMessage(), 500, 50000));
		}

		/* wait for subscription sample conditions to be met */
		subCondition.waitForCondition(logger);
		return produceResult(result, channel);
	}

	@Override
	public void threadFinished() {
		closeSSEClient(logger, client);
	}

	private static class ConnectionLogger implements com.launchdarkly.eventsource.Logger {
		private Logger logger;

		ConnectionLogger(Logger logger) {
			this.logger = logger;
		}

		@Override
		public void debug(String format, Object param) {
			logger.debug(format, param);
		}

		@Override
		public void debug(String format, Object param1, Object param2) {
			logger.debug(format, param1, param2);
		}

		@Override
		public void info(String message) {
			logger.info(message);
		}

		@Override
		public void warn(String message) {
			logger.warn(message);
		}

		@Override
		public void error(String message) {
			logger.error(message);
		}
	}

	class SSESubscriptionHandler implements EventHandler, ConnectionErrorHandler {
		final SubscriptionCondition subCondition;
		final String clientId;
		private Throwable connectionError;
		private boolean hasOutcome;

		SSESubscriptionHandler(SubscriptionCondition subCondition, String clientId) {
			this.subCondition = subCondition;
			this.clientId = clientId;
		}

		private synchronized void setOutcome(Throwable connectionError) {
			if(!hasOutcome) {
				this.connectionError = connectionError;
				hasOutcome = true;
				notify();
			}
		}

		synchronized Throwable waitForOutcome() throws InterruptedException {
			while(!hasOutcome) {
				wait();
			}
			return connectionError;
		}

		@Override
		public Action onConnectionError(Throwable t) {
			/* TODO: retry when it's a retriable error */
			setOutcome(t);
			return Action.SHUTDOWN;
		}

		@Override
		public void onOpen() throws Exception {
			setOutcome(null);
		}

		@Override
		public void onClosed() throws Exception {
			/* assume this only happens when there is a normal close */
		}

		@Override
		public void onMessage(String event, MessageEvent messageEvent) throws Exception {
			final Message msg = Serialisation.gson.fromJson(messageEvent.getData(), Message.class);
			if(subCondition.sampleByTime) {
				synchronized(subCondition) {
					addMessageToBean(subCondition, msg);
				}
			} else {
				synchronized(subCondition) {
					Bean bean = addMessageToBean(subCondition, msg);
					if(bean.getReceivedCount() == subCondition.sampleCount) {
						subCondition.notify();
					}
				}
			}
		}

		@Override
		public void onComment(String comment) throws Exception {}

		@Override
		public void onError(Throwable t) {
			/* TODO: retry when it's a retriable error */
			setOutcome(t);
		}
	}
}
