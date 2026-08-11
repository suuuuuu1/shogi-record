package shogi_record
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
data class ShogiRecord(
    val id: Int = 0,
    val date : String,
    val result: String,
    val side : String,
    val opening : String,
    val moves : Int
)
@RestController
class RecordController {

    private val records = mutableListOf<ShogiRecord>()

    @GetMapping("/record")
    fun getAll(): List<ShogiRecord> = records

    @PostMapping("/records")
    fun add(@RequestBody record: ShogiRecord): ShogiRecord {
        val newRecord = record.copy(id = (records.maxOfOrNull { it.id } ?: 0) + 1)
        records.add(newRecord)
        return newRecord
    }

    @GetMapping("/records/{id}")
    fun getOne(@PathVariable id: Int): ResponseEntity<ShogiRecord> {
        val record = records.find { it.id == id }
        return if (record != null) ResponseEntity.ok(record)
        else ResponseEntity.notFound().build()
    }

    @DeleteMapping("records/{id}")
    fun delete(@PathVariable id : Int):Map<String , Boolean>{
        val removed = records.removeIf{ it.id == id }
        return mapOf("deleted" to removed)
    }

    @PutMapping("/records/{id}")
    fun update(@PathVariable id : Int, @RequestBody record: ShogiRecord): ShogiRecord?{
        val index = records.indexOfFirst {it.id == id}
        if(index == -1)return null
        records[index] = record.copy(id = id)
        return record
    }
}