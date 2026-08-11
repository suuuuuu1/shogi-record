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
import java.util.concurrent.CopyOnWriteArrayList

// 中継局 + 座席係: 先着 1 人目 = 先手、2 人目 = 後手、3 人目以降 = 観戦
@Component
class GameSocketHandler : TextWebSocketHandler() {

    // つながっている電話回線 (セッション) の一覧
    private val sessions = CopyOnWriteArrayList<WebSocketSession>()

    // 座席。空席なら null
    private var sente: WebSocketSession? = null
    private var gote: WebSocketSession? = null

    @Synchronized
    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
        val role = when {
            sente == null -> { sente = session; "s" }     // 先着 → 先手の席へ
            gote == null  -> { gote = session; "g" }      // 2 人目 → 後手の席へ
            else -> "spec"                                 // 満席 → 観戦席
        }
        // 座った本人にだけ「あなたの役割」を通知
        session.sendMessage(TextMessage("""{"type":"role","role":"$role"}"""))
    }

    @Synchronized
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session)
        if (sente === session) sente = null   // 席を空ける (次に来た人が座れる)
        if (gote === session) gote = null
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        for (s in sessions) {
            if (s !== session && s.isOpen) s.sendMessage(message)
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