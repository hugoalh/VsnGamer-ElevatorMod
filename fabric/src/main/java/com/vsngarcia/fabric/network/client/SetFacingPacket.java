package com.vsngarcia.fabric.network.client;

import com.vsngarcia.ElevatorMod;
import com.vsngarcia.fabric.ElevatorBlock;
import com.vsngarcia.fabric.network.NetworkHandler;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;


public record SetFacingPacket(Direction direction, BlockPos pos) implements CustomPacketPayload {
    public static final Type<SetFacingPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ElevatorMod.ID, "set_facing"));

    public static final StreamCodec<ByteBuf, SetFacingPacket> STREAM_CODEC = StreamCodec.composite(
            Direction.STREAM_CODEC,
            SetFacingPacket::direction,
            BlockPos.STREAM_CODEC,
            SetFacingPacket::pos,
            SetFacingPacket::new
    );

    @Override
    public Type<SetFacingPacket> type() {
        return TYPE;
    }

    public static void handle(SetFacingPacket msg, ServerPlayNetworking.Context ctx) {
        ctx.server().execute(() -> {
            if (NetworkHandler.isBadClientPacket(ctx.player(), msg.pos))
                return;

            Level world = ctx.player().level();
            BlockState state = world.getBlockState(msg.pos);
            if (state.getBlock() instanceof ElevatorBlock) {
                world.setBlockAndUpdate(msg.pos, state.setValue(ElevatorBlock.FACING, msg.direction));
            }
        });
    }
}
