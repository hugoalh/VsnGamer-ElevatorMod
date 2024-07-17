package com.vsngarcia.neoforge;

import com.vsngarcia.neoforge.init.Config;
import com.vsngarcia.neoforge.init.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import com.vsngarcia.ElevatorMod;
import net.neoforged.fml.config.ModConfig;

@Mod(ElevatorMod.ID)
public final class ElevatorModNeoForge {
    public ElevatorModNeoForge(IEventBus eventBus, ModContainer container) {
        // Run our common setup.
        ElevatorMod.init();

        Registry.init(eventBus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }
}
