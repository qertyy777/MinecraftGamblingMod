package com.qertyy.gamblingmod.screen;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public class MyRouletteScreen extends Screen {

    private final RouletteManager manager = RouletteManager.getInstance();

    // Кнопки интерфейса
    private Button spinButton;
    private Button upgradeButton;
    private Button nextInputBtn, prevInputBtn;
    private Button nextOutputBtn, prevOutputBtn;

    public MyRouletteScreen() {
        super(Component.literal("Казино Мод"));
    }

    @Override
    protected void init() {
        super.init();
        // Синхронизируем время менеджера с моментом открытия экрана, чтобы не было резкого скачка
        manager.setLastTime(Util.getMillis());
        this.clearWidgets();

        int tabWidth = 100;
        int startX = (this.width - (tabWidth * 2 + 10)) / 2;
        int tabY = 15;

        // Кнопки вкладок
        this.addRenderableWidget(Button.builder(Component.literal("🎰 Рулетка"), button -> {
            if (!manager.isSpinning()) { manager.setCurrentTab(RouletteManager.Tab.ROULETTE); refreshWidgets(); }
        }).bounds(startX, tabY, tabWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("⚡ Апгрейдер"), button -> {
            if (!manager.isSpinning()) { manager.setCurrentTab(RouletteManager.Tab.UPGRADER); refreshWidgets(); }
        }).bounds(startX + tabWidth + 10, tabY, tabWidth, 20).build());

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        spinButton = Button.builder(Component.literal("Крутить рулетку!"), button -> manager.startRouletteSpin())
                .bounds(centerX - 60, centerY + 55, 120, 20).build();

        upgradeButton = Button.builder(Component.literal("Попытаться улучшить"), button -> this.tryUpgrade())
                .bounds(centerX - 80, centerY + 50, 160, 20).build();

        int slotsY = centerY - 30;
        prevInputBtn = Button.builder(Component.literal("◀"), b -> manager.changeInput(-1)).bounds(centerX - 95, slotsY + 8, 20, 20).build();
        nextInputBtn = Button.builder(Component.literal("▶"), b -> manager.changeInput(1)).bounds(centerX - 20, slotsY + 8, 20, 20).build();

        prevOutputBtn = Button.builder(Component.literal("◀"), b -> manager.changeOutput(-1)).bounds(centerX + 3, slotsY + 8, 20, 20).build();
        nextOutputBtn = Button.builder(Component.literal("▶"), b -> manager.changeOutput(1)).bounds(centerX + 78, slotsY + 8, 20, 20).build();

        refreshWidgets();
    }

    private void refreshWidgets() {
        this.removeWidget(spinButton);
        this.removeWidget(upgradeButton);
        this.removeWidget(prevInputBtn);
        this.removeWidget(nextInputBtn);
        this.removeWidget(prevOutputBtn);
        this.removeWidget(nextOutputBtn);

        if (manager.getCurrentTab() == RouletteManager.Tab.ROULETTE) {
            this.addRenderableWidget(spinButton);
        } else if (manager.getCurrentTab() == RouletteManager.Tab.UPGRADER) {
            this.addRenderableWidget(upgradeButton);
            this.addRenderableWidget(prevInputBtn);
            this.addRenderableWidget(nextInputBtn);
            this.addRenderableWidget(prevOutputBtn);
            this.addRenderableWidget(nextOutputBtn);
        }
    }

    private void tryUpgrade() {
        if (this.minecraft == null || this.minecraft.player == null) return;

        // ИЗМЕНЕНО: Обращаемся к ModGamblePrices вместо RouletteManager
        Item inputItem = com.qertyy.gamblingmod.item.ModGamblePrices.ALLOWED_ITEMS.get(manager.getSelectedInputIndex());
        Item outputItem = com.qertyy.gamblingmod.item.ModGamblePrices.ALLOWED_ITEMS.get(manager.getSelectedOutputIndex());

        if (!this.minecraft.player.getInventory().contains(new ItemStack(inputItem))) {
            this.minecraft.player.displayClientMessage(
                    Component.literal("§c[Апгрейдер] У вас в инвентаре нет предмета: " + new ItemStack(inputItem).getHoverName().getString()), false
            );
            return;
        }

        String inputName = BuiltInRegistries.ITEM.getKey(inputItem).toString();
        String outputName = BuiltInRegistries.ITEM.getKey(outputItem).toString();

        this.minecraft.player.connection.sendChat("/clear @s " + inputName + " 1");

        int roll = this.minecraft.player.getRandom().nextInt(100);
        if (roll < manager.getUpgradeChance()) {
            this.minecraft.player.displayClientMessage(Component.literal("§2[Апгрейдер] §aУспех (" + manager.getUpgradeChance() + "%)! Предмет улучшен!"), false);
            this.minecraft.player.connection.sendChat("/give @s " + outputName + " 1");
        } else {
            this.minecraft.player.displayClientMessage(Component.literal("§4[Апгрейдер] §cНеудача (" + (100 - manager.getUpgradeChance()) + "%). Предмет сгорел!"), false);
        }
    }

    private void renderUpgraderTab(GuiGraphics graphics) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        graphics.drawCenteredString(this.font, "⚡ Станция Улучшения Предметов", centerX, centerY - 60, 0x00FFDD);

        int boxSize = 36;
        int inputX = centerX - 70;
        int outputX = centerX + 34;
        int slotsY = centerY - 30;

        // ИЗМЕНЕНО: Обращаемся к ModGamblePrices вместо RouletteManager
        Item currentInputItem = com.qertyy.gamblingmod.item.ModGamblePrices.ALLOWED_ITEMS.get(manager.getSelectedInputIndex());
        Item currentOutputItem = com.qertyy.gamblingmod.item.ModGamblePrices.ALLOWED_ITEMS.get(manager.getSelectedOutputIndex());

        graphics.fill(inputX, slotsY, inputX + boxSize, slotsY + boxSize, 0xFF333333);
        graphics.fill(inputX + 2, slotsY + 2, inputX + boxSize - 2, slotsY + boxSize - 2, 0xFF151515);
        graphics.pose().pushPose();
        graphics.pose().translate(inputX + 2, slotsY + 2, 0);
        graphics.pose().scale(2.0f, 2.0f, 1.0f);
        graphics.renderItem(new ItemStack(currentInputItem), 0, 0);
        graphics.pose().popPose();

        graphics.drawCenteredString(this.font, "➔", centerX + 2, slotsY + 6, 0x00FFDD);
        graphics.drawCenteredString(this.font, manager.getUpgradeChance() + "%", centerX + 2, slotsY + 20, 0xFFD700);

        graphics.fill(outputX, slotsY, outputX + boxSize, slotsY + boxSize, 0xFF443322);
        graphics.fill(outputX + 2, slotsY + 2, outputX + boxSize - 2, slotsY + boxSize - 2, 0xFF151515);
        graphics.pose().pushPose();
        graphics.pose().translate(outputX + 2, slotsY + 2, 0);
        graphics.pose().scale(2.0f, 2.0f, 1.0f);
        graphics.renderItem(new ItemStack(currentOutputItem), 0, 0);
        graphics.pose().popPose();

        String inName = new ItemStack(currentInputItem).getHoverName().getString();
        String outName = new ItemStack(currentOutputItem).getHoverName().getString();

        // ИЗМЕНЕНО: Получаем цену через ModGamblePrices.getValue()
        graphics.drawCenteredString(this.font, "Из: " + inName + " (" + com.qertyy.gamblingmod.item.ModGamblePrices.getValue(currentInputItem) + "$)", centerX, slotsY + 45, 0xAAAAAA);
        graphics.drawCenteredString(this.font, "В: " + outName + " (" + com.qertyy.gamblingmod.item.ModGamblePrices.getValue(currentOutputItem) + "$)", centerX, slotsY + 58, 0xAAAAAA);
    }


    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Мы НЕ считаем физику тут, менеджер сам считает её в глобальном тике в фоне.
        // Просто рисуем черный фон и виджеты
        graphics.fill(0, 0, this.width, this.height, 0xFF000000);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (manager.getCurrentTab() == RouletteManager.Tab.ROULETTE) {
            renderRouletteTab(graphics);
        } else if (manager.getCurrentTab() == RouletteManager.Tab.UPGRADER) {
            renderUpgraderTab(graphics);
        }
    }

    private void renderRouletteTab(GuiGraphics graphics) {
        int viewWidth = 220;
        int viewHeight = 44;
        int viewX = (this.width - viewWidth) / 2;
        int viewY = (this.height - viewHeight) / 2 - 15;

        graphics.fill(viewX - 4, viewY - 4, viewX + viewWidth + 4, viewY + viewHeight + 4, 0xFF222222);
        graphics.fill(viewX, viewY, viewX + viewWidth, viewY + viewHeight, 0xFF111111);

        List<ItemStack> items = manager.getRouletteItems();
        double currentScrollX = manager.getCurrentScrollX();

        graphics.enableScissor(viewX, viewY, viewX + viewWidth, viewY + viewHeight);
        for (int i = 0; i < items.size(); i++) {
            double itemX = viewX + (i * RouletteManager.SLOT_STEP) - currentScrollX + (viewWidth / 2.0) - (RouletteManager.ITEM_SIZE / 2.0);
            if (itemX + RouletteManager.ITEM_SIZE > viewX && itemX < viewX + viewWidth) {
                ItemStack stack = items.get(i);
                graphics.fill((int)itemX - 2, viewY + 4, (int)itemX + RouletteManager.ITEM_SIZE + 2, viewY + viewHeight - 4, 0xFF2A2A2A);

                graphics.pose().pushPose();
                graphics.pose().translate(itemX, viewY + 6, 0);
                graphics.pose().scale(2.0f, 2.0f, 1.0f);
                graphics.renderItem(stack, 0, 0);
                graphics.pose().popPose();
            }
        }
        graphics.disableScissor();

        int centerX = this.width / 2;
        graphics.fill(centerX - 1, viewY - 6, centerX + 1, viewY + viewHeight + 6, 0xFFFF0000);
        graphics.drawCenteredString(this.font, "🎰 Безумная Рулетка", this.width / 2, viewY - 20, 0xFFD700);
    }

 

    @Override
    public boolean isPauseScreen() { return false; }
}
