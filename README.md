# Bren — Vanilla Gun Mod (Fabric 26.2)

A Minecraft Fabric mod that adds four guns and their supporting crafting items, designed to feel at home in vanilla survival.

This is a port of **Bren** (1.20.1) and **Bren_Bin** (26.1), with all world-generation extras stripped out — no gun-trader villagers, no loot-table injections, no structures, no special spawns. Just guns you craft and use.

---

## Guns

| Gun | Ammo | Capacity | Notes |
|-----|------|----------|-------|
| Auto Gun | Magazine | 20 | Full-auto, low damage per shot |
| Rifle | Magazine | 20 | Semi-auto, high damage |
| Revolver | Bullet | 6 | Loaded one shell at a time |
| Shotgun | Shell | 8 | 5 pellets per shot, loaded one shell at a time |

All four guns have a **Netherite upgrade** via the smithing table.

Crouch while firing to reduce recoil by 50%.

---

## Ammo & Crafting Components

| Item | Use |
|------|-----|
| Bullet | Revolver ammo |
| Shell | Shotgun ammo |
| Magazine | Auto Gun / Rifle ammo (20 rounds) |
| Short Magazine | *(reserved for future weapons, 6 rounds)* |
| Metal Tube | Intermediate crafting component for all guns |
| Auto Loader Contraption | Intermediate crafting component for all guns |

---

## Recipe Unlock

Recipes unlock automatically when you pick up a required ingredient — no `/recipe give` or advancements needed beyond normal play.

- Pick up **Iron Ingot** → unlocks `metal_tube` and `auto_loader_contraption`
- Pick up **Auto Loader Contraption** → unlocks all four base guns
- Pick up **Netherite Ingot** → unlocks all four netherite upgrades

---

## What Was Removed vs Upstream

- Gun-trader villager structures (`gunpowder_town`, `abandoned_factory`)
- Machine gun, SMG, air gun, lever gun, double-barrel shotgun, big bore revolver, flare gun, auto pistol
- Dragon breath shell, drum magazine, clothed magazine
- Melee weapons (fire axe) and utility items (grappling hook, lunge mine)
- Workbench block

See [BALANCE_CHANGES.md](BALANCE_CHANGES.md) for the full list with reasons.

---

## Credits

Original mod: [Bren by Sniffiandros](https://modrinth.com/mod/bren)
