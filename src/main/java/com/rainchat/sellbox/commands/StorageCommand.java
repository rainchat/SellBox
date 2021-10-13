package com.rainchat.sellbox.commands;


import com.rainchat.sellbox.EssentialsWorthConverter;
import com.rainchat.sellbox.SellBox;
import com.rainchat.sellbox.customitem.CustomItem;
import com.rainchat.sellbox.customitem.Flags;
import me.mattstudios.mf.annotations.Alias;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Command("StardewValley")
@Alias({"sv", "sellbox"})
public class StorageCommand extends CommandBase {

    SellBox plugin;

    public StorageCommand(SellBox plugin) {
        this.plugin = plugin;
    }

    @SubCommand("reload")
    @Permission("sellbox.command.reload")
    public void onReload(CommandSender commandSender) {
        commandSender.sendMessage("§aConfig.yml reloaded!");
        plugin.reloadItems();
    }

    @SubCommand("convert")
    public void onConvert(CommandSender commandSender) {
        List<String> log = EssentialsWorthConverter.convert(plugin);
        commandSender.sendMessage(log.toArray(new String[0]));
    }

    @SubCommand("add")
    @Permission("sellbox.command.add")
    public void onAdd(final Player p, final String[] args) {
        if (p.getInventory().getItemInMainHand().getType() != Material.AIR) {
            // item holding
            ItemStack inHand = p.getInventory().getItemInMainHand();
            double price = Double.parseDouble(args[1]);


            CustomItem customItem = new CustomItem(p.getInventory().getItemInMainHand(), price);

            if(args.length > 2) {
                // handling flags
                for(int i = 2; i < args.length; i++) {
                    String flagName = args[i].toUpperCase();
                    try{
                        Flags flag = Flags.valueOf(flagName);
                        customItem.addFlag(flag);
                    }catch(Exception e) {
                        p.sendMessage("§cFlag " + flagName + " not found. Valid flags are:");
                        List<String> flags = Arrays.stream(Flags.values()).map(flag -> flag.name().toLowerCase()).collect(Collectors.toList());
                        p.sendMessage("§a" + String.join(", ", flags));
                        return;
                    }
                }
            }

            Optional<CustomItem> result = plugin.findCustomItem(inHand);
            if(result.isPresent()) {
                plugin.getCustomItems().remove(result.get());
                p.sendMessage("§aSuccessfully updated item:");
            }else {
                p.sendMessage("§aSuccessfully added item:");
            }

            plugin.addCustomItem(customItem);
            plugin.saveMainConfig();

            p.sendMessage(plugin.getCustomItemDescription(customItem, 1).stream().toArray(String[]::new));
            return;
        }

        p.sendMessage("§cYou need to hold an item in your hand!");
    }

}
