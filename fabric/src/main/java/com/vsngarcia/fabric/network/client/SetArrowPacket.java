package com.vsngarcia.fabric.network.client;

import com.vsngarcia.ElevatorMod;
import com.vsngarcia.fabric.ElevatorBlock;
import com.vsngarcia.fabric.network.NetworkHandler;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;


public record SetArrowPacket(boolean value, BlockPos pos) implements CustomPacketPayload {
    public static final Type<SetArrowPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ElevatorMod.ID, "set_arrow"));

    public static final StreamCodec<ByteBuf, SetArrowPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SetArrowPacket::value,
            BlockPos.STREAM_CODEC,
            SetArrowPacket::pos,
            SetArrowPacket::new
    );

    @Override
    public Type<SetArrowPacket> type() {
        return TYPE;
    }

    public static void handle(SetArrowPacket msg, ServerPlayNetworking.Context ctx) {
        ctx.server().execute(() -> {
            if (NetworkHandler.isBadClientPacket(ctx.player(), msg.pos))
                return;

            Level world = ctx.player().level();
            BlockState state = world.getBlockState(msg.pos);
            if (state.getBlock() instanceof ElevatorBlock) {
                world.setBlockAndUpdate(msg.pos, state.setValue(ElevatorBlock.SHOW_ARROW, msg.value));
            }
        });
    }
}
