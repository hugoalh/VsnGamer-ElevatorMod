package com.vsngarcia.fabric.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import static com.vsngarcia.fabric.FabricRegistry.ELEVATOR_CONTAINER;


public class ElevatorContainer extends AbstractContainerMenu {

    private final Direction playerFacing;
    private ElevatorTileEntity elevatorTile;
    private final BlockPos pos;

    public ElevatorContainer(int id, BlockPos pos, Player player) {
        super(ELEVATOR_CONTAINER, id);

        // TODO: 08/06/2023 Check if this is the correct way to get the level

        BlockEntity tile = player.level().getBlockEntity(pos);
        if (tile instanceof ElevatorTileEntity)
            elevatorTile = (ElevatorTileEntity) tile;

        playerFacing = player.getDirection();
        this.pos = pos;
    }

    @Override
    @NotNull
    public ItemStack quickMoveStack(@NotNull Player player, int id) {
        return ItemStack.EMPTY; // TODO: Handle this properly if in the future we start using items
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(ContainerLevelAccess.create(playerIn.level(), elevatorTile.getBlockPos()), playerIn, elevatorTile.getBlockState().getBlock());
    }

    public BlockPos getPos() {
        return pos;
    }

    public ElevatorTileEntity getTile() {
        return elevatorTile;
    }

    public Direction getPlayerFacing() {
        return playerFacing;
    }

    public record ElevatorContainerData(BlockPos pos) {
        public static final StreamCodec<RegistryFriendlyByteBuf, ElevatorContainerData> CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC,
                        ElevatorContainerData::pos,
                        ElevatorContainerData::new
                );
    }
}
