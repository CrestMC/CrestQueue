package club.crestmc.queue.listeners;

import club.crestmc.queue.Queue;
import club.crestmc.queue.queue.QueueManager;
import me.blurmit.basics.events.PlaceholderRequestEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QueuePlaceholder implements Listener {

    private final Queue plugin;

    public QueuePlaceholder(Queue plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlaceholderRequest(PlaceholderRequestEvent event) {
        String placeholder = event.getPlaceholder();

        if (!placeholder.startsWith("queue-")) {
            return;
        }

        QueueManager queue = plugin.getQueueManager();
        String placeholderName = placeholder.replaceFirst("queue-", "");

        try {
            if (placeholderName.startsWith("players-")) {
                String server = placeholderName.replaceFirst("players-", "");
                event.setResponse(queue.getPlayersQueuedFor(server).size() + "");
            }
        } catch (Exception e) {
            event.setResponse("");
        }
    }

}
