package me.goomer.regionsSkyblock;

import me.goomer.regionsSkyblock.commands.*;
import me.goomer.regionsSkyblock.events.AllowedBlockBreakListener;
import me.goomer.regionsSkyblock.events.NewBlockBreak;
import me.goomer.regionsSkyblock.events.SkyblockItemsListener;
import me.goomer.regionsSkyblock.hooks.AuraSkillsHook;
import me.goomer.regionsSkyblock.hooks.WorldGuardHook;
import me.goomer.regionsSkyblock.regions.BlockLoc;
import me.goomer.regionsSkyblock.regions.Farm;
import me.goomer.regionsSkyblock.regions.RegionsHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public final class RegionsSkyblock extends JavaPlugin {

    public static RegionsSkyblock instance;

    HashMap<String, ArrayList<BlockLoc>> blocks;
    HashMap<String, ItemDisplay> stars;
    private AuraSkillsHook auraSkillsHook;

    @Override
    public void onLoad() {
        WorldGuardHook.registerFlag();
    }

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        saveDefaultConfig();
        blocks = new HashMap<>();
        stars = new HashMap<>();

        WorldGuardHook.init();
        new BukkitRunnable() {
            @Override
            public void run() {
                WorldGuardHook.reloadFlag();
            }
        }.runTaskLater(this, 1L);

        if (getServer().getPluginManager().isPluginEnabled("AuraSkills")) {
            this.auraSkillsHook = new AuraSkillsHook();
        }

        getServer().getPluginManager().registerEvents(new AllowedBlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new NewBlockBreak(this), this);
        getServer().getPluginManager().registerEvents(new SkyblockItemsListener(), this);
        getCommand("regions").setExecutor(new MainCommand(this));

        regenerateStars();

        new BukkitRunnable() {
            float yaw = 0;

            @Override
            public void run() {
                yaw += 3;
                for(ItemDisplay star : stars.values()){
                    star.setRotation(yaw, 0);
                    Location l = star.getLocation();
                    l.setY(l.getY() + Math.sin(yaw/30) * 0.02);
                    star.teleport(l);
                }

            }
        }.runTaskTimer(this, 0L, 1L);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        removeAllStars();
    }

    public void regenerateFirst(String key, boolean isStar){
        ArrayList<BlockLoc> rblocks = blocks.get(key);
        if(rblocks != null && !rblocks.isEmpty()){
            BlockLoc blockLoc = rblocks.removeFirst();
            Block block = blockLoc.getBlockAt();
            block.setType(Material.getMaterial(blockLoc.getBlock()));
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

    public void addStar(String farm, int x, int y, int z, String worldName){
        ItemStack head = createHead();

        World world = Bukkit.getWorld(worldName);
        Location location = new Location(world, x, y, z);

        ItemDisplay star = (ItemDisplay) world.spawnEntity(
                location,
                EntityType.ITEM_DISPLAY
        );

        star.setItemStack(head);

        Transformation t = star.getTransformation();
        t.getScale().set(0.6f);
        star.setTransformation(t);

        stars.put(farm, star);

    }

    public ItemStack createHead(){
        String string = getConfig().getString("head");
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());

        try {
            profile.getTextures().setSkin(URI.create(string).toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        meta.setOwnerProfile(profile);
        head.setItemMeta(meta);

        return head;
    }

    public void removeStar(String farm){
        if(stars.containsKey(farm))
            stars.get(farm).remove();
    }

    public void regenerateStars(){
        RegionsHelper helper = new RegionsHelper(this);
        ArrayList<Farm> farms = helper.getAllFarms();
        for(Farm f : farms){
            if(f.getStar() != null){
                addStar(f.getKey(), f.getStar().getX(), f.getStar().getY(), f.getStar().getZ(), f.getStar().getWorld());
            }
        }
    }

    public void removeAllStars(){
        for(ItemDisplay star : stars.values()){
            star.remove();
        }
    }

    public AuraSkillsHook getAuraSkillsHook() {
        return auraSkillsHook;
    }

    public boolean isAuraSkillsEnabled() {
        return auraSkillsHook != null;
    }
}
