package club.crestmc.queue.command;

import club.crestmc.queue.QueuePlugin;
import club.crestmc.queue.util.ReflectionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;

public class CommandManager {

    private final QueuePlugin plugin;
    private SimpleCommandMap commandMap;

    public CommandManager(QueuePlugin plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        ReflectionUtil.consume("club.crestmc.queue.command.defined", QueuePlugin.class.getClassLoader(), CommandBase.class, CommandBase::registerCommand, true, plugin);
    }

    public void register(String command, CommandBase commandClass) {
        // Check if the server's plugin manager is SimplePluginManager
        if (!(plugin.getServer().getPluginManager() instanceof SimplePluginManager)) {
            plugin.getLogger().severe("Could not register command " + command + ": SimplePluginManager not found!");
            return;
        }

        final Map<String, Command> knownCommandsMap;

        try {
            // Get the commandmap field
            Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (SimpleCommandMap) commandMapField.get(plugin.getServer().getPluginManager());

            // Get the direct hashmap of known commands
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            knownCommandsMap = (Map<String, Command>) knownCommandsField.get(commandMap);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not register command " + command + ": " + e.getMessage(), e);
            return;
        }

        // Remove command if it's already registered by another plugin
        knownCommandsMap.remove(command.toLowerCase());
        commandClass.getAliases().forEach(alias -> knownCommandsMap.remove(alias.toLowerCase()));

        // Register command in the command map
        commandMap.register(commandClass.getName(), plugin.getName().toLowerCase(), commandClass);

        // Remove fallback prefix command
        knownCommandsMap.remove(plugin.getName().toLowerCase() + ":" + command);
        commandClass.getAliases().forEach(alias -> knownCommandsMap.remove(plugin.getName().toLowerCase() + ":" + alias.toLowerCase()));
    }

}
