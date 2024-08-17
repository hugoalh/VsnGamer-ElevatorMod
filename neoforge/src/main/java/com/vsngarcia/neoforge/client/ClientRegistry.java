package com.vsngarcia.neoforge.client;

import com.vsngarcia.ElevatorMod;
import com.vsngarcia.client.ColorCamoElevator;
import com.vsngarcia.level.ElevatorContainer;
import com.vsngarcia.neoforge.ElevatorBlock;
import com.vsngarcia.client.gui.ElevatorScreen;
import com.vsngarcia.neoforge.client.render.ElevatorBakedModel;
import com.vsngarcia.neoforge.init.Registry;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;


@EventBusSubscriber(modid = ElevatorMod.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistry {
    @SubscribeEvent
    public static void onMenuScreensRegistry(RegisterMenuScreensEvent e) {
        e.register(
                Registry.ELEVATOR_CONTAINER.get(),
                (ElevatorContainer container, Inventory inv, Component title) ->
                        new ElevatorScreen(container, inv, title, PacketDistributor::sendToServer)
        );
    }

    @SubscribeEvent
    public static void onBlockColorHandlersRegistry(RegisterColorHandlersEvent.Block e) {
        e.register(
                new ColorCamoElevator(),
                Registry.ELEVATOR_BLOCKS.values().stream().map(DeferredHolder::get).toArray(ElevatorBlock[]::new)
        );
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelEvent.RegisterAdditional e) {
        e.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("elevatorid", "arrow")));
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult e) {
        e.getModels().entrySet().stream()
                .filter(entry -> "elevatorid".equals(entry.getKey().id().getNamespace()) && entry.getKey().id().getPath().contains("elevator_"))
                .forEach(entry -> e.getModels().put(entry.getKey(), new ElevatorBakedModel(entry.getValue())));
    }
}
