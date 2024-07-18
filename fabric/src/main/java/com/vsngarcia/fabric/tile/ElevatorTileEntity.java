package com.vsngarcia.fabric.tile;

import com.vsngarcia.fabric.ElevatorBlock;
import com.vsngarcia.fabric.FabricRegistry;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static com.vsngarcia.fabric.FabricRegistry.CAMOUFLAGE_SOUND;


public class ElevatorTileEntity extends BlockEntity implements RenderDataBlockEntity, ExtendedScreenHandlerFactory<ElevatorContainer.ElevatorContainerData> {

    private BlockState heldState;

    public ElevatorTileEntity(BlockPos pos, BlockState state) {
        super(FabricRegistry.ELEVATOR_BLOCK_ENTITY_TYPE, pos, state);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider holder) {
        super.loadAdditional(tag, holder);
        if (tag.contains("held_id", Tag.TAG_COMPOUND)) {
            // Get blockstate from compound, always check if it's valid
            BlockState state = NbtUtils.readBlockState(
                    this.level != null ? this.level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup(),
                    tag.getCompound("held_id")
            );
            heldState = isValidState(state) ? state : null;
        } else {
            heldState = null;
        }

        if (level != null && level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), null, null, 0);
            level.getLightEngine().checkBlock(getBlockPos());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider holder) {
        super.saveAdditional(tag, holder);

        if (heldState != null) {
            tag.put("held_id", NbtUtils.writeBlockState(heldState));
        } else {
            tag.putBoolean("held_id", false);
        }
    }

    @Override
    public @Nullable Object getRenderData() {
        // TODO: Check if this is thread safe
        return heldState;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider holder) {
        return saveCustomOnly(holder);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

//    @Override
//    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider holder) {
//        handleUpdateTag(pkt.getTag(), holder);
//    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("screen.elevatorid.elevator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ElevatorContainer(id, worldPosition, player);
    }

    public void setHeldState(BlockState state) {
        this.heldState = state;
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
            level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
            level.getLightEngine().checkBlock(getBlockPos());
        }
    }

    public BlockState getHeldState() {
        return heldState;
    }

    public boolean setCamoAndUpdate(BlockState newState) {
        if (heldState == newState) return false;

        if (!isValidState(newState)) return false;

        setHeldState(newState);
        if (getLevel() != null) {
            getLevel().playSound(null, getBlockPos(), CAMOUFLAGE_SOUND, SoundSource.BLOCKS, 1F, 1F);
        }

        return true;
    }

    public static boolean isValidState(BlockState state) {
        if (state == null) return true;

        if (state.getBlock() == Blocks.AIR) return false;

        // Tile entities can cause problems
//        if (state.hasBlockEntity()) return false;

        // Don't try to camouflage with itself
        if (state.getBlock() instanceof ElevatorBlock) {
            return false;
        }

        // Only normally rendered blocks (not chests, ...)
        if (state.getRenderShape() != RenderShape.MODEL) {
            return false;
        }

        // Only blocks with a collision box
        return !state.getCollisionShape(null, null).isEmpty();
    }

    @Override
    public ElevatorContainer.ElevatorContainerData getScreenOpeningData(ServerPlayer player) {
        return new ElevatorContainer.ElevatorContainerData(getBlockPos());
    }
}
