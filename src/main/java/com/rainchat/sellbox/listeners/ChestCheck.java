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
    private int thisDay;
    private long time;
    private World world;

    public ChestCheck(SellBox plugin, SellManager sellManager) {
        this.plugin = plugin;
        this.sellManager = sellManager;
        this.thisDay = (int) (Bukkit.getWorld("world").getFullTime()/24000);

        time = plugin.getConfig().getLong("time", 6000);
        world = Bukkit.getWorld(plugin.getConfig().getString("world","world"));


        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            int x = (int) (world.getFullTime()/24000);
            if (thisDay < x && world.getTime() <= time ) {
                thisDay = x;
                for (PlayerSellChest playerSellChest: sellManager.getAllChests()) {
                    Player player = Bukkit.getPlayer(playerSellChest.getOwner());
                    if (player == null) return;
                    if (!player.isOnline()) return;
                    sellDrop(playerSellChest, player);
                }
            }


        }, 20L, 20L);
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

            if (playerSellChest.equals(sellManager.getSellChest(block.getLocation()))) {
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

    public void sellDrop(PlayerSellChest playerSellChest, Player player) {
        Block block = playerSellChest.getChestLocation().toLocation(plugin).getBlock();

        if (block.getType().name().contains("CHEST")) {
            Chest chest = (Chest) block.getState();
            ItemStack[] inventory = chest.getInventory().getContents();
            double total = plugin.calcWorthOfContent(chest.getInventory().getContents());

            if (total <= 0) {
                return;
            }

            for (int a = 0; a < inventory.length; ++a) {
                ItemStack item = inventory[a];

                if (plugin.isSalable(item)) {
                    chest.getInventory().setItem(a, new ItemStack(Material.AIR));
                }
            }



            plugin.getEconomy().depositPlayer(player, total);

            player.sendMessage(plugin.getConfig().getString("messages.message-sold").replace('&', '§').replace("%total%", plugin.getEconomy().format(total)));
        }
    }

}

