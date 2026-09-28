package me.goomer.regionsSkyblock.regions;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;

public class BlockLoc extends Loc{
    private String block;
    private Axis face;
    private BlockData blockData;

    public BlockLoc(int x, int y, int z, String world, String block, Axis face) {
        super(x, y, z, world);
        this.block = block;
        this.face = face;
    }

    public BlockLoc(Block block){
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
        this.block = block.getType().name();
        this.face = null;
        this.blockData = block.getBlockData().clone();
        if(block.getBlockData() instanceof Orientable orientable){
            this.face = orientable.getAxis();
        }
    }

    /** Exact state at capture time (leaf persistence, log axis, stairs facing...), or null if unknown. */
    public BlockData getBlockData() {
        return blockData == null ? null : blockData.clone();
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public void setBlockAt(){
        Block block = getBlockAt();
        if(block != null)
            block.setType(Material.getMaterial(this.block));
    }

    public Axis getFace() {
        return face;
    }

    public void setFace(Axis face) {
        this.face = face;
    }
}
