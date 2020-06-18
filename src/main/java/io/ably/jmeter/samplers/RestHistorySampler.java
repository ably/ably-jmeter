package io.ably.jmeter.samplers;

import io.ably.lib.rest.AblyRest;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.Message;
import io.ably.lib.types.PaginatedResult;
import io.ably.lib.types.Param;
import io.ably.lib.util.Serialisation;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A sampler that makes a single stats request using the Ably REST client
 */
public class RestHistorySampler extends BaseSampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = LoggerFactory.getLogger(RestHistorySampler.class.getCanonicalName());

	public RestHistorySampler() {
		super(logger);
	}

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		AblyRest client = (AblyRest) vars.getObject(BaseSampler.REST_CLIENT);
		if(client == null) {
			return fillFailedResult(result, "Client not found", 500);
		}

		try {
			List<Param> params = new ArrayList<>();
			String start = getHistoryStart();
			if(start != null && !start.isEmpty()) {
				params.add(new Param("start", start));
			}
			String end = getHistoryEnd();
			if(end != null && !end.isEmpty()) {
				params.add(new Param("end", end));
			}
			String limit = getHistoryLimit();
			if(limit != null && !limit.isEmpty()) {
				params.add(new Param("limit", limit));
			}
			String direction = getDirectionValues()[getHistoryDirectionIndex()];
			params.add(new Param("direction", direction));

			result.sampleStart();
			PaginatedResult<Message> history = client.channels.get(getChannelPrefix()).history(params.toArray(new Param[params.size()]));
			byte[] historyJson = writeJson(history.items()).getBytes();
			return fillOKResult(result, historyJson.length, 0, null, historyJson);
		} catch (AblyException e) {
			logger.error("Failed to get stats " + client , e);
			return fillFailedResult(result, e.errorInfo);
		}
	}

	public static String writeJson(Message[] items) {
		return Serialisation.gson.toJson(items);
	}
}
