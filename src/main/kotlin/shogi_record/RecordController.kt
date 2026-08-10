package shogi_record
import org.springframework.web.bind.annotation.*
data class ShogiRecord(
    val id: Int,
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
    fun add(@RequestBody record: ShogiRecord): ShogiRecord{
        records.add(record)
        return record
    }

    @GetMapping("/records/{id}")
    fun getOne(@PathVariable id : Int ): ShogiRecord?{
        return records.find { it.id == id }
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
        records[index] = record
        return record
    }
}