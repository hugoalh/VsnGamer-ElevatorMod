package com.vsngarcia.fabric.client;

import com.vsngarcia.ElevatorMod;
import com.vsngarcia.fabric.ElevatorHandler;
import com.vsngarcia.fabric.client.render.ColorCamoElevator;
import com.vsngarcia.fabric.client.render.ElevatorBakedModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.vsngarcia.fabric.FabricRegistry.ELEVATOR_BLOCKS;

@Environment(EnvType.CLIENT)
public final class ElevatorModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        ElevatorHandler.init();
        ColorCamoElevator.init();

        ModelLoadingPlugin.register(new ElevatorModelLoadingPlugin());

        // TODO: Don't forget to register elevator screen
    }

    public static class ElevatorModelLoadingPlugin implements ModelLoadingPlugin {
        @Override
        public void onInitializeModelLoader(Context ctx) {
            ctx.modifyModelAfterBake().register(
                    (model, context) -> {
                        ModelResourceLocation location = context.topLevelId();
                        if (location == null) {
                            return model;
                        }

                        var modelId = location.id();
                        Logger logger = LogManager.getLogger(ElevatorMod.ID);
//                        logger.info("Model ID: {}", modelId);
                        if (!ElevatorMod.ID.equals(modelId.getNamespace()) || !modelId.getPath().startsWith("elevator_")) {
                            return model;
                        }

                        if ("inventory".equals(location.variant())) {
                            logger.warn("Ignoring inventory model: {}", modelId);
                            return model;
                        }

                        logger.warn("Baking Elevator model: {}:{}", modelId, location.variant());
                        return new ElevatorBakedModel(model);
                    }
            );

            ctx.addModels(ResourceLocation.fromNamespaceAndPath(ElevatorMod.ID, "arrow"));
        }
    }
}
