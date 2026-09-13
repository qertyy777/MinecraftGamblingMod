package com.qertyy.gamblingmod.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.qertyy.gamblingmod.item.ModGamblePrices;
import com.qertyy.gamblingmod.network.RequestUpgradePayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.Util;

public class UpgraderTabScreen {

    private final MyRouletteScreen parent;
    private final RouletteManager manager = RouletteManager.getInstance();

    private Button upgradeButton;
    private Button nextInputBtn, prevInputBtn;
    private Button nextOutputBtn, prevOutputBtn;

    public UpgraderTabScreen(MyRouletteScreen parent) {
        this.parent = parent;
    }

    public void initSub(Minecraft mc, int width, int height) {
        int centerX = width / 2;
        int centerY = height / 2;

        upgradeButton = Button.builder(Component.literal("Попытаться улучшить"), button -> this.tryUpgrade())
                .bounds(centerX - 80, centerY + 65, 160, 20).build();

        int slotsY = centerY - 45;
        prevInputBtn = Button.builder(Component.literal("◀"), b -> manager.changeInput(-1)).bounds(centerX - 105, slotsY + 8, 20, 20).build();
        nextInputBtn = Button.builder(Component.literal("▶"), b -> manager.changeInput(1)).bounds(centerX - 35, slotsY + 8, 20, 20).build();

        prevOutputBtn = Button.builder(Component.literal("◀"), b -> manager.changeOutput(-1)).bounds(centerX + 15, slotsY + 8, 20, 20).build();
        nextOutputBtn = Button.builder(Component.literal("▶"), b -> manager.changeOutput(1)).bounds(centerX + 85, slotsY + 8, 20, 20).build();
    }

    public void addSubWidgets() {
        boolean isBlock = manager.isUpgrading();
        upgradeButton.active = !isBlock;
        prevInputBtn.active = !isBlock;
        nextInputBtn.active = !isBlock;
        prevOutputBtn.active = !isBlock;
        nextOutputBtn.active = !isBlock;

        this.parent.addWidgetFromSub(upgradeButton);
        this.parent.addWidgetFromSub(prevInputBtn);
        this.parent.addWidgetFromSub(nextInputBtn);
        this.parent.addWidgetFromSub(prevOutputBtn);
        this.parent.addWidgetFromSub(nextOutputBtn);
    }

    public void clearSubWidgets() {
        this.parent.removeWidgetFromSub(upgradeButton);
        this.parent.removeWidgetFromSub(prevInputBtn);
        this.parent.removeWidgetFromSub(nextInputBtn);
        this.parent.removeWidgetFromSub(prevOutputBtn);
        this.parent.removeWidgetFromSub(nextOutputBtn);
    }

    private void tryUpgrade() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || manager.isUpgrading()) return;

        Item inputItem = ModGamblePrices.ALLOWED_ITEMS.get(manager.getSelectedInputIndex());

        if (!mc.player.getInventory().contains(new ItemStack(inputItem))) {
            mc.player.displayClientMessage(
                    Component.literal("§c[Апгрейдер] У вас в инвентаре нет предмета: " + new ItemStack(inputItem).getHoverName().getString()), false
            );
            return;
        }

        manager.startClientUpgradeAnimation();
        this.parent.refreshWidgets();
        PacketDistributor.sendToServer(new RequestUpgradePayload(manager.getSelectedInputIndex(), manager.getSelectedOutputIndex()));
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.parent.width / 2;
        int centerY = this.parent.height / 2;

        graphics.drawCenteredString(Minecraft.getInstance().font, "⚡ Станция Улучшения Предметов", centerX, centerY - 75, 0x00FFDD);

        int boxSize = 36;
        int inputX = centerX - 80;
        int outputX = centerX + 44;
        int slotsY = centerY - 45;

        Item currentInputItem = ModGamblePrices.ALLOWED_ITEMS.get(manager.getSelectedInputIndex());
        Item currentOutputItem = ModGamblePrices.ALLOWED_ITEMS.get(manager.getSelectedOutputIndex());

        graphics.fill(inputX, slotsY, inputX + boxSize, slotsY + boxSize, 0xFF333333);
        graphics.fill(inputX + 2, slotsY + 2, inputX + boxSize - 2, slotsY + boxSize - 2, 0xFF151515);
        graphics.pose().pushPose();
        graphics.pose().translate(inputX + 2, slotsY + 2, 0);
        graphics.pose().scale(2.0f, 2.0f, 1.0f);
        graphics.renderItem(new ItemStack(currentInputItem), 0, 0);
        graphics.pose().popPose();

        graphics.fill(outputX, slotsY, outputX + boxSize, slotsY + boxSize, 0xFF443322);
        graphics.fill(outputX + 2, slotsY + 2, outputX + boxSize - 2, slotsY + boxSize - 2, 0xFF151515);
        graphics.pose().pushPose();
        graphics.pose().translate(outputX + 2, slotsY + 2, 0);
        graphics.pose().scale(2.0f, 2.0f, 1.0f);
        graphics.renderItem(new ItemStack(currentOutputItem), 0, 0);
        graphics.pose().popPose();

        renderMinecraftUpgradeCircle(graphics, centerX, slotsY + (boxSize / 2));

        String inName = new ItemStack(currentInputItem).getHoverName().getString();
        String outName = new ItemStack(currentOutputItem).getHoverName().getString();

        graphics.drawCenteredString(Minecraft.getInstance().font, "Из: " + inName + " (" + ModGamblePrices.getValue(currentInputItem) + "$)", centerX, slotsY + 45, 0xAAAAAA);
        graphics.drawCenteredString(Minecraft.getInstance().font, "В: " + outName + " (" + ModGamblePrices.getValue(currentOutputItem) + "$)", centerX, slotsY + 55, 0xAAAAAA);
    }

    private void renderMinecraftUpgradeCircle(GuiGraphics graphics, int cx, int cy) {
        int radius = 24;
        int pixelSize = 3;

        double progress = 0.0;
        if (manager.isUpgrading()) {
            long elapsed = Util.getMillis() - manager.getUpgradeStartTime();
            progress = Math.min(1.0, (double) elapsed / manager.getUpgradeDurationMs());
        } else if (manager.getUpgradeAnimationState() == 2 || manager.getUpgradeAnimationState() == 3) {
            progress = 1.0;
        }

        int baseColor = 0xFF2D2D2D;
        int activeColor = 0xFF00FFDD;

        if (!manager.isUpgrading()) {
            if (manager.getUpgradeAnimationState() == 2) {
                activeColor = 0xFF00FF00;
            } else if (manager.getUpgradeAnimationState() == 3) {
                activeColor = 0xFFFF0000;
            }
        }

        int totalPoints = 32;
        for (int i = 0; i < totalPoints; i++) {
            double angle = (i * (2 * Math.PI / totalPoints)) - (Math.PI / 2);
            int px = cx + (int) Math.round(Math.cos(angle) * radius) - (pixelSize / 2);
            int py = cy + (int) Math.round(Math.sin(angle) * radius) - (pixelSize / 2);

            double pointProgress = (double) i / totalPoints;
            int color = (progress >= pointProgress && progress > 0) ? activeColor : baseColor;

            graphics.fill(px, py, px + pixelSize, py + pixelSize, color);
        }

        graphics.drawCenteredString(Minecraft.getInstance().font, "➔", cx, cy - 8, activeColor);
        graphics.drawCenteredString(Minecraft.getInstance().font, manager.getUpgradeChance() + "%", cx, cy + 2, 0xFFD700);
    }
}
