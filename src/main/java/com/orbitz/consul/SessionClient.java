package com.orbitz.consul;

import com.google.common.base.Optional;
import com.orbitz.consul.model.session.Session;
import com.orbitz.consul.model.session.SessionCreatedResponse;
import com.orbitz.consul.model.session.SessionInfo;
import com.orbitz.consul.option.QueryOptions;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

import static com.orbitz.consul.util.ClientUtil.addParams;

/**
 * HTTP Client for /v1/session/ endpoints.
 *
 * @see <a href="http://www.consul.io/docs/agent/http.html#session">The Consul API Docs</a>
 */
public class SessionClient {

    private static final GenericType<SessionCreatedResponse> SESSION_CREATED_RESPONSE_TYPE =
            new GenericType<SessionCreatedResponse>() {
            };
    private static final GenericType<List<SessionInfo>> SESSION_INFO_LIST_TYPE =
            new GenericType<List<SessionInfo>>() {
            };

    private final WebTarget webTarget;

    /**
     * Constructs an instance of this class.
     *
     * @param webTarget The {@link WebTarget} to base requests from.
     */
    SessionClient(WebTarget webTarget) {
        this.webTarget = webTarget;
    }

    /**
     * Retrieves the host/port of the Consul leader.
     * 
     * GET /v1/status/leader
     *
     * @return The host/port of the leader.
     */
    public String getLeader() {
        return webTarget.path("leader").request().get(String.class)
                .replace("\"", "").trim();
    }

    /**
     * Create Session.
     * 
     * PUT /v1/session/create
     *
     * @param value The session to create.
     * @return ID of the newly created session .
     */
    public SessionCreatedResponse createSession(final Session value) {
        return createSession(value, null);
    }

    /**
     * Create Session.
     * 
     * PUT /v1/session/create
     *
     * @param value The session to create.
     * @param dc    The data center.
     * @return Response containing the session ID.
     */
    public SessionCreatedResponse createSession(final Session value, final String dc) {
        WebTarget target = webTarget;

        if (dc != null) {
            target = webTarget.queryParam("dc", dc);
        }

        SessionCreatedResponse session = target.path("create").request().put(Entity.entity(value,
                MediaType.APPLICATION_JSON_TYPE), SESSION_CREATED_RESPONSE_TYPE);

        return session;
    }

    public Optional<SessionInfo> renewSession(final String sessionId) {
        return renewSession(null, sessionId);
    }

    /**
     * Renews a session.
     *
     * @param dc        The datacenter.
     * @param sessionId The session ID to renew.
     * @return The {@link SessionInfo} object for the renewed session.
     */
    public Optional<SessionInfo> renewSession(final String dc, final String sessionId) {
        WebTarget target = webTarget;

        if (dc != null) {
            target = webTarget.queryParam("dc", dc);
        }

        List<SessionInfo> sessionInfo = target.path("renew").path(sessionId).request().put(Entity.entity("{}",
                MediaType.APPLICATION_JSON_TYPE), SESSION_INFO_LIST_TYPE);

        return sessionInfo != null && sessionInfo.isEmpty() ? Optional.<SessionInfo>absent() :
                Optional.of(sessionInfo.get(0));
    }

    /**
     * Destroys a session.
     * 
     * PUT /v1/session/destroy/{sessionId}
     *
     * @param sessionId The session ID to destroy.
     */
    public void destroySession(final String sessionId) {
        destroySession(sessionId, null);
    }

    /**
     * Destroys a session.
     * 
     * PUT /v1/session/destroy/{sessionId}
     *
     * @param sessionId The session ID to destroy.
     * @param dc        The data center.
     */
    public void destroySession(final String sessionId, final String dc) {
        WebTarget target = webTarget;

        if (dc != null) {
            target = webTarget.queryParam("dc", dc);
        }

        target.path("destroy").path(sessionId).request().put(Entity.entity("",
                MediaType.TEXT_PLAIN_TYPE));
    }

    /**
     * Retrieves session info.
     * 
     * GET /v1/session/info/{sessionId}
     *
     * @param sessionId
     * @return {@link SessionInfo}.
     */
    public Optional<SessionInfo> getSessionInfo(final String sessionId) {
        return getSessionInfo(sessionId, null);
    }

    /**
     * Retrieves session info.
     * 
     * GET /v1/session/info/{sessionId}
     *
     * @param sessionId
     * @param dc        Data center
     * @return {@link SessionInfo}.
     */
    public Optional<SessionInfo> getSessionInfo(final String sessionId, final String dc) {
        WebTarget target = webTarget;

        if (dc != null) {
            target = target.queryParam("dc", dc);
        }

        target = addParams(target.path("info").path(sessionId), QueryOptions.BLANK);

        List<SessionInfo> sessionInfo = Arrays.asList(target
                .request().accept(MediaType.APPLICATION_JSON_TYPE).get(SessionInfo[].class));

        return sessionInfo != null && sessionInfo.isEmpty() ? Optional.<SessionInfo>absent() :
                Optional.of(sessionInfo.get(0));
    }

    /**
     * Lists all sessions.
     * 
     * GET /v1/session/list
     *
     * @param dc The data center.
     * @return A list of available sessions.
     */
    public List<SessionInfo> listSessions(final String dc) {
        WebTarget target = webTarget.path("list");

        if (dc != null) {
            target = target.queryParam("dc", dc);
        }

        return target.request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(SESSION_INFO_LIST_TYPE);
    }

    /**
     * Lists all sessions.
     * 
     * GET /v1/session/list
     *
     * @return A list of available sessions.
     */
    public List<SessionInfo> listSessions() {
        return listSessions(null);
    }
}
