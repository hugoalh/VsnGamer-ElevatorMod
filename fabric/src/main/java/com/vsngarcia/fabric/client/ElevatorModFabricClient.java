package com.vsngarcia.fabric.client;

import com.vsngarcia.ElevatorHandler;
import com.vsngarcia.ElevatorMod;
import com.vsngarcia.client.ColorCamoElevator;
import com.vsngarcia.client.gui.ElevatorScreen;
import com.vsngarcia.fabric.client.render.ElevatorBakedModel;
import com.vsngarcia.level.ElevatorContainer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;

import static com.vsngarcia.fabric.FabricRegistry.ELEVATOR_BLOCKS;
import static com.vsngarcia.fabric.FabricRegistry.ELEVATOR_CONTAINER;

@Environment(EnvType.CLIENT)
public final class ElevatorModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> ElevatorHandler.handleInput(ClientPlayNetworking::send));

        ColorProviderRegistry.BLOCK.register(
                new ColorCamoElevator(),
                ELEVATOR_BLOCKS.values().toArray(new Block[0])
        );
        ModelLoadingPlugin.register(new ElevatorModelLoadingPlugin());

        MenuScreens.register(
                ELEVATOR_CONTAINER,
                (ElevatorContainer container, Inventory inv, Component title) ->
                        new ElevatorScreen(container, inv, title, ClientPlayNetworking::send)
        );
    }

    public static class ElevatorModelLoadingPlugin implements ModelLoadingPlugin {
        @Override
        public void initialize(Context ctx) {
            ctx.modifyModelAfterBake().register(
                    (model, context) -> {
                        ModelResourceLocation location = context.topLevelId();
                        if (location == null || "inventory".equals(location.variant())) {
                            return model;
                        }

                        var modelId = location.id();
                        if (!ElevatorMod.ID.equals(modelId.getNamespace()) ||
                                !modelId.getPath().startsWith("elevator_")) {
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
