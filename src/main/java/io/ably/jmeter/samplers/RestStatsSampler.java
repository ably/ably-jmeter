package io.ably.jmeter.samplers;

import io.ably.lib.rest.AblyRest;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.PaginatedResult;
import io.ably.lib.types.Param;
import io.ably.lib.types.Stats;
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
public class RestStatsSampler extends BaseSampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = LoggerFactory.getLogger(RestStatsSampler.class.getCanonicalName());

	public RestStatsSampler() {
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
			String start = getStatsStart();
			if(start != null && !start.isEmpty()) {
				params.add(new Param("start", start));
			}
			String end = getStatsEnd();
			if(end != null && !end.isEmpty()) {
				params.add(new Param("end", end));
			}
			String limit = getStatsLimit();
			if(limit != null && !limit.isEmpty()) {
				params.add(new Param("limit", limit));
			}
			String unit = getUnitValues()[getStatsUnitIndex()];
			params.add(new Param("unit", unit));
			String direction = getDirectionValues()[getStatsDirectionIndex()];
			params.add(new Param("direction", direction));

			result.sampleStart();
			PaginatedResult<Stats> statsResult = client.stats(params.toArray(new Param[params.size()]));
			byte[] statsJson = writeJson(statsResult.items()).getBytes();
			return fillOKResult(result, statsJson.length, 0, null, statsJson);
		} catch (AblyException e) {
			logger.error("Failed to make stats request", e);
			return fillFailedResult(result, e.errorInfo);
		}
	}

	public static String writeJson(Stats[] items) {
		return Serialisation.gson.toJson(items);
	}
}
