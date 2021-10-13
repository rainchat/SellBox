package com.rainchat.sellbox.data;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class ChestLocation {

    private double x, y, z;
    private String world;

    ChestLocation(Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = location.getWorld().getName();
    }

    ChestLocation(double x, double y, double z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public Location toLocation(Plugin plugin) {
        if(plugin.getServer().getWorld(world) == null) return null;
        Location location = new Location(plugin.getServer().getWorld(world), x, y, z);
        return location;
    }

    @Override
    public String toString() {
        return "world=" + world + ", x=" + x + ", y=" + y + ", z=" + z;
    }
}
