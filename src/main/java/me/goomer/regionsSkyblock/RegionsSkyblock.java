package me.goomer.regionsSkyblock;

import me.goomer.regionsSkyblock.commands.*;
import me.goomer.regionsSkyblock.events.AllowedBlockBreakListener;
import me.goomer.regionsSkyblock.events.NewBlockBreak;
import me.goomer.regionsSkyblock.events.SkyblockItemsListener;
import me.goomer.regionsSkyblock.hooks.AuraSkillsHook;
import me.goomer.regionsSkyblock.hooks.WorldGuardHook;
import me.goomer.regionsSkyblock.regions.BlockLoc;
import me.goomer.regionsSkyblock.regions.Loc;
import me.goomer.regionsSkyblock.stars.StarManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Orientable;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public final class RegionsSkyblock extends JavaPlugin {

    public static RegionsSkyblock instance;

    HashMap<String, ArrayList<BlockLoc>> blocks;
    private StarManager starManager;
    private NewBlockBreak blockBreakListener;
    private AuraSkillsHook auraSkillsHook;

    @Override
    public void onLoad() {
        WorldGuardHook.registerFlag();
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        blocks = new HashMap<>();

        WorldGuardHook.init();
        new BukkitRunnable() {
            @Override
            public void run() {
                WorldGuardHook.reloadFlag();
            }
        }.runTaskLater(this, 1L);

        if (getServer().getPluginManager().isPluginEnabled("AuraSkills")) {
            try {
                this.auraSkillsHook = new AuraSkillsHook();
            } catch (Throwable e) {
                getLogger().log(Level.WARNING, "AuraSkills hook failed, foraging XP disabled", e);
            }
        }

        blockBreakListener = new NewBlockBreak(this);
        getServer().getPluginManager().registerEvents(new AllowedBlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(blockBreakListener, this);
        if (getServer().getPluginManager().isPluginEnabled("SkyBlockItems")) {
            getServer().getPluginManager().registerEvents(new SkyblockItemsListener(), this);
        } else {
            getLogger().warning("SkyBlockItems is not enabled - tree capitator / thunder strike regrowth disabled.");
        }

        PluginCommand command = getCommand("regions");
        if (command != null) {
            command.setExecutor(new MainCommand(this));
        }

        starManager = new StarManager(this);
        getServer().getPluginManager().registerEvents(starManager, this);
        starManager.start();
    }

    @Override
    public void onDisable() {
        regenerateEverything();
        if (starManager != null) {
            starManager.shutdown();
        }
    }

    /** Immediately puts back every broken tree, farm crop and mine block still waiting to regenerate. */
    public int regenerateEverything() {
        int restored = 0;
        if (blockBreakListener != null) {
            restored += blockBreakListener.restoreAllNow();
        }
        if (blocks != null) {
            for (ArrayList<BlockLoc> list : blocks.values()) {
                restored += list.size();
            }
            regenerateAll();
        }
        return restored;
    }

    public void regenerateFirst(String key, boolean isStar){
        ArrayList<BlockLoc> rblocks = blocks.get(key);
        if(rblocks != null && !rblocks.isEmpty()){
            BlockLoc blockLoc = rblocks.removeFirst();
            Block block = blockLoc.getBlockAt();
            if (block != null && blockLoc.getBlockData() != null) {
                block.setBlockData(blockLoc.getBlockData());
                return;
            }
            Material material = Material.getMaterial(blockLoc.getBlock());
            if (block == null || material == null) {
                return;
            }
            block.setType(material);
            if(block.getBlockData() instanceof Ageable ageable){
                ageable.setAge(ageable.getMaximumAge());
                block.setBlockData(ageable);
            }
            if(block.getBlockData() instanceof Orientable orientable){
                if(blockLoc.getFace()!=null){
                    orientable.setAxis(blockLoc.getFace());
                    block.setBlockData(orientable);
                }
            }
        }
    }

    public void regenerateByKey(String key){
        if(exists(key)){
            while(exists(key)){
                regenerateFirst(key, false);
            }
        }
    }

    public void regenerateAll(){
        for(String key : blocks.keySet()){
            regenerateByKey(key);
        }
    }

    public boolean exists(String key){
        if(blocks.containsKey(key))
            return !blocks.get(key).isEmpty();
        return false;
    }

    public void addBlock(String key, Block block){
        addBlockLoc(key, new BlockLoc(block));
    }

    public void addBlockLoc(String key, BlockLoc blockLoc){
        ArrayList<BlockLoc> list = blocks.computeIfAbsent(key, k -> new ArrayList<>());
        for (BlockLoc existing : list) {
            if (sameGridCell(existing, blockLoc)) {
                return;
            }
        }
        list.add(blockLoc);
    }

    private static boolean sameGridCell(BlockLoc a, BlockLoc b) {
        return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ()
                && a.getWorld().equals(b.getWorld());
    }

    public void addStar(String farm, int x, int y, int z, String worldName) {
        starManager.spawn(farm, new Loc(x, y, z, worldName));
    }

    public void removeStar(String farm) {
        starManager.remove(farm);
    }

    public StarManager getStarManager() {
        return starManager;
    }

    public AuraSkillsHook getAuraSkillsHook() {
        return auraSkillsHook;
    }

    public boolean isAuraSkillsEnabled() {
        return auraSkillsHook != null;
    }
}
