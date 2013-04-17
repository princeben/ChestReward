package fr.princeben.ChestReward;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GestionEvent implements Listener {

	File fichier_config = new File("plugins"+File.separator+"ChestReward"+File.separator+"chest.yml");
	FileConfiguration config = YamlConfiguration.loadConfiguration(fichier_config);
	public Logger log = Logger.getLogger("minecraft");
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) 
	{
		Block bl = e.getClickedBlock();
		Player pl = e.getPlayer();
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && bl.getTypeId() == 54) {
			
			if (!pl.hasPermission("ChestReward.open") && !pl.isOp())
			{
				e.setCancelled(true);
				pl.sendMessage(ChatColor.RED +"Tu n'as pas la permission pour ouvrir des ChestReward.");
				return;
			}
			
			File fichier_config = new File("plugins"+File.separator+"ChestReward"+File.separator+"chest.yml");
			FileConfiguration config = YamlConfiguration.loadConfiguration(fichier_config);
			
			String location = bl.getLocation().getWorld().getName()+"/"+bl.getLocation().getBlockX()+"/"+bl.getLocation().getBlockY()+"/"+bl.getLocation().getBlockZ();
			log.info(location);
			if (config.contains(location))
			{
				List<String> playerlist = config.getStringList(location+".players");
				if (playerlist.contains(pl.getName()) && config.getString(location+".creator") != pl.getName())
				{
					e.setCancelled(true);
					pl.sendMessage(ChatColor.GREEN + "[ChestReward] "+ "Vous avez déjà ouvert ce coffre !");
					return;
				}
				Inventory chestinv = ((Chest) bl.getState()).getInventory();
				chestinv.clear();
				boolean fin = true;
				int nb = 0;
				while (fin)
				{
					int amount = config.getInt(location+".items."+nb+".amount");
					if (amount > 0) {
						int type = config.getInt(location+".items."+nb+".type");
						Short damage = (short) config.getInt(location+".items."+nb+".damage");
						chestinv.addItem(new ItemStack(type,amount,damage));
					}
					nb += 1;
					if (!config.contains(location+".items."+nb))
					{
						fin = false;
					}
				}
				if (Core.isVault) {
					int money = config.getInt(location+".money");
					if (money > 0) 
					{
						Core.economy.depositPlayer(pl.getName(), money);
						pl.sendMessage(ChatColor.GREEN + "[ChestReward] "+ "Vous avez gagné "+money+" "+ Core.economy.currencyNamePlural());
					}
				}
				
				playerlist.add(pl.getName());
				config.set(location+".players", playerlist);
			    try {
					config.save(fichier_config);
				} catch (IOException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				}
			}
			log.info("il n'existe pas");
			
			
		}
	}
}