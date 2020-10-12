---
title: Queues
---

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

[weakqueue\*(smith_generic,3)](https://github.com/RuneStar/leaks/blob/master/205.0.png)

[clearqueue(fade_clear)](https://github.com/RuneStar/leaks/blob/master/242.spawns-runescript.png)

[soul-wars bandage heal queue](https://github.com/RuneStar/leaks/blob/master/319.SoulWars8.png)

## Ai queues

Are they related?

| Id    | Description  |
| ----- | ------------ |
| 1     | Retaliation  |
| 2     | Damage       |
| 3     | Bind effects |
| 4..20 | Custom       |

[ai_queue](https://twitter.com/Chrischis2/status/644620927519617024)

## Weak - cancelled by movement

- Make-X
  - Smithing
  - Crafting
  - Fletching
  - Herblore
  - Bones on altar
  - Runecrafting
  - Cooking
  - Fishing
  - Mining
  - Farming
  - Summoning infuse pouches
- Gathering
  - Woodcutting
  - Firemaking
- Normal emotes
- Sitting home teleport
- Movement - (interaction)
- Filling vials

## Normal - Waits for interfaces

- Level up (gfx, sound, dialogue)
- Poison - Not cancelled
- ~~XP drops - Not cancelled~~
- ~~Consuming food and potions - Not cancelled~~
- ~~Container modifications - Not cancelled~~
- Movement - (walk to tile, follow)
- Combat - Cancellable (attacking)
- ~~Most dialogues~~

## Strong - Closes interfaces

Cancels Weak, suspends normal

- Hits
- Death
- Teleports

## None

- Hitpoint renewal

## Interaction Movement

Can't be suspended if right next too - means there's a pre-movement check
Can be suspended if have to move to reach target - Movement isn't suspended like walk to tile
Only walk to tile can have movement-suspended on the first tick

## Food

Doesn't stop movement, stops action at end of movement

Pot + food doesn't work in the same tick, only food + pot

Kara + food takes normal delay
1 tick healing must be Food + Pot + Karambwan

## Bury bones

Stops movement, but movement is allowed to start after first tick (animations combine and still get xp later)

## Drop item & alching

Doesn't stop movement, does stop action at the end of movement

All actions cancels walking and combat, closes interfaces

- Emotes
- Bury bones
- Dropping items
- Using one item on another
- Using item on a npc

> Appears all player actions close interfaces

## Interfaces

### Interfaces that pause movement

- Skill guides
- ~~Dialogues~~ (Is it dialogues or is it leveling up itself?)

> Opening a skill guide will stall an action from performing e.g attacking, picking up an item until the interface is closed

### Interfaces that don't stop movement

- Quest guides
- Achievement guides
- Clan setup

### Interfaces that stop movement

- Settings
- Equipment/Price Checker/Items kept on death

# Questions

- Are delays handled before or as part of the queue?
- If all actions clear queues, what about poison?
- What queue priorities cancel what? - Cancels all priority queues below it, except poisons? see above
- Are interfaces part of the queue?

- How do actions which a player can't click out of work? (Obstacles, Ectofunctus) - Just queue 3 actions with the correct delays, unfreezing the player after
- How do animations play into this? (Teleporting, Emotes) - Not all emotes suspend hits, most suspend movement?
- Are dialogues part of the queue? - Yes definitely, level up suspends combat
- If something is added to the queue by a queue action, does it activate on the same tick or the next one? - Same, delay depending.
- When is a queue processed, is it each tick or more frequently?

# Notes

- Effects
  - Passive
    - Godwars snow
    - Monkey madness tunnel rock-fall
    - Barrows crypt crumble
  - Active
    - Spells (confuse, bind, etc..)
    - Prayers (leech, boost etc..)
    - Poison
- Granite maul special; click the spec bar twice, switch to a different weapon, hit, switch back to granite maul and it would auto spec
- Karambwan/pizzas/pies/chocolate bomb/tangled toad legs - doesn't clear queue, separate from regular food
- Potions and food are separate actions but on the same priority
- Equipping and 1-tick changing
- D-Spear (suspends hits)
- Level teleport interface stall - configure exp drops

first two foods are cooked at 3 ticks ea, ones after that cook at 4 ticks
most hits are determined upon added to damage queue
combat delay is that of the last weapon attacked with
