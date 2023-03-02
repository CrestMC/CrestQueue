package club.crestmc.queue.queue;

import club.crestmc.queue.QueuePlugin;
import club.crestmc.queue.util.Collections;
import club.crestmc.queue.util.Messages;
import club.crestmc.queue.util.PluginMessageHelper;
import me.blurmit.basics.Basics;
import me.blurmit.basics.rank.Rank;
import me.blurmit.basics.rank.RankManager;
import me.blurmit.basics.util.Placeholders;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class QueueManager {

    private final QueuePlugin plugin;

    private final Set<club.crestmc.queue.queue.Queue> queues;
    private final NavigableMap<UUID, String> queuedPlayers;
    private final Map<UUID, String> onlineQueuedPlayers;
    private final Map<UUID, Integer> exitingPlayers;

    public QueueManager(QueuePlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getServer().getPluginManager().registerEvents(new QueueListener(plugin), plugin);

        int waitMessageSendInterval = plugin.getConfig().getInt("Waiting-Message-Send-Interval");
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::runQueue, 0L, 10L);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::sendStatusMessages, 20L * waitMessageSendInterval, 20L * waitMessageSendInterval);

        this.queues = new HashSet<>();
        this.queuedPlayers = new TreeMap<>();
        this.onlineQueuedPlayers = new HashMap<>();
        this.exitingPlayers = new HashMap<>();

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

    public void sendToServer(UUID uuid, String server) {
        Player player = plugin.getServer().getPlayer(uuid);

        if (player == null) {
            return;
        }

        if (exitingPlayers.containsKey(uuid)) {
            int attempt = exitingPlayers.get(uuid) + 1;
            int threshold = plugin.getConfig().getInt("Send-Fail-Threshold");

            exitingPlayers.replace(uuid, attempt);

            if (attempt >= threshold) {
                player.sendMessage(Messages.SEND_FAILED.toString().replace("{name}", queuedPlayers.get(uuid)).replace("{attempts}", threshold + ""));

                queuedPlayers.remove(uuid);
                onlineQueuedPlayers.remove(uuid);
                exitingPlayers.remove(uuid);
                return;
            }
        } else {
            exitingPlayers.put(uuid, 0);
        }

        player.sendMessage(Messages.SENT_QUEUE.toString().replace("{name}", server));
        PluginMessageHelper.sendData("BungeeCord", "ConnectOther", player.getName(), server);
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

        Map.Entry<UUID, String> playerEntry = queuedPlayers.firstEntry();

        UUID uuid = playerEntry.getKey();
        Player player = plugin.getServer().getPlayer(uuid);

        String server = playerEntry.getValue();

        if (checkServerState(server, "Offline")) {
            return;
        }

        if (checkServerState(server, "Whitelisted") || isServerFull(server)) {
            if (player == null) {
                return;
            }

            if (player.hasPermission("queue.bypass." + server)) {
                sendToServer(uuid, server);
                return;
            }

            return;
        }

        UUID queuedPlayer = queuedPlayers.keySet().stream().sorted(Comparator.comparingLong(priority -> -getRankPriority(priority))).distinct().toArray(UUID[]::new)[0];
        sendToServer(queuedPlayer, server);
    }

    private void sendStatusMessages() {
        Set<UUID> sortedQueuedPlayers = queuedPlayers.keySet().stream().sorted(Comparator.comparingLong(priority -> -getRankPriority(priority))).collect(Collectors.toCollection(LinkedHashSet::new));

        if (plugin.getServer().getOnlinePlayers().size() == 0) {
            return;
        }

        if (queuedPlayers.size() == 0) {
            return;
        }

        sortedQueuedPlayers.forEach(uuid -> {
            Player player = plugin.getServer().getPlayer(uuid);
            String server = queuedPlayers.get(uuid);
            int position = Collections.getPositionOfObject(sortedQueuedPlayers, uuid) + 1;
            String waitMessage = Messages.QUEUE_WAIT.toString()
                    .replace("{name}", server)
                    .replace("{queue-total}", getPlayersQueuedFor(server).size() + "")
                    .replace("{queue-position}", position + "");

            if (checkServerState(server, "Offline")) {
                if (player != null) {
                    player.sendMessage(waitMessage.replace("{queue-server-state}", "Offline"));
                }
                return;
            }

            if (checkServerState(server, "Whitelisted")) {
                if (player != null) {
                    player.sendMessage(waitMessage.replace("{queue-server-state}", "Whitelisted"));
                }
                return;
            }

            if (isServerFull(server)) {
                if (player == null) {
                    return;
                }

                player.sendMessage(waitMessage.replace("{queue-server-state}", "Full"));
            }
        });
    }

    private void loadQueues() {
        plugin.getConfig().getConfigurationSection("Valid-Queues").getValues(false).forEach((key, values) -> {
            ConfigurationSection section = (ConfigurationSection) values;

            club.crestmc.queue.queue.Queue queue = new club.crestmc.queue.queue.Queue();
            queue.setName(key);
            queue.setPermission(section.getString("permission"));
            queue.setServer(section.getString("server"));

            queues.add(queue);
        });
    }

    public club.crestmc.queue.queue.Queue getQueue(String queueName) {
        return queues.stream().filter(queue -> queue.getName().equalsIgnoreCase(queueName)).findFirst().orElse(null);
    }

    public Set<UUID> getPlayersQueuedFor(String serverName) {
        return queuedPlayers.keySet().stream().filter(player -> queuedPlayers.get(player).equalsIgnoreCase(serverName)).collect(Collectors.toSet());
    }

    public String getServerPlayerQueuedFor(UUID player) {
        return queuedPlayers.get(player);
    }

    public Map<UUID, Integer> getExitingPlayers() {
        return exitingPlayers;
    }

    public Map<UUID, String> getQueuedPlayers() {
        return queuedPlayers;
    }

    public Map<UUID, String> getOnlineQueuedPlayers() {
        return onlineQueuedPlayers;
    }

    public Set<club.crestmc.queue.queue.Queue> getQueues() {
        return queues;
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
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkServerState(String server, String state) {
        return getServerStatus(server).contains(state);
    }


    private String getServerStatus(String server) {
        return ChatColor.stripColor(Placeholders.parsePlaceholder("{server-playercount-" + server + "}", true));
    }

    private long getRankPriority(UUID uuid) {
        if (plugin.getServer().getPluginManager().getPlugin("Basics") != null) {
            Basics basics = JavaPlugin.getPlugin(Basics.class);
            RankManager rankManager = basics.getRankManager();

            if (rankManager == null) {
                return getDefaultPriority(uuid);
            }

            Rank rank = rankManager.getHighestRankByPriority(uuid);

            return rank.getPriority();
        }

        return getLuckPermsPriority(uuid);
    }

    private int getLuckPermsPriority(UUID uuid) {
        if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            LuckPerms luckPerms = LuckPermsProvider.get();
            GroupManager groupManager = luckPerms.getGroupManager();
            UserManager userManager = luckPerms.getUserManager();

            String groupName = userManager.getUser(uuid).getPrimaryGroup();
            Group group = groupManager.getGroup(groupName);
            return group.getWeight().getAsInt();
        }

        return getDefaultPriority(uuid);
    }

    private int getDefaultPriority(UUID uuid) {
        Player player = plugin.getServer().getPlayer(uuid);

        if (player == null) {
            return 0;
        }

        return player.hasPermission("queue.priority") ? 1 : 0;
    }

}
