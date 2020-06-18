package io.ably.jmeter.samplers;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.ably.jmeter.Constants;
import io.ably.jmeter.Util;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.ErrorInfo;
import io.ably.lib.types.Message;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public abstract class SubscribeSampler extends BaseSampler {
	protected transient ConcurrentLinkedQueue<Bean> batches = new ConcurrentLinkedQueue<>();
	private boolean printFlag = false;

	protected SubscribeSampler(Logger logger) {
		super(logger);
	}

	protected SampleResult produceResult(SampleResult result, String channelName) {
		Bean bean = batches.poll();
		if(bean == null) { // In "elapsed time" mode, return "dummy" when time is reached
			bean = new Bean();
		}
		int receivedCount = bean.getReceivedCount();
		String message = MessageFormat.format("Received {0} of message.", receivedCount);
		byte[] content = isDebugResponse() ? bean.mergeContents("\n".getBytes()) : new byte[0];
		fillOKResult(result, bean.getReceivedMessageSize(), bean.getAvgElapsedTime(), message, content);
		if(logger.isDebugEnabled()) {
			logger.debug("sub [channel]: " + channelName + ", [payload]: " + new String(content));
		}

		if(receivedCount == 0) {
			result.setEndTime(result.getStartTime()); // dummy result, rectify sample time
		} else {
			result.setEndTime(result.getStartTime()); // received messages w/o timestamp, then we cannot reliably calculate elapsed time
		}
		result.setSampleCount(receivedCount);

		return result;
	}

	protected Bean addMessageToBean(SubscriptionCondition subCondition, Message msg) {
		Bean bean = null;
		if(batches.isEmpty()) {
			bean = new Bean();
			batches.add(bean);
		} else {
			Bean[] beans = new Bean[batches.size()];
			batches.toArray(beans);
			bean = beans[beans.length - 1];
		}

		if((!subCondition.sampleByTime) && (bean.getReceivedCount() == subCondition.sampleCount)) { //Create a new batch when latest bean is full.
			logger.info("The tail bean is full, will create a new bean for it.");
			bean = new Bean();
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
				logger.info("Payload does not include timestamp: " + msg);
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

	static class Bean {
		private int receivedMessageSize = 0;
		private int receivedCount = 0;
		private int elapsedTimeCount = 0;
		private double totalElapsedTime = 0f;

		private List<byte[]> contents = new ArrayList<>();

		public int getReceivedMessageSize() {
			return receivedMessageSize;
		}
		public void setReceivedMessageSize(int receivedMessageSize) {
			this.receivedMessageSize = receivedMessageSize;
		}

		public int getReceivedCount() {
			return receivedCount;
		}
		public void setReceivedCount(int receivedCount) {
			this.receivedCount = receivedCount;
		}

		public double getAvgElapsedTime() {
			return elapsedTimeCount == 0 ? 0 : totalElapsedTime / elapsedTimeCount;
		}
		public void incrementElapsedTime(double elapsedTime) {
			totalElapsedTime += elapsedTime;
			++elapsedTimeCount;
		}

		public List<byte[]> getContents() {
			return contents;
		}

		public byte[] mergeContents(byte[] delim) {
			if(receivedCount == 0) {
				return new byte[0];
			}

			int delimLength = delim.length;
			int finalLength = receivedMessageSize + (receivedCount - 1) * delimLength;
			byte[] dest = new byte[finalLength];
			int destPos = 0;

			for(byte[] element : contents) {
				System.arraycopy(element, 0, dest, destPos, element.length);
				destPos += element.length;
				if(destPos < finalLength) {
					System.arraycopy(delim, 0, dest, destPos, delimLength);
					destPos += delimLength;
				}
			}
			return dest;
		}
	}

	class SubscriptionCondition {
		final boolean sampleByTime;
		final int sampleElapsedTime;
		final int sampleCount;

		SubscriptionCondition() {
			sampleByTime = Constants.SAMPLE_ON_CONDITION_OPTION1.equals(getSampleCondition());
			sampleElapsedTime = getSampleElapsedTime();
			sampleCount = getSampleCount();
		}

		String validate() {
			if(sampleByTime && sampleElapsedTime <=0 ) {
				return "Sample on elapsed time: must be greater than 0 ms.";
			} else if(sampleCount < 1) {
				return "Sample on message count: must be greater than 1.";
			}
			return null;
		}

		void waitSampleTime() {
			if(sampleByTime) {
				try {
					TimeUnit.MILLISECONDS.sleep(sampleElapsedTime);
				} catch (InterruptedException e) {
					logger.info("Received exception when waiting for notification signal", e);
				}
			}
		}
	}

	class RealtimeSubscription {
		private final Logger logger;
		private final SubscriptionCondition subCondition;
		private final Channel channel;
		private int count;

		RealtimeSubscription(Logger logger, SubscriptionCondition subCondition, AblyRealtime client, String channelName) {
			this.logger = logger;
			this.subCondition = subCondition;
			channel = client.channels.get(channelName);
		}

		protected ErrorInfo subscribe() {
			ErrorInfo subError;
			try {
				SubResult subResult = new SubResult();
				channel.attach(subResult);
				subError = subResult.waitForResult();
			} catch (AblyException e) {
				logger.info("attach failed", e);
				subError = e.errorInfo;
			} catch (InterruptedException e) {
				logger.info("attach failed", e);
				subError = new ErrorInfo(e.getMessage(), 50000, 500);
			}
			if(subError == null) {
				try {
					channel.subscribe(message -> {
						synchronized(this) {
							addMessageToBean(subCondition, message);
							if(!subCondition.sampleByTime) {
								if(++count >= subCondition.sampleCount) {
									notify();
								}
							}
						}
					});
				} catch (AblyException e) {
					subError = e.errorInfo;
				}
			}
			return subError;
		}

		protected void unsubscribe() {
			try {
				channel.detach();
			} catch(AblyException ae) {}
		}

		protected synchronized void waitForCount() {
			if(!subCondition.sampleByTime && count < subCondition.sampleCount) {
				try {
					wait();
				} catch (InterruptedException e) {
					logger.info("Received exception when waiting for notification signal", e);
				}
			}
		}
	}
}
