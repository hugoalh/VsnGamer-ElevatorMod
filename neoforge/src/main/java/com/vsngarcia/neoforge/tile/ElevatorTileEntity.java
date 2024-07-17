package com.vsngarcia.neoforge.tile;

import com.vsngarcia.neoforge.ElevatorBlock;
import com.vsngarcia.neoforge.client.render.ElevatorBakedModel;
import com.vsngarcia.neoforge.init.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.Optional;


public class ElevatorTileEntity extends BlockEntity implements MenuProvider {

    private BlockState heldState;

    public ElevatorTileEntity(BlockPos pos, BlockState state) {
        super(Registry.ELEVATOR_TILE_ENTITY.get(), pos, state);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider holder) {
        super.loadAdditional(tag, holder);
        if (tag.contains("held_id")) {
            // Get blockstate from compound, always check if it's valid
            BlockState state = NbtUtils.readBlockState(this.level != null ? this.level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("held_id"));
            heldState = isValidState(state) ? state : null;
        } else {
            heldState = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider holder) {
        super.saveAdditional(tag, holder);

        if (heldState != null) tag.put("held_id", NbtUtils.writeBlockState(heldState));
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(ElevatorBakedModel.HELD_STATE, heldState).build();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider holder) {
        return saveWithId(holder);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider holder) {
        super.handleUpdateTag(tag, holder);
        markUpdated();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider holder) {
        handleUpdateTag(pkt.getTag(), holder);
    }

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
        markUpdated();
    }

    private void markUpdated() {
        setChanged();
        requestModelDataUpdate();

        if (level == null) {
            return;
        }

        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);

        level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        getBlockState().updateNeighbourShapes(level, worldPosition, 2);

        Optional.ofNullable(level.getAuxLightManager(worldPosition))
                .ifPresent(lm -> lm.setLightAt(
                        worldPosition,
                        Optional.ofNullable(this.heldState)
                                .map(b -> b.getLightEmission(level, worldPosition))
                                .orElse(0)
                ));

        level.getLightEngine().checkBlock(worldPosition);
    }

    public BlockState getHeldState() {
        return heldState;
    }

    public boolean setCamoAndUpdate(BlockState newState) {
        if (heldState == newState) return false;

        if (!isValidState(newState)) return false;

        setHeldState(newState);
        if (getLevel() != null)
            getLevel().playSound(null, getBlockPos(), Registry.CAMOUFLAGE_SOUND.get(), SoundSource.BLOCKS, 1F, 1F);

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
}
