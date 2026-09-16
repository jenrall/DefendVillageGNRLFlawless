package com.example.dv;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DVListener implements Listener {

    private final GameManager gameManager;

    public DVListener(GameManager gm) {
        this.gameManager = gm;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Zombie) {
            gameManager.onZombieDeath(e.getEntity().getUniqueId());
        } else if (e.getEntity() instanceof Villager) {
            gameManager.onVillagerDeath(e.getEntity().getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        gameManager.leave(e.getPlayer());
    }
}
