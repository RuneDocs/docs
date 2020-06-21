interface Packet

/**
 * Model for a floor item
 * Note: Not necessarily best practises, just a complete example
 * @param id The item id
 * @param amount The item stack size
 * @param x The floor tile x coordinate
 * @param y The floor tile x coordinate
 * @param plane The mapsquare plane
 * @param zone A unique id for a zone and plane
 * @param stackable Whether the item can have an [amount] greater than one
 * @param state The current visibility state
 * @param revealTime Ticks until item changes to [State.Public] (-1 for never)
 * @param disappearTime Ticks until item is removed (-1 for never)
 * @param owner The index of the player who's private item it is
 */
data class FloorItem(
        val id: Int,
        var amount: Int,
        val x: Int,
        val y: Int,
        val plane: Int,
        val zone: Int = y + (x shl 12) + (plane shl 24),
        val stackable: Boolean = amount > 1,
        var state: State = State.Private,
        var revealTime: Int = -1,
        var disappearTime: Int = -1,
        var owner: Int = -1
) {
    /**
     * Zone offset coordinates combined
     */
    fun offset() = (x.rem(8) shl 4) or y.rem(8)
}

/**
 * Floor item visibility state
 */
sealed class State {
    object Private : State()
    object Public : State()
}

/**
 * Note: Does not include packets or logic for sending batched packets
 */
class FloorItems {

    /**
     * List of floor items grouped per zone
     */
    val items: HashMap<Int, MutableList<FloorItem>> = hashMapOf()

    /**
     * List of batched updates per zone
     */
    val updates: HashMap<Int, MutableList<Packet>> = hashMapOf()

    /**
     * Adds a packet to [zone] for the next batched update
     */
    fun update(zone: Int, packet: Packet) = updates.getOrPut(zone) { mutableListOf() }.add(packet)

    /**
     * Drops a floor item, combining with existing items when stackable and private
     */
    fun drop(item: FloorItem) {
        if (item.stackable) {
            val existing = items[item.zone]?.firstOrNull {
                it.state == State.Private &&
                it.id == item.id &&
                it.x == item.x &&
                it.y == item.y &&
                it.plane == item.plane
            }
            if (existing != null && combinedStacks(existing, item)) {
                return
            }
        }

        val zoneList = items.getOrPut(item.zone) { mutableListOf() }
        zoneList.add(item)
//        update(item, AddFloorItemMessage(item.offset(), item.id, item.amount))
    }

    /**
     * Combines stacks of two items and resets the disappear count down
     * Note: If total of combined stacks exceeds [Int.MAX_VALUE] then returns false
     */
    private fun combinedStacks(existing: FloorItem, item: FloorItem): Boolean {
        val stack = existing.amount
        val amount = item.amount
        val combined = stack + amount
        // Overflow should add as separate item
        if (stack xor combined and (amount xor combined) < 0) {
            return false
        }
        existing.amount = combined
//        update(item.zone, UpdateFloorItemPacket(existing.offset(), existing.id, stack, combined))
        existing.disappearTime = item.disappearTime
        disappearCountDown(item)
        return true
    }

    fun tick() {
        items.forEach { (_, list) ->
            list.forEach { item ->
                disappearCountDown(item)
                revealCountDown(item)
            }
        }
    }

    private fun disappearCountDown(item: FloorItem) {
        if (item.disappearTime >= 0) {
            if (--item.disappearTime <= 0) {
//                update(item.zone, RemoveFloorItemPacket(item.offset(), item.id))
                items[item.zone]?.remove(item)
            }
        }
    }

    private fun revealCountDown(item: FloorItem) {
        if (item.revealTime >= 0) {
            if (--item.revealTime <= 0) {
                item.state = State.Public
//                update(item.zone, RevealFloorItemPacket(item.offset(), item.id, item.amount, item.owner))
            }
        }
    }
}

fun main() {
    val items = FloorItems()

    val item = FloorItem(id = 995, amount = 10, x = 3260, y = 3260, plane = 0, revealTime = 2, disappearTime = 2)
    val second = item.copy(amount = 20)
    items.drop(item)
    items.tick()

    items.drop(second)

    assert(item.amount == 30)
    assert(item.revealTime == 1)
    assert(item.disappearTime == 2)

    items.tick()

    assert(item.state == State.Public)
    assert(item.revealTime == 0)
    assert(item.disappearTime == 1)
}
