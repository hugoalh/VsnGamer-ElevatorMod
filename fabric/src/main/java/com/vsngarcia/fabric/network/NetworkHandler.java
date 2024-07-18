package com.vsngarcia.fabric.network;

import com.vsngarcia.fabric.network.client.RemoveCamoPacket;
import com.vsngarcia.fabric.network.client.SetArrowPacket;
import com.vsngarcia.fabric.network.client.SetDirectionalPacket;
import com.vsngarcia.fabric.network.client.SetFacingPacket;
import com.vsngarcia.fabric.tile.ElevatorContainer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;


public class NetworkHandler {


    public static void init() {
        PayloadTypeRegistry.playC2S().register(TeleportRequest.TYPE, TeleportRequest.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TeleportRequest.TYPE, TeleportHandler::handle);

        PayloadTypeRegistry.playC2S().register(SetDirectionalPacket.TYPE, SetDirectionalPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SetDirectionalPacket.TYPE, SetDirectionalPacket::handle);

        PayloadTypeRegistry.playC2S().register(SetArrowPacket.TYPE, SetArrowPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SetArrowPacket.TYPE, SetArrowPacket::handle);

        PayloadTypeRegistry.playC2S().register(RemoveCamoPacket.TYPE, RemoveCamoPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RemoveCamoPacket.TYPE, RemoveCamoPacket::handle);

        PayloadTypeRegistry.playC2S().register(SetFacingPacket.TYPE, SetFacingPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SetFacingPacket.TYPE, SetFacingPacket::handle);
    }

    public static boolean isBadClientPacket(Player player, BlockPos pos) {
        if (player == null || player.isDeadOrDying() || player.isRemoved())
            return true;

        Level world = player.level();
        if (!world.isLoaded(pos))
            return true;

        if (!(player.containerMenu instanceof ElevatorContainer container))
            return true;

        return !container.getPos().equals(pos);
    }
}
