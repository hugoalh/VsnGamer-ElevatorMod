package com.vsngarcia.fabric;

import com.vsngarcia.ElevatorMod;
import com.vsngarcia.fabric.tile.ElevatorContainer;
import com.vsngarcia.fabric.tile.ElevatorTileEntity;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Arrays;
import java.util.EnumMap;

public class FabricRegistry {

    public static final EnumMap<DyeColor, Block> ELEVATOR_BLOCKS = new EnumMap<>(DyeColor.class);

    static {
        Arrays.stream(DyeColor.values()).forEach(color ->
                ELEVATOR_BLOCKS.put(
                        color,
                        Registry.register(
                                BuiltInRegistries.BLOCK,
                                ResourceLocation.fromNamespaceAndPath(
                                        ElevatorMod.ID,
                                        "elevator_" + color.getName()
                                ),
                                new ElevatorBlock(color)
                        )
                )
        );
    }

    public static final EnumMap<DyeColor, BlockItem> ELEVATOR_ITEMS = new EnumMap<>(DyeColor.class);

    static {
        ELEVATOR_BLOCKS.forEach(
                (color, block) -> ELEVATOR_ITEMS.put(
                        color,
                        Registry.register(
                                BuiltInRegistries.ITEM,
                                ResourceLocation.fromNamespaceAndPath(ElevatorMod.ID, "elevator_" + color.getName()),
                                new BlockItem(block, new BlockItem.Properties())
                        )
                )
        );
    }

    public static final BlockEntityType<ElevatorTileEntity> ELEVATOR_BLOCK_ENTITY_TYPE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ElevatorMod.ID, "elevator_tile"),
            BlockEntityType.Builder.of(
                    ElevatorTileEntity::new, ELEVATOR_BLOCKS.values().toArray(new Block[0])
            ).build(null)
    );

    public static final ExtendedScreenHandlerType<ElevatorContainer, ElevatorContainer.ElevatorContainerData> ELEVATOR_CONTAINER =
            new ExtendedScreenHandlerType<>(
                    (syncId, inventory, data) -> new ElevatorContainer(syncId, data.pos(), inventory.player),
                    ElevatorContainer.ElevatorContainerData.CODEC
            );

    static {
        Registry.register(
                BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(ElevatorMod.ID, "elevator_container"),
                ELEVATOR_CONTAINER
        );
    }

    public static final SoundEvent TELEPORT_SOUND = Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            ResourceLocation.fromNamespaceAndPath(ElevatorMod.ID, "teleport"),
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ElevatorMod.ID, "teleport"))
    );

    public static final SoundEvent CAMOUFLAGE_SOUND = Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            ResourceLocation.fromNamespaceAndPath(ElevatorMod.ID, "camouflage"),
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ElevatorMod.ID, "camouflage"))
    );

    static {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath(ElevatorMod.ID, "elevators_tab"),
                FabricItemGroup.builder()
                        .icon(() -> ELEVATOR_BLOCKS.get(DyeColor.WHITE).asItem().getDefaultInstance())
                        .displayItems((params, output) -> ELEVATOR_ITEMS.values().forEach(output::accept))
                        .title(Component.translatable("itemGroup.elevators_tab"))
                        .build()
        );
    }

    public static void init() {
        // Register our items and blocks here.
    }
}
