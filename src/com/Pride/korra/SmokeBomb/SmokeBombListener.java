package com.Pride.korra.SmokeBomb;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class SmokeBombListener implements Listener {
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		if (event.isCancelled()) {
			return;
			
		} else if (CoreAbility.hasAbility(event.getPlayer(), SmokeBomb.class)) {
			return;
			
		}
		new SmokeBomb(event.getPlayer());
	}
	
	@EventHandler
	public void onSwing(PlayerAnimationEvent e) {
		if (e.isCancelled()) return;
		
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(e.getPlayer());
		if (bPlayer != null && CoreAbility.getAbility(e.getPlayer(), SmokeBomb.class) != null) {
			Player player = e.getPlayer();
			SmokeBomb.smokeBomb(player);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		Integer id = event.getEntity().getEntityId();
		SmokeBomb smokebomb = SmokeBomb.getSnowballs().get(id);
		if (smokebomb != null) {
			Location loc = event.getEntity().getLocation();
			SmokeBomb.playEffect(loc);
			for (Entity en : GeneralMethods.getEntitiesAroundPoint(loc, smokebomb.getRadius())) {
				smokebomb.applyBlindness(en);
			}
			SmokeBomb.getSnowballs().remove(id);
		}
	}

}
