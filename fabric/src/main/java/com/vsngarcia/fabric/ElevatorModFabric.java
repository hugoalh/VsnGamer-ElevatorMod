package com.vsngarcia.fabric;

import com.vsngarcia.ElevatorMod;
import com.vsngarcia.fabric.network.NetworkHandler;
import net.fabricmc.api.ModInitializer;

public final class ElevatorModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        ElevatorMod.init();

        FabricRegistry.init();

        NetworkHandler.init();
    }
}
