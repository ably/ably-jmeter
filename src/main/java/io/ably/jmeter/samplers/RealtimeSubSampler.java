package io.ably.jmeter.samplers;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.ably.jmeter.Constants;
import io.ably.jmeter.SubBean;
import io.ably.jmeter.Util;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.realtime.Channel.MessageListener;
import io.ably.lib.realtime.CompletionListener;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.ErrorInfo;
import io.ably.lib.types.Message;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import java.text.MessageFormat;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A sampler that makes a subscription to a channel using an already-established realtime connection
 */
public class RealtimeSubSampler extends AbstractAblySampler {
	private static final long serialVersionUID = 2979978053740194951L;
	private static final Logger logger = Logger.getLogger(RealtimeSubSampler.class.getCanonicalName());

	private static class SubResult implements CompletionListener {
		private ErrorInfo error;

		private synchronized ErrorInfo waitForResult() throws InterruptedException {
			wait();
			return error;
		}

		@Override
		public synchronized void onSuccess() {
			notify();
		}

		@Override
		public void onError(ErrorInfo reason) {
			this.error = reason;
			notify();
		}
	}

	private transient AblyRealtime connection = null;
	private SubResult subResult;

	private int sampleElapsedTime = 1000;
	private int sampleCount = 1;

	private transient ConcurrentLinkedQueue<SubBean> batches = new ConcurrentLinkedQueue<>();
	private boolean printFlag = false;

	private final transient Object dataLock = new Object();

	public String getSampleCondition() {
		return getPropertyAsString(Constants.SAMPLE_CONDITION, Constants.SAMPLE_ON_CONDITION_OPTION1);
	}
	public void setSampleCondition(String option) {
		setProperty(Constants.SAMPLE_CONDITION, option);
	}

	public String getSampleCount() {
		return getPropertyAsString(Constants.SAMPLE_CONDITION_VALUE, Constants.DEFAULT_SAMPLE_VALUE_COUNT);
	}
	public void setSampleCount(String count) {
		setProperty(Constants.SAMPLE_CONDITION_VALUE, count);
	}

	public String getSampleElapsedTime() {
		return getPropertyAsString(Constants.SAMPLE_CONDITION_VALUE, Constants.DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_MILLI_SEC);
	}
	public void setSampleElapsedTime(String elapsedTime) {
		setProperty(Constants.SAMPLE_CONDITION_VALUE, elapsedTime);
	}

	public boolean isDebugResponse() {
		return getPropertyAsBoolean(Constants.DEBUG_RESPONSE, false);
	}
	public void setDebugResponse(boolean debugResponse) {
		setProperty(Constants.DEBUG_RESPONSE, debugResponse);
	}

	@Override
	public SampleResult sample(Entry arg0) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		connection = (AblyRealtime) vars.getObject(AbstractAblySampler.CLIENT);
		if (connection == null) {
			return fillFailedResult(result, "500", "Subscribe failed because connection is not established.");
		}

		// initial values
		boolean sampleByTime = Constants.SAMPLE_ON_CONDITION_OPTION1.equals(getSampleCondition());
		try {
			if (sampleByTime) {
				sampleElapsedTime = Integer.parseInt(getSampleElapsedTime());
			} else {
				sampleCount = Integer.parseInt(getSampleCount());
			}
		} catch (NumberFormatException e) {
			return fillFailedResult(result, "510", "Unrecognized value for sample elapsed time or message count.");
		}

		if (sampleByTime && sampleElapsedTime <=0 ) {
			return fillFailedResult(result, "511", "Sample on elapsed time: must be greater than 0 ms.");
		} else if (sampleCount < 1) {
			return fillFailedResult(result, "512", "Sample on message count: must be greater than 1.");
		}

		String channelName = getChannel();
		ErrorInfo subError = subscribe(channelName, sampleByTime, sampleCount);
		if(subError != null) {
			return fillFailedResult(result, String.valueOf(subError.statusCode), "Failed to subscribe to channel:" + channelName + "; error: " + subError.message);
		}

		if(sampleByTime) {
			try {
				TimeUnit.MILLISECONDS.sleep(sampleElapsedTime);
			} catch (InterruptedException e) {
				logger.log(Level.INFO, "Received exception when waiting for notification signal", e);
			}
			synchronized (dataLock) {
				result.sampleStart();
				return produceResult(result, channelName);
			}
		} else {
			synchronized (dataLock) {
				int receivedCount1 = (batches.isEmpty() ? 0 : batches.element().getReceivedCount());;
				boolean needWait = false;
				if(receivedCount1 < sampleCount) {
					needWait = true;
				}

				if(needWait) {
					try {
						dataLock.wait();
					} catch (InterruptedException e) {
						logger.log(Level.INFO, "Received exception when waiting for notification signal", e);
					}
				}
				result.sampleStart();
				return produceResult(result, channelName);
			}
		}
	}

	private SampleResult produceResult(SampleResult result, String channelName) {
		SubBean bean = batches.poll();
		if(bean == null) { // In "elapsed time" mode, return "dummy" when time is reached
			bean = new SubBean();
		}
		int receivedCount = bean.getReceivedCount();
		String message = MessageFormat.format("Received {0} of message.", receivedCount);
		byte[] content = isDebugResponse() ? bean.mergeContents("\n".getBytes()) : new byte[0];
		fillOKResult(result, bean.getReceivedMessageSize(), bean.getAvgElapsedTime(), message, content);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("sub [channel]: " + channelName + ", [payload]: " + new String(content));
		}

		if(receivedCount == 0) {
			result.setEndTime(result.getStartTime()); // dummy result, rectify sample time
		} else {
			result.setEndTime(result.getStartTime()); // received messages w/o timestamp, then we cannot reliably calculate elapsed time
		}
		result.setSampleCount(receivedCount);

		return result;
	}

	private ErrorInfo subscribe(final String channelName, final boolean sampleByTime, final int sampleCount) {
		if(subResult == null) {
			subResult = new SubResult();
		}
		final Channel channel = connection.channels.get(channelName);
		ErrorInfo subError;
		try {
			channel.attach(subResult);
			subError = subResult.waitForResult();
		} catch (AblyException e) {
			logger.log(Level.INFO, "attach failed", e);
			subError = e.errorInfo;
		} catch (InterruptedException e) {
			logger.log(Level.INFO, "attach failed", e);
			subError = new ErrorInfo(e.getMessage(), 50000, 500);
		}
		if(subError == null) {
			try {
				channel.subscribe(getListener(sampleByTime, sampleCount));
			} catch (AblyException e) {
				subError = e.errorInfo;
			}
		}
		return subError;
	}

	private MessageListener getListener(final boolean sampleByTime, final int sampleCount) {
		return message -> {
			if(sampleByTime) {
				synchronized (dataLock) {
					handleSubBean(sampleByTime, message, sampleCount);
				}
			} else {
				synchronized (dataLock) {
					SubBean bean = handleSubBean(sampleByTime, message, sampleCount);
					if(bean.getReceivedCount() == sampleCount) {
						dataLock.notify();
					}
				}
			}
		};
	}

	private SubBean handleSubBean(boolean sampleByTime, Message msg, int sampleCount) {
		SubBean bean = null;
		if(batches.isEmpty()) {
			bean = new SubBean();
			batches.add(bean);
		} else {
			SubBean[] beans = new SubBean[batches.size()];
			batches.toArray(beans);
			bean = beans[beans.length - 1];
		}

		if((!sampleByTime) && (bean.getReceivedCount() == sampleCount)) { //Create a new batch when latest bean is full.
			logger.info("The tail bean is full, will create a new bean for it.");
			bean = new SubBean();
			batches.add(bean);
		}
		if(isAddTimestamp()) {
			long now = System.currentTimeMillis();
			long msgTimestamp = 0;
			JsonObject extras = msg.extras;
			if(extras != null) {
				JsonObject metadata = extras.getAsJsonObject("metadata");
				if(metadata != null) {
					JsonPrimitive jsonTimestamp = metadata.getAsJsonPrimitive("timestamp");
					if(jsonTimestamp != null) {
						msgTimestamp = jsonTimestamp.getAsLong();
					}
				}
			}
			if(msgTimestamp == 0 && (!printFlag)) {
				logger.info(() -> "Payload does not include timestamp: " + msg);
				printFlag = true;
			} else {
				bean.incrementElapsedTime(now - msgTimestamp);
			}
		}
		if(isDebugResponse()) {
			bean.getContents().add(Util.payloadBytes(msg.data));
		}
		bean.setReceivedMessageSize(bean.getReceivedMessageSize() + Util.payloadLength(msg.data));
		bean.setReceivedCount(bean.getReceivedCount() + 1);
		return bean;
	}

	private SampleResult fillFailedResult(SampleResult result, String code, String message) {
		result.sampleStart();
		result.setResponseCode(code); // 5xx means various failures
		result.setSuccessful(false);
		result.setResponseMessage(message);
		if (connection != null) {
			result.setResponseData(MessageFormat.format("Client [{0}]: {1}", connection.options.clientId, message).getBytes());
		} else {
			result.setResponseData(message.getBytes());
		}
		result.sampleEnd();

		// avoid massive repeated "early stage" failures in a short period of time
		// which probably overloads JMeter CPU and distorts test metrics such as TPS, avg response time
		try {
			TimeUnit.MILLISECONDS.sleep(Constants.SUB_FAIL_PENALTY);
		} catch (InterruptedException e) {
			logger.log(Level.INFO, "Received exception when waiting for notification signal", e);
		}
		return result;
	}

	private void fillOKResult(SampleResult result, int size, double latency, String message, byte[] contents) {
		result.setResponseCode("200");
		result.setSuccessful(true);
		result.setResponseMessage(message);
		result.setBodySize((long)size);
		result.setBytes((long)size);
		result.setLatency((long)latency);
		result.setResponseData(contents);
		result.sampleEnd();
	}
}
