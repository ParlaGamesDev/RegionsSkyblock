package me.goomer.regionsSkyblock.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Set;

public final class WorldGuardHook {

    private static boolean enabled;
    private static SetFlag<String> allowedBlockBreakFlag;

    private WorldGuardHook() {
    }

    /** Must run in onLoad (before regions load). */
    public static void registerFlag() {
        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            try {
                allowedBlockBreakFlag = new SetFlag<>("allowed-block-break", new StringFlag("material"));
                registry.register(allowedBlockBreakFlag);
            } catch (FlagConflictException e) {
                Flag<?> existing = registry.get("allowed-block-break");
                if (existing instanceof SetFlag) {
                    @SuppressWarnings("unchecked")
                    SetFlag<String> setFlag = (SetFlag<String>) existing;
                    allowedBlockBreakFlag = setFlag;
                }
            }
            enabled = true;
        } catch (Throwable ignored) {
            enabled = false;
        }
    }

    public static void init() {
        if (!enabled) {
            registerFlag();
        } else {
            reloadFlag();
        }
    }

    public static void reloadFlag() {
        if (!enabled) {
            return;
        }
        try {
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            Flag<?> flag = registry.get("allowed-block-break");
            if (flag instanceof SetFlag) {
                @SuppressWarnings("unchecked")
                SetFlag<String> setFlag = (SetFlag<String>) flag;
                allowedBlockBreakFlag = setFlag;
            }
        } catch (Exception ignored) {
            allowedBlockBreakFlag = null;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean hasBypass(Player player) {
        if (!enabled) {
            return false;
        }
        try {
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            return WorldGuard.getInstance().getPlatform().getSessionManager()
                    .hasBypass(localPlayer, BukkitAdapter.adapt(player.getWorld()));
        } catch (NoClassDefFoundError | Exception ignored) {
            return false;
        }
    }

    /**
     * Materials on {@code allowed-block-break} at this location, or null if flag is not set.
     */
    public static Set<String> getAllowedMaterials(Player player, Location location) {
        if (!enabled || allowedBlockBreakFlag == null || location.getWorld() == null) {
            return null;
        }
        try {
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(location));
            return regions.queryValue(localPlayer, allowedBlockBreakFlag);
        } catch (NoClassDefFoundError | Exception ignored) {
            return null;
        }
    }

    public static boolean isMaterialAllowed(String allowedEntry, Material material) {
        if (allowedEntry == null || material.isAir()) {
            return false;
        }
        if (allowedEntry.equalsIgnoreCase(material.name())) {
            return true;
        }
        Material parsed = Material.matchMaterial(allowedEntry.toUpperCase());
        return parsed != null && parsed == material;
    }

    public static boolean isMaterialInAllowList(Player player, Location location, Material material) {
        Set<String> allowed = getAllowedMaterials(player, location);
        if (allowed == null) {
            return true;
        }
        if (allowed.isEmpty()) {
            return false;
        }
        return allowed.stream().anyMatch(entry -> isMaterialAllowed(entry, material));
    }

    public static boolean shouldRegenerateBlock(Player player, Location location, Material material) {
        if (!enabled) {
            return true;
        }
        if (hasBypass(player)) {
            return false;
        }
        return isMaterialInAllowList(player, location, material);
    }
}
