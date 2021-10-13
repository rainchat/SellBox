package com.rainchat.sellbox.managers;

import com.rainchat.sellbox.data.PlayerSellChest;
import com.rainchat.sellbox.utils.Manager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public class SellManager extends Manager<PlayerSellChest> {

    private final Plugin plugin;

    public SellManager(Plugin plugin) {
        super("SellChest", plugin);
        this.plugin = plugin;
    }

    public PlayerSellChest getSellChest(Location location) {
        for (PlayerSellChest playerSellChest : toSet()) {
            if (playerSellChest.getChestLocation().toLocation(plugin).equals(location)) return playerSellChest;
        }
        return null;
    }

    public PlayerSellChest getSellChest(Player player) {
        for (PlayerSellChest playerSellChest : toSet()) {
            if (playerSellChest.getOwner().equals(player.getUniqueId())) return playerSellChest;
        }
        return null;
    }

    public Set<PlayerSellChest> getAllChests() {
        return toSet();
    }

    public void remove(Player player) {
        for (PlayerSellChest playerSellChest : toSet()) {
            if (playerSellChest.getOwner().equals(player.getUniqueId())) {
                remove(playerSellChest);
                return;
            }
        }
    }
}
