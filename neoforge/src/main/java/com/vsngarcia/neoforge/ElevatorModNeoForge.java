package com.vsngarcia.neoforge;

import net.neoforged.fml.common.Mod;

import com.vsngarcia.ElevatorMod;

@Mod(ElevatorMod.ID)
public final class ElevatorModNeoForge {
    public ElevatorModNeoForge() {
        // Run our common setup.
        ElevatorMod.init();
    }
}
