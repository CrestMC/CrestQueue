package club.crestmc.crestqueue.command.defined;

import club.crestmc.crestqueue.CrestQueue;
import club.crestmc.crestqueue.command.CommandBase;
import club.crestmc.crestqueue.queue.Queue;
import club.crestmc.crestqueue.util.lang.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class LeaveQueueCommand extends CommandBase {

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
}
