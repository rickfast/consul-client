package com.orbitz.consul.util;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.SessionClient;
import com.orbitz.consul.model.kv.Value;

public class LeaderElectionUtil {

    Consul client;
    KeyValueClient keyValueClient;
    SessionClient sessionClient;

    public void LeaderElectionUtil() {
        this.client = Consul.newClient();
        this.keyValueClient = client.keyValueClient();
        this.sessionClient = client.sessionClient();
    }

    public static String getLeaderInfoForService(Consul client, final String serviceName) {
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

    private static String getLeaderInfo(Optional<Value> value) {
        return ClientUtil.decodeBase64(value.get().getValue());
    }

    public static String electNewLeaderForService(Consul client, final String serviceName, final String info) {
        final String key = getServiceKey(serviceName);
        String sessionId = createSession(client, serviceName);
        if(client.keyValueClient().acquireLock(key, info, sessionId)){
            return info;
        }else{
            return getLeaderInfoForService(client, serviceName);
        }
    }

    public static boolean releaseLockForService(Consul client, final String serviceName) {
        final String key = getServiceKey(serviceName);
        KeyValueClient kv = client.keyValueClient();
        return kv.releaseLock(key, kv.getValue(key).get().getSession());
    }


    private static String createSession(Consul client, String serviceName) {
        final String value = "{\"Name\":\"" + serviceName + "\"}";
        return client.sessionClient().createSession(value).get();
    }

    private static String getServiceKey(String serviceName) {
        return "service/" + serviceName + "/leader";
    }

}
