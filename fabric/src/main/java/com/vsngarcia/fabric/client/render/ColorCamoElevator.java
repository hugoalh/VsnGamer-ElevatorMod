package com.vsngarcia.fabric.client.render;

import com.vsngarcia.fabric.ElevatorBlock;
import com.vsngarcia.fabric.tile.ElevatorTileEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static com.vsngarcia.fabric.FabricRegistry.ELEVATOR_BLOCKS;

@Environment(EnvType.CLIENT)
public class ColorCamoElevator implements BlockColor {
    public static void init() {
        ColorProviderRegistry.BLOCK.register(
                new ColorCamoElevator(),
                ELEVATOR_BLOCKS.values().toArray(new Block[0])
        );
    }

    @Override
    public int getColor(BlockState state, BlockAndTintGetter lightReader, BlockPos pos, int tintIndex) {
        if (lightReader == null || pos == null) {
            return -1;
        }

        if (state.getBlock() instanceof ElevatorBlock && lightReader.getBlockEntity(pos) instanceof ElevatorTileEntity tile) {
            if (tile.getHeldState() != null) {
                return Minecraft.getInstance().getBlockColors().getColor(tile.getHeldState(), lightReader, pos, tintIndex);
            }
        }
        return -1;
    }
}
