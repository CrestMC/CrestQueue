package club.crestmc.queue.queue;

import club.crestmc.queue.Queue;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class QueueListener implements Listener {

    private final Queue plugin;

    public QueueListener(Queue plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (!plugin.getQueueManager().getQueuedPlayers().containsKey(uuid)) {
            return;
        }

        plugin.getQueueManager().getOnlineQueuedPlayers().put(uuid, plugin.getQueueManager().getQueuedPlayers().get(uuid));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getQueueManager().getExitingPlayers().containsKey(event.getPlayer().getUniqueId())) {
            plugin.getQueueManager().getQueuedPlayers().remove(event.getPlayer().getUniqueId());
            plugin.getQueueManager().getOnlineQueuedPlayers().remove(event.getPlayer().getUniqueId());
            plugin.getQueueManager().getExitingPlayers().remove(event.getPlayer().getUniqueId());
            return;
        }

        if (!plugin.getQueueManager().getQueuedPlayers().containsKey(event.getPlayer().getUniqueId())) {
            return;
        }

        plugin.getQueueManager().getOnlineQueuedPlayers().remove(event.getPlayer().getUniqueId());
    }

}
