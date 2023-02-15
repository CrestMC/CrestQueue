package club.crestmc.queue.util;

import club.crestmc.queue.Queue;
import me.blurmit.basics.util.Placeholders;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public enum Messages {

    QUEUE_WAIT() {
        @Override
        public String toString() {
            return ChatColor.translateAlternateColorCodes('&', JavaPlugin.getPlugin(Queue.class).getConfig().getString("Messages.Waiting-Queue"));
        }
    },

    ALREADY_IN_QUEUE() {
        @Override
        public String toString() {
            return ChatColor.translateAlternateColorCodes('&', JavaPlugin.getPlugin(Queue.class).getConfig().getString("Messages.Already-In-Queue"));
        }
    },

    NOT_IN_QUEUE() {
        @Override
        public String toString() {
            return ChatColor.translateAlternateColorCodes('&', JavaPlugin.getPlugin(Queue.class).getConfig().getString("Messages.Not-In-Queue"));
        }
    },

    JOIN_QUEUE() {
        @Override
        public String toString() {
            return ChatColor.translateAlternateColorCodes('&', JavaPlugin.getPlugin(Queue.class).getConfig().getString("Messages.Join-Queue"));
        }
    },

    LEAVE_QUEUE() {
        @Override
        public String toString() {
            return ChatColor.translateAlternateColorCodes('&', JavaPlugin.getPlugin(Queue.class).getConfig().getString("Messages.Leave-Queue"));
        }
    },

    SEND_FAILED() {
        @Override
        public String toString() {
            return ChatColor.translateAlternateColorCodes('&', JavaPlugin.getPlugin(Queue.class).getConfig().getString("Messages.Send-Failed"));
        }
    },

    SENT_QUEUE() {
        @Override
        public String toString() {
            return ChatColor.translateAlternateColorCodes('&', JavaPlugin.getPlugin(Queue.class).getConfig().getString("Messages.Sent-Queue"));
        }
    },

    QUEUE_INVALID() {
        @Override
        public String toString() {
            return ChatColor.translateAlternateColorCodes('&', JavaPlugin.getPlugin(Queue.class).getConfig().getString("Messages.Invalid-Queue"));
        }
    },

    NO_PERMISSION() {
        @Override
        public String toString() {
            return ChatColor.translateAlternateColorCodes('&', JavaPlugin.getPlugin(Queue.class).getConfig().getString("Messages.No-Permission"));
        }
    },

    NO_PERMISSION_JOIN_QUEUE() {
        @Override
        public String toString() {
            return ChatColor.translateAlternateColorCodes('&', JavaPlugin.getPlugin(Queue.class).getConfig().getString("Messages.No-Permission-Join-Queue"));
        }
    };

}
