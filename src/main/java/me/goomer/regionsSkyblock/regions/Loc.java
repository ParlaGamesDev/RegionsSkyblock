package me.goomer.regionsSkyblock.regions;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Loc  {
    protected int x;
    protected int y;
    protected int z;
    protected String world;

    public Loc(int x, int y, int z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public Block getBlockAt() {
        World world = Bukkit.getWorld(this.world);
        if (world != null) {
            return world.getBlockAt(x, y, z);
        }
        return null;
    }
}
