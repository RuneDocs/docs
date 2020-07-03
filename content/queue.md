# Queues

Most in game actions are queued.

The priority of a queued action is either weak, normal, strong

> A weakqueue is cancelled if the player clicks away - commonly used for Make-X. Queues are weak / strong / normal.
> https://github.com/RuneStar/leaks/blob/master/153.txt
>
> A standard one will wait if you have a menu open, and execute when it closes. A strong one will close menus to execute itself sooner.
> https://github.com/RuneStar/leaks/blob/master/142.txt


Both npcs and players have queues

Runescript queue has the format queue(type, tick rate)(parameters)

[npc_queue(npc_say,2)](https://github.com/RuneStar/leaks/blob/master/102.txt)

[combat_clearqueue & player_end_poison](https://github.com/RuneStar/leaks/blob/master/176.txt)

[weakqueue*(smith_generic,3)](https://github.com/RuneStar/leaks/blob/master/205.txt)

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
    
## Normal - Waits for interfaces

* Poison
* XP drops
* Movement
* Combat
* Consuming food and potions
* Teleports

## Strong
* Hits
* Death

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

> Opening a skill guide will stall an action from performing e.g attacking, picking up an item until the interface is closed
 
### Interfaces that don't stop movement

* Quest guides
* Achivement guides
* Clan setup

### Interfaces that stop movement

* Settings
* Equipment/Price Checker/Items kept on death

# Questions

* How do actions which a player can't click out of work? (Obstacles, Ectofunctus)
* How do animations play into this? (Teleporting, Emotes)
* Are dialogues part of the queue?
* Are interfaces part of the queue?
* When is a queue processed?
* If something is added to the queue by a queue action, does it activate on the same tick or the next one?
* When is the queue processed?
* What queue priorities cancel what?
* When is a queue processed in a tick?