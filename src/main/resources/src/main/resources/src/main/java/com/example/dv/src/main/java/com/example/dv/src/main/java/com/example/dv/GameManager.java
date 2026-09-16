package com.example.dv;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {

    private final DVPlugin plugin;
    private final ArenaManager arenaManager;

    private Arena currentArena;
    private boolean running = false;
    private int currentWave = 0;
    private int maxWaves = 10;
    private final Set<UUID> participants = new HashSet<>();
    private final Set<UUID> activeZombies = new HashSet<>();
    private final Set<UUID> activeVillagers = new HashSet<>();

    private BukkitTask waveTask;
    private BossBar bossBar;

    public GameManager(DVPlugin plugin, ArenaManager am) {
        this.plugin = plugin;
        this.arenaManager = am;
        this.bossBar = Bukkit.createBossBar("Defend Village", BarColor.RED, BarStyle.SOLID);
    }

    public boolean isRunning() { return running; }
    public Arena getCurrentArena() { return currentArena; }
    public Set<UUID> getParticipants() { return participants; }

    public boolean join(Player p) {
        if (!running || currentArena == null) {
            p.sendMessage(Component.text("بازی در حال اجرا نیست.", NamedTextColor.RED));
            return false;
        }
        if (participants.contains(p.getUniqueId())) {
            p.sendMessage(Component.text("شما قبلاً وارد شدید.", NamedTextColor.YELLOW));
            return false;
        }
        participants.add(p.getUniqueId());
        bossBar.addPlayer(p);
        if (currentArena.getCenter() != null) p.teleport(currentArena.getCenter());
        p.sendMessage(Component.text("✅ وارد بازی شدی! از روستایی‌ها دفاع کن!", NamedTextColor.GREEN));
        return true;
    }

    public void leave(Player p) {
        participants.remove(p.getUniqueId());
        bossBar.removePlayer(p);
    }

    public boolean start(String arenaName) {
        Arena arena = arenaManager.getArena(arenaName);
        if (arena == null) return false;
        if (running) return false;
        if (arena.getCenter() == null) return false;
        if (arena.getVillagerSpots().isEmpty()) return false;
        if (arena.getZombieSpawns().isEmpty()) return false;

        currentArena = arena;
        running = true;
        currentWave = 0;
        maxWaves = plugin.getConfig().getInt("defaults.waves", 10);
        participants.clear();
        activeZombies.clear();
        activeVillagers.clear();

        // spawn villagers
        for (Location loc : arena.getVillagerSpots()) {
            Villager v = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
            v.setAI(false);
            v.setInvulnerable(false);
            v.setSilent(true);
            activeVillagers.add(v.getUniqueId());
        }

        Bukkit.broadcast(Component.text("⚔ Defend Village شروع شد! /dv join برای ورود", NamedTextColor.GOLD));

        // schedule waves
        int interval = plugin.getConfig().getInt("defaults.wave-interval-seconds", 30) * 20;
        waveTask = Bukkit.getScheduler().runTaskTimer(plugin, this::nextWave, 20L, interval);
        return true;
    }

    private void nextWave() {
        if (!running || currentArena == null) return;
        currentWave++;

        if (currentWave > maxWaves) {
            win();
            return;
        }

        int first = plugin.getConfig().getInt("defaults.zombies-first-wave", 3);
        int perWave = plugin.getConfig().getInt("defaults.zombies-per-wave", 2);
        int count = first + (currentWave - 1) * perWave;

        List<Location> spawns = currentArena.getZombieSpawns();
        for (int i = 0; i < count; i++) {
            Location spawn = spawns.get(i % spawns.size());
            Zombie z = (Zombie) spawn.getWorld().spawnEntity(spawn, EntityType.ZOMBIE);
            // قوی‌تر کردن زامبی‌ها هر موج
            z.setHealth(Math.min(20.0, 10.0 + currentWave));
            z.setShouldBurnInDay(false);
            z.setTarget(null);
            activeZombies.add(z.getUniqueId());
        }

        Bukkit.broadcast(Component.text("🌊 موج " + currentWave + "/" + maxWaves + " شروع شد! (" + count + " زامبی)", NamedTextColor.RED));
    }

    public void onZombieDeath(UUID id) {
        activeZombies.remove(id);
        updateBossBar();
    }

    public void onVillagerDeath(UUID id) {
        activeVillagers.remove(id);
        if (activeVillagers.isEmpty() && running) {
            lose();
        } else {
            updateBossBar();
        }
    }

    private void updateBossBar() {
        if (!running) return;
        bossBar.setTitle("موج " + currentWave + "/" + maxWaves +
                " | زامبی: " + activeZombies.size() +
                " | روستایی: " + activeVillagers.size());
        double progress = maxWaves > 0 ? Math.min(1.0, (double) currentWave / maxWaves) : 0;
        bossBar.setProgress(progress);
    }

    private void win() {
        Bukkit.broadcast(Component.text("🎉 تبریک! همه موج‌ها رو رد کردید!", NamedTextColor.GOLD));
        Material mat = Material.matchMaterial(plugin.getConfig().getString("defaults.reward-material", "DIAMOND"));
        if (mat == null) mat = Material.DIAMOND;
        int amt = plugin.getConfig().getInt("defaults.reward-amount", 10);
        int xp = plugin.getConfig().getInt("defaults.reward-xp", 200);

        for (UUID id : participants) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) {
                p.getInventory().addItem(new ItemStack(mat, amt));
                p.giveExp(xp);
                p.sendMessage(Component.text("🎁 جایزه: " + amt + " " + mat.name() + " + " + xp + " XP", NamedTextColor.GREEN));
            }
        }
        stop();
    }

    private void lose() {
        Bukkit.broadcast(Component.text("💀 همه روستایی‌ها مردن! باخت!", NamedTextColor.DARK_RED));
        stop();
    }

    public void stop() {
        running = false;
        if (waveTask != null) { waveTask.cancel(); waveTask = null; }

        // kill remaining zombies and villagers
        for (UUID id : new HashSet<>(activeZombies)) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) e.remove();
        }
        for (UUID id : new HashSet<>(activeVillagers)) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) e.remove();
        }
        activeZombies.clear();
        activeVillagers.clear();

        for (UUID id : new HashSet<>(participants)) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) bossBar.removePlayer(p);
        }
        participants.clear();
        bossBar.removeAll();
        bossBar.setProgress(0);
        bossBar.setTitle("Defend Village");
        currentArena = null;
        currentWave = 0;
    }
}
