package com.vsngarcia.fabric;

import com.vsngarcia.fabric.network.TeleportHandler;
import com.vsngarcia.fabric.network.TeleportRequest;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;


public class ElevatorHandler {
    private static boolean lastSneaking;
    private static boolean lastJumping;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(
                client -> handleInput()
        );
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

//        ElevatorBlock fromElevator = (ElevatorBlock) world.getBlockState(fromPos).getBlock();

        while (true) {
            toPos.setY(toPos.getY() + facing.getStepY());
            if (world.isOutsideBuildHeight(toPos))
                break;

            ElevatorBlock toElevator = TeleportHandler.getElevator(world.getBlockState(toPos));
            if (toElevator != null && TeleportHandler.isValidPos(world, toPos)) {
//                if (!Config.GENERAL.sameColor.get() || fromElevator.getColor() == toElevator.getColor()) {
                ClientPlayNetworking.send(new TeleportRequest(fromPos, toPos));
                break;
//                }
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
        BlockPos pos = player.blockPosition();

        // Check the player's feet and the 2 blocks under it
        for (int i = 0; i < 3; i++) {
            if (TeleportHandler.getElevator(player.level().getBlockState(pos)) != null)
                return pos;
            pos = pos.below();
        }

        // Elevator doesn't exist or it's invalid
        return null;
    }
}
