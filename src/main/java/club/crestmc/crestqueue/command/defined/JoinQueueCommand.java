package club.crestmc.crestqueue.command.defined;

import club.crestmc.crestqueue.CrestQueue;
import club.crestmc.crestqueue.command.CommandBase;
import club.crestmc.crestqueue.queue.Queue;
import club.crestmc.crestqueue.util.lang.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class JoinQueueCommand extends CommandBase {

    private final CrestQueue plugin;

    protected JoinQueueCommand(CrestQueue plugin) {
        super(plugin.getName());
        setName("joinqueue");
        setAliases(Arrays.asList("joinq", "queuejoin", "enterqueue", "join"));
        setDescription("Joins a server queue");
        setPermission("crestqueue.join");

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
            sender.sendMessage(ChatColor.RED + "Invalid arguments! Usage: /joinqueue <queue name>");
            return true;
        }

        Player player = (Player) sender;
        Queue queue = plugin.getQueueManager().getQueue(args[0]);

        if (queue == null) {
            player.sendMessage(Messages.QUEUE_INVALID.toString().replace("{name}", args[0]));
            return true;
        }

        if (plugin.getQueueManager().isPlayerQueued(player.getUniqueId())) {
            player.sendMessage(Messages.ALREADY_IN_QUEUE + "");
            return true;
        }

        if (!player.hasPermission(queue.getPermission())) {
            player.sendMessage(Messages.NO_PERMISSION_JOIN_QUEUE.toString().replace("{name}", queue.getName()));
            return true;
        }

        player.sendMessage(Messages.JOIN_QUEUE.toString().replace("{name}", queue.getName()));
        plugin.getQueueManager().addToQueue(player.getUniqueId(), queue.getName());
        return true;
    }
}
