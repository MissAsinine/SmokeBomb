package com.Pride.korra.SmokeBomb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ParticleEffect;

	public class SmokeBomb extends ChiAbility implements AddonAbility {
	
	private static final Map<Integer, SmokeBomb> SNOWBALLS = new ConcurrentHashMap<>();
	private static final Map<String, Long> BLINDED_TIMES = new ConcurrentHashMap<>();
	private static final Map<String, SmokeBomb> BLINDED_TO_ABILITY = new ConcurrentHashMap<>();
	
	public long cooldown;
	public static List<SmokeBombs> bombs = new ArrayList<SmokeBombs>();
	private int startAmount;
	private int duration;
	private double radius;

	public int amount;

	public SmokeBomb(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		this.cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Prride.SmokeBomb.Cooldown");
		this.duration = ConfigManager.getConfig().getInt("ExtraAbilities.Prride.SmokeBomb.BlindDuration");
		this.radius = ConfigManager.getConfig().getDouble("ExtraAbilities.Prride.SmokeBomb.BombRadius");
		this.startAmount = ConfigManager.getConfig().getInt("ExtraAbilities.Prride.SmokeBomb.StartAmount");
		amount = startAmount;
		start();
	}
	
	public class SmokeBombs {
		
		private final Map<Integer, SmokeBomb> SNOWBALLS = new ConcurrentHashMap<>();
		private Ability ability;
		private Player player;
		
		
		public SmokeBombs(Ability ability, Player player) {
			this.ability = ability;
			this.player = player;
		}
		
		public boolean progress(){
			if(player.isDead() || !player.isOnline()){
				return false;
			}
			SNOWBALLS.put(player.launchProjectile(Snowball.class).getEntityId(), (SmokeBomb) ability);
			return true;
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void playEffect(Location loc) {
		int z = -2;
		int x = -2;
		int y = 0;

		for (int i = 0; i < 125; i++) {
			Location newLoc = new Location(loc.getWorld(), loc.getX() + x, loc.getY() + y, loc.getZ() + z);
			for (int direction = 0; direction < 8; direction++) {
				loc.getWorld().playEffect(newLoc, Effect.POTION_SWIRL, direction);
			}
			if (z == 2) {
				z = -2;
			}
			if (x == 2) {
				x = -2;
				z++;
			}
			x++;
		}
	}

	public void applyBlindness(Entity entity) {
		if (entity instanceof Player) {
			if (Commands.invincible.contains(((Player) entity).getName())) {
				return;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
				return;
			}
			Player p = (Player) entity;
			p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, 2));
			p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, duration, 2));
			BLINDED_TIMES.put(p.getName(), System.currentTimeMillis());
			BLINDED_TO_ABILITY.put(p.getName(), this);
		}
	}

	public static void removeFromHashMap(Entity entity) {
		if (entity instanceof Player) {
			Player p = (Player) entity;
			if (BLINDED_TIMES.containsKey(p.getName())) {
				SmokeBomb smokebomb = BLINDED_TO_ABILITY.get(p.getName());
				if (BLINDED_TIMES.get(p.getName()) + smokebomb.duration >= System.currentTimeMillis()) {
					BLINDED_TIMES.remove(p.getName());
					BLINDED_TO_ABILITY.remove(p.getName());
				}
			}
		}
	}
	
	public static void smokeBomb(Player player) {
		SmokeBomb sb = getAbility(player, SmokeBomb.class);
		if (sb != null) {
			sb.smokeBomb();
		}
	}
	
	public void smokeBomb() {
		if (amount >= 1) {
			if (--amount <= 0) {
				bPlayer.addCooldown(this);
			}
			SNOWBALLS.put(player.launchProjectile(Snowball.class).getEntityId(), this);	
			return;
		}
	}

	public Location getRightHandPos(){
		return GeneralMethods.getRightSide(player.getLocation(), .55).add(0, 1.2, 0);
	}

	private void displayBombs(){
	ParticleEffect.CRIT.display(getRightHandPos().toVector().add(player.getEyeLocation().getDirection().clone().multiply(.8D)).toLocation(player.getWorld()), 0.1F, 0.1F, 0.1F, 0.01F, 15);
	}
	
	public static void progressBombs() {
		List<Integer> ids = new ArrayList<Integer>();
		for (SmokeBombs sb : bombs) {
			if (!sb.progress()) {
				ids.add(bombs.indexOf(sb));
			}
		}
		for (int id : ids) {
			if (id >= bombs.size()) {
				continue;
			}
			bombs.remove(id);
		}
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "SmokeBomb";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public void progress() {
		if(player.isDead() || !player.isOnline()){
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			amount = 0;
			if (!bPlayer.isOnCooldown(this)) {
				bPlayer.addCooldown(this);
			}
		}

		for (Iterator<SmokeBombs> iterator = bombs.iterator(); iterator.hasNext();) {
			SmokeBombs bomb = iterator.next();

			if (!bomb.progress()) {
				iterator.remove();
			}
		}

		if (amount <= 0 && bombs.isEmpty()) {
			remove();
			return;
		}

		if (amount > 0) {
			displayBombs();
		}
	}
	
	public static Map<Integer, SmokeBomb> getSnowballs() {
		return SNOWBALLS;
	}

	public static Map<String, Long> getBlindedTimes() {
		return BLINDED_TIMES;
	}

	public static Map<String, SmokeBomb> getBlindedToAbility() {
		return BLINDED_TO_ABILITY;
	}
	
	public double getRadius() {
		return radius;
	}

	@Override
	public String getAuthor() {
		return "Prride and MrAsinine";
	}

	@Override
	public String getVersion() {
		return "Build v1.0";
	}

	@Override
	public void load() {
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new SmokeBombListener(), ProjectKorra.plugin);
		ProjectKorra.log.info(getName() + " " + getVersion() + " by " + getAuthor() + " loaded! ");
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Prride.SmokeBomb.Cooldown", 5500);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Prride.SmokeBomb.BlindDuration", 150);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Prride.SmokeBomb.BombRadius", 4);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Prride.SmokeBomb.StartAmount", 5);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		
	}

}
