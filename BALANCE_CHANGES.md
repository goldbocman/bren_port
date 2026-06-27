# Balance Changes & Disabled Content

This document tracks everything that was removed, disabled, or rebalanced relative to the upstream `Bren_Bin` (26.1) source.

---

## Disabled Weapons

These guns exist in code but are not registered and cannot appear in-game.
Re-enable by uncommenting the relevant block in `ItemReg.java` and restoring the recipe JSON.

| Item | Reason disabled |
|------|----------------|
| `machine_gun` | Fire rate and ammo economy not tuned |
| `netherite_machine_gun` | Follows machine_gun |
| `smg` | Fire rate not differentiated from auto_gun |
| `netherite_tactical_auto_gun` | Unique role not defined |
| `air_gun` | Special ammo type not implemented |
| `netherite_lever_gun` | Ammo type not assigned |
| `netherite_double_barrels_shotgun` | Burst mechanics not tuned |
| `netherite_big_bore_revolver` | High-damage tier not balanced |
| `flare_gun` | Requires special ammo not yet implemented |
| `auto_pistol` | Pistol tier not designed |

---

## Disabled Ammo

| Item | Reason disabled |
|------|----------------|
| `dragonbreath_shell` | Special ammo type not tuned |

---

## Disabled Magazines

| Item | Reason disabled |
|------|----------------|
| `drum_magazine` (120 rounds) | High-capacity variant not balanced |
| `clothed_magazine` (50 rounds, colorable) | Not balanced |

---

## Disabled Melee / Utility

| Item | Reason disabled |
|------|----------------|
| `fire_axe` | Melee tier not designed |
| `grappling_hook` | Utility mechanics not implemented |
| `lunge_mine` (explosive_spear) | Throwable mechanics not tuned |

---

## Disabled World Features

| Feature | How disabled |
|---------|-------------|
| Gun-trader villager structures (`gunpowder_town`, `abandoned_factory`) | Structure set JSON files moved to `worldgen/structure_set/disabled/` |
| Workbench block | Block defined but never registered |

---

## Active Weapons — Balance Values

Values applied from the legacy `Bren` (1.20.1) project.

### Auto Gun
- Damage: `5.5` (netherite: `6.0`)
- Recoil: `12`
- Fire rate: `5` ticks between shots
- Ammo: Magazine (20 rounds)

### Rifle
- Damage: `10.0` (netherite: `11.0`)
- Recoil: `22`
- Fire rate: `20` ticks between shots
- Ammo: Magazine (20 rounds)

### Revolver
- Damage: `8.0` (netherite: `9.0`)
- Recoil: `15`
- Fire rate: `15` ticks between shots
- Ammo: Bullet (6 capacity, loaded one by one)

### Shotgun
- Damage per pellet: `3.5` (netherite: `3.75`)
- Recoil: `25`
- Fire rate: `20` ticks between shots
- Ammo: Shell (8 capacity, loaded one by one)
- Pellets per shot: `5`

### Recoil multiplier (global): `1.0`

---

## Magazine Capacities

| Item | Capacity |
|------|----------|
| `magazine` | 20 rounds |
| `short_magazine` | 6 rounds |

---

## Changes vs Upstream Bren_Bin

| Change | Details |
|--------|---------|
| All damage values | Reverted to legacy (Bren 1.20.1) defaults |
| All recoil values | Reverted to legacy defaults |
| Fire rates | Reverted to legacy defaults |
| Magazine capacities | `magazine` 30→20, `short_magazine` 15→6 |
| Crouch recoil reduction | Added: 50% recoil when crouching (was not present) |
| Ammo type tooltip | Added: each gun shows what ammo it uses on hover |
| HUD max ammo display | Fixed: guns now show correct max ammo (e.g. `0/20`) without needing a magazine pre-loaded |
| Recipe auto-unlock | Recipes unlock automatically when a required ingredient is picked up |
| Shotgun null crash | Fixed: `DRAGONBREATH_SHELL` null reference in `ShotgunItem.reloadTick()` and `compatibleBullet()` |
