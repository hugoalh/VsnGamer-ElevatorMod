package com.vsngarcia.fabric.client.render;

import com.vsngarcia.ElevatorMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.DelegateBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.vsngarcia.fabric.ElevatorBlock.DIRECTIONAL;
import static com.vsngarcia.fabric.ElevatorBlock.SHOW_ARROW;
import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

@Environment(EnvType.CLIENT)
public class ElevatorBakedModel extends DelegateBakedModel {
    private static final Minecraft MC = Minecraft.getInstance();

    public ElevatorBakedModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(
            QuadEmitter emitter,
            BlockAndTintGetter blockView,
            BlockState state,
            BlockPos pos,
            Supplier<RandomSource> randomSupplier,
            Predicate<@Nullable Direction> cullTest
    ) {
        if (state != null && state.getValue(DIRECTIONAL) && state.getValue(SHOW_ARROW)) {
            emitter.pushTransform(
                    quad -> {
                        quad.material(
                                Renderer.get()
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
                    .emitBlockQuads(emitter, blockView, state, pos, randomSupplier, cullTest);
            emitter.popTransform();
        }

        if (blockView.getBlockEntityRenderData(pos) instanceof BlockState heldState) {
//            ElevatorMod.LOGGER.warn("Found held state for elevator at {}", pos);

            emitter.pushTransform(
                    quad -> {
                        quad.material(
                                Renderer.get()
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
                    .emitBlockQuads(emitter, blockView, heldState, pos, randomSupplier, cullTest);

            emitter.popTransform();
            return;
        }

//        ElevatorMod.LOGGER.warn("No held state found for elevator at {}", pos);
        super.emitBlockQuads(emitter, blockView, state, pos, randomSupplier, cullTest);
    }
}
