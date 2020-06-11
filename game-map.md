# Game Map

## Layout

The game map in RuneScape is made out of a three dimensional box of exactly four altitudes or height levels where each height level holds a symmetrical two dimensional grid of 16384 x 16384 tiles. Each grid on a height level is divided into two separate units:

- Map squares
- Zones

A map square is a 64 x 64 area of tiles. In contrast with a map square, a zone is a more fine-grained area consisting of 8 x 8 tiles. This means that each map square can hold up to 8 x 8 zones.

TODO: diagrams

Conceptually, the grid of the game world can be modeled as a three dimensional array of:

```java
private static final int
    SIZE = 16384,
    ZONE_COUNT = SIZE / 8, // 16384 tiles on each axis divided by 8 tiles per zone
    ALTITUDE_COUNT = 4;

private final Zone[][][] zones = new Zone[ALTITUDE_COUNT][ZONE_COUNT][ZONE_COUNT];
```

Alternative and arguably more memory efficient ways involve using a `HashMap` but that's a mere detail.

## Build Area

The build area is a limited view of the map the player can roam freely. Once the player reaches the edge of the build area, a rebuild is required which is to update the player's view. The build area consists of 104 x 104 tiles and showcases events that are currently happening in the game in that particular part of the map. As the build area consists of 104 x 104 tiles and each zone being 8 x 8 tiles, our build area consists of 13 x 13 zones. When a rebuild occurs, the build area recenters itself around the player's current position. The center of the build area is calculated by dividing the view in two which equals 6 zones (48 tiles, exclusively). This means that the player's position local to the build area, right after a rebuild will always be in zone 7, 7 (48, 48).

TODO: diagram

The margin that defines the edges of the build area is 16 tiles, which means that we can check if a rebuild is required by:

```java
private boolean rebuildRequired(Position position) {
    int x = getX(position);
    int z = getZ(position);

    boolean reachedLowerEdge = x < 16 || z < 16;
    boolean reachedUpperEdge = x >= 88 || z >= 88;

    return reachedLowerEdge || reachedUpperEdge;
}
```

The methods `getX()` and `getZ()` get the player's current x and z coordinates within the build area, respectively.

```java
private static final int RADIUS = 6;

private void doRebuild(World world, Position position) {
    int centerZoneX = position.getZoneX();
    int centerZoneZ = position.getZoneZ();

    int bottomLeftZoneX = centerZoneX - RADIUS;
    int bottomLeftZoneZ = centerZoneZ - RADIUS;

    for (int altitude = 0; altitude < ALTITUDE_COUNT; altitude++) {
        for (int localZoneX = 0; localZoneX < ZONE_COUNT; localZoneX++) {
            for (int localZoneZ = 0; localZoneZ < ZONE_COUNT; localZoneZ++) {
                int zoneX = bottomLeftZoneX + localZoneX;
                int zoneZ = bottomLeftZoneZ + localZoneZ;

                zones[altitude][localZoneX][localZoneZ] = world.get(altitude, zoneX, zoneZ);
            }
        }
    }

    lastRebuild = position;

    notifyRebuild(centerZoneX, centerZoneZ);
}
```

A rebuild will also involve refreshing the entities in ALL zones within the new build area. Every floor item and object is visually removed from the client and re-applied.

```java
private void refreshAllZones(Position center) {
    clearPendingUpdates();

    for (int localZoneX = 0; localZoneX < ZONE_COUNT; localZoneX++) {
        for (int localZoneZ = 0; localZoneZ < ZONE_COUNT; localZoneZ++) {
            Zone zone = zones[center.getAltitude()][localZoneX][localZoneZ];
            if (zone == null) {
                 continue;
            }

            refreshZone(zone);
        }
    }
}

private void refreshZone(Zone zone) {
    clearZone(zone.getPosition());

    for (int tileX = 0; tileX < Zone.SIZE; tileX++) {
        for (int tileZ = 0; tileZ < Zone.SIZE; tileZ++) {
            FloorItemStack itemStack = zone.getFloorItemStack(tileX, tileZ);
            if (itemStack == null || itemStack.isEmpty()) {
                continue;
            }

            for (Item item : itemStack) {
                spawnFloorItem(itemStack.getPosition(), item);
            }
        }
    }

    Collection<Loc> dynamicLocs = zone.getLocs();
    for (Loc loc : dynamicLocs) {
        spawnLoc(loc);
    }
}
```

## Instancing

In the past, RuneScape private servers would attempt to emulate instancing by placing players on specific height levels by using a bit value overflow trick that the client ended up treating as the bottom height level (0). However, this isn't how Jagex solved the instancing problem.

TODO: diagrams

In order to create an instance, the game searches for an empty spot that zones can be copied over to. In many cases, the game will prematurely calculate how many zones or map squares an instance will occupy. This may vary however in areas such as Chambers of Xeric and Theatre of Blood.

```java
public Position findEmptyArea(int altitude, int width, int length) {
    int mapSpanX = width / MapSquare.SIZE;
    int mapSpanZ = length / MapSquare.SIZE;

    VerticalSearch:
    for (int z = OFFSET_Z; z < SIZE; z += length) {
        HorizontalSearch:
        for (int x = OFFSET_X; x < SIZE; x += width) {
            int mapX = x / MapSquare.SIZE;
            int mapZ = z / MapSquare.SIZE;

            for (int i = mapX; i < mapX + mapSpanX; i++) {
                int zoneX = i * Zone.SIZE;
                int zoneZ = mapZ * Zone.SIZE;

                Zone zone = zones[altitude][zoneX][zoneZ];
                if (zone != null) {
                    continue HorizontalSearch;
                }
            }

            for (int i = mapZ; i < mapZ + mapSpanZ; i++) {
                int zoneX = mapX * Zone.SIZE;
                int zoneZ = i * Zone.SIZE;

                Zone zone = zones[altitude][zoneX][zoneZ];
                if (zone != null) {
                    continue VerticalSearch;
                }
            }

            return Position.abs(x, z, altitude);
        }
    }

    return null;
}
```

Once a suitable area has been found somewhere on the map, the target zone can be copied. It is important that the collision matrix is included in the copy.

```java
public Zone copy(Position position, Rotation rotation) {
    Zone original = Objects.requireNonNull(get(position));
    Zone copy = Zone.at(true, position, rotation);
    for (int x = 0; x < Zone.SIZE; x++) {
        for (int z = 0; z < Zone.SIZE; z++) {
            copy.getCollisionMatrix().set(x, z, original.getCollisionMatrix().get(x, z));
        }
    }

    return copy;
}
```

And the copied zone can then be pasted at the designated location of our empty area.

```java
public void paste(Position target, Zone zone) {
    if (get(target) != null) {
        throw new IllegalArgumentException();
    }

    zone.setPosition(Position.abs(target.getZoneX() * Zone.SIZE, target.getZoneZ() * Zone.SIZE, target.getAltitude()));
    put(zone);
}
```

The client then needs to showcase these changes. There are two types of packets that the client supports. These are called static rebuild and dynamic rebuild, respectively. The static rebuild packet is used when there are no dynamic changes (such as zones being copied over) to the map. This will tell the client to draw the regular map based on a given tile position. For most of the time, the static rebuild packet is used. 

```java
public static EventEncoder<BuildAreaStaticRebuild> encoder() {
    return (out, evt) -> {
        out.writeShort(evt.getCenterX());
        out.writeShort(evt.getCenterZ());

        out.writeShort(evt.getKeySets().size());
        for (MapSquareConfig.KeySet keySet : evt.getKeySets()) {
            for (int key : keySet) {
                out.writeInt(key);
            }
        }
    };
}
```

The dynamic rebuild packet on the other hand will draw the zones that have been copied over to our empty area.

```java
    public static EventEncoder<BuildAreaDynamicRebuild> encoder() {
        return (out, evt) -> {
            out
                    .writeBoolean(evt.requireImmediateRebuild())
                    .writeShort(evt.getCenterX())
                    .writeShort(evt.getCenterZ())
                    .writeShort(evt.getKeySets().size());

            bitBlock(BitAccessType.WRITE, out, bitIndex -> {
                for (int altitude = 0; altitude < evt.getPalette().getHeight(); altitude++) {
                    for (int localZoneX = 0; localZoneX < evt.getPalette().getWidth(); localZoneX++) {
                        for (int localZoneZ = 0; localZoneZ < evt.getPalette().getLength(); localZoneZ++) {
                            BuildAreaPalette.Zone zone = evt.getPalette().get(altitude, localZoneX, localZoneZ);

                            writeBit(out, bitIndex, zone != null);
                            if (zone != null) {
                                writeBits(out, bitIndex, 26, zone.getAltitude() << 24 |
                                        zone.getRotation() << 1 |
                                        zone.getOriginX() << 14 |
                                        zone.getOriginZ() << 3);
                            }
                        }
                    }
                }
            });

            for (MapSquareConfig.KeySet keySet : evt.getKeySets()) {
                for (int key : keySet) {
                    out.writeInt(key);
                }
            }
        };
    }
```