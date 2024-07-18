package com.vsngarcia.fabric.client;

import com.vsngarcia.ElevatorMod;
import com.vsngarcia.fabric.ElevatorHandler;
import com.vsngarcia.fabric.client.gui.ElevatorScreen;
import com.vsngarcia.fabric.client.render.ColorCamoElevator;
import com.vsngarcia.fabric.client.render.ElevatorBakedModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

import static com.vsngarcia.fabric.FabricRegistry.ELEVATOR_CONTAINER;

@Environment(EnvType.CLIENT)
public final class ElevatorModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        ElevatorHandler.init();
        ColorCamoElevator.init();

        ModelLoadingPlugin.register(new ElevatorModelLoadingPlugin());

        MenuScreens.register(ELEVATOR_CONTAINER, ElevatorScreen::new);
    }

    public static class ElevatorModelLoadingPlugin implements ModelLoadingPlugin {
        @Override
        public void onInitializeModelLoader(Context ctx) {
            ctx.modifyModelAfterBake().register(
                    (model, context) -> {
                        ModelResourceLocation location = context.topLevelId();
                        if (location == null || "inventory".equals(location.variant())) {
                            return model;
                        }

                        var modelId = location.id();
                        if (!ElevatorMod.ID.equals(modelId.getNamespace()) || !modelId.getPath().startsWith("elevator_")) {
                            return model;
                        }

                        ElevatorMod.LOGGER.debug("Wrapping elevator model: {}", modelId);
                        return new ElevatorBakedModel(model);
                    }
            );

            ctx.addModels(ResourceLocation.fromNamespaceAndPath(ElevatorMod.ID, "arrow"));
        }
    }
}
