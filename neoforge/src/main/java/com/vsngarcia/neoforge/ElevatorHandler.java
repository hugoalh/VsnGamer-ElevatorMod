package com.vsngarcia.neoforge;

import com.vsngarcia.ElevatorMod;
import com.vsngarcia.Config;
import com.vsngarcia.neoforge.network.TeleportHandler;
import com.vsngarcia.neoforge.network.TeleportRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;


@EventBusSubscriber(value = Dist.CLIENT, modid = ElevatorMod.ID)
public class ElevatorHandler {
    private static boolean lastSneaking;
    private static boolean lastJumping;

    @SubscribeEvent
    public static void onInput(InputEvent.Key event) {
        handleInput();
    }

    @SubscribeEvent
    private static void onMouseInput(InputEvent.MouseButton.Post event) {
        handleInput();
    }

    private static void handleInput() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator() || !player.isAlive() || player.input == null)
            return;

        boolean sneaking = player.input.shiftKeyDown;
        if (lastSneaking != sneaking) {
            lastSneaking = sneaking;
            if (sneaking)
                tryTeleport(player, Direction.DOWN);
        }

        boolean jumping = player.input.jumping;
        if (lastJumping != jumping) {
            lastJumping = jumping;
            if (jumping)
                tryTeleport(player, Direction.UP);
        }
    }

    private static void tryTeleport(LocalPlayer player, Direction facing) {
        Level world = player.level();

        BlockPos fromPos = getOriginElevator(player);
        if (fromPos == null) return;

        BlockPos.MutableBlockPos toPos = fromPos.mutable();

        ElevatorBlock fromElevator;
        fromElevator = (ElevatorBlock) world.getBlockState(fromPos).getBlock();

        while (true) {
            toPos.setY(toPos.getY() + facing.getStepY());
            if (world.isOutsideBuildHeight(toPos) || Math.abs(toPos.getY() - fromPos.getY()) > Config.GENERAL.range.get())
                break;

            ElevatorBlock toElevator = TeleportHandler.getElevator(world.getBlockState(toPos));
            if (toElevator != null && TeleportHandler.isValidPos(world, toPos)) {
                if (!Config.GENERAL.sameColor.get() || fromElevator.getColor() == toElevator.getColor()) {
                    PacketDistributor.sendToServer(new TeleportRequest(fromPos, toPos));
                    break;
                }
            }
        }
    }

    /**
     * Checks if a player(lower part) is in or has an elevator up to 2 blocks below
     *
     * @param player the player trying to teleport
     * @return the position of the first valid elevator or null if it doesn't exist
     */
    private static BlockPos getOriginElevator(LocalPlayer player) {
        Level world = player.level();
        BlockPos pos = player.blockPosition();

        // Check the player's feet and the 2 blocks under it
        for (int i = 0; i < 3; i++) {
            if (TeleportHandler.getElevator(world.getBlockState(pos)) != null)
                return pos;
            pos = pos.below();
        }

        // Elevator doesn't exist or it's invalid
        return null;
    }
}
