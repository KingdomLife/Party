package MM.krookedball;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class Party{
	
	public static boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		final Player player = (Player) sender;
		if(args.length < 1){
			KrookedBall.messageP(player, ChatColor.RED+"Not enough arguments.");
			help(player);
			return true;
		}
		
		if(args[0].equalsIgnoreCase("invite")){
			if(args.length < 2){
				KrookedBall.messageP(player, ChatColor.RED+"Not enough arguments.");
				help(player, "invite");
				return true;
			}
			
			KrookedBall.refreshP();
			
			Player target;
			if(args[1].equalsIgnoreCase("random")){
				if((!player.hasMetadata("partyRandInvCool") || player.getMetadata("partyRandInvCool").size() == 0 || player.getMetadata("partyRandInvCool").get(0).asBoolean())){
					Player[] oPL = Bukkit.getServer().getOnlinePlayers();
					target = oPL[(int)Math.floor(Math.random()*oPL.length)];
					while(target.equals(player)){
						target = oPL[(int)Math.floor(Math.random()*oPL.length)];
					}
					player.setMetadata("partyRandInvCool", new FixedMetadataValue(KrookedBall.plugin, false));
					new BukkitRunnable(){
						public void run(){
							player.setMetadata("partyRandInvCool", new FixedMetadataValue(KrookedBall.plugin, true));
						}
					}.runTaskLater(KrookedBall.plugin, 200);
				}else {
					KrookedBall.messageP(player, ChatColor.RED+"You are still on cooldown for this command!");
					return true;
				}
			}
			
			target = Bukkit.getServer().getPlayer(args[1]);
			
			if(target == null){
				KrookedBall.messageP(player, ChatColor.RED+"Player not found.");
				return true;
			}
			
			if(target.getName().equalsIgnoreCase(player.getName())){
				KrookedBall.messageP(player, ChatColor.RED+"You can't invite yourself to a party!");
				return true;
			}
			
			if(KrookedBall.party.contains("Players."+target.getName()+".party")){
				if(KrookedBall.party.getString("Players."+target.getName()+".party").equalsIgnoreCase(player.getName())){
					KrookedBall.messageP(player, ChatColor.RED+"That player is in your party already!");
				}else {
					KrookedBall.messageP(player, ChatColor.RED+"That player is in a party already!");
				}
				return true;
			}
			
			KrookedBall.party.set("Players."+target.getName()+".invite", player.getName());
			KrookedBall.saveP();
			
			KrookedBall.messageP(player, ChatColor.GRAY+"Invited "+ChatColor.YELLOW+target.getName()+ChatColor.GRAY+" to the party.");
			KrookedBall.messageP(target, ChatColor.YELLOW+player.getName()+ChatColor.GRAY+" has invited you to a party.");
			KrookedBall.messageP(target, ChatColor.GRAY+"Type "+ChatColor.YELLOW+"/party accept"+ChatColor.GRAY+" or "+ChatColor.YELLOW+"/party decline"+ChatColor.GRAY+" to respond.");
			return true;
		}
		else if(args[0].equalsIgnoreCase("accept")){
			KrookedBall.refreshP();
			if(!KrookedBall.party.contains("Players."+player.getName()+".invite")){
				KrookedBall.messageP(player, ChatColor.RED+"You have no pending invites.");
				return true;
			}
			
			String owner = KrookedBall.party.getString("Players."+player.getName()+".invite");
			List<String> members = (ArrayList<String>)KrookedBall.party.getList("Parties."+owner);
			if(members == null){
				members = new ArrayList<String>();
				KrookedBall.party.set("Players."+owner+".party", owner);
			}
			for(int i = 0; i < members.size(); i++){
				Player memberP = Bukkit.getServer().getPlayer(members.get(i));
				if(memberP != null)
					KrookedBall.messageP(memberP, ChatColor.YELLOW+player.getName()+ChatColor.GRAY+" has joined the party.");
			}
			
			Player ownerP = Bukkit.getServer().getPlayer(owner);
			if(ownerP != null)
				KrookedBall.messageP(ownerP, ChatColor.YELLOW+player.getName()+ChatColor.GRAY+" has joined the party.");
			
			members.add(player.getName());
			KrookedBall.party.set("Parties."+owner, members);
			KrookedBall.party.set("Players."+player.getName()+".party", owner);
			KrookedBall.party.set("Players."+player.getName()+".invite", null);
			KrookedBall.saveP();
			
			KrookedBall.messageP(player, ChatColor.GRAY+"Successfully joined "+ChatColor.YELLOW+owner+"'s "+ChatColor.GRAY+"party.");
			
			return true;
		}
		else if(args[0].equalsIgnoreCase("decline")){
			KrookedBall.refreshP();
			if(!KrookedBall.party.contains("Players."+player.getName()+".invite")){
				KrookedBall.messageP(player, ChatColor.RED+"You have no pending invites.");
				return true;
			}
			String owner = KrookedBall.party.getString("Players."+player.getName()+".invite");
			KrookedBall.party.set("Players."+player.getName(), null);
			KrookedBall.saveP();
			
			KrookedBall.messageP(player, ChatColor.GRAY+"Successfully declined "+ChatColor.YELLOW+owner+"'s "+ChatColor.GRAY+"invite.");
			Player ownerP = Bukkit.getServer().getPlayer(owner);
			if(ownerP != null)
				KrookedBall.messageP(ownerP, ChatColor.YELLOW+player.getName()+ChatColor.GRAY+" declined your invite.");
			
			return true;
		}
		else if(args[0].equalsIgnoreCase("info")){
			KrookedBall.refreshP();
			if(!KrookedBall.party.contains("Players."+player.getName()+".party")){
				KrookedBall.messageP(player, ChatColor.RED+"You are not in a party.");
				return true;
			}
			String owner = KrookedBall.party.getString("Players."+player.getName()+".party");
			List<String> members = (ArrayList<String>)KrookedBall.party.getList("Parties."+owner);
			String membs = ChatColor.GRAY + "Members: ";
			for(int i = 0; i < members.size(); i++){
				membs += ChatColor.YELLOW+members.get(i);
				if(i < members.size()-1)
					membs += ChatColor.GRAY + ", ";
			}
			
			KrookedBall.messageP(player, ChatColor.AQUA+"------------------------");
			KrookedBall.messageP(player, ChatColor.GRAY+"Owner: "+ChatColor.YELLOW+owner);
			KrookedBall.messageP(player, membs);
			KrookedBall.messageP(player, ChatColor.AQUA+"------------------------");
			
			return true;
		}
		else if(args[0].equalsIgnoreCase("kick")){
			KrookedBall.refreshP();
			if(args.length < 2){
				KrookedBall.messageP(player, ChatColor.RED+"Not enough arguments.");
				help(player, "kick");
				return true;
			}
			
			if(!KrookedBall.party.contains("Parties."+player.getName())){
				KrookedBall.messageP(player, ChatColor.RED+"You are not the leader of a party.");
				return true;
			}
			
			if(args[1].equalsIgnoreCase(player.getName())){
				KrookedBall.messageP(player, ChatColor.RED+"You can't kick yourself!");
				return true;
			}
			
			Player target = Bukkit.getServer().getPlayer(args[1]);
			//if(target == null){
			//	KrookedBall.messageP(player, ChatColor.RED+"Player not found.");
			//	return true;
			//}
			
			if(!KrookedBall.party.contains("Players."+args[1]+".party") || !KrookedBall.party.getString("Players."+args[1]+".party").equals(player.getName())){
				KrookedBall.messageP(player, ChatColor.RED+"That player is not in your party!");
				return true;
			}
			
			KrookedBall.messageP(player, ChatColor.GRAY+"Successfully kicked "+ChatColor.YELLOW+args[1]+ChatColor.GRAY+" from the party.");
			if(target != null)
				KrookedBall.messageP(target, ChatColor.GRAY+"You have been kicked from "+ChatColor.YELLOW+player.getName()+"'s "+ChatColor.GRAY+"party.");
			
			List<String> members = (ArrayList<String>)KrookedBall.party.getList("Parties."+player.getName());
			for(int i = 0; i < members.size(); i++){
				Player mem = Bukkit.getServer().getPlayer(members.get(i));
				if(mem != null)
					KrookedBall.messageP(mem, ChatColor.GOLD+player.getName()+ChatColor.GRAY+" kicked "+ChatColor.YELLOW+args[1]+ChatColor.GRAY+" from the party.");
			}
			
			members.remove(args[1]);
			if(members.size() == 0){
				KrookedBall.party.set("Parties."+player.getName(), null);
				KrookedBall.party.set("Players."+player.getName()+".party", null);
			}else
				KrookedBall.party.set("Parties."+player.getName(), members);
			KrookedBall.party.set("Players."+args[1]+".party", null);
			KrookedBall.saveP();
			return true;
		}
		else if(args[0].equalsIgnoreCase("leave")){
			KrookedBall.refreshP();
			if(!KrookedBall.party.contains("Players."+player.getName()+".party")){
				KrookedBall.messageP(player, ChatColor.RED+"You are not in a party.");
				return true;
			}
			
			String owner = KrookedBall.party.getString("Players."+player.getName()+".party");
			
			if(!owner.equals(player.getName())){
				KrookedBall.messageP(player, ChatColor.GRAY+"Successfully left "+ChatColor.YELLOW+owner+"'s "+ChatColor.GRAY+"party.");
				
				List<String> members = (ArrayList<String>)KrookedBall.party.getList("Parties."+owner);
				
				for(int i = 0; i < members.size(); i++){
					Player mem = Bukkit.getServer().getPlayer(members.get(i));
					if(mem != null)
						KrookedBall.messageP(mem, ChatColor.YELLOW+player.getName()+ChatColor.GRAY+" left the party.");
				}
				
				members.remove(player.getName());
				if(members.size() == 0){
					KrookedBall.party.set("Parties."+owner, null);
					KrookedBall.party.set("Players."+owner, null);
				}else
					KrookedBall.party.set("Parties."+owner, members);
				KrookedBall.party.set("Players."+player.getName(), null);
				KrookedBall.saveP();
			}else {
				List<String> members = (ArrayList<String>)KrookedBall.party.getList("Parties."+owner);
				
				for(int i = 0; i < members.size(); i++){
					Player mem = Bukkit.getServer().getPlayer(members.get(i));
					if(mem != null)
						KrookedBall.messageP(mem, ChatColor.YELLOW+player.getName()+ChatColor.GRAY+" has disbanded the party.");
					KrookedBall.party.set("Players."+members.get(i), null);
				}
				
				KrookedBall.messageP(player, ChatColor.GRAY+"Successfully disbanded your party.");
				
				KrookedBall.party.set("Players."+owner, null);
				KrookedBall.party.set("Parties."+owner, null);
				KrookedBall.saveP();
			}
			
			return true;
		}
		
		help(player);
		return true;
	}
	
	public static void help(Player p){
		KrookedBall.messageP(p, ChatColor.GRAY+"======================"+ChatColor.GREEN+"==================================");
		KrookedBall.messageP(p, ChatColor.YELLOW+"/party help             "+ChatColor.GRAY+"- Display help page.");
		KrookedBall.messageP(p, ChatColor.YELLOW+"/party invite <player>  "+ChatColor.GRAY+"- Invite player to party.");
		KrookedBall.messageP(p, ChatColor.YELLOW+"/party invite random    "+ChatColor.GRAY+"- Invite random player to party.");
		KrookedBall.messageP(p, ChatColor.YELLOW+"/party accept           "+ChatColor.GRAY+"- Accept last party invite.");
		KrookedBall.messageP(p, ChatColor.YELLOW+"/party decline          "+ChatColor.GRAY+"- Decline all party invites.");
		KrookedBall.messageP(p, ChatColor.YELLOW+"/party info             "+ChatColor.GRAY+"- Display party information.");
		KrookedBall.messageP(p, ChatColor.YELLOW+"/party kick <player>    "+ChatColor.GRAY+"- Kick player from party.");
		KrookedBall.messageP(p, ChatColor.YELLOW+"/party leave            "+ChatColor.GRAY+"- Leave your party.");
		KrookedBall.messageP(p, ChatColor.GRAY+"======================"+ChatColor.GREEN+"==================================");
	}
	
	public static void help(Player p, String with){
		KrookedBall.messageP(p, ChatColor.GRAY+"======================"+ChatColor.GREEN+"==================================");
		if(with.equals("invite")){
			KrookedBall.messageP(p, ChatColor.YELLOW+"/party invite <player>  "+ChatColor.GRAY+"- Invite player to party.");
			KrookedBall.messageP(p, ChatColor.YELLOW+"/party invite random    "+ChatColor.GRAY+"- Invite random player to party.");
		}else if(with.equals("kick"))
			KrookedBall.messageP(p, ChatColor.YELLOW+"/party kick <player>    "+ChatColor.GRAY+"- Kick player from party.");
		KrookedBall.messageP(p, ChatColor.GRAY+"======================"+ChatColor.GREEN+"==================================");
	}
}
