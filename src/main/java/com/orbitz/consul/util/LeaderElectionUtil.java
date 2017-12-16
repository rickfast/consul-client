package com.orbitz.consul.util;

import java.util.Optional;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.Session;

public class LeaderElectionUtil {

    private final Consul client;

    public LeaderElectionUtil(Consul client) {
        this.client = client;
    }

    public Optional<String> getLeaderInfoForService(final String serviceName) {
        String key = getServiceKey(serviceName);
        Optional<Value> value = client.keyValueClient().getValue(key);
        return value.flatMap(val -> {
            if(val.getSession().isPresent()) {
                return val.getValueAsString();
            }
            return Optional.empty();
        });
    }

    public Optional<String> electNewLeaderForService(final String serviceName, final String info) {
        final String key = getServiceKey(serviceName);
        String sessionId = createSession(serviceName);
        if(client.keyValueClient().acquireLock(key, info, sessionId)){
            return Optional.of(info);
        }else{
            return getLeaderInfoForService(serviceName);
        }
    }

    public boolean releaseLockForService(final String serviceName) {
        final String key = getServiceKey(serviceName);
        KeyValueClient kv = client.keyValueClient();
        Optional<Value> value = kv.getValue(key);
        if(value.isPresent() && value.get().getSession().isPresent()) {
            return kv.releaseLock(key, value.get().getSession().get());
        } else {
            return true;
        }
    }


    private String createSession(String serviceName) {
        final Session session = ImmutableSession.builder().name(serviceName).build();
        return client.sessionClient().createSession(session).getId();
    }

    private static String getServiceKey(String serviceName) {
        return "service/" + serviceName + "/leader";
    }

}
