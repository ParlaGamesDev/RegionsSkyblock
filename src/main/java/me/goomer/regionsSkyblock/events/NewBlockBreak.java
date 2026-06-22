package me.goomer.regionsSkyblock.events;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import me.goomer.regionsSkyblock.hooks.WorldGuardHook;
import me.goomer.regionsSkyblock.regions.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.PointedDripstone;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NewBlockBreak implements Listener {

    private final RegionsSkyblock plugin;
    private final RegionsHelper helper;
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
        BlockData blockData = block.getBlockData().clone();
        List<DripstoneSnapshot> dripstoneColumn = blockData.getMaterial() == Material.POINTED_DRIPSTONE
                ? collectDripstoneColumn(block)
                : List.of();
        pendingBreaks.put(
                BlockKey.from(block),
                new PendingBreak(blockData, new BlockLoc(block), cropFullyMature, dripstoneColumn)
        );
    }

    /**
     * Runs after QSkyblockCore uncancels allowed breaks (HIGHEST).
     * ignoreCancelled=true → only blocks that actually broke.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        PendingBreak pending = pendingBreaks.remove(BlockKey.from(block));
        if (pending == null || pending.blockData().getMaterial().isAir()) {
            return;
        }

        if (WorldGuardHook.hasBypass(player)) {
            return;
        }

        BlockData blockData = pending.blockData();
        Material material = blockData.getMaterial();

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
            if (material == Material.POINTED_DRIPSTONE && !pending.dripstoneColumn().isEmpty()) {
                handleDripstoneColumnBreak(mine, BlockKey.from(block), pending.dripstoneColumn());
            } else {
                handleMineBreak(mine, location, blockData);
            }
            return;
        }

        if (tree != null) {
            BlockLoc blockLoc = pending.blockLoc();
            boolean loop = plugin.exists(tree.getKey());
            plugin.addBlockLoc(tree.getKey(), blockLoc);
            if (!loop) {
                respawnTree(tree);
            }
        }
    }

    private void handleDripstoneColumnBreak(Mine mine, BlockKey brokenKey, List<DripstoneSnapshot> column) {
        Location brokenLocation = brokenKey.toLocation();
        if (brokenLocation == null) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                brokenLocation.getBlock().setType(Material.COBBLESTONE);
            }
        }.runTask(plugin);

        List<DripstoneSnapshot> restoreOrder = sortDripstoneColumnForRestore(column);
        int delay = mine.getDelay();
        new BukkitRunnable() {
            @Override
            public void run() {
                for (DripstoneSnapshot snapshot : restoreOrder) {
                    Location location = snapshot.key().toLocation();
                    if (location == null) {
                        continue;
                    }
                    location.getBlock().setBlockData(snapshot.blockData().clone());
                }
            }
        }.runTaskLater(plugin, delay);
    }

    private static List<DripstoneSnapshot> collectDripstoneColumn(Block origin) {
        if (origin.getType() != Material.POINTED_DRIPSTONE) {
            return List.of();
        }

        PointedDripstone originData = (PointedDripstone) origin.getBlockData();
        BlockFace direction = originData.getVerticalDirection();

        List<DripstoneSnapshot> blocks = new ArrayList<>();
        collectDripstoneInDirection(origin, BlockFace.UP, direction, blocks);
        blocks.add(new DripstoneSnapshot(BlockKey.from(origin), origin.getBlockData().clone()));
        collectDripstoneInDirection(origin, BlockFace.DOWN, direction, blocks);
        return blocks;
    }

    private static void collectDripstoneInDirection(
            Block start,
            BlockFace step,
            BlockFace direction,
            List<DripstoneSnapshot> blocks
    ) {
        Block current = start.getRelative(step);
        while (current.getType() == Material.POINTED_DRIPSTONE) {
            PointedDripstone dripstone = (PointedDripstone) current.getBlockData();
            if (dripstone.getVerticalDirection() != direction) {
                break;
            }
            blocks.add(new DripstoneSnapshot(BlockKey.from(current), current.getBlockData().clone()));
            current = current.getRelative(step);
        }
    }

    private static List<DripstoneSnapshot> sortDripstoneColumnForRestore(List<DripstoneSnapshot> column) {
        BlockFace direction = ((PointedDripstone) column.getFirst().blockData()).getVerticalDirection();
        Comparator<DripstoneSnapshot> order = direction == BlockFace.DOWN
                ? Comparator.comparingInt(snapshot -> -snapshot.key().y())
                : Comparator.comparingInt(snapshot -> snapshot.key().y());
        return column.stream().sorted(order).toList();
    }

    private void handleMineBreak(Mine mine, Location location, BlockData original) {
        Material originalMaterial = original.getMaterial();
        Material immediateType = isCobbleVariant(originalMaterial) ? Material.BEDROCK : Material.COBBLESTONE;

        new BukkitRunnable() {
            @Override
            public void run() {
                location.getBlock().setType(immediateType);
            }
        }.runTask(plugin);

        BlockData restored = original.clone();
        new BukkitRunnable() {
            @Override
            public void run() {
                location.getBlock().setBlockData(restored);
            }
        }.runTaskLater(plugin, mine.getDelay());
    }

    private static boolean isCobbleVariant(Material material) {
        return material == Material.COBBLESTONE || material == Material.COBBLED_DEEPSLATE;
    }

    private void scheduleFarmRestore(Location location, Material material, Farm farm) {
        Material original = material;
        new BukkitRunnable() {
            @Override
            public void run() {
                Block block = location.getBlock();
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

        Location toLocation() {
            World bukkitWorld = Bukkit.getWorld(world);
            if (bukkitWorld == null) {
                return null;
            }
            return new Location(bukkitWorld, x, y, z);
        }
    }

    private record DripstoneSnapshot(BlockKey key, BlockData blockData) {
    }

    private record PendingBreak(
            BlockData blockData,
            BlockLoc blockLoc,
            boolean cropFullyMature,
            List<DripstoneSnapshot> dripstoneColumn
    ) {
    }
}
