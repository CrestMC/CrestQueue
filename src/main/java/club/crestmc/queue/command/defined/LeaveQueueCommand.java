package club.crestmc.queue.command.defined;

import club.crestmc.queue.QueuePlugin;
import club.crestmc.queue.command.CommandBase;
import club.crestmc.queue.queue.Queue;
import club.crestmc.queue.util.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LeaveQueueCommand extends CommandBase implements TabCompleter {

    private final QueuePlugin plugin;

    protected LeaveQueueCommand(QueuePlugin plugin) {
        super(plugin.getName());
        setName("leavequeue");
        setAliases(Arrays.asList("leaveq", "queueleave", "exitqueue", "leave"));
        setDescription("Leaves a server queue");
        setPermission("queue.leave");

        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return true;
        }

        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Messages.NO_PERMISSION + "");
            return true;
        }

        Player player = (Player) sender;
        String serverName = plugin.getQueueManager().getServerPlayerQueuedFor(player.getUniqueId());
        Queue queue = plugin.getQueueManager().getQueue(serverName);

        player.sendMessage(Messages.LEAVE_QUEUE.toString().replace("{name}", queue.getName()));
        plugin.getQueueManager().removeFromQueue(player.getUniqueId());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;

        return Collections.singletonList(plugin.getQueueManager().getServerPlayerQueuedFor(player.getUniqueId()));
    }
}
