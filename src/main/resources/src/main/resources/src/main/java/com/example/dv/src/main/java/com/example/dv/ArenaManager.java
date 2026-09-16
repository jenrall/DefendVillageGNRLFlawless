package com.example.dv;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ArenaManager {

    private final DVPlugin plugin;
    private final Map<String, Arena> arenas = new HashMap<>();

    public ArenaManager(DVPlugin plugin) {
        this.plugin = plugin;
        loadArenas();
    }

    public Arena getArena(String name) { return arenas.get(name.toLowerCase()); }
    public Map<String, Arena> getArenas() { return arenas; }

    public boolean createArena(String name) {
        if (arenas.containsKey(name.toLowerCase())) return false;
        arenas.put(name.toLowerCase(), new Arena(name));
        return true;
    }

    public boolean deleteArena(String name) {
        if (!arenas.containsKey(name.toLowerCase())) return false;
        arenas.remove(name.toLowerCase());
        plugin.getConfig().set("arenas." + name.toLowerCase(), null);
        plugin.saveConfig();
        return true;
    }

    public void saveArenas() {
        FileConfiguration cfg = plugin.getConfig();
        for (Arena a : arenas.values()) {
            String path = "arenas." + a.getName().toLowerCase();
            cfg.set(path + ".pos1", a.getPos1());
            cfg.set(path + ".pos2", a.getPos2());
            cfg.set(path + ".center", a.getCenter());

            // villager spots
            cfg.set(path + ".villager-spots", null);
            for (int i = 0; i < a.getVillagerSpots().size(); i++) {
                cfg.set(path + ".villager-spots." + i, a.getVillagerSpots().get(i));
            }
            // zombie spawns
            cfg.set(path + ".zombie-spawns", null);
            for (int i = 0; i < a.getZombieSpawns().size(); i++) {
                cfg.set(path + ".zombie-spawns." + i, a.getZombieSpawns().get(i));
            }
        }
        plugin.saveConfig();
    }

    private void loadArenas() {
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection section = cfg.getConfigurationSection("arenas");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String path = "arenas." + key;
            Arena arena = new Arena(key);
            arena.setPos1(cfg.getLocation(path + ".pos1"));
            arena.setPos2(cfg.getLocation(path + ".pos2"));
            arena.setCenter(cfg.getLocation(path + ".center"));

            ConfigurationSection vs = cfg.getConfigurationSection(path + ".villager-spots");
            if (vs != null) {
                for (String k : vs.getKeys(false)) {
                    Location l = vs.getLocation(k);
                    if (l != null) arena.addVillagerSpot(l);
                }
            }
            ConfigurationSection zs = cfg.getConfigurationSection(path + ".zombie-spawns");
            if (zs != null) {
                for (String k : zs.getKeys(false)) {
                    Location l = zs.getLocation(k);
                    if (l != null) arena.addZombieSpawn(l);
                }
            }
            arenas.put(key, arena);
        }
        plugin.getLogger().info("Loaded " + arenas.size() + " arena(s).");
    }
}
