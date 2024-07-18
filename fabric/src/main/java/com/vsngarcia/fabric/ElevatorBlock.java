package com.vsngarcia.fabric;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vsngarcia.fabric.tile.ElevatorTileEntity;
import com.vsngarcia.util.FakeUseContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

import static com.vsngarcia.fabric.FabricRegistry.ELEVATOR_BLOCK_ENTITY_TYPE;


public class ElevatorBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty DIRECTIONAL = BooleanProperty.create("directional");
    public static final BooleanProperty SHOW_ARROW = BooleanProperty.create("show_arrow");

    private static final MapCodec<ElevatorBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(DyeColor.CODEC.fieldOf("color").forGetter(ElevatorBlock::getColor))
            .apply(instance, ElevatorBlock::new)
    );

    private final DyeColor dyeColor;

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    public ElevatorBlock(DyeColor color) {
        super(Properties
                        .of()
                        .mapColor(color)
                        .sound(SoundType.WOOL)
                        .strength(0.8F)
                        .dynamicShape()
                        .noOcclusion()

//                        .isValidSpawn(ElevatorBlock::isValidSpawn)
//                .forceSolidOn()
        );


        registerDefaultState(
                defaultBlockState()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(DIRECTIONAL, false)
                        .setValue(SHOW_ARROW, true)
        );

        dyeColor = color;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DIRECTIONAL, SHOW_ARROW);
    }

//    @Override
//    public BlockState getStateForPlacement(BlockPlaceContext context) {
//        return defaultBlockState()
//                .setValue(FACING, context.getHorizontalDirection().getOpposite())
//                .setValue(DIRECTIONAL, false)
//                .setValue(SHOW_ARROW, true);
//    }


    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElevatorTileEntity(pos, state);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return false;
    }


    @Override
    public ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        return getElevatorTile(worldIn, pos)
                .map(tile -> {
                    Block handBlock = Block.byItem(player.getItemInHand(handIn).getItem());
                    BlockState stateToApply = handBlock.getStateForPlacement(new FakeUseContext(player, handIn, hit));
                    if (stateToApply != null && tile.setCamoAndUpdate(stateToApply))
                        return ItemInteractionResult.SUCCESS; // If we successfully set camo, don't open the menu

                    // Remove camo
                    if (player.isCrouching() && tile.getHeldState() != null) {
                        tile.setCamoAndUpdate(null);
                        return ItemInteractionResult.SUCCESS;
                    }

                    player.openMenu(tile);
                    return ItemInteractionResult.SUCCESS;
                })
                .orElse(ItemInteractionResult.FAIL);
    }

//    public static boolean isValidSpawn(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> entityType) {
//        return Config.GENERAL.mobSpawn.get() &&
//                getHeldState(world, pos)
//                        .map(s -> s.isValidSpawn(world, pos, entityType))
//                        .orElse(state.isFaceSturdy(world, pos, Direction.UP));
//    }

    // Collision

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return getHeldState(worldIn, pos)
                .map(s -> s.getCollisionShape(worldIn, pos, context))
                .orElse(super.getCollisionShape(state, worldIn, pos, context));
    }

//    @Override
//    public boolean collisionExtendsVertically(BlockState state, BlockGetter world, BlockPos pos, Entity collidingEntity) {
//        return getHeldState(world, pos)
//                .map(s -> s.collisionExtendsVertically(world, pos, collidingEntity))
//                .orElse(super.collisionExtendsVertically(state, world, pos, collidingEntity));
//    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return getHeldState(world, pos)
                .map(s -> s.isCollisionShapeFullBlock(world, pos))
                .orElse(super.isCollisionShapeFullBlock(state, world, pos));
    }

    // Visual outline
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return getHeldState(worldIn, pos)
                .map(s -> s.getShape(worldIn, pos, context))
                .orElse(super.getShape(state, worldIn, pos, context));
    }

//    @Override
//    public float getFriction(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
//        return getHeldState(world, pos)
//                .map(s -> s.getFriction(world, pos, entity))
//                .orElse(super.getFriction(state, world, pos, entity));
//    }

    // TODO soulsand and honey use this
    @Override
    public float getSpeedFactor() {
        return super.getSpeedFactor();
    }

    // TODO honey uses this
    @Override
    public float getJumpFactor() {
        return super.getJumpFactor();
    }


    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!worldIn.isClientSide()) {
            getElevatorTile(worldIn, currentPos)
                    .ifPresent(t -> {
                        if (t.getHeldState() == null) {
                            return;
                        }

                        BlockState appearance = facingState.getAppearance(worldIn, facingPos, facing.getOpposite(), t.getHeldState(), currentPos);
                        BlockState updatedState = t.getHeldState().updateShape(facing, appearance, worldIn, currentPos, facingPos);
                        if (updatedState != t.getHeldState()) {
                            t.setHeldState(updatedState);
                        }
                    });
        }

        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    // Redstone
    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

//    @Override
//    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
//        return getHeldState(world, pos)
//                .map(s -> s.getBlock().canConnectRedstone(s, world, pos, side))
//                .orElse(false);
//    }

//    @Override
//    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
//        return getHeldState(level, pos)
//                .map(s -> s.shouldCheckWeakPower(level, pos, side))
//                .orElse(super.shouldCheckWeakPower(state, level, pos, side));
//    }

    @Override
    public int getSignal(BlockState state, BlockGetter reader, BlockPos pos, Direction direction) {
        return getHeldState(reader, pos)
                .map(s -> s.getSignal(reader, pos, direction))
                .orElse(super.getSignal(state, reader, pos, direction));
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter reader, BlockPos pos, Direction direction) {
        return getHeldState(reader, pos)
                .map(s -> s.getDirectSignal(reader, pos, direction))
                .orElse(super.getDirectSignal(state, reader, pos, direction));
    }

    // Light
//    @Override
//    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
//        return Optional.ofNullable(world.getAuxLightManager(pos)).map(lm -> lm.getLightAt(pos)).orElse(0);
//    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
//        return getHeldState(reader, pos)
//                .map(s -> s.propagatesSkylightDown(reader, pos))
//                .orElse(false);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        if (worldIn instanceof ServerLevel) {
            return getHeldState(worldIn, pos)
                    .map(s -> s.getLightBlock(worldIn, pos))
                    .orElse(worldIn.getMaxLightLevel());
        }

        if (worldIn.getBlockEntityRenderData(pos) instanceof BlockState heldState) {
            return heldState.getLightBlock(worldIn, pos);
        }

        return worldIn.getMaxLightLevel();
    }

//    @Override
//    public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
//        return getHeldState(world, pos)
//                .map(s -> s.getOcclusionShape(world, pos))
//                .orElse(super.getOcclusionShape(state, world, pos));
//    }


//    @Override
//    public boolean skipRendering(BlockState state, BlockState otherState, Direction side) {
//        return super.skipRendering(state, otherState, side);
//    }

//    @Override
//    public boolean supportsExternalFaceHiding(BlockState state) {
//        return true;
//    }

//    @Override
//    public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
//        var modelData = level.getModelData(pos);
//        if (modelData == ModelData.EMPTY) {
//            return super.hidesNeighborFace(level, pos, state, neighborState, dir);
//        }
//
//        var heldState = modelData.get(ElevatorBakedModel.HELD_STATE);
//        if (heldState == null) {
//            return super.hidesNeighborFace(level, pos, state, neighborState, dir);
//        }
//
//        var neighborModelData = level.getModelData(pos.relative(dir));
//        if (neighborModelData == ModelData.EMPTY) {
//            return heldState.skipRendering(neighborState, dir);
//        }
//
//        var neighborHeldState = neighborModelData.get(ElevatorBakedModel.HELD_STATE);
//        return heldState.skipRendering(Objects.requireNonNullElse(neighborHeldState, neighborState), dir);
//    }

    @Override
    public BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side, BlockState queryState, BlockPos queryPos) {
        if (level instanceof ServerLevel) {
            return getHeldState(level, pos)
                    .map(s -> s.getAppearance(level, pos, side, queryState, queryPos))
                    .orElse(state);
        }

        if (level.getBlockEntityRenderData(pos) instanceof BlockState heldState) {
            return heldState;
        }

        return state;
    }

//    @Override
//    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
//        return getHeldState(world, pos)
//                .map(s -> s.getSoundType(world, pos, entity))
//                .orElse(super.getSoundType(state, world, pos, entity));
//    }

    public DyeColor getColor() {
        return dyeColor;
    }

    private static Optional<ElevatorTileEntity> getElevatorTile(BlockGetter world, BlockPos pos) {
        if (world == null || pos == null) {
            return Optional.empty();
        }

        return world.getBlockEntity(pos, ELEVATOR_BLOCK_ENTITY_TYPE);
    }

    private static Optional<BlockState> getHeldState(BlockGetter world, BlockPos pos) {
        return getElevatorTile(world, pos).map(ElevatorTileEntity::getHeldState);
    }
}
