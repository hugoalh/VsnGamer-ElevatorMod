package com.vsngarcia.fabric;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vsngarcia.ElevatorBlockBase;
import com.vsngarcia.fabric.tile.ElevatorBlockEntity;
import com.vsngarcia.level.ElevatorBlockEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ElevatorBlock extends ElevatorBlockBase {
    private final MapCodec<ElevatorBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(DyeColor.CODEC.fieldOf("color").forGetter(ElevatorBlockBase::getColor))
            .apply(instance, ElevatorBlock::new)
    );

    public ElevatorBlock(DyeColor color) {
        super(color, () -> FabricRegistry.ELEVATOR_BLOCK_ENTITY_TYPE);
    }

    @Override
    protected void openMenu(Player player, ElevatorBlockEntityBase tile, BlockPos pos) {
        player.openMenu(tile);
    }

    @Override
    protected BlockState getAppearance(BlockState facingState, LevelReader worldIn, BlockPos facingPos, Direction opposite, BlockState heldState, BlockPos currentPos) {
        return facingState.getAppearance(worldIn, facingPos, opposite, heldState, currentPos);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ElevatorBlockEntity(blockPos, blockState);
    }

    @Override
    public BlockState getAppearance(BlockState state, BlockAndTintGetter renderView, BlockPos pos, Direction side, @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        if (renderView instanceof ServerLevel) {
            return getHeldState(renderView, pos)
                    .map(s -> s.getAppearance(renderView, pos, side, sourceState, sourcePos))
                    .orElse(state);
        }

        if (renderView.getBlockEntityRenderData(pos) instanceof BlockState heldState) {
            return heldState;
        }

        return state;
    }
}
