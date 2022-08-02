package br.saintmc.lobby;

import br.saintmc.commons.Commons;
import br.saintmc.commons.bukkit.BukkitMain;
import br.saintmc.commons.bukkit.command.BukkitCommandFramework;
import br.saintmc.commons.core.backend.redis.PubSubListener;
import br.saintmc.commons.core.server.ServerType;
import br.saintmc.commons.core.server.loadbalancer.server.MinigameServer;
import br.saintmc.commons.core.server.loadbalancer.server.SaintServer;
import br.saintmc.lobby.gamer.manager.PlayerManager;
import br.saintmc.lobby.listener.*;
import br.saintmc.lobby.manager.NPCManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

@Getter
public class Lobby extends JavaPlugin {

    @Getter
    private static Lobby plugin;

    private PlayerManager playerManager;
    private NPCManager npcManager;

    private PubSubListener pubSubListener;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        for (Map.Entry<String, Map<String, String>> entry : Commons.getServerData().loadServers()
                .entrySet()) {
            try {
                if (!entry.getValue().containsKey("type"))
                    continue;

                if (!entry.getValue().containsKey("address"))
                    continue;

                if (!entry.getValue().containsKey("maxplayers"))
                    continue;

                if (!entry.getValue().containsKey("onlineplayers"))
                    continue;

                if (ServerType.valueOf(entry.getValue().get("type").toUpperCase()) == ServerType.NETWORK)
                    continue;

                SaintServer server = BukkitMain.getPlugin().getServerManager().addActiveServer(
                        entry.getValue().get("address"), entry.getKey(),
                        ServerType.valueOf(entry.getValue().get("type").toUpperCase()),
                        Integer.valueOf(entry.getValue().get("maxplayers")));

                BukkitMain.getPlugin().getServerManager().getServer(entry.getKey())
                        .setOnlinePlayers(Commons.getServerData().getPlayers(entry.getKey()));
                BukkitMain.getPlugin().getServerManager().getServer(entry.getKey())
                        .setJoinEnabled(Boolean.valueOf(entry.getValue().get("joinenabled")));

                if (server instanceof MinigameServer) {
                    MinigameServer minigameServer = (MinigameServer) server;

                    minigameServer.setTime(Commons.getServerData().getTime(entry.getKey()));
                    minigameServer.setMap(Commons.getServerData().getMap(entry.getKey()));
                    minigameServer.setState(Commons.getServerData().getState(entry.getKey()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            BukkitCommandFramework.INSTANCE.loadCommands(this.getClass(), "br.saintmc.lobby.command");
            playerManager = new PlayerManager();
            npcManager = new NPCManager(this);

            BukkitMain.getPlugin().setServerLog(true);
            Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
            Bukkit.getPluginManager().registerEvents(new ScoreboardListener(), this);
            Bukkit.getPluginManager().registerEvents(new LaunchListener(), this);
            Bukkit.getPluginManager().registerEvents(new LoginListener(), this);
            //Bukkit.getPluginManager().registerEvents(new NPCListener(), this);
        }
    }
}
