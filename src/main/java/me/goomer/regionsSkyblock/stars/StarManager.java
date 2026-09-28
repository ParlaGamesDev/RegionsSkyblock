package me.goomer.regionsSkyblock.stars;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import me.goomer.regionsSkyblock.regions.Farm;
import me.goomer.regionsSkyblock.regions.Loc;
import me.goomer.regionsSkyblock.regions.RegionsHelper;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class StarManager implements Listener {

    private static final float SCALE = 0.6f;

    private final RegionsSkyblock plugin;
    private final NamespacedKey farmStarKey;
    private final Map<String, ItemDisplay> stars = new HashMap<>();
    private final Map<String, Location> bases = new HashMap<>();
    private BukkitTask animationTask;
    private float yaw;

    public StarManager(RegionsSkyblock plugin) {
        this.plugin = plugin;
        this.farmStarKey = new NamespacedKey(plugin, "farm-star");
    }

    public void start() {
        purgeLoadedChunks();
        spawnAll();
        animationTask = new BukkitRunnable() {
            @Override
            public void run() {
                animate();
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void shutdown() {
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        for (ItemDisplay star : List.copyOf(stars.values())) {
            if (star.isValid()) {
                star.remove();
            }
        }
        stars.clear();
        bases.clear();
    }

    public void spawn(String farmKey, Loc starLoc) {
        if (starLoc == null || starLoc.getWorld() == null) {
            return;
        }
        World world = Bukkit.getWorld(starLoc.getWorld());
        if (world == null) {
            return;
        }
        Location base = new Location(world, starLoc.getX() + 0.5, starLoc.getY() + 0.5, starLoc.getZ() + 0.5);
        try {
            spawnAt(farmKey, base);
        } catch (RuntimeException e) {
            plugin.getLogger().log(Level.WARNING, "Could not spawn star for farm " + farmKey, e);
        }
    }

    public void remove(String farmKey) {
        bases.remove(farmKey);
        ItemDisplay star = stars.remove(farmKey);
        if (star != null && star.isValid()) {
            star.remove();
        }
    }

    public void refreshAppearance() {
        for (ItemDisplay star : stars.values()) {
            if (star.isValid()) {
                applyAppearance(star);
            }
        }
    }

    /** Point just under the star head, following its bobbing. */
    public Location beamTarget(String farmKey) {
        ItemDisplay display = stars.get(farmKey);
        Location base = display != null && display.isValid() ? display.getLocation() : bases.get(farmKey);
        return base == null ? null : base.clone().add(0.0, -0.3, 0.0);
    }

    private void spawnAt(String farmKey, Location base) {
        World world = base.getWorld();
        ItemDisplay existing = findExisting(world, base, farmKey);
        if (existing != null) {
            existing.setPersistent(false);
            existing.teleport(base);
            applyAppearance(existing);
            track(farmKey, existing, base);
            return;
        }

        removeOrphans(world, base, farmKey);

        ItemDisplay star = (ItemDisplay) world.spawnEntity(base, EntityType.ITEM_DISPLAY);
        star.setPersistent(false);
        applyAppearance(star);
        star.getPersistentDataContainer().set(farmStarKey, PersistentDataType.STRING, farmKey);
        track(farmKey, star, base);
    }

    private void track(String farmKey, ItemDisplay star, Location base) {
        stars.put(farmKey, star);
        bases.put(farmKey, base.clone());
    }

    private void animate() {
        yaw += 6f;
        if (yaw >= 360f * 10) {
            yaw -= 360f * 10;
        }
        Quaternionf spin = new Quaternionf().rotateY((float) Math.toRadians(yaw));
        double bob = Math.sin(Math.toRadians(yaw * 0.7)) * 0.1;

        for (Map.Entry<String, ItemDisplay> entry : stars.entrySet()) {
            ItemDisplay star = entry.getValue();
            Location base = bases.get(entry.getKey());
            if (base == null || !star.isValid()) {
                continue;
            }
            Transformation transformation = star.getTransformation();
            transformation.getLeftRotation().set(spin);
            transformation.getScale().set(SCALE);
            star.setTransformation(transformation);

            Location location = base.clone();
            location.setY(base.getY() + bob);
            star.teleport(location);
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        for (Farm farm : farms()) {
            Loc star = farm.getStar();
            if (star != null && world.getName().equals(star.getWorld())) {
                spawn(farm.getKey(), star);
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        String worldName = chunk.getWorld().getName();
        for (Farm farm : farms()) {
            Loc star = farm.getStar();
            if (star == null || !worldName.equals(star.getWorld())) {
                continue;
            }
            if ((star.getX() >> 4) != chunk.getX() || (star.getZ() >> 4) != chunk.getZ()) {
                continue;
            }
            ItemDisplay current = stars.get(farm.getKey());
            if (current == null || !current.isValid()) {
                spawn(farm.getKey(), star);
            }
        }
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof ItemDisplay display
                    && display.getPersistentDataContainer().has(farmStarKey, PersistentDataType.STRING)
                    && !stars.containsValue(display)) {
                display.remove();
            }
        }
    }

    private List<Farm> farms() {
        try {
            return new RegionsHelper(plugin).getAllFarms();
        } catch (RuntimeException e) {
            plugin.getLogger().log(Level.WARNING, "Could not read farms from config.yml", e);
            return List.of();
        }
    }

    private void spawnAll() {
        for (Farm farm : farms()) {
            if (farm.getStar() != null) {
                spawn(farm.getKey(), farm.getStar());
            }
        }
    }

    private void purgeLoadedChunks() {
        for (World world : Bukkit.getWorlds()) {
            for (ItemDisplay display : world.getEntitiesByClass(ItemDisplay.class)) {
                if (display.getPersistentDataContainer().has(farmStarKey, PersistentDataType.STRING)) {
                    display.remove();
                }
            }
        }
    }

    private ItemDisplay findExisting(World world, Location location, String farmKey) {
        for (Entity entity : world.getNearbyEntities(location, 3, 3, 3)) {
            if (entity instanceof ItemDisplay display) {
                String tagged = display.getPersistentDataContainer().get(farmStarKey, PersistentDataType.STRING);
                if (farmKey.equals(tagged)) {
                    return display;
                }
            }
        }
        return null;
    }

    private void removeOrphans(World world, Location location, String farmKey) {
        ItemDisplay tracked = stars.remove(farmKey);
        if (tracked != null && tracked.isValid()) {
            tracked.remove();
        }
        for (Entity entity : world.getNearbyEntities(location, 3, 3, 3)) {
            if (entity instanceof ItemDisplay display) {
                String tagged = display.getPersistentDataContainer().get(farmStarKey, PersistentDataType.STRING);
                if (farmKey.equals(tagged) || tagged == null) {
                    display.remove();
                }
            }
        }
    }

    private void applyAppearance(ItemDisplay star) {
        star.setItemStack(createHead());
        star.setBillboard(Display.Billboard.FIXED);
        star.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
        star.setTeleportDuration(2);
        star.setInterpolationDuration(2);
        star.setInterpolationDelay(0);
        star.setBrightness(new Display.Brightness(15, 15));
        Transformation transformation = star.getTransformation();
        transformation.getScale().set(SCALE);
        star.setTransformation(transformation);
    }

    private ItemStack createHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        String texture = plugin.getConfig().getString("head", "");
        if (texture == null || texture.isBlank()) {
            return head;
        }
        try {
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            profile.getTextures().setSkin(URI.create(texture).toURL());
            meta.setOwnerProfile(profile);
            head.setItemMeta(meta);
        } catch (IllegalArgumentException | MalformedURLException e) {
            plugin.getLogger().warning("Invalid star head URL in config.yml (head: " + texture + "). Using a default player head.");
        }
        return head;
    }
}
