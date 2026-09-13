package com.qertyy.gamblingmod.screen;

import com.qertyy.gamblingmod.item.ModGamblePrices;
import com.qertyy.gamblingmod.network.StartClientSpinPayload;
import com.qertyy.gamblingmod.network.UpgradeResultPayload;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.TickTask;

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

    private final List<ItemStack> rouletteItems = new ArrayList<>();
    private final RandomSource random = RandomSource.create();

    private double currentScrollX = 0;
    private boolean isSpinning = false;
    private long spinStartTime = 0;
    private final long spinDurationMs = 5000;
    private double maxScrollDist = 0;

    public static final int ITEM_SIZE = 32;
    public static final int ITEM_GAP = 12;
    public static final int SLOT_STEP = ITEM_SIZE + ITEM_GAP;

    private int selectedInputIndex = 0;
    private int selectedOutputIndex = 1;
    private int upgradeChance = 0;

    private boolean isUpgrading = false;
    private long upgradeStartTime = 0;
    private final long upgradeDurationMs = 1500;
    private int upgradeAnimationState = 0;

    private RouletteManager() {
        generateRandomItems();
        this.currentScrollX = SLOT_STEP * 2;
        calculateChance();
    }

    public void updateAnimation() {
        if (isSpinning) {
            long elapsed = Util.getMillis() - spinStartTime;
            double progress = (double) elapsed / spinDurationMs;
            if (progress >= 1.0) {
                progress = 1.0;
                isSpinning = false;
            }
            double easeOut = 1.0 - Math.pow(1.0 - progress, 3);
            this.currentScrollX = maxScrollDist * easeOut;
        }

        if (isUpgrading) {
            long elapsed = Util.getMillis() - upgradeStartTime;
            if (elapsed >= upgradeDurationMs && upgradeAnimationState > 1) {
                isUpgrading = false;
            }
        }
    }

    public void startServerSpin(ServerPlayer player) {
        long seed = player.getRandom().nextLong();
        double randomSpeedFactor = 400.0 + player.getRandom().nextDouble() * 300.0;

        PacketDistributor.sendToPlayer(player, new StartClientSpinPayload(seed, randomSpeedFactor));

        double maxDist = randomSpeedFactor * (spinDurationMs / 1000.0) * 0.5;

        this.random.setSeed(seed);
        List<Item> pool = ModGamblePrices.ALLOWED_ITEMS;
        List<ItemStack> serverItems = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            serverItems.add(new ItemStack(pool.get(random.nextInt(pool.size()))));
        }

        int winningIndex = (int) Math.round(maxDist / SLOT_STEP);
        if (winningIndex >= 0 && winningIndex < serverItems.size()) {
            ItemStack winStack = serverItems.get(winningIndex);

            player.server.tell(new TickTask(player.server.getTickCount() + 100, () -> {
                if (player.isRemoved()) return;

                if (!player.getInventory().add(winStack.copy())) {
                    player.drop(winStack.copy(), false);
                }

                player.displayClientMessage(
                        Component.literal("§6[Казино] §fВы выиграли: §a" + winStack.getHoverName().getString() + " !"), false
                );
            }));
        }
    }

    public void processServerUpgrade(ServerPlayer player, int inputIdx, int outputIdx) {
        List<Item> pool = ModGamblePrices.ALLOWED_ITEMS;
        if (inputIdx < 0 || inputIdx >= pool.size() || outputIdx < 0 || outputIdx >= pool.size()) return;

        Item inputItem = pool.get(inputIdx);
        Item outputItem = pool.get(outputIdx);
        ItemStack inputStack = new ItemStack(inputItem);

        if (!player.getInventory().contains(inputStack)) {
            player.displayClientMessage(
                    Component.literal("§c[Апгрейдер] У вас в инвентаре нет предмета: " + inputStack.getHoverName().getString()), false
            );
            return;
        }

        double v1 = ModGamblePrices.getValue(inputItem);
        double v2 = ModGamblePrices.getValue(outputItem);
        int chance = (int) Math.max(1, Math.min(95, Math.round((v1 / v2) * 100.0)));

        boolean removed = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack slotStack = player.getInventory().getItem(i);
            if (!slotStack.isEmpty() && slotStack.is(inputItem)) {
                slotStack.shrink(1);
                removed = true;
                break;
            }
        }

        if (!removed) return;

        int roll = player.getRandom().nextInt(100);
        boolean success = roll < chance;

        player.server.tell(new TickTask(player.server.getTickCount() + 30, () -> {
            if (player.isRemoved()) return;

            PacketDistributor.sendToPlayer(player, new UpgradeResultPayload(success));

            if (success) {
                player.displayClientMessage(Component.literal("§2[Апгрейдер] §aУспех (" + chance + "%)! Предмет улучшен!"), false);
                ItemStack reward = new ItemStack(outputItem);
                if (!player.getInventory().add(reward.copy())) {
                    player.drop(reward.copy(), false);
                }
            } else {
                player.displayClientMessage(Component.literal("§4[Апгрейдер] §cНеудача (" + (100 - chance) + "%). Предмет сгорел!"), false);
            }
        }));
    }

    public void startClientSpin(long seed, double speedX) {
        this.random.setSeed(seed);
        generateRandomItems();

        this.spinStartTime = Util.getMillis();
        this.maxScrollDist = speedX * (spinDurationMs / 1000.0) * 0.5;
        this.currentScrollX = 0;
        this.isSpinning = true;
    }

    public void startClientUpgradeAnimation() {
        this.isUpgrading = true;
        this.upgradeStartTime = Util.getMillis();
        this.upgradeAnimationState = 1;
    }

    public void onClientUpgradeResult(boolean success) {
        this.upgradeAnimationState = success ? 2 : 3;
    }

    public void resetUpgradeAnimationState() {
        this.upgradeAnimationState = 0;
        this.isUpgrading = false;
    }

    private void generateRandomItems() {
        rouletteItems.clear();
        List<Item> pool = ModGamblePrices.ALLOWED_ITEMS;
        if (pool.isEmpty()) return;
        for (int i = 0; i < 60; i++) {
            rouletteItems.add(new ItemStack(pool.get(random.nextInt(pool.size()))));
        }
    }

    public void calculateChance() {
        if (ModGamblePrices.ALLOWED_ITEMS.isEmpty()) return;
        double v1 = ModGamblePrices.getValue(ModGamblePrices.ALLOWED_ITEMS.get(selectedInputIndex));
        double v2 = ModGamblePrices.getValue(ModGamblePrices.ALLOWED_ITEMS.get(selectedOutputIndex));
        this.upgradeChance = (int) Math.max(1, Math.min(95, Math.round((v1 / v2) * 100.0)));
    }

    public Tab getCurrentTab() { return currentTab; }
    public void setCurrentTab(Tab tab) { this.currentTab = tab; }
    public List<ItemStack> getRouletteItems() { return rouletteItems; }
    public double getCurrentScrollX() { return currentScrollX; }
    public boolean isSpinning() { return isSpinning; }
    public int getSelectedInputIndex() { return selectedInputIndex; }
    public int getSelectedOutputIndex() { return selectedOutputIndex; }
    public int getUpgradeChance() { return upgradeChance; }
    public boolean isUpgrading() { return isUpgrading; }
    public long getUpgradeStartTime() { return upgradeStartTime; }
    public long getUpgradeDurationMs() { return upgradeDurationMs; }
    public int getUpgradeAnimationState() { return upgradeAnimationState; }

    public void changeInput(int amt) {
        int total = ModGamblePrices.ALLOWED_ITEMS.size();
        if (total == 0) return;
        selectedInputIndex = (selectedInputIndex + amt + total) % total;
        resetUpgradeAnimationState();
        calculateChance();
    }

    public void changeOutput(int amt) {
        int total = ModGamblePrices.ALLOWED_ITEMS.size();
        if (total == 0) return;
        selectedOutputIndex = (selectedOutputIndex + amt + total) % total;
        resetUpgradeAnimationState();
        calculateChance();
    }
}
