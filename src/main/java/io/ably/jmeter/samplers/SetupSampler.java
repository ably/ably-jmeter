package io.ably.jmeter.samplers;

import io.ably.lib.rest.AblyRest;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.ClientOptions;
import org.slf4j.Logger;

public abstract class SetupSampler extends BaseSampler {

    public SetupSampler(Logger logger) {
        super(logger);
    }

    protected AblyRest getAbly(String environment, String key) {
        ClientOptions opts = new ClientOptions();
        /* we need to provide an appId to keep the library happy,
         * but we are only instancing the library to use the http
         * convenience methods */
        opts.key = key == null ? "none:none" : key;
        opts.environment = environment;
        try {
            return new AblyRest(opts);
        } catch (AblyException e) {
            logger.error("Unable to set up library", e);
            return null;
        }
    }

    static class Key {
        public String keyStr;
    }

    static class Namespace {
        public String id;
        public boolean persisted;
        public boolean pushEnabled;
    }

    static class AppSpec {
        public String appId;
        public String accountId;
        public Key[] keys;
        public Namespace[] namespaces;
        public String notes;
    }
}
