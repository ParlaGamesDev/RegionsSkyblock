package me.goomer.regionsSkyblock.events;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import me.goomer.regionsSkyblock.hooks.WorldGuardHook;
import me.goomer.regionsSkyblock.regions.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NewBlockBreak implements Listener {

    private final RegionsSkyblock plugin;
    private final RegionsHelper helper;
    /** Material before break — at MONITOR the block is already AIR on Paper/Spigot. */
    private final Map<BlockKey, PendingBreak> pendingBreaks = new ConcurrentHashMap<>();

    public NewBlockBreak(RegionsSkyblock plugin) {
        this.plugin = plugin;
        this.helper = new RegionsHelper(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void captureBlock(BlockBreakEvent event) {
        Block block = event.getBlock();
        boolean cropFullyMature = true;
        if (block.getBlockData() instanceof Ageable ageable) {
            cropFullyMature = ageable.getAge() == ageable.getMaximumAge();
        }
        pendingBreaks.put(
                BlockKey.from(block),
                new PendingBreak(block.getType(), new BlockLoc(block), cropFullyMature)
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void discardCancelledBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            pendingBreaks.remove(BlockKey.from(event.getBlock()));
        }
    }

    /**
     * MONITOR runs after SkyblockCore {@code AllowedBlockBreakListener} (HIGHEST) finished.
     * ignoreCancelled=true → only real breaks (allowed materials) reach here.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        PendingBreak pending = pendingBreaks.remove(BlockKey.from(block));
        if (pending == null || pending.material().isAir()) {
            return;
        }

        if (WorldGuardHook.hasBypass(player)) {
            return;
        }

        Material material = pending.material();
        BlockLoc blockLoc = pending.blockLoc();

        Tree tree = helper.getTreeByLocation(location);
        if (tree != null && isForagingMaterial(material)) {
            if (plugin.isAuraSkillsEnabled()) {
                plugin.getAuraSkillsHook().addXP(player, "foraging", 5.0);
            }
        }

        Farm farm = helper.getFarmByBlock(block);
        if (farm != null && pending.cropFullyMature()) {
            scheduleFarmRestore(location, material, farm);
            return;
        }

        Mine mine = helper.getMineByLocation(location);
        if (mine != null) {
            if (!WorldGuardHook.shouldRegenerateBlock(player, location, material)) {
                return;
            }
            handleMineBreak(mine, location, material, blockLoc);
            return;
        }

        if (tree != null) {
            boolean loop = plugin.exists(tree.getKey());
            plugin.addBlockLoc(tree.getKey(), blockLoc);
            if (!loop) {
                respawnTree(tree);
            }
        }
    }

    private void handleMineBreak(Mine mine, Location location, Material material, BlockLoc blockLoc) {
        Material original = material;
        if (material == Material.COBBLESTONE) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Block block = location.getBlock();
                    if (block.getType() != Material.AIR) {
                        return;
                    }
                    block.setType(Material.BEDROCK);
                }
            }.runTaskLater(plugin, 1L);
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Block block = location.getBlock();
                if (block.getType() != Material.AIR) {
                    return;
                }
                block.setType(Material.COBBLESTONE);
            }
        }.runTaskLater(plugin, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {
                Block block = location.getBlock();
                Material current = block.getType();
                if (current != Material.AIR && current != Material.COBBLESTONE) {
                    return;
                }
                block.setType(original);
            }
        }.runTaskLater(plugin, mine.getDelay());
    }

    private void scheduleFarmRestore(Location location, Material material, Farm farm) {
        Material original = material;
        new BukkitRunnable() {
            @Override
            public void run() {
                Block block = location.getBlock();
                Material current = block.getType();
                if (current != Material.AIR && current != original) {
                    return;
                }
                block.setType(original);
                farm.drawParticle(location, plugin);
                if (block.getBlockData() instanceof Ageable ageable) {
                    ageable.setAge(ageable.getMaximumAge());
                    block.setBlockData(ageable);
                }
            }
        }.runTaskLater(plugin, farm.getDelay());
    }

    private void respawnTree(Tree tree) {
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.regenerateByKey(tree.getKey());
            }
        }.runTaskLater(plugin, tree.getDelay());
    }

    private static boolean isForagingMaterial(Material material) {
        String name = material.name();
        return name.contains("_LOG") || name.contains("_STEM") || name.contains("_WOOD");
    }

    private record BlockKey(String world, int x, int y, int z) {
        static BlockKey from(Block block) {
            Location loc = block.getLocation();
            return new BlockKey(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }

    private record PendingBreak(Material material, BlockLoc blockLoc, boolean cropFullyMature) {
    }
}
