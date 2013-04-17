package fr.princeben.ChestReward;

import java.io.File;
import java.io.IOException;
import java.util.ListIterator;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import fr.princeben.ChestReward.GestionEvent;

public class Core extends JavaPlugin{

	File fichier_config = new File("plugins"+File.separator+"ChestReward"+File.separator+"chest.yml");
	FileConfiguration config = YamlConfiguration.loadConfiguration(fichier_config);
	public Logger log = Logger.getLogger("minecraft");
	private GestionEvent EventListener = null;
	public static Boolean isVault = null;
	public static Economy economy = null;
	
	private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
	
	@Override
	public void onEnable()
	{
		saveDefaultConfig();
		isVault = getConfig().getBoolean("Vault");
		if (!fichier_config.exists()) {
			try {
				fichier_config.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (isVault) {
			setupEconomy();	
		}
		EventListener = new GestionEvent();
		log.info("[ChestReward] by Princeben");
		getServer().getPluginManager().registerEvents(EventListener, this);
		
		try {
		    MetricsLite metrics = new MetricsLite(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
		
	}
	
	@Override
	public void onDisable()
	{
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[]args){
		Player player = (Player) sender;
		Block bl = player.getTargetBlock(null, 5);
		String location = bl.getLocation().getWorld().getName()+"/"+bl.getLocation().getBlockX()+"/"+bl.getLocation().getBlockY()+"/"+bl.getLocation().getBlockZ();
		if(label.equalsIgnoreCase("ChestReward"))
		{
			if (!player.hasPermission("ChestReward.create") && !player.isOp())
			{
				player.sendMessage(ChatColor.RED +"Tu n'as pas la permission pour définir des ChestReward.");
				return true;
			}
			if(args.length != 0)
			{
			
			if(args[0].equalsIgnoreCase("Remove"))
			{
				if(bl.getTypeId() != 54) 
				{
					player.sendMessage(ChatColor.GREEN + "[ChestReward] "+ "Ce block n'est pas un coffre.");
					return true;
				}
				
				config.set(location+".active", false);
				player.sendMessage(ChatColor.GREEN + "[ChestReward] "+ "Suppression effectué");
				return true;
			}
			
			if(args[0].equalsIgnoreCase("setMoney"))
			{
				if(bl.getTypeId() != 54) 
				{
					player.sendMessage(ChatColor.GREEN + "[ChestReward] "+ "Ce block n'est pas un coffre.");
					return true;
				}
				
				if (args.length != 2 || !isVault)
				{
					player.sendMessage(ChatColor.RED + "Précise un montant et utilise Vault");
					return true;
				}
				
				config.set(location+".money", args[1]);
			    try {
					config.save(fichier_config);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    player.sendMessage(ChatColor.GREEN + "[ChestReward] "+ "Ce coffre dispose maintenant d'une récompense de "+args[1]);
				return true;
			}
			
			if(args[0].equalsIgnoreCase("setChest"))
			{
				
				if(bl.getTypeId() != 54) 
				{
					player.sendMessage(ChatColor.GREEN + "[ChestReward] "+ "Ce block n'est pas un coffre.");
					return true;
				}
				
				int nb = 0;
				ListIterator<ItemStack> inv = ((Chest) bl.getState()).getInventory().iterator();
				
				while(inv.hasNext())
				{
					ItemStack next = inv.next();
					if (next != null)
					{
						ItemStack item = next;
						config.set(location+".items."+nb+".type", item.getTypeId());
						config.set(location+".items."+nb+".amount", item.getAmount());
						config.set(location+".items."+nb+".damage", item.getDurability());
					}
					else {
						config.set(location+".items."+nb+".amount", 0);
					}
					nb += 1;
				}
				
				config.set(location+".creator", player.getName());
				config.set(location+".players", null);
				config.set(location+".active", true);
				config.set(location+".money", 0);
				    try {
						config.save(fichier_config);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				player.sendMessage(ChatColor.GREEN + "[ChestReward] "+ "Ce coffre est maintenant un ChestReward !");
				return true;
			}
			}
		player.sendMessage(ChatColor.GREEN + "[ChestReward] Liste des Commandes");
		player.sendMessage(ChatColor.GREEN + "/ChestReward setChest");
		player.sendMessage(ChatColor.GREEN + "/ChestReward setMoney <montant>");
		}
		return true;
		 
	}
	
}
