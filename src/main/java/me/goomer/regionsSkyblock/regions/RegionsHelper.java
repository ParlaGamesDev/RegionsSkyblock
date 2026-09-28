package me.goomer.regionsSkyblock.regions;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Set;

public class RegionsHelper {

    private RegionsSkyblock plugin;

    public RegionsHelper(RegionsSkyblock plugin){
        this.plugin = plugin;
    }

    private static Set<String> keys(RegionsSkyblock plugin, String section) {
        ConfigurationSection cs = plugin.getConfig().getConfigurationSection(section);
        return cs == null ? Set.of() : cs.getKeys(false);
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
        for(String key : keys(plugin, "mines")){
            Mine mine = getMineByKey(key);
            if(mine.contains(location)){
                return mine;
            }
        }
        return null;
    }

    public ArrayList<Mine> getAllMines(){
        ArrayList<Mine> mines = new ArrayList<>();

        for(String key : keys(plugin, "mines")){
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

    /** Among farms matching this block, the one whose star is closest wins; farms without a star come last. */
    public Farm getFarmByBlock(Block block){
        Farm best = null;
        double bestDistance = Double.MAX_VALUE;
        for(String key : keys(plugin, "farms")){
            Farm farm = getFarmByKey(key);
            if(!farm.contains(block)){
                continue;
            }
            double distance = starDistanceSquared(farm, block);
            if(best == null || distance < bestDistance){
                best = farm;
                bestDistance = distance;
            }
        }
        return best;
    }

    private static double starDistanceSquared(Farm farm, Block block){
        Loc star = farm.getStar();
        if(star == null || star.getWorld() == null || !star.getWorld().equals(block.getWorld().getName())){
            return Double.MAX_VALUE;
        }
        double dx = star.getX() - block.getX();
        double dy = star.getY() - block.getY();
        double dz = star.getZ() - block.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    public ArrayList<Farm> getAllFarms(){
        ArrayList<Farm> farms = new ArrayList<>();

        for(String key : keys(plugin, "farms")){
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
        for(String key : keys(RegionsSkyblock.instance, "trees")){
            Tree tree = getTreeByKey(key);
            if(tree.contains(location)){
                return tree;
            }
        }
        return null;
    }

    public ArrayList<Tree> getAllTrees(){
        ArrayList<Tree> trees = new ArrayList<>();

        for(String key : keys(plugin, "trees")){
            trees.add(getTreeByKey(key));
        }

        return trees;
    }
}
