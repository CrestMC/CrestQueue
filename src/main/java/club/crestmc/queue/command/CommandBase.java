package club.crestmc.queue.command;

import club.crestmc.queue.Queue;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CommandBase extends BukkitCommand {

    protected CommandBase(String name) {
        super(name);
    }

    public void registerCommand() {
        JavaPlugin.getPlugin(Queue.class).getCommandManager().register(getName(), this);
    }

}
