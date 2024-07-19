package com.vsngarcia.neoforge;

import com.vsngarcia.Config;
import com.vsngarcia.ElevatorMod;
import com.vsngarcia.neoforge.init.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

@Mod(ElevatorMod.ID)
public final class ElevatorModNeoForge {
    public ElevatorModNeoForge(IEventBus eventBus, ModContainer container) {
        ElevatorMod.init();
        Registry.init(eventBus);

        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        eventBus.addListener((ModConfigEvent.Reloading event) -> ElevatorMod.LOGGER.info("Config reloaded"));
    }
}
