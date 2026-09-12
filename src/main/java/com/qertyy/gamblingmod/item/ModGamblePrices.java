package com.qertyy.gamblingmod.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModGamblePrices {

    // Глобальная карта ценностей предметов
    public static final Map<Item, Integer> ITEM_VALUES = new HashMap<>();

    static {
        // Ресурсы
        ITEM_VALUES.put(Items.IRON_INGOT, 10);
        ITEM_VALUES.put(Items.GOLD_INGOT, 15);
        ITEM_VALUES.put(Items.LAPIS_LAZULI, 12);
        ITEM_VALUES.put(Items.EMERALD, 30);
        ITEM_VALUES.put(Items.DIAMOND, 100);
        ITEM_VALUES.put(Items.NETHERITE_INGOT, 500);

        // Оружие и снаряжение
        ITEM_VALUES.put(Items.IRON_SWORD, 30);
        ITEM_VALUES.put(Items.DIAMOND_SWORD, 300);
        ITEM_VALUES.put(Items.NETHERITE_SWORD, 1500);
    }

    // Список всех доступных для казино предметов (ключи из карты)
    public static final List<Item> ALLOWED_ITEMS = new ArrayList<>(ITEM_VALUES.keySet());

    // Вспомогательный метод для быстрого получения цены предмета
    public static int getValue(Item item) {
        return ITEM_VALUES.getOrDefault(item, 0);
    }
}
