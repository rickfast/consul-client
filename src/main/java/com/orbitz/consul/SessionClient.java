package com.orbitz.consul;

import com.google.common.base.Optional;
import com.orbitz.consul.model.session.SessionInfo;
import com.orbitz.consul.option.QueryOptions;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.orbitz.consul.util.ClientUtil.addParams;

public class SessionClient {

   private static final GenericType<Map<String, String>> TYPE_STRING_MAP =
           new GenericType<Map<String, String>>() {};

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
    * @param value empty string or JSON containing one or more SessionInfo
    * parameters (see {@link SessionInfo})
    *
    * @return ID of the newly created session .
    */
   public Optional<String> createSession(final String value) {
      return createSession(value, null);
   }

   /**
    * Create Session.
    *
    * PUT /v1/session/create
    *
    * @param value empty string or JSON containing one or more SessionInfo
    * parameters (see {@link SessionInfo})
    * @param dc Data center
    *
    * @return ID of the newly created session .
    */
   public Optional<String> createSession(final String value, final String dc) {
      Map<String, String> session = null;
      WebTarget target = webTarget;

      if (dc != null) {
         target = webTarget.queryParam("dc", dc);
      }

      session = target.path("create").request().put(Entity.entity(value,
              MediaType.APPLICATION_JSON_TYPE), TYPE_STRING_MAP);

      return session != null ? Optional.of(session.get("ID")) : Optional.<String>absent();
   }

   public boolean renewSession(final String sessionId) {
      return renewSession(null, sessionId);
   }

   /**
    * Renew a session
    *
    * @param dc
    * @param sessionId
    * @return if the session was renewed
    */
   public boolean renewSession(final String dc, final String sessionId) {
      WebTarget target = webTarget;

      if (dc != null) {
         target = webTarget.queryParam("dc", dc);
      }

      Response session = target.path("renew").path(sessionId).request().put(Entity.entity("{}",
              MediaType.APPLICATION_JSON_TYPE));
      boolean result = session.hasEntity();
      session.close();
      return result;

   }

   /**
    * Destroy session.
    *
    * PUT /v1/session/destroy/{sessionId}
    *
    * @param sessionId
    *
    * @return ID of the newly created session .
    */
   public boolean destroySession(final String sessionId) {
      return destroySession(sessionId, null);
   }

   /**
    * Destroy session.
    *
    * PUT /v1/session/destroy/{sessionId}
    *
    * @param sessionId
    * @param dc Data center
    *
    * @return ID of the newly created session .
    */
   public boolean destroySession(final String sessionId, final String dc) {
      WebTarget target = webTarget;

      if (dc != null) {
         target = webTarget.queryParam("dc", dc);
      }

      return target.path("destroy").path(sessionId).request().put(Entity.entity("",
              MediaType.TEXT_PLAIN_TYPE), Boolean.class);
   }

   /**
    * Retrieves session info.
    *
    * GET /v1/session/info/{sessionId}
    *
    * @param sessionId
    *
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
    * @param dc Data center
    *
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

      return sessionInfo != null && sessionInfo.size() != 0 ? Optional.of(sessionInfo.get(0)) : Optional.<SessionInfo>absent();
   }

}
