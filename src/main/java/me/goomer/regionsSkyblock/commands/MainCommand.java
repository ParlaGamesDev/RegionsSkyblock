package me.goomer.regionsSkyblock.commands;

import me.goomer.regionsSkyblock.RegionsSkyblock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class MainCommand implements CommandExecutor {

    private ArrayList<SubCommand> commands;
    private RegionsSkyblock plugin;

    public MainCommand(RegionsSkyblock plugin){
        commands = new ArrayList<>();
        this.plugin = plugin;
        commands.add(new Add(plugin));
        commands.add(new Remove(plugin));
        commands.add(new SetHead(plugin));
        commands.add(new ListRegions(plugin));
        commands.add(new RegenAll(plugin));
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length>0){
            for(SubCommand cmd : commands){
                if(cmd.getName().equals(strings[0])){
                    cmd.execute(commandSender, strings);
                }
            }
        }
        else{
            for(SubCommand cmd : commands){
                send(commandSender, cmd.getSyntax() + " - " + cmd.getDesc());
            }
        }
        return true;
    }

    public void send(CommandSender commandSender, String msg){
        if(commandSender instanceof Player p){
            p.sendMessage(msg);
        }
        else{
            System.out.println(msg);
        }
    }
}
