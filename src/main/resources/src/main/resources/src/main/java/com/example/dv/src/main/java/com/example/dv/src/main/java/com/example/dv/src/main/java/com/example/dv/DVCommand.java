package com.example.dv;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DVCommand implements CommandExecutor {

    private final DVPlugin plugin;
    private final ArenaManager arenaManager;
    private final GameManager gameManager;

    public DVCommand(DVPlugin plugin, ArenaManager am, GameManager gm) {
        this.plugin = plugin;
        this.arenaManager = am;
        this.gameManager = gm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Only players.");
            return true;
        }

        if (args.length == 0) { help(p); return true; }

        switch (args[0].toLowerCase()) {
            case "help" -> help(p);
            case "list" -> list(p);
            case "create" -> create(p, args);
            case "delete" -> delete(p, args);
            case "pos1" -> pos1(p, args);
            case "pos2" -> pos2(p, args);
            case "addvillager" -> addVillager(p, args);
            case "addspawn" -> addSpawn(p, args);
            case "start" -> start(p, args);
            case "stop" -> stop(p);
            case "join" -> gameManager.join(p);
            case "leave" -> gameManager.leave(p);
            case "invite" -> invite(p, args);
            default -> p.sendMessage(Component.text("دستور ناشناخته. /dv help", NamedTextColor.RED));
        }
        return true;
    }

    private void help(Player p) {
        p.sendMessage(Component.text("=== Defend Village ===", NamedTextColor.GOLD));
        p.sendMessage(Component.text("/dv create <name> - ساخت آرنا", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/dv delete <name> - حذف آرنا", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/dv list - لیست آرناها", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/dv pos1 <name> - گوشه اول", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/dv pos2 <name> - گوشه دوم", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/dv addvillager <name> - اضافه کردن روستایی", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/dv addspawn <name> - نقطه spawn زامبی", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/dv start <name> - شروع بازی", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/dv stop - توقف", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/dv join - ورود به بازی", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/dv leave - خروج", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/dv invite <player> - دعوت دوست", NamedTextColor.YELLOW));
    }

    private void list(Player p) {
        if (arenaManager.getArenas().isEmpty()) {
            p.sendMessage(Component.text("هیچ آرنایی نیست.", NamedTextColor.RED));
            return;
        }
        p.sendMessage(Component.text("=== Arenas ===", NamedTextColor.GOLD));
        arenaManager.getArenas().forEach((name, a) ->
            p.sendMessage(Component.text("- " + name +
                " | روستایی: " + a.getVillagerSpots().size() +
                " | spawn: " + a.getZombieSpawns().size(), NamedTextColor.YELLOW))
        );
    }

    private void create(Player p, String[] args) {
        if (!p.hasPermission("dv.admin")) { noPerm(p); return; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /dv create <name>", NamedTextColor.RED)); return; }
        if (arenaManager.createArena(args[1])) {
            p.sendMessage(Component.text("آرنا ساخته شد. حالا pos1/pos2/addvillager/addspawn", NamedTextColor.GREEN));
        } else {
            p.sendMessage(Component.text("این آرنا هست.", NamedTextColor.RED));
        }
    }

    private void delete(Player p, String[] args) {
        if (!p.hasPermission("dv.admin")) { noPerm(p); return; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /dv delete <name>", NamedTextColor.RED)); return; }
        if (arenaManager.deleteArena(args[1])) {
            p.sendMessage(Component.text("حذف شد.", NamedTextColor.GREEN));
        } else {
            p.sendMessage(Component.text("آرنا نیست.", NamedTextColor.RED));
        }
    }

    private void pos1(Player p, String[] args) {
        if (!p.hasPermission("dv.admin")) { noPerm(p); return; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /dv pos1 <name>", NamedTextColor.RED)); return; }
        Arena a = arenaManager.getArena(args[1]);
        if (a == null) { p.sendMessage(Component.text("آرنا نیست.", NamedTextColor.RED)); return; }
        a.setPos1(p.getLocation());
        arenaManager.saveArenas();
        p.sendMessage(Component.text("pos1 ثبت شد.", NamedTextColor.GREEN));
    }

    private void pos2(Player p, String[] args) {
        if (!p.hasPermission("dv.admin")) { noPerm(p); return; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /dv pos2 <name>", NamedTextColor.RED)); return; }
        Arena a = arenaManager.getArena(args[1]);
        if (a == null) { p.sendMessage(Component.text("آرنا نیست.", NamedTextColor.RED)); return; }
        a.setPos2(p.getLocation());
        arenaManager.saveArenas();
        p.sendMessage(Component.text("pos2 ثبت شد.", NamedTextColor.GREEN));
    }

    private void addVillager(Player p, String[] args) {
        if (!p.hasPermission("dv.admin")) { noPerm(p); return; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /dv addvillager <name>", NamedTextColor.RED)); return; }
        Arena a = arenaManager.getArena(args[1]);
        if (a == null) { p.sendMessage(Component.text("آرنا نیست.", NamedTextColor.RED)); return; }
        a.addVillagerSpot(p.getLocation());
        arenaManager.saveArenas();
        p.sendMessage(Component.text("روستایی ثبت شد (" + a.getVillagerSpots().size() + ")", NamedTextColor.GREEN));
    }

    private void addSpawn(Player p, String[] args) {
        if (!p.hasPermission("dv.admin")) { noPerm(p); return; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /dv addspawn <name>", NamedTextColor.RED)); return; }
        Arena a = arenaManager.getArena(args[1]);
        if (a == null) { p.sendMessage(Component.text("آرنا نیست.", NamedTextColor.RED)); return; }
        a.addZombieSpawn(p.getLocation());
        arenaManager.saveArenas();
        p.sendMessage(Component.text("نقطه spawn ثبت شد (" + a.getZombieSpawns().size() + ")", NamedTextColor.GREEN));
    }

    private void start(Player p, String[] args) {
        if (!p.hasPermission("dv.admin")) { noPerm(p); return; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /dv start <name>", NamedTextColor.RED)); return; }
        if (!gameManager.start(args[1])) {
            p.sendMessage(Component.text("شروع نشد. چک کن: pos1/pos2/villager/spawn", NamedTextColor.RED));
        }
    }

    private void stop(Player p) {
        if (!p.hasPermission("dv.admin")) { noPerm(p); return; }
        gameManager.stop();
        p.sendMessage(Component.text("متوقف شد.", NamedTextColor.YELLOW));
    }

    private void invite(Player p, String[] args) {
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /dv invite <player>", NamedTextColor.RED)); return; }
        if (!gameManager.isRunning()) { p.sendMessage(Component.text("بازی اجرا نیست.", NamedTextColor.RED)); return; }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) { p.sendMessage(Component.text("بازیکن آنلاین نیست.", NamedTextColor.RED)); return; }
        if (target.equals(p)) { p.sendMessage(Component.text("خودت رو نمی‌تونی دعوت کنی.", NamedTextColor.RED)); return; }

        target.sendMessage(Component.text("🎮 " + p.getName() + " تو رو دعوت کرد! با /dv join وارد شو!", NamedTextColor.GOLD));
        p.sendMessage(Component.text("دعوت فرستاده شد.", NamedTextColor.GREEN));
    }

    private void noPerm(Player p) {
        p.sendMessage(Component.text("دسترسی نداری.", NamedTextColor.RED));
    }
}
