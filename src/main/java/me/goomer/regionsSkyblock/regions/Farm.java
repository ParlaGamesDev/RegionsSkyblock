package me.goomer.regionsSkyblock.regions;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class Farm {
    private String key, block;
    private int minDelay, maxDelay;
    private Loc star;

    public Farm(String key, String block, int minDelay, int maxDelay){
        this.block = block;
        this.key = key;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.star = null;
    }

    public Farm(String key, String block, int minDelay, int maxDelay, Loc star){
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean contains(Block block){
        return block.getRelative(0, -2, 0).getType().name().equals(this.block);
    }

    public void drawParticle(Location end, RegionsSkyblock plugin){
        if(star == null)
            return;
        end.add(0.5, 0.5, 0.5);
        Location star = new Location(end.getWorld(), this.star.getX() + 0.5, this.star.getY() + 0.5, this.star.getZ() + 0.5);
        int pointsPerLine = 20;
        Vector vector = end.clone().subtract(star).toVector().multiply(1.0 / pointsPerLine);
        int DELAY = 20;

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > DELAY) {
                    cancel();
                    return;
                }
                for (int i = 0; i <= pointsPerLine; i++) {
                    Location point = star.clone().add(vector.clone().multiply(i));
                    star.getWorld().spawnParticle(
                            Particle.INSTANT_EFFECT,
                            point,
                            1,
                            0,
                            0,
                            0,
                            new Particle.Spell(Color.fromRGB(255, 255, 200), 1.0f)
                    );
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1); // כל טיק



    }
}
