package com.rainchat.sellbox.commands;

import com.rainchat.sellbox.SellBox;
import com.rainchat.sellbox.data.PlayerSellChest;
import com.rainchat.sellbox.managers.SellManager;
import com.rainchat.sellbox.utils.ChestItem;
import com.rainchat.sellbox.utils.Item;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Command("StardewValley")
@Alias({"sv", "sellbox"})
public class PlayerCommands extends CommandBase {

    private final SellManager sellManager;
    private final SellBox plugin;

    public PlayerCommands(SellManager sellManager, SellBox plugin) {
        this.sellManager = sellManager;
        this.plugin = plugin;
    }

    @Default
    @Permission("sellbox.command.stats")
    public void onDefault(Player player) {
        if (sellManager.getSellChest(player) == null) {
            player.sendMessage(plugin.getConfig().getString("messages.no-chest").replace('&', '§'));
            return;
        }

        PlayerSellChest playerSellChest = sellManager.getSellChest(player);

        Block block = playerSellChest.getChestLocation().toLocation(plugin).getBlock();

        if (!block.getType().name().contains("CHEST")) return;

        Chest chest = (Chest) block.getState();
        double total = plugin.calcWorthOfContent(chest.getInventory().getContents());

        for (String line: plugin.getConfig().getStringList("messages.chest-stats")) {
            line = line.replace('&', '§');
            line = line.replace("%total%", plugin.getEconomy().format(total));
            line = line.replace("%cords%", playerSellChest.getChestLocation().toString());
            player.sendMessage(line);
        }
    }

    @SubCommand("getChest")
    @Permission("sellbox.command.getchest")
    public void getChest(Player player) {

        Item item = new Item();
        item.material(Material.CHEST);
        item.name(plugin.getConfig().getString("Chest-item.name"));
        item.lore(plugin.getConfig().getStringList("Chest-item.lore"));

        ItemStack itemStack = item.build();

        ChestItem.setCustomFood(itemStack);
        player.getInventory().addItem(itemStack);
    }




    @SubCommand("removeChest")
    @Permission("sellbox.command.removechest")
    public void onRemove(Player player) {
        if (sellManager.getSellChest(player) == null) {
            player.sendMessage(plugin.getConfig().getString("messages.no-chest").replace('&', '§'));
            return;
        }

        sellManager.remove(player);

    }

}
