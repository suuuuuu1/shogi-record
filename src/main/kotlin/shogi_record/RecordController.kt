package shogi_record

import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.web.bind.annotation.*

data class ShogiRecord(
    val id: Int = 0,
    val date: String,
    val result: String,
    val side: String,
    val opening: String,
    val moves: Int
)

data class GameRequest(val winner: String, val kifu: String , val data : String)

@RestController
class RecordController(private val jdbc: JdbcTemplate) {

    // DB の 1 行 → ShogiRecord に変換する翻訳係
    private val rowMapper = RowMapper<ShogiRecord> { rs, _ ->
        ShogiRecord(
            id = rs.getInt("id"),
            date = rs.getString("date"),
            result = rs.getString("result"),
            side = rs.getString("side"),
            opening = rs.getString("opening"),
            moves = rs.getInt("moves")
        )
    }

    @GetMapping("/records")
    fun getAll(): List<ShogiRecord> =
        jdbc.query("SELECT * FROM records ORDER BY id", rowMapper)

    @GetMapping("/records/{id}")
    fun getOne(@PathVariable id: Int): ResponseEntity<ShogiRecord> {
        val record = jdbc.query("SELECT * FROM records WHERE id = ?", rowMapper, id).firstOrNull()
        return if (record != null) ResponseEntity.ok(record)
        else ResponseEntity.notFound().build()
    }

    @GetMapping("/games")
    fun getGames():List<Map<String, Any?>> =
        jdbc.queryForList("SELECT * FROM games ORDER BY id DESC", )

    @PostMapping("/records")
    fun add(@RequestBody record: ShogiRecord): ShogiRecord {
        val newId = jdbc.queryForObject(
            "INSERT INTO records (date, result, side, opening, moves) VALUES (?, ?, ?, ?, ?) RETURNING id",
            Int::class.java,
            record.date, record.result, record.side, record.opening, record.moves
        )
        return record.copy(id = newId ?: 0)
    }
    @PostMapping("/games")
    fun addGame(@RequestBody game: GameRequest): GameRequest {
        jdbc.update(
            "INSERT INTO games (winner, kifu, data) VALUES (?, ?, ?)",
            game.winner, game.kifu, game.data
        )
        return game
    }

    @PutMapping("/records/{id}")
    fun update(@PathVariable id: Int, @RequestBody record: ShogiRecord): ResponseEntity<ShogiRecord> {
        val count = jdbc.update(
            "UPDATE records SET date = ?, result = ?, side = ?, opening = ?, moves = ? WHERE id = ?",
            record.date, record.result, record.side, record.opening, record.moves, id
        )
        return if (count == 1) ResponseEntity.ok(record.copy(id = id))
        else ResponseEntity.notFound().build()
    }

    @DeleteMapping("/records/{id}")
    fun delete(@PathVariable id: Int): Map<String, Boolean> {
        val count = jdbc.update("DELETE FROM records WHERE id = ?", id)
        return mapOf("deleted" to (count == 1))
    }

    @GetMapping("/stats")
    fun stats(): List<Map<String, Any?>> =
        jdbc.queryForList("""
        SELECT opening,
               COUNT(*) AS games,
               SUM(CASE WHEN result = 'win' THEN 1 ELSE 0 END) AS wins,
               ROUND(100.0 * SUM(CASE WHEN result = 'win' THEN 1 ELSE 0 END) / COUNT(*), 1) AS win_rate
        FROM records
        GROUP BY opening
        ORDER BY games DESC
    """.trimIndent())



}