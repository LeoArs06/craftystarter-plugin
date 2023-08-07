package ares06.craftystarter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class Craftystarter extends JavaPlugin {
    @Override
    public void onEnable() {
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!getConfig().getBoolean("Enabled")) {
            sender.sendMessage("ServerStartPlugin is currently disabled.");
            return true;
        }

        if (label.equalsIgnoreCase("startserver")) {
            if (args.length < 1) {
                sender.sendMessage("Usage: /startserver <server_name>");
                return true;
            }

            String serverName = args[0];
            if (getConfig().getBoolean(serverName + ".enabled")) {
                String ip = getConfig().getString(serverName + ".ip");
                if (!isServerOnline(ip)) {
                    sender.sendMessage("The server is offline. Starting it now...");

                    try {
                        String response = startServer(serverName);
                        if (response != null && response.contains("\"status\":\"ok\"")) {
                            sender.sendMessage("Server started successfully.");
                        } else {
                            sender.sendMessage("An error occurred while starting the server.");
                        }
                    } catch (IOException e) {
                        sender.sendMessage("An error occurred while starting the server: " + e.getMessage());
                    }
                } else {
                    sender.sendMessage("The server is already online.");
                }
            } else {
                sender.sendMessage(serverName + " server is currently disabled.");
            }

            return true;
        }

        return false;
    }
}