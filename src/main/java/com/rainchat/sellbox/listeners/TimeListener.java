package com.rainchat.sellbox.listeners;

import com.rainchat.sellbox.SellBox;
import com.rainchat.sellbox.data.PlayerSellChest;
import com.rainchat.sellbox.data.TimeSaver;
import com.rainchat.sellbox.managers.SellManager;
import com.sun.org.apache.xerces.internal.xs.StringList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalTime;
import java.util.*;

public class TimeListener {

    private final SellBox plugin;

    private static World world;
    private int month;
    private int day;
    private int hours;
    private int minutes;
    private static List<TimeSaver> timeSavers;

    public TimeListener(SellBox plugin, SellManager sellManager) {
        this.plugin = plugin;

        world = Bukkit.getWorld(plugin.getConfig().getString("world","world"));

        timeSavers = new ArrayList<>();
        for (String string: plugin.getConfig().getStringList("time")) {
            String[] times = string.split(":");

            if (times.length == 2) {
                timeSavers.add(new TimeSaver(Integer.parseInt(times[0]), Integer.parseInt(times[1])));
            }

        }

        if (plugin.getConfig().getString("timeType", "minecraft").equalsIgnoreCase("minecraft")) {

            long seconds = (int)(world.getTime()*3.6);
            LocalTime timeOfDay = LocalTime.ofSecondOfDay(seconds);

            this.day = (int) (world.getFullTime()/24000);
            this.hours = timeOfDay.getHour();
            this.minutes = timeOfDay.getMinute();
        } else {
            Date date = new Date();
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            month = calendar.get(Calendar.MONTH);
            hours = calendar.get(Calendar.HOUR_OF_DAY);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            minutes = calendar.get(Calendar.MINUTE);
        }



        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            if (plugin.getConfig().getString("timeType", "minecraft").equalsIgnoreCase("minecraft")) {
                if (!checkMinecraftTime()) return;
            } else {
                if (!checkRealTime()) return;
            }
            for (PlayerSellChest playerSellChest: sellManager.getAllChests()) {

                Player player = Bukkit.getPlayer(playerSellChest.getOwner());
                if (player == null) return;
                if (!player.isOnline()) return;
                sellDrop(playerSellChest, player);
            }




        }, 20L, 20L);
    }

    public boolean checkMinecraftTime() {
        int worldDay = (int) (world.getFullTime()/24000);
        long seconds = (int)(world.getTime()*3.6);

        if (day != worldDay) {
            for (TimeSaver timer: timeSavers) {
                day = worldDay;
                timer.setComplete(false);
            }
        }

        LocalTime timeOfDay = LocalTime.ofSecondOfDay(seconds);

        for (TimeSaver timer: timeSavers) {
            if (timer.isComplete()) continue;

            int x = (int)((timer.getHours()*60*60+timer.getMinutes()*60)/3.6);
            int y = (int)((timeOfDay.getHour()*60*60+timeOfDay.getMinute()*60)/3.6);
            int z = (int)((x-y)*3.6);
            if (z <= 0) {

                timer.setComplete(true);
                this.day = (int) (world.getFullTime()/24000);
                this.hours = timeOfDay.getHour();
                this.minutes = timeOfDay.getMinute();

                return true;
            }
        }
        return false;
    }

    public boolean checkRealTime() {
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        if (day != calendar.get(Calendar.DAY_OF_MONTH)) {
            for (TimeSaver timer: timeSavers) {
                timer.setComplete(false);
            }
        }

        for (TimeSaver timer: timeSavers) {

            if (timer.isComplete()) continue;

            int x = ((timer.getHours() * 60 * 60 + timer.getMinutes() * 60) );
            int y = ((calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 + calendar.get(Calendar.MINUTE) * 60));
            int z = (x-y);


            if (calendar.get(Calendar.DAY_OF_MONTH) > day + 1 || calendar.get(Calendar.MONTH) != month ) {

                timer.setComplete(true);
                month = calendar.get(Calendar.MONTH);
                hours = calendar.get(Calendar.HOUR_OF_DAY);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                minutes = calendar.get(Calendar.MINUTE);

                return true;

            } else  if (z <= 0) {



                timer.setComplete(true);
                month = calendar.get(Calendar.MONTH);
                hours = calendar.get(Calendar.HOUR_OF_DAY);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                minutes = calendar.get(Calendar.MINUTE);

                return true;

            }
        }
        return false;

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

    public static List<TimeSaver> getTimeSavers() {
        return timeSavers;
    }


    public static HashMap<String,TimeSaver> getTimers() {

        HashMap<String,TimeSaver> list = new HashMap<>();

        if (SellBox.getPlugin(SellBox.class).getConfig().getString("timeType", "minecraft").equalsIgnoreCase("minecraft")) {
            long seconds = (int)(world.getTime()*3.6);

            LocalTime timeOfDay = LocalTime.ofSecondOfDay(seconds);

            for (TimeSaver timer: timeSavers) {
                if (timer.isComplete()) continue;
                int x = (int) ((timer.getHours() * 60 * 60 + timer.getMinutes() * 60));
                int y = (int) ((timeOfDay.getHour() * 60 * 60 + timeOfDay.getMinute() * 60));
                int z = (int)((x-y));
                if (z > 0) {
                    list.put(LocalTime.ofSecondOfDay(z).toString(),timer);
                } else {
                    z = (24* 60 * 60)-z;
                    list.put(LocalTime.ofSecondOfDay(z).toString(),timer);
                }
            }

            return list;
        }
        for (TimeSaver timer: timeSavers) {
            Date date = new Date();
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);

            int x = ((timer.getHours() * 60 * 60 + timer.getMinutes() * 60) );
            int y = ((calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 + calendar.get(Calendar.MINUTE) * 60));
            int z = (x-y);

            if (z > 0) {
                list.put(LocalTime.ofSecondOfDay(z).toString(),timer);
            } else {
                z = (24* 60 * 60)+z;
                list.put(LocalTime.ofSecondOfDay(z).toString(),timer);
            }
        }
        return list;
    }
}
