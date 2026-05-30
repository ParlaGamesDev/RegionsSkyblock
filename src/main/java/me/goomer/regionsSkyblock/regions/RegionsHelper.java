package me.goomer.regionsSkyblock.regions;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Random;

public class RegionsHelper {

    private RegionsSkyblock plugin;

    public RegionsHelper(RegionsSkyblock plugin){
        this.plugin = plugin;
    }

    public Mine getMineByKey(String key){
        String world = plugin.getConfig().getString("mines."+key+".world");

        int x1 = plugin.getConfig().getInt("mines." + key + ".loc1.x");
        int y1 = plugin.getConfig().getInt("mines." + key + ".loc1.y");
        int z1 = plugin.getConfig().getInt("mines." + key + ".loc1.z");
        Loc loc1 = new Loc(x1, y1, z1, world);

        int x2 = plugin.getConfig().getInt("mines." + key + ".loc2.x");
        int y2 = plugin.getConfig().getInt("mines." + key + ".loc2.y");
        int z2 = plugin.getConfig().getInt("mines." + key + ".loc2.z");
        Loc loc2 = new Loc(x2, y2, z2, world);

        int minDelay = plugin.getConfig().getInt("mines."+key+".minDelay");
        int maxDelay = plugin.getConfig().getInt("mines."+key+".maxDelay");

        return new Mine(loc1, loc2, minDelay, maxDelay, key);
    }

    public Mine getMineByLocation(Location location){
        if (plugin.getConfig().getConfigurationSection("mines") == null) {
            return null;
        }
        for(String key : plugin.getConfig().getConfigurationSection("mines").getKeys(false)){
            Mine mine = getMineByKey(key);
            if(mine.contains(location)){
                return mine;
            }
        }
        return null;
    }

    public ArrayList<Mine> getAllMines(){
        ArrayList<Mine> mines = new ArrayList<>();

        for(String key : plugin.getConfig().getConfigurationSection("mines").getKeys(false)){
            mines.add(getMineByKey(key));
        }

        return mines;
    }

    public Farm getFarmByKey(String key){
        int minDelay = plugin.getConfig().getInt("farms."+key+".minDelay");
        int maxDelay = plugin.getConfig().getInt("farms."+key+".maxDelay");
        String block = plugin.getConfig().getString("farms."+key+".block");

        if(plugin.getConfig().contains("farms." + key + ".star")){
            int x = plugin.getConfig().getInt("farms." + key + ".star.x");
            int y = plugin.getConfig().getInt("farms." + key + ".star.y");
            int z = plugin.getConfig().getInt("farms." + key + ".star.z");
            String world = plugin.getConfig().getString("farms."+key+".star.world");
            Loc star = new Loc(x, y, z, world);

            return new Farm(key, block, minDelay, maxDelay, star);
        }

        return new Farm(key, block, minDelay, maxDelay);
    }

    public Farm getFarmByBlock(Block block){
        for(String key : plugin.getConfig().getConfigurationSection("farms").getKeys(false)){
            Farm farm = getFarmByKey(key);
            if(farm.contains(block)){
                return farm;
            }
        }
        return null;
    }

    public ArrayList<Farm> getAllFarms(){
        ArrayList<Farm> farms = new ArrayList<>();

        for(String key : plugin.getConfig().getConfigurationSection("farms").getKeys(false)){
            farms.add(getFarmByKey(key));
        }

        return farms;
    }

    public static Tree getTreeByKey(String key){
        String world = RegionsSkyblock.instance.getConfig().getString("trees."+key+".world");

        int x1 = RegionsSkyblock.instance.getConfig().getInt("trees." + key + ".loc1.x");
        int y1 = RegionsSkyblock.instance.getConfig().getInt("trees." + key + ".loc1.y");
        int z1 = RegionsSkyblock.instance.getConfig().getInt("trees." + key + ".loc1.z");
        Loc loc1 = new Loc(x1, y1, z1, world);

        int x2 = RegionsSkyblock.instance.getConfig().getInt("trees." + key + ".loc2.x");
        int y2 = RegionsSkyblock.instance.getConfig().getInt("trees." + key + ".loc2.y");
        int z2 = RegionsSkyblock.instance.getConfig().getInt("trees." + key + ".loc2.z");
        Loc loc2 = new Loc(x2, y2, z2, world);

        int minDelay = RegionsSkyblock.instance.getConfig().getInt("trees."+key+".minDelay");
        int maxDelay = RegionsSkyblock.instance.getConfig().getInt("trees."+key+".maxDelay");

        return new Tree(loc1, loc2, minDelay, maxDelay, key);
    }

    public static Tree getTreeByLocation(Location location){
        for(String key : RegionsSkyblock.instance.getConfig().getConfigurationSection("trees").getKeys(false)){
            Tree tree = getTreeByKey(key);
            if(tree.contains(location)){
                return tree;
            }
        }
        return null;
    }

    public ArrayList<Tree> getAllTrees(){
        ArrayList<Tree> trees = new ArrayList<>();

        for(String key : plugin.getConfig().getConfigurationSection("trees").getKeys(false)){
            trees.add(getTreeByKey(key));
        }

        return trees;
    }
}
