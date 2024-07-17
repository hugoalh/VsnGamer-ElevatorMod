package com.vsngarcia.fabric;

import com.vsngarcia.fabric.network.TeleportHandler;
import com.vsngarcia.fabric.network.TeleportRequest;
import net.fabricmc.api.ModInitializer;

import com.vsngarcia.ElevatorMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ElevatorModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        ElevatorMod.init();

        FabricRegistry.init();

        PayloadTypeRegistry.playC2S().register(TeleportRequest.TYPE, TeleportRequest.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TeleportRequest.TYPE, TeleportHandler::handle);
    }
}
