package ares06.craftystarter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;


public final class Craftystarter extends JavaPlugin {
    private YamlConfiguration messagesConfig;
    @Override
    public void onEnable() {

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Load messages from messages.yml
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        saveDefaultConfig();
        if (!getConfig().getBoolean("Enabled")) {
            getLogger().info("ServerStartPlugin is disabled in the configuration.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("ServerStartPlugin has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ServerStartPlugin has been disabled.");
    }

    // method to get a formatted message from messages.yml
    private String getFormattedMessage(String key, Object... args) {
        String message = messagesConfig.getString("messages." + key);
        if (message != null) {
            String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);
            return String.format(formattedMessage, args);
        }
        return ChatColor.RED + "Message not found: " + key;
    }

    // Check if the server is online
    private boolean isServerOnline(String ip) {
        String[] parts = ip.split(":");
        if (parts.length == 2) {
            String ipAddress = parts[0];
            int port = Integer.parseInt(parts[1]);
            return HttpUtils.isServerOnline(ipAddress, port);
        }
        return false;
    }

    private void sendBungeeCommand(Player player, String targetServer) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("ConnectOther"); // Tipo di messaggio
            out.writeUTF(player.getName()); // Nome del giocatore a cui inviare il comando
            out.writeUTF(targetServer); // Nome del server a cui connettere il giocatore
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
    }

    // Execute post request for starting the server using Crafty API
    private String startServer(String serverName) throws IOException {
        // Load server configuration from config.yml
        String url = getConfig().getString(serverName + ".url");
        String token = getConfig().getString(serverName + ".token");
        String id = getConfig().getString(serverName + ".id");

        // Construct the full API path using the server ID
        String apiPath = "/api/v2/servers/" + id + "/action/start_server";
        String fullUrl = url + apiPath;

        try {
            String response = HttpUtils.executePostRequest(fullUrl, token);
            getLogger().info("Starting server " + serverName + "...");
            return response; // Return the response string
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean startCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getFormattedMessage("command_player_only"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            sender.sendMessage("Usage: /startserver <server_name>");
            return true;
        }

        String serverName = args[0];
        if (getConfig().contains(serverName)) {
            boolean serverEnabled = getConfig().getBoolean(serverName + ".enabled");
            if (serverEnabled) {
                String ip = getConfig().getString(serverName + ".ip");
                if (!isServerOnline(ip)) {
                    // Server is offline, start it
                    try {
                        String response = startServer(serverName);
                        if (response != null && response.contains("\"status\":\"ok\"")) {
                            sender.sendMessage(getFormattedMessage("server_started"));
                        } else {
                            sender.sendMessage(getFormattedMessage("server_start_error", serverName));
                        }
                    } catch (IOException e) {
                        sender.sendMessage(getFormattedMessage("server_start_error", serverName) + e.getMessage());
                    }
                } else {
                    sender.sendMessage(getFormattedMessage("server_already_online"));
                }
            } else {
                sender.sendMessage(getFormattedMessage("server_disabled", serverName));
            }
        } else {
            sender.sendMessage(getFormattedMessage("server_not_found", serverName));
        }
        return true;
    }

    private boolean joinCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getFormattedMessage("command_player_only"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            sender.sendMessage("Usage: /joinserver <server_name>");
            return true;
        }

        String serverName = args[0];
        if (getConfig().contains(serverName)) {
            boolean serverEnabled = getConfig().getBoolean(serverName + ".enabled");
            if (serverEnabled) {
                String ip = getConfig().getString(serverName + ".ip");
                if (!isServerOnline(ip)) {
                    // Server is offline, start it
                    try {
                        String response = startServer(serverName);
                        if (response != null && response.contains("\"status\":\"ok\"")) {
                            sender.sendMessage(getFormattedMessage("server_started", serverName));
                        } else {
                            sender.sendMessage(getFormattedMessage("server_start_error", serverName));
                        }
                    } catch (IOException e) {
                        sender.sendMessage(getFormattedMessage("server_start_error", serverName) + e.getMessage());
                    }
                } else {
                    // Redirect the player to another server using BungeeCord /server command
                    player.sendMessage(getFormattedMessage("redirecting", serverName));
                    sendBungeeCommand(player, serverName);
                }
            } else {
                sender.sendMessage(getFormattedMessage("server_disabled", serverName));
            }
        } else {
            sender.sendMessage(getFormattedMessage("server_not_found", serverName));
        }
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!getConfig().getBoolean("Enabled")) {
            sender.sendMessage("CraftyStarter is currently disabled.");
            return true;
        }

        if (label.equalsIgnoreCase("joinserver")) {
            if (getConfig().getBoolean("BungeeCordFeatures")) { //check if bungeecord features is enables
                return joinCommand(sender, args);
            } else {
                sender.sendMessage(getFormattedMessage("bungeecord_disabled"));
                return true;
            }
        } else if (label.equalsIgnoreCase("startserver")) {
            return startCommand(sender, args);
        }

        return false;
    }
}