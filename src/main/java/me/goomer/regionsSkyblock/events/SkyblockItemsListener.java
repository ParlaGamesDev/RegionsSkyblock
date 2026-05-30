package me.goomer.regionsSkyblock.events;

import dev.agam.skyblockitems.api.events.AbilityBlockBreakEvent;
import me.goomer.regionsSkyblock.RegionsSkyblock;
import me.goomer.regionsSkyblock.hooks.WorldGuardHook;
import me.goomer.regionsSkyblock.regions.BlockLoc;
import me.goomer.regionsSkyblock.regions.RegionsHelper;
import me.goomer.regionsSkyblock.regions.Tree;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

/**
 * Listens for SkyBlockItems mass tree breaks ({@code TREE_CAPITATOR} extra logs,
 * {@code THUNDER_STRIKE}) so regrowth queues match normal {@link NewBlockBreak} behavior.
 */
public class SkyblockItemsListener implements Listener {

    private static final String TREE_CAPITATOR = "TREE_CAPITATOR";
    private static final String THUNDER_STRIKE = "THUNDER_STRIKE";

    @EventHandler(ignoreCancelled = true)
    public void onAbilityBlockBreak(AbilityBlockBreakEvent event) {
        String id = event.getAbility().getId();
        if (!TREE_CAPITATOR.equals(id) && !THUNDER_STRIKE.equals(id)) {
            return;
        }

        for (Map.Entry<Location, Material> entry : event.getBlocks().entrySet()) {
            addToRegeneration(event.getPlayer(), entry.getKey(), entry.getValue());
        }
    }

    private void addToRegeneration(org.bukkit.entity.Player player, Location loc, Material expectedType) {
        if (WorldGuardHook.hasBypass(player)) {
            return;
        }
        if (!WorldGuardHook.shouldRegenerateBlock(player, loc, expectedType)) {
            return;
        }
        Block block = loc.getBlock();
        if (block.getType() != expectedType) {
            return;
        }

        Tree tree = RegionsHelper.getTreeByLocation(loc);
        if (tree == null) {
            return;
        }

        RegionsSkyblock plugin = RegionsSkyblock.instance;
        BlockLoc snapshot = new BlockLoc(block);
        boolean loop = plugin.exists(tree.getKey());
        plugin.addBlockLoc(tree.getKey(), snapshot);
        if (!loop) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.regenerateByKey(tree.getKey());
                }
            }.runTaskLater(plugin, tree.getDelay());
        }

        // AuraSkills Foraging XP Integration
        if (plugin.isAuraSkillsEnabled() && (expectedType.name().contains("_LOG") || expectedType.name().contains("_STEM") || expectedType.name().contains("_WOOD"))) {
            plugin.getAuraSkillsHook().addXP(player, "foraging", 5.0);
        }
    }
}
