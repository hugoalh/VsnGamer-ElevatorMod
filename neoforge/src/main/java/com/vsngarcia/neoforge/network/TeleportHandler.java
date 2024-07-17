package com.vsngarcia.neoforge.network;

import com.vsngarcia.neoforge.ElevatorBlock;
import com.vsngarcia.neoforge.init.Config;
import com.vsngarcia.neoforge.init.Registry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.EnumSet;


public class TeleportHandler {
    static void handle(final TeleportRequest message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (isBadTeleportPacket(message, player))
                return;

            // XP
            if (Config.GENERAL.useXP.get() && !player.isCreative()) {
                Integer xpCost = Config.GENERAL.XPPointsAmount.get();
                if (getPlayerExperienceProgress(player) - xpCost >= 0 || player.experienceLevel > 0) {
                    player.giveExperiencePoints(-xpCost);
                } else {
                    player.displayClientMessage(Component.translatable("elevatorid.message.missing_xp").withStyle(ChatFormatting.RED), true);
                    return;
                }
            }

            if (!(player.level() instanceof ServerLevel world))
                return;

            BlockPos toPos = message.to();
            BlockState toState = world.getBlockState(message.to());

            // Check yaw and pitch
            final float yaw = toState.getValue(ElevatorBlock.DIRECTIONAL)
                    ? toState.getValue(ElevatorBlock.FACING).toYRot() : player.getYRot();

            final float pitch = (toState.getValue(ElevatorBlock.DIRECTIONAL) && Config.GENERAL.resetPitchDirectional.get())
                    || (!toState.getValue(ElevatorBlock.DIRECTIONAL) && Config.GENERAL.resetPitchNormal.get())
                    ? 0F : player.getXRot();

            // Check X and Z
            final double toX, toZ;
            if (Config.GENERAL.precisionTarget.get()) {
                toX = toPos.getX() + .5D;
                toZ = toPos.getZ() + .5D;
            } else {
                toX = player.getX();
                toZ = player.getZ();
            }

            double blockYOffset = toState.getBlockSupportShape(world, toPos).max(Direction.Axis.Y);
            player.teleportTo(world, toX, Math.max(toPos.getY(), toPos.getY() + blockYOffset), toZ, EnumSet.noneOf(RelativeMovement.class), yaw, pitch);
            player.setDeltaMovement(player.getDeltaMovement().multiply(new Vec3(1D, 0D, 1D)));

            world.playSound(null, toPos, Registry.TELEPORT_SOUND.get(), SoundSource.BLOCKS, 1F, 1F);
        });
    }

    private static boolean isBadTeleportPacket(TeleportRequest msg, Player player) {
        if (player == null || !player.isAlive())
            return true;

        Level world = player.level();
        BlockPos fromPos = msg.from();
        BlockPos toPos = msg.to();

        if (!world.isLoaded(fromPos) || !world.isLoaded(toPos))
            return true;

        // This ensures the player is still standing on the "from" elevator
        final double distanceSq = player.distanceToSqr(Vec3.atCenterOf(fromPos));
        if (distanceSq > 6D)
            return true;

        if (fromPos.getX() != toPos.getX() || fromPos.getZ() != toPos.getZ())
            return true;

        if (fromPos.getY() == toPos.getY())
            return true;

        ElevatorBlock fromElevator = getElevator(world.getBlockState(fromPos));
        ElevatorBlock toElevator = getElevator(world.getBlockState(toPos));

        if (fromElevator == null || toElevator == null)
            return true;

        if (!isValidPos(world, toPos))
            return true;

        return Config.GENERAL.sameColor.get() && fromElevator.getColor() != toElevator.getColor();
    }

    private static int getPlayerExperienceProgress(Player player) {
        return Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
    }

    public static boolean isValidPos(BlockGetter world, BlockPos pos) {
        return !world.getBlockState(pos.above()).isSuffocating(world, pos);
    }

    public static ElevatorBlock getElevator(BlockState blockState) {
        if (blockState.getBlock() instanceof ElevatorBlock elevator)
            return elevator;
        return null;
    }
}
