package com.rainchat.sellbox.listeners;

import com.rainchat.sellbox.SellBox;
import com.rainchat.sellbox.data.PlayerSellChest;
import com.rainchat.sellbox.managers.SellManager;
import com.rainchat.sellbox.utils.ChestItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ChestCheck implements Listener {

    private final SellBox plugin;
    private final SellManager sellManager;


    public ChestCheck(SellBox plugin, SellManager sellManager) {
        this.plugin = plugin;
        this.sellManager = sellManager;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        if (block.getType().name().contains("CHEST") && sellManager.getSellChest(player) == null && ChestItem.isCustomFood(event.getItemInHand())) {
            PlayerSellChest playerSellChest = new PlayerSellChest(player.getUniqueId());
            playerSellChest.setChestLocation(block.getLocation());
            sellManager.add(playerSellChest);
            player.sendMessage(plugin.getConfig().getString("messages.place-chest").replace('&', '§'));
        } else if (block.getType().name().contains("CHEST") && sellManager.getSellChest(player) != null && ChestItem.isCustomFood(event.getItemInHand())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfig().getString("messages.place-chest-err").replace('&', '§'));
        }

    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getType().name().contains("CHEST") && sellManager.getSellChest(block.getLocation()) != null) {
            PlayerSellChest playerSellChest = sellManager.getSellChest(player);

            if (sellManager.getSellChest(block.getLocation()).equals(playerSellChest)) {
                sellManager.remove(player);
                player.sendMessage(plugin.getConfig().getString("messages.break-chest").replace('&', '§'));
            } else {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfig().getString("messages.break-chest-err").replace('&', '§'));
            }
        }

    }



    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null) return;

        if (sellManager.getSellChest(player) == null || !block.getType().name().contains("CHEST")) return;

        if (sellManager.getSellChest(player).getChestLocation().toLocation(plugin).equals(block.getLocation())) {
            // something
        }

    }
}

