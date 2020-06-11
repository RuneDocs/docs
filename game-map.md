# Game Map

## Layout

The game map in RuneScape is made out of a three dimensional box of four altitudes or height levels where each height level holds a symmetrical two dimensional grid of 16384 x 16384 tiles. Each grid on a height level is divided into two separate units:

- Map squares
- Zones

A map square is a 64 x 64 area of tiles. In contrast with a map square, a zone is a more fine-grained area consisting of 8 x 8 tiles. This means that each map square can hold up to 8 x 8 zones.

TODO: diagrams

Conceptually, the grid of the game world can be modeled as a three dimensional array of:

```kotlin
const val ZONE_COUNT = 16384 / 8 // 16384 tiles on each axis divided by 8 tiles per zone
const val ALTITUDE_COUNT = 4

private val zones = Array(ALTITUDE_COUNT) { Array(ZONE_COUNT) { arrayOfNulls<Zone?>(ZONE_COUNT) } }
```

Alternative and arguably more memory efficient ways involve using a `HashMap` but that's a mere detail.

## Instancing

In the past, RuneScape private servers would attempt to emulate instancing by placing players on specific height levels by using a bit value overflow trick that the client ended up treating as the bottom height level (0). However, this isn't how Jagex solved the instancing problem.

TODO: diagrams

```kotlin
fun findEmptyArea(altitude: Int, width: Int, length: Int): Position? {
    val mapSpanX = width / MapSquare.SIZE
    val mapSpanZ = length / MapSquare.SIZE

    VerticalSearch@
    for (z in 0 until SIZE step width) {
        HorizontalSearch@
        for (x in 0 until SIZE step length) {
            val mapX = x / MapSquare.SIZE
            val mapZ = z / MapSquare.SIZE

            for (i in mapX until mapX + mapSpanX) {
                val zoneX = i * Zone.SIZE
                val zoneZ = mapZ * Zone.SIZE

                val zone = zones[altitude][zoneX][zoneZ]
                if (zone != null) {
                    continue@HorizontalSearch
                }
            }

            for (i in mapZ until (mapZ + mapSpanZ)) {
                val zoneX = mapX * Zone.SIZE
                val zoneZ = i * Zone.SIZE

                val zone = zones[altitude][zoneX][zoneZ]
                if (zone != null) {
                    continue@VerticalSearch
                }
            }

            return Position(x, z, altitude)
        }
    }

    return null
}
```

TODO

```kotlin
fun copy(position: Position, rotation: Rotation): Zone {
    val original = get(position) ?: throw IllegalArgumentException()
    val copy = Zone(original.getPosition(), rotation)

    for (x in 0 until Zone.SIZE) {
         for (z in 0 until Zone.SIZE) {
             copy.collisions.add(x, z, original.collisions.get(x, z))
        }
    }

    return copy
}
```

TODO

```kotlin
fun paste(destination: Position, zone: Zone) {
    val existing = get(destination)
    if (existing != null) {
        clear(destination)
    }

     zone.setPosition(
        Position(
            x = destination.zoneX() * Zone.SIZE,
            z = destination.zoneZ() * Zone.SIZE,
            altitude = destination.altitude
        )
    )

    put(zone)
}
```

