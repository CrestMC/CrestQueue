package club.crestmc.queue;

import club.crestmc.queue.command.CommandManager;
import club.crestmc.queue.listeners.QueuePlaceholder;
import club.crestmc.queue.queue.QueueManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Queue extends JavaPlugin {

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

        getLogger().info("Registering placeholders...");
        new QueuePlaceholder(this);

        getLogger().info(getName() + " has been successfully enabled.");
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

}
