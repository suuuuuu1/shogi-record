package shogi_record

import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

// 中継局 + 座席係 + 部屋係: 部屋ごとに独立した対局室 (先着 = 先手、2 人目 = 後手、以降 = 観戦)
@Component
class GameSocketHandler : TextWebSocketHandler() {

    // 1 部屋ぶんの状態: つながってる回線と座席
    class Room {
        val sessions = CopyOnWriteArrayList<WebSocketSession>()
        var sente: WebSocketSession? = null
        var gote: WebSocketSession? = null
    }

    // 部屋の一覧 (部屋名 → Room)。無い部屋は入った瞬間に作られる
    private val rooms = ConcurrentHashMap<String, Room>()

    // 接続 URL (/ws?room=名前) から部屋名を取り出す。無指定は "lobby"
    private fun roomName(session: WebSocketSession): String {
        val q = session.uri?.query ?: return "lobby"
        return q.split("&").firstOrNull { it.startsWith("room=") }
            ?.substringAfter("=")?.ifBlank { null } ?: "lobby"
    }

    @Synchronized
    override fun afterConnectionEstablished(session: WebSocketSession) {
        val room = rooms.getOrPut(roomName(session)) { Room() }
        room.sessions.add(session)
        val role = when {
            room.sente == null -> { room.sente = session; "s" }
            room.gote == null  -> { room.gote = session; "g" }
            else -> "spec"
        }
        session.sendMessage(TextMessage("""{"type":"role","role":"$role"}"""))
    }

    @Synchronized
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val name = roomName(session)
        val room = rooms[name] ?: return
        room.sessions.remove(session)
        if (room.sente === session) room.sente = null
        if (room.gote === session) room.gote = null
        if (room.sessions.isEmpty()) rooms.remove(name)   // 誰もいなくなった部屋は畳む
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val room = rooms[roomName(session)] ?: return
        for (s in room.sessions) {
            if (s !== session && s.isOpen) s.sendMessage(message)   // 同じ部屋の人にだけ転送
        }
    }
}

// 「/ws に電話をかけてきたら GameSocketHandler につなぐ」という配線
@Configuration
@EnableWebSocket
class WebSocketConfig(private val handler: GameSocketHandler) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(handler, "/ws").setAllowedOrigins("*")   // トンネル/外部経由の接続も受け付ける (学習用)
    }
}