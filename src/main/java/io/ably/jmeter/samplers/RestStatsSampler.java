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

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		AblyRest client = (AblyRest) vars.getObject(BaseSampler.REST_CLIENT);
		if(client == null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage("Stats: client configuration not found.");
			result.setResponseData("Stats failed because client configuration is not found.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
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
			result.sampleEnd();

			result.setSuccessful(true);
			String statsJson = writeJson(statsResult.items());
			result.setResponseData(statsJson, "UTF-8");
			result.setResponseMessage("Success");
			result.setResponseCodeOK();
		} catch (AblyException e) {
			logger.error("Failed to get stats " + client , e);
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Failed to get stats {0}.", client));
			result.setResponseData(MessageFormat.format("Stats [{0}] failed with exception.", client.options.clientId).getBytes());
			result.setResponseCode(String.valueOf(e.errorInfo.statusCode));
		} finally {
			if(result.getEndTime() == 0) {
				result.sampleEnd();
			}
			return result;
		}
	}

	public static String writeJson(Stats[] items) {
		return Serialisation.gson.toJson(items);
	}
}
