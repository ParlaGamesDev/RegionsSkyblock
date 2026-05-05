package me.goomer.regionsSkyblock.events;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import me.goomer.regionsSkyblock.regions.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class NewBlockBreak implements Listener {

    private RegionsSkyblock plugin;
    private RegionsHelper helper;

    public NewBlockBreak(RegionsSkyblock plugin){
        this.plugin = plugin;
        this.helper = new RegionsHelper(plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Material material = block.getType();
        Location location = block.getLocation();
        BlockLoc blockLoc = new BlockLoc(block);

        // Check for Tree region synchronously for XP and reliable detection
        Tree tree = helper.getTreeByLocation(location);
        if (tree != null) {
            // AuraSkills Foraging XP Integration
            if (plugin.isAuraSkillsEnabled() && (material.name().contains("_LOG") || material.name().contains("_STEM") || material.name().contains("_WOOD"))) {
                plugin.getAuraSkillsHook().addXP(event.getPlayer(), "foraging", 5.0);
            }
        }

        boolean farmCheck = true;
        if(block.getBlockData() instanceof Ageable ageable){
            farmCheck = ageable.getAge()==ageable.getMaximumAge();
        }
        boolean finalFarmCheck = farmCheck;
        BukkitRunnable main = new BukkitRunnable() {
            @Override
            public void run() {
                Farm farm = helper.getFarmByBlock(block);
                if(farm != null && finalFarmCheck){
                    BukkitRunnable task = new BukkitRunnable() {
                        @Override
                        public void run() {
                            block.setType(material);
                            farm.drawParticle(location, plugin);
                            if(block.getBlockData() instanceof Ageable ageable){
                                ageable.setAge(ageable.getMaximumAge());
                                block.setBlockData(ageable);
                            }
                        }
                    };
                    task.runTaskLater(plugin, farm.getDelay());
                    return;
                }
                Mine mine = helper.getMineByLocation(block.getLocation());
                if(mine != null){
                    if(material == Material.COBBLESTONE){
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                block.setType(Material.BEDROCK);
                            }
                        }.runTask(plugin);
                    }
                    else{
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                block.setType(Material.COBBLESTONE);
                            }
                        }.runTask(plugin);
                        BukkitRunnable task = new BukkitRunnable() {
                            @Override
                            public void run() {
                                block.setType(material);
                            }
                        };
                        task.runTaskLater(plugin, mine.getDelay());
                    }

                    return;
                }
                // Tree region regrowth logic still runs async
                if(tree!=null){
                    boolean loop = plugin.exists(tree.getKey());
                    plugin.addBlockLoc(tree.getKey(), blockLoc);
                    if(!loop){
                        respawnTree(tree);
                    }
                }
            }

        };
        main.runTaskAsynchronously(plugin);
    }

    public void respawnTree(Tree tree){
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.regenerateByKey(tree.getKey());
            }
        }.runTaskLater(plugin, tree.getDelay());
    }

}
