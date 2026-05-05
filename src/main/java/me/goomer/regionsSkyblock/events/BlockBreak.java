package me.goomer.regionsSkyblock.events;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import me.goomer.regionsSkyblock.regions.Farm;
import me.goomer.regionsSkyblock.regions.Mine;
import me.goomer.regionsSkyblock.regions.RegionsHelper;
import me.goomer.regionsSkyblock.regions.Tree;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class BlockBreak implements Listener {

    private RegionsSkyblock plugin;
    private RegionsHelper helper;

    public BlockBreak(RegionsSkyblock plugin){
        this.plugin = plugin;
        this.helper = new RegionsHelper(plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Farm farm = helper.getFarmByBlock(block);
        if(farm != null){
            boolean loop = plugin.exists(farm.getKey());
            plugin.addBlock(farm.getKey(), block);
            if(!loop){
                farmLoop(farm);
            }
            return;
        }
        Mine mine = helper.getMineByLocation(block.getLocation());
        if(mine != null){
            if(block.getType()== Material.COBBLESTONE){
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        block.setType(Material.BEDROCK);
                    }
                }.runTask(plugin);
            }
            else{
                boolean loop = plugin.exists(mine.getKey());
                plugin.addBlock(mine.getKey(), block);
                if(!loop){
                    mineLoop(mine);
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        block.setType(Material.COBBLESTONE);
                    }
                }.runTask(plugin);
            }

            return;
        }
        Tree tree = helper.getTreeByLocation(block.getLocation());
        if(tree!=null){
            boolean loop = plugin.exists(tree.getKey());
            plugin.addBlock(tree.getKey(), block);
            if(!loop){
                respawnTree(tree);
            }

            // AuraSkills Foraging XP Integration
            if (plugin.isAuraSkillsEnabled() && (block.getType().name().contains("_LOG") || block.getType().name().contains("_STEM") || block.getType().name().contains("_WOOD"))) {
                plugin.getAuraSkillsHook().addXP(event.getPlayer(), "foraging", 5.0);
            }
        }
    }

    public void mineLoop(Mine mine){
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if(plugin.exists(mine.getKey())){
                    plugin.regenerateFirst(mine.getKey(), false);
                    if(plugin.exists(mine.getKey())){
                        mineLoop(mine);
                    }
                }
            }
        }.runTaskLater(plugin, mine.getDelay());
    }

    public void farmLoop(Farm farm){
        new BukkitRunnable() {
            @Override
            public void run() {
                if(plugin.exists(farm.getKey())){
                    // TODO star effect
                    plugin.regenerateFirst(farm.getKey(), farm.getStar()!=null);
                    if(plugin.exists(farm.getKey())){
                        farmLoop(farm);
                    }
                }
            }
        }.runTaskLater(plugin, farm.getDelay());
    }

    public void respawnTree(Tree tree){
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.regenerateByKey(tree.getKey());
            }
        }.runTaskLater(plugin, tree.getDelay());
    }

    public RegionsHelper getHelper() {
        return this.helper;
    }
}
