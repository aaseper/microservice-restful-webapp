package eolopark.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eolopark.server.model.internal.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This component is used to associate the websocket session with the report id.
 * <p>
 * In this way, when a report is updated, a message is sent to the websocket sessions interested on it.
 * <p>
 * The reportId is included in the URL used to connect to the websocket.
 */
@Component
public class NotificationService extends AbstractWebSocketHandler {
    Logger logger = LoggerFactory.getLogger(NotificationService.class);
    @Value ("${controller.verbosity}")
    private int verbosity;

    // This maps contains the websocket sessions interested in every report
    private final ConcurrentMap<Long, List<WebSocketSession>> sessionsPerReport = new ConcurrentHashMap<>();

    //When a session is established, it is added to the map associated with the report id
    @Override
    public void afterConnectionEstablished (WebSocketSession session) throws Exception {
        if (verbosity > 0) logger.info("User connected " + session.getId());

        Long id = getReportIdFromURI(session);

        List<WebSocketSession> sessions = sessionsPerReport.computeIfAbsent(id, __ -> new CopyOnWriteArrayList<>());
        sessions.add(session);
    }

    //When a session is closed, it is removed from map
    @Override
    public void afterConnectionClosed (WebSocketSession session, CloseStatus status) throws Exception {
        if (verbosity > 0) logger.info("User disconnected " + session.getId());

        Long id = getReportIdFromURI(session);

        List<WebSocketSession> sessions = sessionsPerReport.get(id);
        sessions.remove(session);
    }

    private static Long getReportIdFromURI (WebSocketSession session) {

        URI uri = session.getUri();

        MultiValueMap<String, String> parameters =
                UriComponentsBuilder.fromUriString(uri.toString()).build().getQueryParams();

        return Long.parseLong(parameters.getFirst("reportId"));
    }

    //When a report is updated, a message is sent to the user interested on this report
    public void notifyReportUpdate (Report report) {

        Long id = report.getId();

        List<WebSocketSession> sessions = sessionsPerReport.get(id);

        try {
            String reportJSON = new ObjectMapper().writeValueAsString(report);

            if (sessions != null) {
                for (WebSocketSession session : sessions) {
                    try {
                        session.sendMessage(new TextMessage(reportJSON));
                    } catch (IOException e) {
                        if (verbosity > 0) logger.error("Exception sending message to WebSocketSession " + session.getId(), e);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}