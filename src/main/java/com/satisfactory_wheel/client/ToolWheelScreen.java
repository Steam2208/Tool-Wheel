package com.satisfactory_wheel.client;

import com.satisfactory_wheel.ModConfig;
import com.satisfactory_wheel.network.EquipToolPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ToolWheelScreen extends Screen {
    private static class DisplayTool {
        public final ModConfig.ToolEntry entry;
        public final ItemStack stack;

        public DisplayTool(ModConfig.ToolEntry entry, ItemStack stack) {
            this.entry = entry;
            this.stack = stack;
        }
    }

    private final List<DisplayTool> tools = new ArrayList<>();
    private int hoveredIndex = -2;

    public ToolWheelScreen() {
        super(Component.literal("Satisfactory Tool Wheel"));
    }

    @Override
    protected void init() {
        super.init();
        tools.clear();
        for (ModConfig.ToolEntry entry : ModConfig.getTools()) {
            ResourceLocation resLoc = ResourceLocation.tryParse(entry.getId());
            ItemStack stack = ItemStack.EMPTY;
            if (resLoc != null && BuiltInRegistries.ITEM.containsKey(resLoc)) {
                stack = new ItemStack(BuiltInRegistries.ITEM.get(resLoc));
            }
            tools.add(new DisplayTool(entry, stack));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        // Completely disable vanilla menu blur effect
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Completely disable vanilla background rendering
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int radius = ModConfig.getWheelRadius();
        int innerRadius = 26;

        // Subtle dark translucent tint over the world (no blur!)
        guiGraphics.fill(0, 0, this.width, this.height, 0x44000000);

        if (tools.isEmpty()) {
            hoveredIndex = -2;
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.satisfactory_wheel.empty_config"), centerX, centerY, 0xFFFF5555);
            return;
        }

        // Calculate distance and mouse angle
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double dist = Math.hypot(dx, dy);

        if (dist < innerRadius) {
            hoveredIndex = -1; // Center / holster
        } else {
            // Angle starting from top (12 o'clock = -PI/2)
            double mouseAngle = Math.atan2(dy, dx);
            double normAngle = mouseAngle + Math.PI / 2.0;
            while (normAngle < 0) normAngle += Math.PI * 2.0;
            while (normAngle >= Math.PI * 2.0) normAngle -= Math.PI * 2.0;

            double sectorAngle = (Math.PI * 2.0) / tools.size();
            int index = (int) Math.floor((normAngle + sectorAngle / 2.0) / sectorAngle) % tools.size();
            hoveredIndex = index;
        }

        // Render Center / Holster Button
        boolean centerHovered = (hoveredIndex == -1);
        int centerSize = centerHovered ? 28 : 24;
        int cx1 = centerX - centerSize / 2;
        int cy1 = centerY - centerSize / 2;
        int cx2 = cx1 + centerSize;
        int cy2 = cy1 + centerSize;

        int centerBg = centerHovered ? 0xDD2A2A2A : 0x99181818;
        int centerBorder = centerHovered ? 0xFFFA9549 : 0x55777777;

        guiGraphics.fill(cx1, cy1, cx2, cy2, centerBg);
        drawBorder(guiGraphics, cx1, cy1, cx2, cy2, centerBorder, centerHovered ? 2 : 1);
        guiGraphics.drawCenteredString(this.font, "✕", centerX, centerY - 4, centerHovered ? 0xFFFA9549 : 0xFFAAAAAA);

        // Render Tool Slots
        double sectorAngle = (Math.PI * 2.0) / tools.size();
        for (int i = 0; i < tools.size(); i++) {
            double angle = -Math.PI / 2.0 + i * sectorAngle;
            int itemCenterX = centerX + (int) Math.round(Math.cos(angle) * radius);
            int itemCenterY = centerY + (int) Math.round(Math.sin(angle) * radius);

            boolean isHovered = (i == hoveredIndex);
            int boxSize = isHovered ? 40 : 34;
            int x1 = itemCenterX - boxSize / 2;
            int y1 = itemCenterY - boxSize / 2;
            int x2 = x1 + boxSize;
            int y2 = y1 + boxSize;

            int fillCol = isHovered ? 0xEE2A2A2A : 0x99181818;
            int borderCol = isHovered ? 0xFFFA9549 : 0x55666666;

            guiGraphics.fill(x1, y1, x2, y2, fillCol);
            drawBorder(guiGraphics, x1, y1, x2, y2, borderCol, isHovered ? 2 : 1);

            DisplayTool dt = tools.get(i);
            if (!dt.stack.isEmpty()) {
                guiGraphics.renderItem(dt.stack, itemCenterX - 8, itemCenterY - 8);
                guiGraphics.renderItemDecorations(this.font, dt.stack, itemCenterX - 8, itemCenterY - 8);
            }
        }

        // Info text below center
        int infoY = centerY + 20;
        if (hoveredIndex >= 0 && hoveredIndex < tools.size()) {
            DisplayTool dt = tools.get(hoveredIndex);
            String displayName = !dt.entry.getName().isEmpty() ? dt.entry.getName() : dt.stack.getHoverName().getString();
            int strWidth = this.font.width(displayName);
            guiGraphics.fill(centerX - strWidth / 2 - 5, infoY - 2, centerX + strWidth / 2 + 5, infoY + 10, 0x88000000);
            guiGraphics.drawCenteredString(this.font, Component.literal(displayName), centerX, infoY, 0xFFFA9549);
        }
    }

    private void drawBorder(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color, int thickness) {
        guiGraphics.fill(x1, y1, x2, y1 + thickness, color); // Top
        guiGraphics.fill(x1, y2 - thickness, x2, y2, color); // Bottom
        guiGraphics.fill(x1, y1, x1 + thickness, y2, color); // Left
        guiGraphics.fill(x2 - thickness, y1, x2, y2, color); // Right
    }

    private void selectAndClose() {
        if (hoveredIndex >= 0 && hoveredIndex < tools.size()) {
            String toolId = tools.get(hoveredIndex).entry.getId();
            PacketDistributor.sendToServer(new EquipToolPayload(toolId));
        } else if (hoveredIndex == -1) {
            PacketDistributor.sendToServer(new EquipToolPayload("unequip"));
        }
        onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            selectAndClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (ModConfig.isHoldToSelect() && ModClient.OPEN_KEY.matches(keyCode, scanCode)) {
            selectAndClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        if (!ModConfig.isHoldToSelect() && ModClient.OPEN_KEY.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
