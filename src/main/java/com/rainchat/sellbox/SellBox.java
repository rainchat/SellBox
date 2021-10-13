package com.rainchat.sellbox;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rainchat.sellbox.commands.PlayerCommands;
import com.rainchat.sellbox.commands.StorageCommand;
import com.rainchat.sellbox.customitem.CustomItem;
import com.rainchat.sellbox.data.PlayerSellChest;
import com.rainchat.sellbox.listeners.ChestCheck;
import com.rainchat.sellbox.managers.SellManager;
import com.rainchat.sellbox.utils.ChestItem;
import com.rainchat.sellbox.utils.Utils;
import me.mattstudios.mf.base.CommandManager;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class SellBox extends JavaPlugin {

	/*
	 * Vault Economy plugin
	 */
	private Economy economy = null;

	/*
	 * Main config.yml
	 */
	private FileConfiguration cfg;

	/*
	 * List that contains all information about sell items
	 */
	private List<CustomItem> customItems = new ArrayList<CustomItem>();
	
	/*
	 * Gson object for serializing processes
	 */
	private Gson gson = new Gson();

	private SellManager sellManager;

	@Override
	public void onEnable() {
		setupEconomy();
		
		this.saveDefaultConfig();
		this.loadItems();

		ChestItem chestItem = new ChestItem(this);

		sellManager = new SellManager(this);
		sellManager.load(new TypeToken<Set<PlayerSellChest>>(){}.getType());

		CommandManager commandManager = new CommandManager(this);
		commandManager.register(new StorageCommand(this), new PlayerCommands(sellManager,this));

		this.getServer().getPluginManager().registerEvents(new ChestCheck(this, sellManager), this);

	}
	
	@Override
	public void onDisable() {
		sellManager.unload();
	}

	public List<String> getCustomItemDescription(CustomItem item, int amount){
		return getCustomItemDescription(item, amount, cfg.getString("messages.item-enumeration-format").replace("&", "§"));
	}

	public List<String> getCustomItemDescription(CustomItem item, int amount, String itemEnumFormat){
		List<String> list = new ArrayList<String>();

		String s = itemEnumFormat.replace("%amount%", amount + "")
				.replace("%material%", item.getDisplayname() == null ? WordUtils.capitalize(item.getMaterial().toLowerCase().replace("_", " ")) : item.getDisplayname())
				.replace("%price%", economy.format(item.getPrice() * amount));
		list.add(s);

		// adding enchantements
		item.getEnchantements().forEach((enchantement, level) -> {
			list.add(String.format("§7%s %s", WordUtils.capitalize(enchantement), Utils.toRoman(level)));
		});

		item.getFlags().forEach(flag -> {
			list.add(String.format("§e%s", flag.name().toLowerCase()));
		});

		return list;
	}

	/**
	 * Saved all Custom items to the config.
	 */
	public void saveMainConfig() {
		List<CustomItem> advancedItems = new ArrayList<CustomItem>();
		List<String> simpleItems = new ArrayList<String>();
		
		for(CustomItem customItem : customItems) {
			if(customItem.isSimpleItem()) {
				simpleItems.add(customItem.getMaterial() + ":" + customItem.getPrice());
			}else {
				advancedItems.add(customItem);
			}
		}
		cfg.set("sell-prices-simple", simpleItems);
		cfg.set("sell-prices", gson.toJson(advancedItems));
		this.saveConfig();
	}

	public HashMap<CustomItem, Integer> getSalableItems(ItemStack[] is) {
		HashMap<CustomItem, Integer> customItemsMap = new HashMap<CustomItem, Integer>();
		for (ItemStack stack : is) {
			if (stack != null) {
				if (stack.getType().toString().toUpperCase().contains("SHULKER_BOX")) {
					Inventory container = ((InventoryHolder) ((BlockStateMeta) stack.getItemMeta()).getBlockState()).getInventory();
					for (int j = 0; j < container.getSize(); j++) {
						ItemStack shulkerItem = container.getItem(j);
						if (shulkerItem != null && !shulkerItem.getType().equals(Material.AIR)) {
							Optional<CustomItem> opt = findCustomItem(shulkerItem);
							if(opt.isPresent() && this.isSalable(shulkerItem)) {
								// add item to map
								customItemsMap.compute(opt.get(), (k, v) -> v == null ? shulkerItem.getAmount() : v + shulkerItem.getAmount());
							}
						}
					}										
				} else {
					// check if item is in the custom item list
					Optional<CustomItem> opt = findCustomItem(stack);
					if(opt.isPresent() && this.isSalable(stack)) {
						// add item to map
						customItemsMap.compute(opt.get(), (k, v) -> v == null ? stack.getAmount() : v + stack.getAmount());
					}
				}		
			}
		}
		return customItemsMap;
	}
	
	/**
	 * Finds the representing Custom Item for a certain Item Stack
	 * @param stack
	 * @return
	 */
	public Optional<CustomItem> findCustomItem(ItemStack stack) {
		return customItems.stream().filter((item) -> item.matches(stack)).findFirst();
	}

	public double calcWorthOfContent(ItemStack[] content) {
		HashMap<CustomItem, Integer> salable = getSalableItems(content);
		return salable.keySet().stream().mapToDouble(v -> v.getPrice() * salable.get(v)).sum();
	}
	
	public boolean isSalable(ItemStack is) {
		if(is == null || is.getType() == null || is.getType() == Material.AIR) return false;
		Optional<CustomItem> customItemOptional = this.findCustomItem(is);
		if(customItemOptional.isPresent()) {
			if(customItemOptional.get().getPrice() > 0d) {
				return true;
			}
		}
		return false;
	}

	public Economy getEconomy() {
		return economy;
	}

	public List<CustomItem> getCustomItems() {
		return customItems;
	}

	boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	public void addCustomItem(CustomItem i) {
		customItems.add(i);
	}

	
	/**
	 * Loads all item configurations from the config.yml
	 */
	private void loadItems() {
		this.cfg = this.getConfig();
		
		if(this.cfg.getString("sell-prices") != null) {
			customItems = gson.fromJson(cfg.getString("sell-prices"), new TypeToken<List<CustomItem>>(){}.getType());
		}
		
		// converting simple items to custom items
		if(this.cfg.contains("sell-prices-simple")) {
			for(String entry : this.cfg.getStringList("sell-prices-simple")) {
				try {
					CustomItem ci = new CustomItem(new ItemStack(Material.valueOf(entry.split(":")[0])), Double.parseDouble(entry.split(":")[1]));
					customItems.add(ci);
				}catch(Exception e) {
					System.out.println("Error in config.yml: " + entry);
				}
			}
		}else {
			// adding default materials
			List<String> entries = Arrays.stream(Material.values()).map(v -> v.name() + ":0.0").collect(Collectors.toList());
			this.cfg.set("sell-prices-simple", entries);
			this.saveConfig();
			for(Material mat : Material.values()) {
				customItems.add(new CustomItem(new ItemStack(mat), 0d));
			}
		}
	}
	
	/**
	 * @return amount of configured custom items
	 */
	public long getCustomItemCount() {
		return customItems.stream().filter(p -> !p.isSimpleItem()).count();
	}
	
	public void reloadItems() {
		this.reloadConfig();
		loadItems();
		
	}
}
