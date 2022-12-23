package club.crestmc.crestqueue.util.pluginmessage;

import club.crestmc.crestqueue.CrestQueue;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginMessageHelper {

    /**
     * Sends a plugin message to the specified channel
     * @param channel The channel to send the plugin message to
     * @param data The data to send in the plugin message
     */
    public static void sendData(String channel, String... data) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        for (String message : data) {
            output.writeUTF(message);
        }

        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

        if (player != null) {
            player.sendPluginMessage(JavaPlugin.getPlugin(CrestQueue.class), "BungeeCord", output.toByteArray());
        }
    }

}
