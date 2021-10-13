package com.rainchat.sellbox;

import com.rainchat.sellbox.customitem.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class EssentialsWorthConverter {
	

	public static List<String> convert(SellBox plugin){
		List<String> output = new ArrayList<String>();
		
		if(!Bukkit.getPluginManager().isPluginEnabled("Essentials")){
			output.add("§cEssentials not installed!");
			return output;
		}
		
		File worthFile = new File("plugins/Essentials/worth.yml");
		
		if(!worthFile.exists()) {
			output.add("§cWorth file does not exists! (" + worthFile.getAbsolutePath() + ")");
			return output;
		}
		
		FileConfiguration worthcfg = YamlConfiguration.loadConfiguration(worthFile);
		int count = 0;
		
		Map<String, Material> stupidEssentialsRenamings = Arrays.stream(Material.values()).
				collect(Collectors.toMap(m -> m.name().replace("_", "").toLowerCase(), m -> m));
		
		for(String itemName : worthcfg.getConfigurationSection("worth").getKeys(false)) {
			double price = worthcfg.getDouble("worth." + itemName);
			Material mat = stupidEssentialsRenamings.getOrDefault(itemName, null);

			if(mat == null) {
				output.add("§cError: Material for item name " + itemName + " not found, continue...");
				continue;
			}
			Optional<CustomItem> customItemOpt = plugin.findCustomItem(new ItemStack(mat));
			
			if(customItemOpt.isPresent()) {
				CustomItem customItem = customItemOpt.get();
				customItem.setPrice(price);
			}else {
				CustomItem customItem = new CustomItem(new ItemStack(mat), price);
				plugin.addCustomItem(customItem);	
			}
			count++;
			
		}

		plugin.saveMainConfig();
		output.add(String.format("§aSuccessfully converted §c%d §aitems!", count));
		
		return output;
	}
}
