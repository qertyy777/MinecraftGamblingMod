package com.qertyy.gamblingmod.item;

import com.qertyy.gamblingmod.GamblingMod;
import com.qertyy.gamblingmod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GamblingMod.MOD_ID);

    public  static  final Supplier<CreativeModeTab> GAMBLING_MOD_TAB = CREATIVE_MODE_TAB.register("gambling_mod_tab",
            ()-> CreativeModeTab.builder().icon(() ->new ItemStack(ModItems.COIN.get()))
                    .title(Component.translatable("creativetab.gamblingmod.coin_items"))
                    .displayItems((itemDisplayParameters, output) ->{
                        output.accept(ModItems.COIN);
                        output.accept(ModItems.COIN_DEV);
                        output.accept(ModBlocks.COIN_BLOCK);
                        output.accept(ModBlocks.COIN_DEV_BLOCK);
                            })



                    .build());

    public  static  void register(IEventBus eventBus)
    {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
