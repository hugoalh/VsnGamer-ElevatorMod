# ElevatorMod

<a href="https://github.com/VsnGamer/ElevatorMod"><img alt="GitHub" height="32" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact-minimal/available/github_vector.svg"></a>
<a href="https://curseforge.com/minecraft/mc-mods/openblocks-elevator" target="_blank"><img alt="CurseForge" height="32" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/curseforge_vector.svg"></a>
<a href="https://modrinth.com/mod/elevatormod" target="_blank"><img alt="Modrinth" height="32" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/modrinth_vector.svg"></a>

Port of the elevator from OpenBlocks to Minecraft 1.8+

## Dependencies

**Forge / NeoForge**

- None

**Fabric**

- [Fabric API](https://github.com/FabricMC/fabric) - Mandatory
- [Forge Config API Port](https://github.com/Fuzss/forgeconfigapiport) - Embedded

## Crafting

The default recipe is the same as in OpenBlocks.

Example of the white elevator recipe:

![Elevator recipe](https://github.com/VsnGamer/ElevatorMod/blob/1.21-architectury/docs/crafting_base.png?raw=true)

You can also re-dye any elevator:

![Dyeing](https://github.com/VsnGamer/ElevatorMod/blob/1.21-architectury/docs/crafting_redye.png?raw=true)

## Usage

To use them, place two or more elevators in the same X and Z coordinates, to move between them simply jump to go to the
elevator above or sneak to go to the one below.

Elevators need at least 1 block of air (or non-solid block) above to be considered valid.

You can configure the range of the elevators and if they should require XP to teleport in
the [configuration file](#configuration).

### Options Interface

You can access the options interface by right-clicking on the elevator with an empty hand.

![Options](https://github.com/VsnGamer/ElevatorMod/blob/1.21-architectury/docs/elevator_options.png?raw=true)

### Directional

When the `Directional` checkbox is enabled, the elevator will adjust the yaw rotation of players who teleport to it
based on the configured direction.

You can change the direction by clicking on the desired direction button. _The display order of the buttons will match
the current direction the player is facing._

It will also show an arrow on the elevator to indicate the direction it is facing. You can hide it by selecting
the `Hide arrow` checkbox.

**This has nothing to do with horizontal movement, it only changes the direction the player is facing.**

### Camouflage

You can use any solid block to camouflage the elevator, just right-click on the elevator with the desired block on your
hand.

Most blocks can be used as camouflage with some exceptions. For example, you can't use a chest because it uses a
different rendering method.

**Note for _Fabric_ users:** If you are using Sodium, you need to install Lithium to make the camouflage
work[^lithium].

[^lithium]: Since Sodium `0.6.0-beta.1` FRAPI is supported out of the box, so it should work **without** Lithium.

## Configuration

The configuration file is per-world and is located at `<world-directory>/serverconfig/elevatorid-server.toml`.

**Note for _Fabric_ users:** By default it is located at `.minecraft/config/elevatorid-server.toml` but per-world
overrides are possible,
see [FCAP Wiki](https://github.com/Fuzss/forgeconfigapiport/wiki/Understanding-ModConfig-Types#server-config-type)

<details>
<summary>Here are the default values:</summary>

```toml
[General]
#Should elevators have the same color in order to teleport ?
sameColor = false

#Elevator range
#Range: 3 ~ 4064
range = 384

#Realign players to the center of elevator ?
precisionTarget = true

#Can mobs spawn on elevators ?
mobSpawn = false

#Reset pitch to 0 when teleporting to normal elevators ?
resetPitchNormal = false

#Reset pitch to 0 when teleporting to directional elevators ?
resetPitchDirectional = true

#Should teleporting require XP ?
useXP = false

#Amount of XP points to use when useXP is enabled
#Note this is NOT experience levels
#Range: > 1
XPPointsAmount = 1
```

</details>

## Thanks

- OpenBlocks Team
- All contributors on GitHub

## Modpacks

Feel free to use this in any modpack.
