package com.hdl.soar.framework.websocket.core.session;

import cn.hutool.core.collection.CollUtil;
import com.hdl.soar.framework.security.core.LoginUser;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import com.hdl.soar.framework.websocket.core.util.WebSocketFrameworkUtils;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory session registry.
 *
 * <p>Holds two indexes: by session id, and by (userType -> userId -> sessions). The
 * per-user index allows targeting all of a user's connections (e.g. multiple tabs).
 */
public class WebSocketSessionManagerImpl implements WebSocketSessionManager {

    /**
     * Mapping between session ID and WebSocketSession.
     * <p>
     * Key: Session ID
     */
    private final ConcurrentMap<String, WebSocketSession> idSessions = new ConcurrentHashMap<>();

    /**
     * Mapping between users and WebSocketSession.
     * <p>
     * Key1: User type <br>
     * Key2: User ID
     */
    private final ConcurrentMap<Integer, ConcurrentMap<Long, CopyOnWriteArrayList<WebSocketSession>>> userSessions =
            new ConcurrentHashMap<>();

    @Override
    public void addSession(WebSocketSession session) {
        idSessions.put(session.getId(), session);
        LoginUser user = WebSocketFrameworkUtils.getLoginUser(session);
        if (user == null) {
            return;
        }
        ConcurrentMap<Long, CopyOnWriteArrayList<WebSocketSession>> userSessionsMap =
                userSessions.computeIfAbsent(user.getUserType(), k -> new ConcurrentHashMap<>());
        userSessionsMap.computeIfAbsent(user.getId(), k -> new CopyOnWriteArrayList<>()).add(session);
    }

    @Override
    public void removeSession(WebSocketSession session) {
        idSessions.remove(session.getId());
        LoginUser user = WebSocketFrameworkUtils.getLoginUser(session);
        if (user == null) {
            return;
        }
        ConcurrentMap<Long, CopyOnWriteArrayList<WebSocketSession>> userSessionsMap =
                userSessions.get(user.getUserType());
        if (userSessionsMap == null) {
            return;
        }
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessionsMap.get(user.getId());
        if (sessions == null) {
            return;
        }
        sessions.removeIf(s -> s.getId().equals(session.getId()));
        if (CollUtil.isEmpty(sessions)) {
            userSessionsMap.remove(user.getId(), sessions);
        }
    }

    @Override
    public WebSocketSession getSession(String id) {
        return idSessions.get(id);
    }

    @Override
    public Collection<WebSocketSession> getSessionList(Integer userType) {
        ConcurrentMap<Long, CopyOnWriteArrayList<WebSocketSession>> userSessionsMap = userSessions.get(userType);
        if (CollUtil.isEmpty(userSessionsMap)) {
            return new ArrayList<>();
        }
        LinkedList<WebSocketSession> result = new LinkedList<>();
        Long contextTenantId = TenantContextHolder.getTenantId();
        for (List<WebSocketSession> sessions : userSessionsMap.values()) {
            if (CollUtil.isEmpty(sessions)) {
                continue;
            }
            // When a tenant context is present, only return sessions of that tenant.
            if (contextTenantId != null) {
                Long userTenantId = WebSocketFrameworkUtils.getTenantId(sessions.get(0));
                if (!contextTenantId.equals(userTenantId)) {
                    continue;
                }
            }
            result.addAll(sessions);
        }
        return result;
    }

    @Override
    public Collection<WebSocketSession> getSessionList(Integer userType, Long userId) {
        ConcurrentMap<Long, CopyOnWriteArrayList<WebSocketSession>> userSessionsMap = userSessions.get(userType);
        if (CollUtil.isEmpty(userSessionsMap)) {
            return new ArrayList<>();
        }
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessionsMap.get(userId);
        return CollUtil.isNotEmpty(sessions) ? new ArrayList<>(sessions) : new ArrayList<>();
    }

}
