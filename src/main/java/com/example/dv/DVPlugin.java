package com.example.dv;

import org.bukkit.plugin.java.JavaPlugin;

public final class DVPlugin extends JavaPlugin {

    private ArenaManager arenaManager;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.arenaManager = new ArenaManager(this);
        this.gameManager = new GameManager(this, arenaManager);

        getCommand("dv").setExecutor(new DVCommand(this, arenaManager, gameManager));
        getServer().getPluginManager().registerEvents(new DVListener(gameManager), this);

        getLogger().info("DefendVillage enabled!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) gameManager.stop();
        if (arenaManager != null) arenaManager.saveArenas();
    }
}
