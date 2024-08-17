package com.vsngarcia.client.gui;

import com.vsngarcia.network.ClientPacketSender;
import com.vsngarcia.network.client.SetFacingPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.awt.*;


class FacingButton extends Button {

    final Direction direction;

    FacingButton(Point slot, Direction direction, BlockPos pos, ClientPacketSender packetSender) {
        super(
                slot.x,
                slot.y,
                20,
                20,
                Component.translatable("screen.elevatorid.elevator.directional_" + direction.getName()),
                but -> {
                    packetSender.sendToServer(new SetFacingPacket(direction, pos));
                    but.setFocused(false);
                },
                DEFAULT_NARRATION
        );

        this.direction = direction;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        //RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (isHoveredOrFocused() && active) {
            guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, -2130706433);
        }

        guiGraphics.drawCenteredString(
                Minecraft.getInstance().font,
                getMessage().getString(),
                getX() + this.width / 2,
                getY() + (this.height - 8) / 2,
                active ? 16777215 : 65280
        );
    }
}
