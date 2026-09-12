package com.qertyy.gamblingmod.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CoinRouletteItem extends Item {
    public CoinRouletteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            openRouletteScreen();
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    private void openRouletteScreen() {
        Minecraft.getInstance().setScreen(new com.qertyy.gamblingmod.screen.MyRouletteScreen());
    }
}
