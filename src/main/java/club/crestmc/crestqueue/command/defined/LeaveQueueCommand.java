package club.crestmc.crestqueue.command.defined;

import club.crestmc.crestqueue.CrestQueue;
import club.crestmc.crestqueue.command.CommandBase;
import club.crestmc.crestqueue.queue.Queue;
import club.crestmc.crestqueue.util.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LeaveQueueCommand extends CommandBase implements TabCompleter {

    private final CrestQueue plugin;

    protected LeaveQueueCommand(CrestQueue plugin) {
        super(plugin.getName());
        setName("leavequeue");
        setAliases(Arrays.asList("leaveq", "queueleave", "exitqueue", "leave"));
        setDescription("Leaves a server queue");
        setPermission("crestqueue.leave");

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

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Invalid arguments! Usage: /leavequeue <queue name>");
            return true;
        }

        Player player = (Player) sender;
        Queue queue = plugin.getQueueManager().getQueue(args[0]);

        if (!plugin.getQueueManager().isPlayerQueued(player.getUniqueId())) {
            player.sendMessage(Messages.NOT_IN_QUEUE + "");
            return true;
        }

        if (queue == null) {
            player.sendMessage(Messages.QUEUE_INVALID.toString().replace("{name}", args[0]));
            return true;
        }

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
