package club.crestmc.queue.command.defined;

import club.crestmc.queue.QueuePlugin;
import club.crestmc.queue.command.CommandBase;
import club.crestmc.queue.util.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JoinQueueCommand extends CommandBase implements TabCompleter {

    private final QueuePlugin plugin;

    protected JoinQueueCommand(QueuePlugin plugin) {
        super(plugin.getName());
        setName("joinqueue");
        setAliases(Arrays.asList("joinq", "queuejoin", "enterqueue", "join"));
        setDescription("Joins a server queue");
        setPermission("queue.join");

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
        club.crestmc.queue.queue.Queue queue = plugin.getQueueManager().getQueue(args[0]);

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


    @Override
    @SuppressWarnings("unchecked")
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return (List<String>) plugin.getQueueManager().getQueues().stream().map(club.crestmc.queue.queue.Queue::getName).collect(Collectors.toSet());
    }
}
