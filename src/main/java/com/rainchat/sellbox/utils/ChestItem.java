package com.rainchat.sellbox.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class ChestItem {

    private static NamespacedKey sellBox;

    public ChestItem(Plugin plugin) {
        sellBox = new NamespacedKey(plugin, "lite-food-id");
    }

    public static boolean isCustomFood(ItemStack pouch) {
        ItemMeta pouchItemMeta = pouch.getItemMeta();
        PersistentDataContainer container = pouchItemMeta.getPersistentDataContainer();
        return container.has(sellBox, PersistentDataType.STRING);
    }

    public static void setCustomFood(ItemStack pouch) {
        ItemMeta pouchItemMeta = pouch.getItemMeta();
        if (pouchItemMeta == null) return;
        PersistentDataContainer container = pouchItemMeta.getPersistentDataContainer();
        container.set(sellBox, PersistentDataType.STRING, "rain_sell_box");
        pouch.setItemMeta(pouchItemMeta);
    }
}
