# WebSocket Quick Reference Card

## 🔌 Connection Endpoints

```
WebSocket: ws://localhost:8080/ws
With SockJS: http://localhost:8080/ws
```

## 📊 Monitoring Endpoints

| Endpoint | Description | Response |
|----------|-------------|----------|
| `GET /api/websocket/metrics` | Full metrics | JSON with all stats |
| `GET /api/websocket/health` | Health check | Status: UP/DEGRADED |
| `GET /api/websocket/online-users` | Online users list | Array of user IDs |
| `GET /api/websocket/sessions` | Active sessions | Session details |
| `GET /api/websocket/cleanup` | Trigger cleanup | Cleanup count |

## ⚙️ Configuration Quick Edit

**Location:** `src/main/resources/application.properties`

```properties
# Quick Performance Tuning
websocket.thread-pool-size=10               # More threads = handle more concurrent users
websocket.message-size-limit=131072         # Larger = support bigger messages (bytes)
websocket.send-buffer-size-limit=1048576    # Larger = better throughput (bytes)

# Quick Reliability Tuning
websocket.heartbeat-interval=20000          # Faster = detect issues sooner (ms)
websocket.max-missed-heartbeats=3           # Higher = more lenient (count)
websocket.session-idle-timeout=600000       # Shorter = cleanup faster (ms)
```

## 🎯 Key Timings (Defaults)

| Event | Timing |
|-------|--------|
| Heartbeat check | Every 20 seconds |
| Stale after | 60 seconds (3 missed heartbeats) |
| Session idle timeout | 10 minutes |
| Auto cleanup | Every 5 minutes |
| Health check | Every 30 seconds |
| Stats logging | Every 10 minutes |

## 🚦 Health Status

| Health % | Status | Action |
|----------|--------|--------|
| ≥ 80% | ✅ UP | Normal |
| < 80% | ⚠️ DEGRADED | Check logs |
| 0% | ❌ DOWN | Restart recommended |

## 📝 Common Tasks

### Check Current Status
```bash
curl http://localhost:8080/api/websocket/health
```

### See Online Users
```bash
curl http://localhost:8080/api/websocket/online-users
```

### Force Cleanup
```bash
curl http://localhost:8080/api/websocket/cleanup
```

### View Metrics
```bash
curl http://localhost:8080/api/websocket/metrics | jq
```

## 🔧 Performance Tuning Guide

### High Load (100+ concurrent users)
```properties
websocket.thread-pool-size=20
websocket.thread-pool-queue-capacity=500
websocket.relay.enabled=true  # Use RabbitMQ
```

### Low Latency Priority
```properties
websocket.heartbeat-interval=10000
websocket.send-time-limit=10000
```

### Resource Constrained
```properties
websocket.thread-pool-size=5
websocket.session-idle-timeout=300000  # 5 min
websocket.stale-session-cleanup-interval=60000  # 1 min
```

## 🐛 Quick Troubleshooting

| Problem | Solution |
|---------|----------|
| Connections drop | Increase `heartbeat-interval` to 30000 |
| High memory | Decrease `session-idle-timeout` to 300000 |
| Slow messages | Increase `thread-pool-size` to 20 |
| Stale sessions piling up | Check `websocket.stale-session-cleanup-interval` |

## 📱 Client-Side (JavaScript)

### Minimal Connection
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);
stompClient.heartbeat.outgoing = 20000;  // Important!
stompClient.heartbeat.incoming = 20000;  // Important!
stompClient.connect({}, onConnect, onError);
```

### Subscribe to Chat
```javascript
stompClient.subscribe('/user/queue/messages', (message) => {
    const data = JSON.parse(message.body);
    console.log('Message:', data);
});
```

### Subscribe to User Status
```javascript
stompClient.subscribe('/topic/user-status', (status) => {
    const data = JSON.parse(status.body);
    console.log('User status:', data.type, data.username);
});
```

### Send Message
```javascript
stompClient.send('/app/chat', {}, JSON.stringify({
    senderId: 'user123',
    recipientId: 'user456',
    content: 'Hello!',
    chatId: 'room-id'
}));
```

## 📊 Metrics Interpretation

### Good Health Signs
- `activeHeartbeats` ≈ `activeSessions`
- `staleSessions` = 0 or very low
- `healthPercentage` > 80%

### Warning Signs
- `staleSessions` > 20% of `totalSessions`
- `healthPercentage` < 80%
- `failedConnections` increasing

### Critical Issues
- `activeConnections` = 0 but should have users
- `healthPercentage` < 50%
- Continuous increase in `staleSessions`

## 🎛️ Feature Toggles

### Disable Heartbeat Monitoring
```properties
# Not recommended, but possible by increasing intervals
websocket.heartbeat-interval=999999999
websocket.max-missed-heartbeats=999
```

### Disable Auto Cleanup
```properties
# Not recommended
websocket.stale-session-cleanup-interval=-1
```

### Enable External Broker
```properties
websocket.relay.enabled=true
websocket.relay.host=localhost
websocket.relay.port=61613
```

## 📞 Quick Commands

```bash
# Build
mvnw clean package -DskipTests

# Run
mvnw spring-boot:run

# Test WebSocket
wscat -c ws://localhost:8080/ws

# Monitor logs (Windows PowerShell)
Get-Content application.log -Wait -Tail 50

# Check if running
curl http://localhost:8080/api/websocket/health
```

## 🔐 Security Checklist

- [ ] Enable HTTPS/WSS in production
- [ ] Configure proper CORS origins
- [ ] Add authentication to WebSocket endpoints
- [ ] Rate limit WebSocket messages
- [ ] Monitor for suspicious connection patterns
- [ ] Use secure STOMP relay credentials

## 📈 Scaling Checklist

- [ ] Enable STOMP relay (`websocket.relay.enabled=true`)
- [ ] Use Redis for session sharing (if load balanced)
- [ ] Configure sticky sessions on load balancer
- [ ] Monitor metrics regularly
- [ ] Set up alerts for health < 80%
- [ ] Increase thread pool for high concurrency

## 🎯 One-Line Health Check

```bash
curl -s http://localhost:8080/api/websocket/health | grep "UP"
```

If output contains "UP", everything is working! ✅

---

**Keep this file handy for quick reference during development and deployment!**
