package me.goomer.regionsSkyblock.events;

import me.goomer.regionsSkyblock.hooks.WorldGuardHook;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Set;

/**
 * Enforces WorldGuard {@code allowed-block-break} — same job as QSkyblockCore.
 * WorldGuard denies build by default; this listener re-allows listed materials.
 */
public class AllowedBlockBreakListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!WorldGuardHook.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (WorldGuardHook.hasBypass(player)) {
            return;
        }

        Set<String> allowedMaterials = WorldGuardHook.getAllowedMaterials(
                player, event.getBlock().getLocation());

        if (allowedMaterials == null) {
            return;
        }

        Material blockMaterial = event.getBlock().getType();
        boolean allowed = allowedMaterials.stream()
                .anyMatch(entry -> WorldGuardHook.isMaterialAllowed(entry, blockMaterial));

        if (allowed) {
            event.setCancelled(false);
        } else {
            event.setCancelled(true);
        }
    }
}
