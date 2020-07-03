# Queues

Most in game actions are queued.

The priority of a queued action is either weak, normal, strong

> A weakqueue is cancelled if the player clicks away - commonly used for Make-X. Queues are weak / strong / normal.
> https://github.com/RuneStar/leaks/blob/master/153.txt
>
> A standard one will wait if you have a menu open, and execute when it closes. A strong one will close menus to execute itself sooner.
> https://github.com/RuneStar/leaks/blob/master/142.txt


Both npcs and players have queues

Runescript queue has the format `queue(type, tick delay)(parameters)`

[npc_queue(npc_say,2)](https://github.com/RuneStar/leaks/blob/master/102.0.jpg)

[combat_clearqueue & player_end_poison](https://github.com/RuneStar/leaks/blob/master/176.0.jpg)

[weakqueue*(smith_generic,3)](https://github.com/RuneStar/leaks/blob/master/205.0.png)

[clearqueue(fade_clear)](https://github.com/RuneStar/leaks/blob/master/242.spawns-runescript.png)

[soul-wars bandage heal queue](https://github.com/RuneStar/leaks/blob/master/319.SoulWars8.png)

## Ai queues

Are they related?

| Id | Description |
|---|---|
| 1 | Retaliation |
| 2 | Damage |
| 3 | Bind effects |
| 4..20 | Custom |
[ai_queue](https://twitter.com/Chrischis2/status/644620927519617024)


## Weak - cancelled by movement

* Make-X
    * Smithing
    * Crafting
    * Fletching
    * Herblore
    * Bones on altar
    * Runecrafting
    * Woodcutting
    * Cooking
    * Fishing
    * Mining
    * Farming
    * Summoning infuse pouches
* Sitting home teleport
    
## Normal - Waits for interfaces

* Poison
* XP drops
* Movement
* Combat
* Consuming food and potions
* Container modifications

## Strong
* Hits
* Death
* Teleports

## None

* Hitpoint renewal


All actions cancels walking and combat, closes interfaces

* Emotes
* Bury bones
* Dropping items
* Using one item on another
* Using item on a npc

> Appears all player actions close interfaces

## Interfaces

### Interfaces that pause movement

* Skill guides
* Dialogues

> Opening a skill guide will stall an action from performing e.g attacking, picking up an item until the interface is closed
 
### Interfaces that don't stop movement

* Quest guides
* Achievement guides
* Clan setup

### Interfaces that stop movement

* Settings
* Equipment/Price Checker/Items kept on death

# Questions

* If all actions clear queues, what about poison?
* Are interfaces part of the queue?
* What queue priorities cancel what? - Cancels all priority queues below it, except poisons?

* How do actions which a player can't click out of work? (Obstacles, Ectofunctus) - Just queue 3 actions with the correct delays, unfreezing the player after
* How do animations play into this? (Teleporting, Emotes) - Not all emotes suspend hits, most suspend movement?
* Are dialogues part of the queue? - Yes definitely, level up suspends combat
* If something is added to the queue by a queue action, does it activate on the same tick or the next one? - Same, delay depending.
* When is a queue processed, is it each tick or more frequently?

# Notes

* Effects
    * Passive
        * Godwars snow
        * Monkey madness tunnel rock-fall
        * Barrows crypt crumble
    * Active
        * Spells (confuse, bind, etc..)
        * Prayers (drain, boost etc..)
        * Poison
* Granite maul special; click the spec bar twice, switch to a different weapon, hit, switch back to granite maul and it would auto spec
* Karambwan - doesn't clear queue, separate from regular food
* Potions and food are separate actions but on the same priority
* Equipping and 1-tick changing
* D-Spear (suspends hits)
* Level teleport interface stall - configure exp drops