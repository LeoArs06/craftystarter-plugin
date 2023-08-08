# CraftyStarter Plugin

The CraftyStarter plugin is a Bukkit Paper plugin that provides server ingame start using [craftycontroller](https://github.com/RMDC-Crafty/crafty) api.

## Features

- Automatically start servers using the Crafty API.
- Redirect players to different servers using BungeeCord.
- Users can modify various messages displayed by the plugin to suit their server environment.
- Server inactivity shutdown using [inactivityshutdown plugin](https://github.com/LeoArs06/inactivityshutdown).

## Installation

1. Download the plugin JAR file from the [Releases](https://github.com/LeoArs06/craftystarter-plugin/releases) page.
2. Place the downloaded JAR file into the `plugins` directory of your Bukkit/Paper server.
3. Restart the server to activate the plugin.

## Configuration

Before using the CraftyStarter plugin, make sure to configure it properly in the `config.yml` file located in the plugin's folder. Here are some configuration options:

- `Enabled`: Set to `true` to enable the CraftyStarter plugin.

- `server_name`:
  - `enabled`: Set to `true` to enable the specific server.
  - `ip`: IP address and port of the server (e.g., `127.0.0.1:25565`).
  - `url`: URL for the Crafty API.
  - `token`: API token for authentication.
  - `id`: Server ID for API actions.

## Usage

Once the CraftyStarter plugin is enabled and configured, you can use the following commands:

- `/startserver <server_name>`: Allows players to start a specific server. If the server is offline, it will be started automatically. If the server is online, nothing happens.

- `/joinserver <server_name>`: Allows players to join a specific server. If the server is offline, it will be started automatically. If the server is online, the player will be redirected using BungeeCord if bungeecord feature is enabled in config.yml.

## License

This plugin is licensed under the [MIT License](LICENSE).

## Credits

- Author: [LeoArs06](https://github.com/LeoArs06)

For any inquiries or support, feel free to contact the author.

---

