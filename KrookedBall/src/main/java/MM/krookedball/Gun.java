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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;


public class Gun implements Listener{
	ItemStack basicAmmo;
	ItemStack superAmmo;
	ItemStack fireAmmo;
	ItemStack advancedAmmo;
	ItemStack speedAmmo;
	
	public Gun(){
		basicAmmo = createItem(Material.SNOW_BALL, ChatColor.WHITE+"Basic Gun Ammo");
		superAmmo = createItem(Material.SNOW_BALL, ChatColor.YELLOW+"Super Gun Ammo");
		fireAmmo = createItem(Material.FIREBALL, ChatColor.RED+"Fire Gun Ammo");
		advancedAmmo = createItem(Material.SNOW_BALL, ChatColor.WHITE+"Advanced Gun Ammo");
		speedAmmo = createItem(Material.SNOW_BALL, ChatColor.WHITE+"Speed Gun Ammo");
	}
	
	private ItemStack createItem(Material material, String display){
		ItemStack item = new ItemStack(material);
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(display);
		item.setItemMeta(im);
		return item;
	}
	
	private boolean holdEvalDisp(Player player, String cont){
		if(player.getInventory().getItemInHand() == null || !player.getInventory().getItemInHand().hasItemMeta() || !player.getInventory().getItemInHand().getItemMeta().hasDisplayName()){
			return false;
		}
		
		return player.getInventory().getItemInHand().getItemMeta().getDisplayName().contains(cont);
	}
	
	private boolean hasMetadata(Entity ent, String metadata){
		return (ent.hasMetadata(metadata) && ent.getMetadata(metadata).size() > 0);
	}
	
	private boolean hasAmmo(Player player, ItemStack ammo){
		return player.getInventory().contains(ammo);
	}
	
	private void takeAmmo(Player player, ItemStack ammo){
		for(ItemStack item : player.getInventory().getContents()){
			if(item.equals(ammo)){
				if(item.getAmount() == 1)
					player.getInventory().remove(item);
				else 
					item.setAmount(item.getAmount()-1);
				player.updateInventory();
				
				return;
			}
		}
	}
	
	
	@EventHandler
	public void onClick(PlayerInteractEvent ev){
		if(ev.getAction().equals(Action.RIGHT_CLICK_AIR) || ev.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			final Player player = ev.getPlayer();
			
			if(holdEvalDisp(player, "Basic Gun") && (hasAmmo(player, basicAmmo) || hasAmmo(player, speedAmmo))){
				if(!hasMetadata(player, "basicguncool") || player.getMetadata("basicguncool").get(0).asBoolean()){
					ev.setCancelled(true);
					Location loc = player.getLocation();
					loc.add(0, 1, 0);
					Snowball snowball = player.getWorld().spawn(loc, Snowball.class);
					snowball.setVelocity(player.getLocation().getDirection().multiply(3));
					snowball.setShooter(player);
					snowball.setMetadata("damage", new FixedMetadataValue(KrookedBall.plugin, 2));
					
					if(hasAmmo(player, basicAmmo))
						takeAmmo(player, basicAmmo);
					else
						takeAmmo(player, speedAmmo);
					
					player.setMetadata("basicguncool", new FixedMetadataValue(KrookedBall.plugin, false));
					new BukkitRunnable(){
						public void run(){
							player.setMetadata("basicguncool", new FixedMetadataValue(KrookedBall.plugin, true));
						}
					}.runTaskLater(KrookedBall.plugin, 20);
				}
			}
			else if(holdEvalDisp(player, "Super Gun") && hasAmmo(player, superAmmo)){
				ev.setCancelled(true);
				Location loc = player.getLocation();
				loc.add(0, 1, 0);
				Snowball snowball = player.getWorld().spawn(loc, Snowball.class);
				snowball.setVelocity(player.getLocation().getDirection().multiply(3));
				snowball.setShooter(player);
				snowball.setMetadata("damage", new FixedMetadataValue(KrookedBall.plugin, 3));
				
				takeAmmo(player, superAmmo);
			}
			else if(holdEvalDisp(player, "Fire Gun") && hasAmmo(player, fireAmmo)){
				if(!hasMetadata(player, "fireguncool") || player.getMetadata("fireguncool").get(0).asBoolean()){
					ev.setCancelled(true);
					Location loc = player.getLocation();
					loc.add(0, 1, 0);
					Fireball fireball = player.getWorld().spawn(loc, Fireball.class);
					fireball.setVelocity(player.getLocation().getDirection().multiply(5));
					fireball.setShooter(player);
					fireball.setMetadata("noplode", new FixedMetadataValue(KrookedBall.plugin, true));
					fireball.setIsIncendiary(false);
					fireball.setYield(2);
					fireball.setMetadata("damage", new FixedMetadataValue(KrookedBall.plugin, 8));
					
					takeAmmo(player, fireAmmo);
					
					player.setMetadata("fireguncool", new FixedMetadataValue(KrookedBall.plugin, false));
					new BukkitRunnable(){
						public void run(){
							player.setMetadata("fireguncool", new FixedMetadataValue(KrookedBall.plugin, true));
						}
					}.runTaskLater(KrookedBall.plugin, 60);
				}
			}
			else if(holdEvalDisp(player, "Advanced Gun") && hasAmmo(player, advancedAmmo)){
				ev.setCancelled(true);
				Location loc = player.getLocation();
				loc.add(0, 1, 0);
				Snowball snowball = player.getWorld().spawn(loc, Snowball.class);
				snowball.setVelocity(player.getLocation().getDirection().multiply(3));
				snowball.setShooter(player);
				snowball.setMetadata("damage", new FixedMetadataValue(KrookedBall.plugin, 4));
				snowball.setMetadata("effect", new FixedMetadataValue(KrookedBall.plugin, "slowness-4,weakness-4"));
				
				takeAmmo(player, advancedAmmo);
			}
			else if(holdEvalDisp(player, "Speed Gun") && hasAmmo(player, speedAmmo)){
				ev.setCancelled(true);
				Location loc = player.getLocation();
				loc.add(0, 1, 0);
				Arrow arrow = player.getWorld().spawn(loc, Arrow.class);
				arrow.setVelocity(player.getLocation().getDirection().multiply(3));
				arrow.setShooter(player);
				arrow.setMetadata("effect", new FixedMetadataValue(KrookedBall.plugin, "speed(4)-2,heal(2)"));
				
				takeAmmo(player, speedAmmo);
				
				ItemStack item = new ItemStack(Material.DIAMOND_BARDING);
				ItemMeta im = item.getItemMeta();
				im.setDisplayName(ChatColor.WHITE+"Basic Gun");
				item.setItemMeta(im);
				
				player.getInventory().removeItem(player.getInventory().getItemInHand());
				player.getInventory().addItem(item);
				player.updateInventory();
			}
			else if(holdEvalDisp(player, "Snowball Grenade")){
				ev.setCancelled(true);
				Location loc = player.getLocation();
				loc.add(0, 1, 0);
				Snowball snowball = player.getWorld().spawn(loc, Snowball.class);
				snowball.setVelocity(player.getLocation().getDirection().multiply(3));
				snowball.setShooter(player);
				snowball.setMetadata("grenade", new FixedMetadataValue(KrookedBall.plugin, true));
				
				player.getInventory().removeItem(player.getInventory().getItemInHand());
				player.updateInventory();
			}
			
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
	    Entity ent = event.getEntity();
	   
	    if (ent instanceof Fireball && ent.hasMetadata("noplode")) {
	        event.setCancelled(true); //Removes block damage
	    }
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent ev){
		Entity proj = ev.getEntity();
		if(proj instanceof Snowball && hasMetadata(proj, "grenade")){
			Location loc = proj.getLocation();
			loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0f, true, false);
			for(Entity ent : loc.getWorld().getEntities()){
				Location entLoc = ent.getLocation();
				if(entLoc.distance(loc) <= 3 && ent instanceof Damageable){
					((Damageable)ent).damage(4);
					ent.setVelocity(new Vector(3-Math.abs(entLoc.getX()-loc.getX()), 3-Math.abs(entLoc.getY()-loc.getY()), 3-Math.abs(entLoc.getZ()-loc.getZ())));
				}
			}
		}
	}
	
	@EventHandler
	public void damage(EntityDamageByEntityEvent ev){
		Entity victim = ev.getEntity();
		Entity damager = ev.getDamager();
		
		if(victim instanceof LivingEntity && hasMetadata(damager, "grenade")){
			ev.setCancelled(true);
			Location loc = victim.getLocation();
			loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0f, true, false);
			for(Entity ent : loc.getWorld().getEntities()){
				Location entLoc = ent.getLocation();
				if(entLoc.distance(loc) <= 3 && ent instanceof Damageable){
					((Damageable)ent).damage(4);
					ent.setVelocity(new Vector(3-Math.abs(entLoc.getX()-loc.getX()), 3-Math.abs(entLoc.getY()-loc.getY()), 3-Math.abs(entLoc.getZ()-loc.getZ())));
				}
			}
		}
		
		if(victim instanceof LivingEntity && hasMetadata(damager, "damage")){
			ev.setCancelled(true);
			((Damageable)victim).damage(damager.getMetadata("damage").get(0).asDouble());
		}
		
		if(victim instanceof LivingEntity && hasMetadata(damager, "effect")){
			String effect = damager.getMetadata("effect").get(0).asString();
			LivingEntity vic = (LivingEntity) victim;
			
			if(effect.equals("slowness-4,weakness-4")){
				vic.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 1));
				vic.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));
			}
			else if(effect.equals("speed(4)-2,heal(2)")){
				vic.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 4));
				if(vic.getHealth()+4.0 >= vic.getMaxHealth())
					vic.setHealth(vic.getMaxHealth());
				else
					vic.setHealth(vic.getHealth()+4.0);
			}
		}
	}
	
}
