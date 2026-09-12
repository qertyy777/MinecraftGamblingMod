package com.qertyy.gamblingmod.item;

import com.qertyy.gamblingmod.GamblingMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GamblingMod.MOD_ID);

    public  static final DeferredItem<Item> COIN = ITEMS.register("coin", () -> new Item(new Item.Properties()));
    public  static final DeferredItem<Item> COIN_DEV = ITEMS.register("coin_dev", () -> new Item(new Item.Properties()));


    public  static  void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
