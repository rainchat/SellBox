package com.rainchat.sellbox.data;


import org.bukkit.Location;

import java.util.UUID;

public class PlayerSellChest {

    private UUID owner;
    private ChestLocation chestLocation;

    public PlayerSellChest(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    public ChestLocation getChestLocation() {
        return chestLocation;
    }

    public void setChestLocation(Location location) {
        chestLocation = new ChestLocation(location);
    }


}
