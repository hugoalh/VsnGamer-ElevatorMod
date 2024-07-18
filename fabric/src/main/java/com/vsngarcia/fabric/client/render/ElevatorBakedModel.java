package com.vsngarcia.fabric.client.render;

import com.vsngarcia.ElevatorMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.function.Supplier;

import static com.vsngarcia.fabric.ElevatorBlock.DIRECTIONAL;
import static com.vsngarcia.fabric.ElevatorBlock.SHOW_ARROW;
import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

@Environment(EnvType.CLIENT)
public class ElevatorBakedModel extends ForwardingBakedModel {

    private static final Minecraft MC = Minecraft.getInstance();

//    public static final ModelProperty<BlockState> HELD_STATE = new ModelProperty<>();

    public ElevatorBakedModel(BakedModel originalModel) {
        wrapped = originalModel;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        if (state != null && state.getValue(DIRECTIONAL) && state.getValue(SHOW_ARROW)) {
            context.pushTransform(
                    quad -> {
                        quad.material(
                                RendererAccess.INSTANCE
                                        .getRenderer()
                                        .materialFinder()
                                        .blendMode(BlendMode.CUTOUT)
                                        .find()
                        );

                        Vector3f vec = null;
                        for (int i = 0; i < 4; i++) {
                            vec = quad.copyPos(i, vec);
                            vec.sub(.5f, 0, .5f);
                            vec.rotateY((float) Math.toRadians(-state.getValue(FACING).toYRot()));
                            vec.add(.5f, 0, .5f);

                            quad.pos(i, vec);
                        }
                        return true;
                    }
            );

            MC.getModelManager()
                    .getModel(ResourceLocation.fromNamespaceAndPath(ElevatorMod.ID, "arrow"))
                    .emitBlockQuads(blockView, state, pos, randomSupplier, context);
            context.popTransform();
        }

        if (blockView.getBlockEntityRenderData(pos) instanceof BlockState heldState) {
//            ElevatorMod.LOGGER.warn("Found held state for elevator at {}", pos);

            context.pushTransform(
                    quad -> {
                        quad.material(
                                RendererAccess.INSTANCE
                                        .getRenderer()
                                        .materialFinder()
                                        .blendMode(BlendMode.fromRenderLayer(
                                                ItemBlockRenderTypes.getChunkRenderType(heldState)
                                        ))
                                        .find()
                        );

                        return true;
                    }
            );

            MC.getBlockRenderer().getBlockModel(heldState)
                    .emitBlockQuads(blockView, heldState, pos, randomSupplier, context);

            context.popTransform();
            return;
        }

//        ElevatorMod.LOGGER.warn("No held state found for elevator at {}", pos);
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
    }
}
