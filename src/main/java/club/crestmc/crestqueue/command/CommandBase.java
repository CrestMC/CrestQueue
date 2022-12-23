package club.crestmc.crestqueue.command;

import club.crestmc.crestqueue.CrestQueue;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CommandBase extends BukkitCommand {

    protected CommandBase(String name) {
        super(name);
    }

    public void registerCommand() {
        JavaPlugin.getPlugin(CrestQueue.class).getCommandManager().register(getName(), this);
    }

}
