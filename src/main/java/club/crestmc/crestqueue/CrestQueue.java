package club.crestmc.crestqueue;

import club.crestmc.crestqueue.command.CommandManager;
import club.crestmc.crestqueue.queue.QueueManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrestQueue extends JavaPlugin {

    private QueueManager queueManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        getLogger().info("Loading configuration...");
        saveDefaultConfig();

        getLogger().info("Loading queue manager...");
        queueManager = new QueueManager(this);

        getLogger().info("Loading commands...");
        commandManager = new CommandManager(this);
        commandManager.registerCommands();

        getLogger().info("CrestQueue has been successfully enabled.");
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

}
