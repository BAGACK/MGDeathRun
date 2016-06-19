package com.comze_instancelabs.deathrun;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaType;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class IArena extends Arena {

	Main m = null;
	BukkitTask tt;

	public IArena(Main m, String arena) {
		super(m, arena, ArenaType.REGENERATION);
		this.m = m;
		MinigamesAPI.getAPI().pinstances.get(m).getArenaListener().loseY = 15;
	}

	@Override
	public void start(boolean tp) {
		super.start(tp);
		final IArena a = this;
		Bukkit.getScheduler().runTaskLater(m, new Runnable() {
			public void run() {
				for (String p_ : a.getAllPlayers()) {
					if (Validator.isPlayerOnline(p_)) {
						Player p = Bukkit.getPlayer(p_);
						p.setWalkSpeed(0.2F);
						p.setFoodLevel(20);
						p.removePotionEffect(PotionEffectType.JUMP);
					}
				}
			}
		}, 20L);
	}

	public void remove(final Location loc) {
		Bukkit.getScheduler().runTaskLater(m, new Runnable() {
			public void run() {
					
					for (Entity t_ : loc.getChunk().getEntities()) {
						if (t_.getType() == EntityType.FALLING_BLOCK) {
								t_.remove();
						}
				}
			}
			}, 38L);
		}
	
	
	@Override
	public void started() {
		final IArena a = this;
		tt = Bukkit.getScheduler().runTaskTimer(m, new Runnable() {
			public void run() {
				for (String p_ : a.getAllPlayers()) {
					Player p = Bukkit.getPlayer(p_);
					if (p != null) {
						Location l = p.getLocation().add(0D, -1D, 0D);
						int jumping = p.getVelocity().getY() > -0.2D ? 1 : 0;
						final ArrayList<Location> locs = new ArrayList<Location>(Arrays.asList(l.clone().add(0.3, 0, -0.3), l.clone().add(-0.3, 0, -0.3), l.clone().add(0.3, 0, 0.3), l.clone().add(-0.3, 0, +0.3)));
						ArrayList<Location> temp = new ArrayList<Location>(locs);

						for (int i = 1; i < m.block_lv_to_remove + jumping; i++) {
							for (Location l_ : temp) {
								Location n = l_.clone().add(0D, -i, 0D);
								locs.add(n);
								a.getSmartReset().addChanged(l_.getBlock());
								a.getSmartReset().addChanged(n.getBlock());
							}
						}

						Bukkit.getScheduler().runTaskLater(m, new Runnable() {
							public void run() {
								for (Location l_ : locs) {
									l_.getBlock().getWorld().spawnFallingBlock(l_.add(0,0.5,0), l_.getBlock().getType(), l_.getBlock().getData());
									l_.getBlock().breakNaturally();
									l_.getBlock().getDrops().clear();
									
									remove(l_);
									
									for (Entity t_ : l_.getChunk().getEntities()) {
										if (t_.getType() == EntityType.DROPPED_ITEM) {
												t_.remove();
												
										}
								}
								}
							}
						
						}, m.ticks);
					}
				}
			}
		}, 20L, 3L);
	}
	

	@Override
	public void stop() {
		if (tt != null) {
			tt.cancel();
			tt = null;
			final Arena a = this;
			Bukkit.getScheduler().runTaskLater(m, new Runnable() {
				public void run() {
					if (m.isEnabled()) {
						a.stopArena();
					}
				}
			}, 20L);
		} else {
			super.stop();
		}

	}

}
