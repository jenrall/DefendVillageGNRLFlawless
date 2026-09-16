package com.example.dv;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class Arena {

    private final String name;
    private Location pos1;
    private Location pos2;
    private Location center;
    private final List<Location> villagerSpots = new ArrayList<>();
    private final List<Location> zombieSpawns = new ArrayList<>();

    public Arena(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public Location getPos1() { return pos1; }
    public Location getPos2() { return pos2; }
    public Location getCenter() { return center; }
    public List<Location> getVillagerSpots() { return villagerSpots; }
    public List<Location> getZombieSpawns() { return zombieSpawns; }

    public void setPos1(Location l) { this.pos1 = l; recalcCenter(); }
    public void setPos2(Location l) { this.pos2 = l; recalcCenter(); }
    public void setCenter(Location l) { this.center = l; }

    public void addVillagerSpot(Location l) { villagerSpots.add(l); }
    public void addZombieSpawn(Location l) { zombieSpawns.add(l); }

    private void recalcCenter() {
        if (pos1 == null || pos2 == null) return;
        World w = pos1.getWorld();
        double x = (pos1.getX() + pos2.getX()) / 2;
        double y = (pos1.getY() + pos2.getY()) / 2;
        double z = (pos1.getZ() + pos2.getZ()) / 2;
        this.center = new Location(w, x, y + 1, z);
    }

    public boolean contains(Location loc) {
        if (loc == null || pos1 == null || pos2 == null) return false;
        if (!loc.getWorld().equals(pos1.getWorld())) return false;

        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return loc.getX() >= minX && loc.getX() <= maxX &&
               loc.getY() >= minY && loc.getY() <= maxY &&
               loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
}
