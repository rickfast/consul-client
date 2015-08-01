package com.orbitz.consul.util;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.model.kv.Value;

public class LeaderElectionUtil {

    private final Consul client;

    public LeaderElectionUtil(Consul client) {
        this.client = client;
    }

    public String getLeaderInfoForService(final String serviceName) {
        String leaderInfo = null;
        String key = getServiceKey(serviceName);
        Optional<Value> value = client.keyValueClient().getValue(key);
        if(value.isPresent()){
            if(!Strings.isNullOrEmpty(value.get().getSession())) {
                leaderInfo = getLeaderInfo(value);
            }
        }
        return leaderInfo;
    }

    private String getLeaderInfo(Optional<Value> value) {
        return ClientUtil.decodeBase64(value.get().getValue());
    }

    public String electNewLeaderForService(final String serviceName, final String info) {
        final String key = getServiceKey(serviceName);
        String sessionId = createSession(serviceName);
        if(client.keyValueClient().acquireLock(key, info, sessionId)){
            return info;
        }else{
            return getLeaderInfoForService(serviceName);
        }
    }

    public boolean releaseLockForService(final String serviceName) {
        final String key = getServiceKey(serviceName);
        KeyValueClient kv = client.keyValueClient();
        if(kv.getValue(key).isPresent()) {
            return kv.releaseLock(key, kv.getValue(key).get().getSession());
        } else {
            return true;
        }
    }


    private String createSession(String serviceName) {
        final String value = "{\"Name\":\"" + serviceName + "\"}";
        return client.sessionClient().createSession(value).get();
    }

    private static String getServiceKey(String serviceName) {
        return "service/" + serviceName + "/leader";
    }

}
