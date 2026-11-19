package me.goomer.regionsSkyblock.regions;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Farm  {
    private String key;
    private String block;
    private int minDelay;
    private int maxDelay;
    private Loc star;

    public Farm(String key, String block, int minDelay, int maxDelay) {
        this.block = block;
        this.key = key;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.star = null;
    }

    public Farm(String key, String block, int minDelay, int maxDelay, Loc star) {
        this.block = block;
        this.key = key;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.star = star;
    }

    public Loc getStar() {
        return star;
    }

    public void setStar(Loc star) {
        this.star = star;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public int getDelay() {
        return Mine.randomDelay(minDelay, maxDelay);
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean contains(Block block) {
        return block.getRelative(0, -2, 0).getType().name().equals(this.block);
    }

    public void drawParticle(Location crop, RegionsSkyblock plugin) {
        if (star == null) {
            return;
        }
        Location end = crop.clone().add(0.5, 0.5, 0.5);
        int pointsPerLine = 20;
        int delay = 20;
        Particle.Spell spell = new Particle.Spell(Color.fromRGB(255, 255, 200), 1.0f);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (plugin.getStarManager() == null) {
                    cancel();
                    return;
                }
                Location from = plugin.getStarManager().beamTarget(key);
                org.bukkit.World world = end.getWorld();
                if (ticks > delay || from == null || world == null || !world.equals(from.getWorld())) {
                    cancel();
                    return;
                }
                Vector step = end.toVector().subtract(from.toVector()).multiply(1.0 / pointsPerLine);
                for (int i = 0; i <= pointsPerLine; i++) {
                    Location point = from.clone().add(step.clone().multiply(i));
                    world.spawnParticle(Particle.INSTANT_EFFECT, point, 1, 0, 0, 0, spell);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1);
    }
}
