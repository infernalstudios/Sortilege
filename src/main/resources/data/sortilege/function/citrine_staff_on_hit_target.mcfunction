effect give @s[type=#minecraft:undead] minecraft:weakness 5 0 false
execute if entity @s[nbt={Health:0.0f}] run summon minecraft:experience_orb ~ ~ ~ {Value:5}