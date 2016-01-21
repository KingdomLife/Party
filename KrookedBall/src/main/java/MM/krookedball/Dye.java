package MM.krookedball;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class Dye implements Listener{
	
	public boolean holdEvalDisp(Player player, String cont){
		if(player.getInventory().getItemInHand() == null || !player.getInventory().getItemInHand().hasItemMeta() || !player.getInventory().getItemInHand().getItemMeta().hasDisplayName()){
			return false;
		}
		
		return player.getInventory().getItemInHand().getItemMeta().getDisplayName().contains(cont);
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent ev){
		if(ev.getAction() == Action.RIGHT_CLICK_AIR || ev.getAction() == Action.RIGHT_CLICK_BLOCK){
			Player player = ev.getPlayer();
			
			boolean bd = holdEvalDisp(player, "Blood Dye");
			boolean wd = holdEvalDisp(player, "Web Dye");
			boolean ib = holdEvalDisp(player, "Ice Bomb");
			boolean pb = holdEvalDisp(player, "Poison Bomb");
			boolean l = holdEvalDisp(player, "Launch");
			
			if(bd || wd || ib || pb || l){
				ev.setCancelled(true);
				Location loc = player.getLocation();
				loc.add(0, 1, 0);
				Snowball snowball = player.getWorld().spawn(loc, Snowball.class);
				snowball.setVelocity(player.getLocation().getDirection().multiply(3));
				snowball.setShooter(player);
				snowball.setMetadata("owner", new FixedMetadataValue(KrookedBall.plugin, player.getName()));
				
				String met = "";
				if(bd)
					met = "Blood Dye";
				else if(wd)
					met = "Web Dye";
				else if(ib)
					met = "Ice Bomb";
				else if(pb)
					met = "Poison Bomb";
				else if(l)
					met = "Launch";
				
				snowball.setMetadata("dyeType", new FixedMetadataValue(KrookedBall.plugin, met));
			}
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent ev){
		Projectile p = ev.getEntity();
		
	    
		if(p instanceof Snowball && p.hasMetadata("dyeType") && p.getMetadata("dyeType").size() > 0){
			Snowball snowball = (Snowball) p;
			Location loca = snowball.getLocation();
			BlockIterator bi = new BlockIterator(loca.getWorld(), loca.toVector(), snowball.getVelocity().normalize(), 0, 4);
		    Block hit = null;
		 
		    while(bi.hasNext())
		    {
		        hit = bi.next();
		        if(hit.getTypeId()!=0){
		            break;
		        }
		    }
			
			String type = p.getMetadata("dyeType").get(0).asString();
			final Location loc = hit.getLocation();
			final Block[][] blocks = new Block[3][3];
			final List<List<List<Object>>> info = new ArrayList<List<List<Object>>>();
			
			for(int x = -1; x <= 1; x++){
				List<List<Object>> row = new ArrayList<List<Object>>();
				for(int z = -1; z <= 1; z++){
					List<Object> dataPair = new ArrayList<Object>();
					Block block = loc.getWorld().getBlockAt(loc.getBlockX()+x, loc.getBlockY(), loc.getBlockZ()+z);
					if(!block.getType().equals(Material.AIR) && !block.getType().equals(Material.WOOL) && !block.getType().equals(Material.WEB)){
						dataPair.add(block.getType().toString());
						dataPair.add(block.getData());
						
						block.setType(Material.WOOL);
						Byte data = 1;
						if(type.equals("Blood Dye")){
							data = DyeColor.RED.getData();
						}else if(type.equals("Web Dye")){
							data = DyeColor.WHITE.getData();
						}else if(type.equals("Ice Bomb")){
							data = DyeColor.LIGHT_BLUE.getData();
						}else if(type.equals("Poison Bomb")){
							data = DyeColor.LIME.getData();
						}else if(type.equals("Launch")){
							data = DyeColor.PURPLE.getData();
						}
						block.setData(data);
						block.setMetadata("dyeType", new FixedMetadataValue(KrookedBall.plugin, type));
						block.setMetadata("owner", new FixedMetadataValue(KrookedBall.plugin, snowball.getMetadata("owner").get(0).asString()));
						block.setMetadata("center", new FixedMetadataValue(KrookedBall.plugin, serializeLoc(loc, true)));
					}
					
					row.add(dataPair);
					
				}
				
				info.add(row);
			}
			
			KrookedBall.refreshD();
			KrookedBall.data.set("Traps."+serializeLoc(loc, true), info);
			KrookedBall.saveD();
			
			new BukkitRunnable(){
				public void run(){
					KrookedBall.refreshD();
					if(KrookedBall.data.contains("Traps."+serializeLoc(loc, true))){
						for(int xx = -1; xx <= 1; xx++){
							for(int zz = -1; zz <= 1; zz++){
								List<Object> dP = info.get(xx+1).get(zz+1);
								if(dP.size() > 0){
									Block block = loc.getWorld().getBlockAt(loc.getBlockX()+xx, loc.getBlockY(), loc.getBlockZ()+zz);
									block.setType(Material.getMaterial((String)dP.get(0)));
									block.setData((Byte)dP.get(1));
									block.removeMetadata("dyeType", KrookedBall.plugin);
									block.removeMetadata("owner", KrookedBall.plugin);
									block.removeMetadata("center", KrookedBall.plugin);
								}
							}
						}
						
						KrookedBall.data.set("Traps."+serializeLoc(loc, true), null);
						KrookedBall.saveD();
					}
				}
			}.runTaskLater(KrookedBall.plugin, 300);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent ev){
		Location ffrom = ev.getFrom();
		Location from = new Location(ffrom.getWorld(), ffrom.getX(),ffrom.getY()-1, ffrom.getZ());
		final Player player = ev.getPlayer();
		if(player.hasMetadata("frozen") && player.getMetadata("frozen").size() > 0 && player.getMetadata("frozen").get(0).asBoolean()){
			player.teleport(ffrom);
			return;
		}
		
		Location tto = ev.getTo();
		Location to = new Location(tto.getWorld(), tto.getX(), tto.getY()-1, tto.getZ());
		if(!from.getBlock().hasMetadata("dyeType") || from.getBlock().getMetadata("dyeType").size() == 0){
			if(to.getBlock().hasMetadata("dyeType") && to.getBlock().getMetadata("dyeType").size() > 0){
				Block block = to.getBlock();
				final Location loc = deserializeLoc(block.getMetadata("center").get(0).asString());
				
				if(player.getName().equals(block.getMetadata("owner").get(0).asString())){ // owner of trap
					player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2));
					if(player.getHealth()+4 > player.getMaxHealth())
						player.setHealth(player.getMaxHealth());
					else
						player.setHealth(player.getHealth()+4);
					
					return;
				}else { // enemy
					String type = to.getBlock().getMetadata("dyeType").get(0).asString();
					
					if(type.equals("Blood Dye")){
						
						final BukkitTask timer = new BukkitRunnable(){
							public void run(){
								player.damage(1);
							}
						}.runTaskTimer(KrookedBall.plugin, 0, 40);
						
						new BukkitRunnable(){
							public void run(){
								timer.cancel();
							}
						}.runTaskLater(KrookedBall.plugin, 240);
						
					}
					else if(type.equals("Web Dye")){
						
						for(int x = -1; x <= 1; x++){
							for(int z = -1; z <= 1; z++){
								Block rBlock = loc.getWorld().getBlockAt(loc.getBlockX()+x, loc.getBlockY()+1, loc.getBlockZ()+z);
								if(rBlock.getType().equals(Material.AIR)){
									rBlock.setType(Material.WEB);
								}
							}
						}
						
						new BukkitRunnable(){
							public void run(){
								for(int x = -1; x <= 1; x++){
									for(int z = -1; z <= 1; z++){
										Block rBlock = loc.getWorld().getBlockAt(loc.getBlockX()+x, loc.getBlockY()+1, loc.getBlockZ()+z);
										if(rBlock.getType().equals(Material.WEB)){
											rBlock.setType(Material.AIR);
										}
									}
								}
							}
						}.runTaskLater(KrookedBall.plugin, 200);
						
					}
					else if(type.equals("Ice Bomb")){
						
						player.setMetadata("frozen", new FixedMetadataValue(KrookedBall.plugin, true));
						player.setWalkSpeed(0f);
						new BukkitRunnable(){
							public void run(){
								player.removeMetadata("frozen", KrookedBall.plugin);
								player.setWalkSpeed(0.2f);
								player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
							}
						}.runTaskLater(KrookedBall.plugin, 60);
						
					}
					else if(type.equals("Poison Bomb")){
						
						player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 400, 1));
						
					}
					else if(type.equals("Launch")){
						
						Vector v = player.getVelocity();
						player.setVelocity(new Vector(v.getX(), 2, v.getZ()));
						player.setMetadata("launched", new FixedMetadataValue(KrookedBall.plugin, true));
						
					}
				}
				
				new BukkitRunnable(){
					public void run(){
						KrookedBall.refreshD();
						List<List<List>> info = (List<List<List>>)KrookedBall.data.get("Traps."+serializeLoc(loc, true));
						for(int x = -1; x <= 1; x++){
							for(int z = -1; z <= 1; z++){
								List<Object> dP = info.get(x+1).get(z+1);
								if(dP.size() > 0){
									Block rBlock = loc.getWorld().getBlockAt(loc.getBlockX()+x, loc.getBlockY(), loc.getBlockZ()+z);
									rBlock.setType(Material.getMaterial((String)dP.get(0)));
									rBlock.setData((Byte)dP.get(0));
									rBlock.removeMetadata("dyeType", KrookedBall.plugin);
									rBlock.removeMetadata("owner", KrookedBall.plugin);
									rBlock.removeMetadata("center", KrookedBall.plugin);
								}
							}
						}
						KrookedBall.data.set(serializeLoc(loc, true), null);
						KrookedBall.saveD();
					}
				}.runTaskLater(KrookedBall.plugin, 100);
				
			}
		}
	}
	
	@EventHandler
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && e.getCause() == DamageCause.FALL && e.getEntity().hasMetadata("launched") && e.getEntity().getMetadata("launched").size() > 0){
        	e.setCancelled(true);
        	((Damageable)e.getEntity()).damage(6);
        	e.getEntity().removeMetadata("launched", KrookedBall.plugin);
        }
	}
	
	public String serializeLoc(Location loc, boolean blockLocs){
		if(blockLocs)
			return (loc.getWorld().getName()+";"+loc.getBlockX()+";"+loc.getBlockY()+";"+loc.getBlockZ()).replace('.', ',');
		else
			return (loc.getWorld().getName()+";"+loc.getX()+";"+loc.getY()+";"+loc.getZ()).replace('.', ',');
	}
	
	public Location deserializeLoc(String sLoc){
		String[] arr = sLoc.replace(',', '.').split(";");
		return new Location(Bukkit.getServer().getWorld(arr[0]), Double.parseDouble(arr[1]), Double.parseDouble(arr[2]), Double.parseDouble(arr[3]));
	}
}
