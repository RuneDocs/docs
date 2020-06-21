interface Packet

class Player {

    /**
     * When called sends a zones batched messages to the client
     */
    val zoneBatchSubscription: (Int, MutableList<Packet>) -> Unit = { zone, messages ->
//        send(ZoneMessage(chunk))
        messages.forEach { message ->
            send(message)
        }
    }

    fun send(packet: Packet) {

    }
}

class ZoneBatch {

    /**
     * Map of which players are subscribed to which zones
     */
    val subscribers = mutableMapOf<Int, MutableSet<(Int, MutableList<Packet>) -> Unit>>()

    /**
     * Map of zones and the batches of update packets within each
     * Note: Cleared each tick
     */
    val batches = mutableMapOf<Int, MutableList<Packet>>()

    /**
     * Map of zones and the creation packets within each
     * For when players enter new zone
     */
    val creation = mutableMapOf<Int, MutableList<Packet>>()

    /**
     * Subscribes to a zone for batched updates
     * And sends creation updates immediatley
     */
    fun subscribe(player: Player, zone: Int) {
        val subscribers = getSubscribers(zone)
        if(subscribers.add(player.zoneBatchSubscription)) {
            val messages = creation[zone] ?: return
            player.zoneBatchSubscription.invoke(zone, messages)
        }
    }

    /**
     * Unsubscribes from a zones batched updates
     */
    fun unsubscribe(player: Player, zone: Int) {
        val subscribers = getSubscribers(zone)
        subscribers.remove(player.zoneBatchSubscription)
    }

    fun getSubscribers(zone: Int) = subscribers.getOrPut(zone) { mutableSetOf() }

    fun getBatch(zone: Int) = batches.getOrPut(zone) { mutableListOf() }

    fun getCreation(zone: Int) = creation.getOrPut(zone) { mutableListOf() }

    /**
     * Calls subscribers with batched updates for each zone
     */
    fun tick() {
        batches.forEach { (zone, messages) ->
            if(messages.isNotEmpty()) {
                subscribers[zone]?.forEach { subscriber ->
                    subscriber.invoke(zone, messages)
                }
                messages.clear()
            }
        }
    }

    /**
     * Publishes an update to a zone batch
     */
    fun publish(zone: Int, packet: Packet) {
        val batch = getBatch(zone)
        batch.add(packet)
    }

    /**
     * Adds creation packet for players entering a new zone
     */
    fun addCreation(zone: Int, packet: Packet) {
        val batch = getCreation(zone)
        creation.add(packet)
    }

    /**
     * Removes creation packet as the entity no longer exists
     */
    fun removeCreation(zone: Int, packet: Packet) {
        val batch = getCreation(zone)
        creation.remove(packet)
    }

}