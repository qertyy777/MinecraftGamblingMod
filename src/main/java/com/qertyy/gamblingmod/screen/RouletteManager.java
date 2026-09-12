package com.qertyy.gamblingmod.screen;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.qertyy.gamblingmod.item.ModGamblePrices; // Импортируем наш новый класс
import java.util.ArrayList;
import java.util.List;

public class RouletteManager {

    public enum Tab { ROULETTE, UPGRADER }
    private Tab currentTab = Tab.ROULETTE;

    private static RouletteManager INSTANCE;

    public static RouletteManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RouletteManager();
        }
        return INSTANCE;
    }

    // --- ПЕРЕМЕННЫЕ ДЛЯ РУЛЕТКИ ---
    private final List<ItemStack> rouletteItems = new ArrayList<>();
    private final RandomSource random = RandomSource.create();
    private double currentScrollX = 0;
    private double speedX = 0;
    private long lastTime = 0;
    private boolean isSpinning = false;

    public static final int ITEM_SIZE = 32;
    public static final int ITEM_GAP = 12;
    public static final int SLOT_STEP = ITEM_SIZE + ITEM_GAP;

    // --- ПЕРЕМЕННЫЕ ДЛЯ АПГРЕЙДЕРА ---
    private int selectedInputIndex = 0;
    private int selectedOutputIndex = 1;
    private int upgradeChance = 0;

    private RouletteManager() {
        generateRandomItems();
        calculateChance();
        this.lastTime = Util.getMillis();
    }

    public void tick() {
        long now = Util.getMillis();
        double deltaTime = (now - lastTime) / 1000.0;
        this.lastTime = now;

        if (isSpinning && speedX > 0) {
            currentScrollX += speedX * deltaTime;
            speedX -= 150.0 * deltaTime;
            if (speedX <= 0) {
                speedX = 0;
                isSpinning = false;
                onSpinComplete();
            }
        }
    }

    public void startRouletteSpin() {
        if (isSpinning) return;
        generateRandomItems();
        this.currentScrollX = 0;
        this.speedX = 400.0 + random.nextDouble() * 300.0;
        this.isSpinning = true;
        this.lastTime = Util.getMillis();
    }

    private void generateRandomItems() {
        rouletteItems.clear();
        // Используем список из нового класса ценностей
        List<Item> pool = ModGamblePrices.ALLOWED_ITEMS;
        for (int i = 0; i < 60; i++) {
            rouletteItems.add(new ItemStack(pool.get(random.nextInt(pool.size()))));
        }
    }

    public void calculateChance() {
        // Считываем ценности через наш новый класс ModGamblePrices
        double v1 = ModGamblePrices.getValue(ModGamblePrices.ALLOWED_ITEMS.get(selectedInputIndex));
        double v2 = ModGamblePrices.getValue(ModGamblePrices.ALLOWED_ITEMS.get(selectedOutputIndex));
        this.upgradeChance = (int) Math.max(1, Math.min(95, Math.round((v1 / v2) * 100.0)));
    }

    private void onSpinComplete() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int winningIndex = (int) Math.round(currentScrollX / SLOT_STEP);
        if (winningIndex >= 0 && winningIndex < rouletteItems.size()) {
            ItemStack winStack = rouletteItems.get(winningIndex);

            mc.player.displayClientMessage(
                    Component.literal("§6[Казино] §fВы выиграли: §a" + winStack.getHoverName().getString() + " !"), false
            );

            String name = BuiltInRegistries.ITEM.getKey(winStack.getItem()).toString();
            mc.player.connection.sendChat("/give @s " + name);
        }
    }

    // --- ГЕТТЕРЫ И СЕТТЕРЫ УПРАВЛЕНИЯ СОСТОЯНИЕМ ---
    public Tab getCurrentTab() { return currentTab; }
    public void setCurrentTab(Tab tab) { this.currentTab = tab; }
    public List<ItemStack> getRouletteItems() { return rouletteItems; }
    public double getCurrentScrollX() { return currentScrollX; }
    public boolean isSpinning() { return isSpinning; }
    public int getSelectedInputIndex() { return selectedInputIndex; }
    public int getSelectedOutputIndex() { return selectedOutputIndex; }
    public int getUpgradeChance() { return upgradeChance; }

    public void changeInput(int amt) {
        int total = ModGamblePrices.ALLOWED_ITEMS.size();
        selectedInputIndex = (selectedInputIndex + amt + total) % total;
        calculateChance();
    }

    public void changeOutput(int amt) {
        int total = ModGamblePrices.ALLOWED_ITEMS.size();
        selectedOutputIndex = (selectedOutputIndex + amt + total) % total;
        calculateChance();
    }

    public void setLastTime(long time) { this.lastTime = time; }
}
