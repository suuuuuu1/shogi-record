package shogi_record

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {
    @GetMapping("/")
    fun hello(): String {
        return "バックエンドの世界へようこそ。対局記録APIのベースが起動しました。"
    }
}