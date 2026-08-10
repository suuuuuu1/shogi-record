package shogi_record

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ShogiRecordApplication

fun main(args: Array<String>) {
	runApplication<ShogiRecordApplication>(*args)
}
