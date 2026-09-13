package com.qertyy.gamblingmod.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.narration.NarratableEntry;

public class MyRouletteScreen extends Screen {

    private final RouletteManager manager = RouletteManager.getInstance();
    private RouletteTabScreen rouletteSubScreen;
    private UpgraderTabScreen upgraderSubScreen;

    public MyRouletteScreen() {
        super(Component.literal("Казино Мод"));
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        if (this.rouletteSubScreen == null) {
            this.rouletteSubScreen = new RouletteTabScreen(this);
        }
        if (this.upgraderSubScreen == null) {
            this.upgraderSubScreen = new UpgraderTabScreen(this);
        }

        this.rouletteSubScreen.initSub(this.minecraft, this.width, this.height);
        this.upgraderSubScreen.initSub(this.minecraft, this.width, this.height);

        int tabWidth = 100;
        int startX = (this.width - (tabWidth * 2 + 10)) / 2;
        int tabY = 15;

        this.addRenderableWidget(Button.builder(Component.literal("🎰 Рулетка"), button -> {
            if (!manager.isSpinning() && !manager.isUpgrading()) {
                manager.setCurrentTab(RouletteManager.Tab.ROULETTE);
                refreshWidgets();
            }
        }).bounds(startX, tabY, tabWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("⚡ Апгрейдер"), button -> {
            if (!manager.isSpinning() && !manager.isUpgrading()) {
                manager.setCurrentTab(RouletteManager.Tab.UPGRADER);
                refreshWidgets();
            }
        }).bounds(startX + tabWidth + 10, tabY, tabWidth, 20).build());

        refreshWidgets();
    }

    public void refreshWidgets() {
        if (this.rouletteSubScreen != null) this.rouletteSubScreen.clearSubWidgets();
        if (this.upgraderSubScreen != null) this.upgraderSubScreen.clearSubWidgets();

        if (manager.getCurrentTab() == RouletteManager.Tab.ROULETTE && this.rouletteSubScreen != null) {
            this.rouletteSubScreen.addSubWidgets();
        } else if (manager.getCurrentTab() == RouletteManager.Tab.UPGRADER && this.upgraderSubScreen != null) {
            this.upgraderSubScreen.addSubWidgets();
        }
    }

    public <T extends net.minecraft.client.gui.components.events.GuiEventListener & net.minecraft.client.gui.components.Renderable & NarratableEntry> T addWidgetFromSub(T widget) {
        return this.addRenderableWidget(widget);
    }

    public void removeWidgetFromSub(net.minecraft.client.gui.components.events.GuiEventListener widget) {
        this.removeWidget(widget);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        manager.updateAnimation();

        graphics.fill(0, 0, this.width, this.height, 0xFF000000);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (manager.getCurrentTab() == RouletteManager.Tab.ROULETTE && this.rouletteSubScreen != null) {
            this.rouletteSubScreen.render(graphics, mouseX, mouseY, partialTick);
        } else if (manager.getCurrentTab() == RouletteManager.Tab.UPGRADER && this.upgraderSubScreen != null) {
            this.upgraderSubScreen.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
