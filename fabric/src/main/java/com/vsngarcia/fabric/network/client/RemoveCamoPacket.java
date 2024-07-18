package com.vsngarcia.fabric.network.client;

import com.vsngarcia.ElevatorMod;
import com.vsngarcia.fabric.network.NetworkHandler;
import com.vsngarcia.fabric.tile.ElevatorTileEntity;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


public record RemoveCamoPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<RemoveCamoPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ElevatorMod.ID, "remove_camo"));

    public static final StreamCodec<ByteBuf, RemoveCamoPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            RemoveCamoPacket::pos,
            RemoveCamoPacket::new
    );

    @Override
    public Type<RemoveCamoPacket> type() {
        return TYPE;
    }

    public static void handle(RemoveCamoPacket msg, ServerPlayNetworking.Context ctx) {
        ctx.server().execute(() -> {
            if (NetworkHandler.isBadClientPacket(ctx.player(), msg.pos)) return;

            if (ctx.player().level().getBlockEntity(msg.pos) instanceof ElevatorTileEntity tile) {
                tile.setCamoAndUpdate(null);
            }
        });
    }
}
