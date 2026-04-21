package me.goomer.regionsSkyblock.events;

import dev.agam.skyblockitems.api.events.AbilityBlockBreakEvent;
import dev.agam.skyblockitems.api.events.TreeCapitatorEvent;
import me.goomer.regionsSkyblock.RegionsSkyblock;
import me.goomer.regionsSkyblock.regions.RegionsHelper;
import me.goomer.regionsSkyblock.regions.Tree;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class SkyblockItemsListener implements Listener {

    @EventHandler
    public void AbilityBlockBreakListener(AbilityBlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        event.getBlocks().forEach(this::addToRegeneration);
    }

    @EventHandler
    public void TreeCapitatorEventListener(TreeCapitatorEvent event) {
        if (event.isCancelled()) {
            return;
        }

        event.getBrokenBlocks().forEach(this::addToRegeneration);
    }

    public void addToRegeneration(Location k, Material v) {
        Tree tree = RegionsHelper.getTreeByLocation(k);
        if(tree!=null){
            boolean loop = RegionsSkyblock.instance.exists(tree.getKey());
            RegionsSkyblock.instance.addBlock(tree.getKey(), v.createBlockData().createBlockState().getBlock());
            if(!loop){
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        RegionsSkyblock.instance.regenerateByKey(tree.getKey());
                    }
                }.runTaskLater(RegionsSkyblock.instance, tree.getDelay());
            }
        }
    }
}
