package MM.krookedball;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class KrookedBall extends JavaPlugin implements Listener{
	public static Plugin plugin;
	private static String prefix = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "[" + ChatColor.GOLD + "KrookedBall" + ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "] ";
	private static File configfile;
	private static File partyFile;
	private static File dataFile;
	public static FileConfiguration config;
	public static FileConfiguration party;
	public static FileConfiguration data;
	
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(new Dye(), this);
		this.getServer().getPluginManager().registerEvents(new Gun(), this);
		
		
		
		try{
            if(!getDataFolder().exists())getDataFolder().mkdir();
            partyFile = new File(getDataFolder(), "party.yml");
            if (!partyFile.exists())partyFile.createNewFile();
            dataFile = new File(getDataFolder(), "data.yml");
            if (!dataFile.exists())dataFile.createNewFile();
        } catch (IOException e){
            e.printStackTrace();
        }
		
		getLogger().info("KrookedBall enabled!");
		
		plugin = this;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	
		if(cmd.getName().equalsIgnoreCase("party"))
			return Party.onCommand(sender, cmd, label, args);
		
		return false;
	}
	
	public static void refreshP(){
		party = YamlConfiguration.loadConfiguration(partyFile);
	}
	
	public static void refreshD(){
		data = YamlConfiguration.loadConfiguration(dataFile);
	}
	
	public static void saveP(){
		try {
			party.save(partyFile);
		} catch(IOException e) {
			  e.printStackTrace();
		}
	}
	
	public static void saveD(){
		try {
			data.save(dataFile);
		} catch(IOException e) {
			  e.printStackTrace();
		}
	}
	
	public static void messageP(Player player, String message){
		player.sendMessage(prefix+message);
	}
	
	public static void messageAll(String message){
		for(Player player : Bukkit.getServer().getOnlinePlayers())
			player.sendMessage(prefix+message);
		plugin.getLogger().info(prefix+message);
	}
	
	/*@EventHandler
	public void onPlayerLeave(PlayerQuitEvent ev){
		refreshP();
		Player player = ev.getPlayer();
		String owner = party.getString("Players."+player.getName()+".party");
		if(owner != null && owner.equals(player.getName())){
			
		}
	}*/
}
