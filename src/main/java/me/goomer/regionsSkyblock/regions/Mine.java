package me.goomer.regionsSkyblock.regions;

import org.bukkit.Location;

import java.util.Random;

public class Mine {
    private Loc loc1, loc2;
    private String key;
    private int minDelay, maxDelay;

    public Mine(Loc loc1, Loc loc2, int minDelay, int maxDelay, String key){
        this.loc1 = loc1;
        this.loc2 = loc2;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.key = key;
    }

    public int getDelay() {
        Random random = new Random();
        return random.nextInt(minDelay, maxDelay+1);
    }

    public int getMinDelay() {
        return minDelay;
    }

    public void setMinDelay(int minDelay) {
        this.minDelay = minDelay;
    }

    public int getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(int maxDelay) {
        this.maxDelay = maxDelay;
    }

    public Loc getLoc1() {
        return loc1;
    }

    public void setLoc1(Loc loc1) {
        this.loc1 = loc1;
    }

    public Loc getLoc2() {
        return loc2;
    }

    public void setLoc2(Loc loc2) {
        this.loc2 = loc2;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean contains(Location loc){
        if (loc.getWorld() == null || loc1.getWorld() == null) {
            return false;
        }
        if (!loc1.getWorld().equalsIgnoreCase(loc.getWorld().getName())) {
            return false;
        }

        int minX = Math.min(loc1.getX(), loc2.getX());
        int maxX = Math.max(loc1.getX(), loc2.getX());
        int minY = Math.min(loc1.getY(), loc2.getY());
        int maxY = Math.max(loc1.getY(), loc2.getY());
        int minZ = Math.min(loc1.getZ(), loc2.getZ());
        int maxZ = Math.max(loc1.getZ(), loc2.getZ());

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return minX <= x && x <= maxX
                && minY <= y && y <= maxY
                && minZ <= z && z <= maxZ;
    }
}
