package club.crestmc.crestqueue.queue;

import club.crestmc.crestqueue.CrestQueue;
import club.crestmc.crestqueue.util.lang.Messages;
import club.crestmc.crestqueue.util.pluginmessage.PluginMessageHelper;
import club.crestmc.crestqueue.util.uuid.UUIDs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class QueueManager {

    private final CrestQueue plugin;
    private BukkitTask task;

    private final Set<Queue> queues;
    private final Map<UUID, String> queuedPlayers;
    private final Map<UUID, String> onlineQueuedPlayers;

    public QueueManager(CrestQueue plugin) {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getServer().getPluginManager().registerEvents(new QueueListener(plugin), plugin);

        this.plugin = plugin;
        this.task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::runQueue, 1L, 1L);

        this.queues = new HashSet<>();
        this.queuedPlayers = new HashMap<>();
        this.onlineQueuedPlayers = new HashMap<>();

        loadQueues();
    }

    public void addToQueue(UUID uuid, String serverName) {
        queuedPlayers.put(uuid, serverName);
        onlineQueuedPlayers.put(uuid, serverName);
    }

    public void removeFromQueue(UUID uuid) {
        queuedPlayers.remove(uuid);
        onlineQueuedPlayers.remove(uuid);
    }

    public void sendToServer(UUID uuid) {
        Player player = plugin.getServer().getPlayer(uuid);

        if (player == null) {
            return;
        }

        player.sendMessage(Messages.SENT_QUEUE.toString().replace("{name}", queuedPlayers.get(uuid)));
        PluginMessageHelper.sendData("BungeeCord", "ConnectOther", player.getName(), queuedPlayers.get(uuid));

        queuedPlayers.remove(uuid);
        onlineQueuedPlayers.remove(uuid);
    }

    private void runQueue() {
        // Don't run the queue if there are no online players
        if (plugin.getServer().getOnlinePlayers().size() == 0) {
            return;
        }

        // Don't run the queue if nobody is queued
        if (queuedPlayers.size() == 0) {
            return;
        }

        // If all players in the queue are offline, clear the queue
        if (onlineQueuedPlayers.size() == 0) {
            queuedPlayers.clear();
            return;
        }

        queuedPlayers.keySet().stream().sorted(Comparator.comparingLong(this::getPriority)).forEach(uuid -> {
            Player player = plugin.getServer().getPlayer(uuid);
            String server = queuedPlayers.get(uuid);
            String waitMessage = Messages.QUEUE_WAIT.toString()
                    .replace("{name}", server)
                    .replace("{queue-total}", getPlayersQueuedFor(server).size() + "")
                    .replace("{queue-position}", getPlayersQueuedFor(server).size() + "");

            if (isServerOffline(server)) {
                if (player != null) {
                    player.sendMessage(waitMessage.replace("{queue-server-state}", "Offline"));
                }
                return;
            }

            if (isServerFull(server)) {
                if (player == null) {
                    return;
                }

                if (player.hasPermission("crestqueue.bypass." + server)) {
                    sendToServer(uuid);
                    return;
                }

                player.sendMessage(waitMessage.replace("{queue-server-state}", "Full"));
                return;
            }

            sendToServer(uuid);
        });
    }

    private void loadQueues() {
        plugin.getConfig().getConfigurationSection("Valid-Queues").getValues(false).forEach((key, values) -> {
            ConfigurationSection section = (ConfigurationSection) values;

            Queue queue = new Queue();
            queue.setName(key);
            queue.setPermission(section.getString("permission"));
            queue.setServer(section.getString("server"));

            queues.add(queue);
        });
    }

    public Queue getQueue(String queueName) {
        return queues.stream().filter(queue -> queue.getName().equalsIgnoreCase(queueName)).findFirst().orElse(null);
    }

    public Set<UUID> getPlayersQueuedFor(String serverName) {
        return queuedPlayers.keySet().stream().filter(player -> queuedPlayers.get(player).equalsIgnoreCase(serverName)).collect(Collectors.toSet());
    }

    public Map<UUID, String> getQueuedPlayers() {
        return queuedPlayers;
    }

    public Map<UUID, String> getOnlineQueuedPlayers() {
        return onlineQueuedPlayers;
    }

    public boolean isPlayerQueued(UUID uuid) {
        return queuedPlayers.containsKey(uuid);
    }

    public boolean isServerFull(String server) {
        try {
            String response = getServerStatus(server);

            int online = Integer.parseInt(response.split("/")[0]);
            int max = Integer.parseInt(response.split("/")[1]);

            return online >= max;
        } catch (ReflectiveOperationException | NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isServerOffline(String server) {
        try {
            return getServerStatus(server).contains("Offline");
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isServerWhitelisted(String server) {
        try {
            return getServerStatus(server).contains("Whitelisted");
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return false;
        }
    }


    private String getServerStatus(String server) throws ReflectiveOperationException {
        Event placeholderRequestEvent = (Event) Class.forName("me.blurmit.basics.events.PlaceholderRequestEvent")
                    .getConstructors()[0]
                    .newInstance("playercount-" + server, null, null, null, null, null, true);

        plugin.getServer().getPluginManager().callEvent(placeholderRequestEvent);

        return (String) placeholderRequestEvent.getClass().getMethod("getResponse").invoke(placeholderRequestEvent);
    }

    private long getPriority(UUID uuid) {
        try {
            Class<? extends JavaPlugin> basicsClass = (Class<? extends JavaPlugin>) Class.forName("me.blurmit.Basics");
            JavaPlugin basicsPlugin = JavaPlugin.getPlugin(basicsClass);

            Object rankManager = basicsClass.getMethod("getRankManager").invoke(basicsPlugin);
            Object rank = rankManager.getClass().getMethod("getHighestRankByPriority", UUID.class).invoke(rankManager, UUID.randomUUID());

            return (long) rank.getClass().getMethod("getPriority").invoke(rank);
        } catch (ReflectiveOperationException e) {
            Player player = plugin.getServer().getPlayer(uuid);

            if (player == null) {
                return 0;
            }

            return player.hasPermission("crestqueue.priority") ? 1 : 0;
        }
    }

}
