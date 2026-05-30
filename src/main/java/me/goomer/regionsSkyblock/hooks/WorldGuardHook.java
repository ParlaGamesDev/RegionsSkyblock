package me.goomer.regionsSkyblock.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Integrates with the {@code allowed-block-break} WorldGuard flag (registered by SkyblockCore).
 * Only materials listed on that flag should trigger mine regen.
 */
public final class WorldGuardHook {

    private static boolean enabled;
    private static SetFlag<String> allowedBlockBreakFlag;

    private WorldGuardHook() {
    }

    public static void init() {
        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            enabled = true;
            loadAllowedBlockBreakFlag();
        } catch (ClassNotFoundException ignored) {
            enabled = false;
        }
    }

    private static void loadAllowedBlockBreakFlag() {
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
     * Same rules as SkyblockCore {@code AllowedBlockBreakListener}:
     * - flag not set on region → no material restriction → regen allowed
     * - flag set → regen only for materials in the set
     */
    public static boolean shouldRegenerateBlock(Player player, Location location, Material material) {
        if (!enabled) {
            return true;
        }
        if (hasBypass(player)) {
            return false;
        }
        if (allowedBlockBreakFlag == null) {
            return true;
        }

        try {
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(location));
            Set<String> allowedMaterials = regions.queryValue(localPlayer, allowedBlockBreakFlag);

            if (allowedMaterials == null) {
                return true;
            }

            String materialName = material.name();
            return allowedMaterials.stream()
                    .anyMatch(allowed -> allowed != null && allowed.equalsIgnoreCase(materialName));
        } catch (NoClassDefFoundError | Exception ignored) {
            return true;
        }
    }
}
