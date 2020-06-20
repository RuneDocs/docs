# Floor Items

Floor item updates should be sent [batched by zone](../zone/zone-batch.md).

## Dropping

Floor items have one of three states:
* Hidden aka private - Can only be seen and interacted by one player.
* Revealed aka public - Can be seen and taken by any players.
* Disappeared - Removed from view and unable to be picked back up; permanently lost. 

An item dropped always starts hidden, `reveal` and `disappear` count downs run independently of one another.
Not all items are revealed and not all items disappear.

| Type | Reveal time (ticks) | Disappear time (ticks) |
|---|---|---|
| Tradeable item dropped by player | 100 | 300 |
| Untradeable item dropped by player | - | 300 |
| Arrow shot or loot from npc | 100 | 200 |
| Item placed on table | 100 | 1000 |
| Item dropped in dungeoneering | 0 | - |

Floor items persist on previously visited planes but items on other planes are updated to be added or removed. 

### Stack rules

Stack rules apply to both floor items and table items.

When an item is dropped if a hidden stack already exists on the same tile then the two stacks are combined.
Dropped stacks don't combine with already revealed stacks, including in dungeoneering where stacks are revealed immediately.

Combining stacks resets the disappear count down but not the reveal time.

TODO test if rules apply if initial stack is dropped by an npc