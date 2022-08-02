package br.saintmc.lobby.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import br.saintmc.commons.bukkit.BukkitMain;
import br.saintmc.commons.bukkit.api.npc.NPC;
import br.saintmc.commons.bukkit.api.npc.character.Character;
import br.saintmc.commons.bukkit.event.server.ServerPlayerJoinEvent;
import br.saintmc.commons.bukkit.event.server.ServerPlayerLeaveEvent;
import br.saintmc.commons.core.server.ServerType;
import br.saintmc.commons.core.server.loadbalancer.server.HungerGamesServer;
import br.saintmc.hologram.hologram.Hologram;
import br.saintmc.hologram.hologram.impl.SimpleHologram;
import br.saintmc.hologram.item.types.GiantItem;
import br.saintmc.lobby.Lobby;
import br.saintmc.lobby.inventory.gladiator.GladiatorInventory;
import br.saintmc.lobby.inventory.kitpvp.KitPvPInventory;
import br.saintmc.lobby.inventory.tournament.TournamentInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import lombok.AllArgsConstructor;

public class NPCListener implements Listener {

    private List<HologramInfo> hologramList;

    public NPCListener() {
        hologramList = new ArrayList<>();

        createCharacter("§bSkyWars", "LuisMaartins", "npc-tournament", new Character.Interact() {

            @Override
            public boolean onInteract(Player player, boolean right) {

                if (right) {
                   player.sendMessage("§cEm breve!");
                } else {
                   player.sendMessage("§cEm breve!");
                }

                return false;
            }
        }, ServerType.SW_SOLO);

        createCharacter("§bHungerGames", "yukiritoFLAME", "npc-hg", new Character.Interact() {

            @Override
            public boolean onInteract(Player player, boolean right) {

                if (right) {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("HGLobby");
                    player.sendPluginMessage(Lobby.getPlugin(), "BungeeCord", out.toByteArray());
                    player.closeInventory();
                } else {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("HGLobby");
                    player.sendPluginMessage(Lobby.getPlugin(), "BungeeCord", out.toByteArray());
                    player.closeInventory();
                }

                return false;
            }
        }, ServerType.HUNGERGAMES);

        createCharacter("§bKitPvP", "broowk", "npc-pvp", new Character.Interact() {

            @Override
            public boolean onInteract(Player player, boolean right) {

                if (right) {
                    new KitPvPInventory(player);
                } else {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("PVP");
                    player.sendPluginMessage(Lobby.getPlugin(), "BungeeCord", out.toByteArray());
                    player.closeInventory();
                }

                return false;
            }
        }, ServerType.FULLIRON, ServerType.SIMULATOR);

        createCharacter("§bGladiator", "SpectroPlayer", "npc-gladiator", new Character.Interact() {

            @Override
            public boolean onInteract(Player player, boolean right) {
                if (right) {
                    new GladiatorInventory(player);
                } else {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Gladiator");
                    player.sendPluginMessage(Lobby.getPlugin(), "BungeeCord", out.toByteArray());
                    player.closeInventory();
                }
                return false;
            }
        }, ServerType.GLADIATOR);

    }

    @EventHandler
    public void onServerPlayerJoin(ServerPlayerJoinEvent event) {
        HologramInfo entry = hologramList.stream().filter(info -> info.typeList.contains(event.getServerType())).findFirst().orElse(null);

        if (entry != null) {
            int playerCount = 0;
            for (int integer : entry.typeList.stream().map(serverType -> BukkitMain.getPlugin().getServerManager().getBalancer(serverType).getTotalNumber()).collect(Collectors.toList()))
                playerCount += integer;
            entry.hologram.setDisplayName("§e" + playerCount + " jogadores!");
        }
    }

    @EventHandler
    public void onServerPlayerQuit(ServerPlayerLeaveEvent event) {
        HologramInfo entry = hologramList.stream().filter(info -> info.typeList.contains(event.getServerType())).findFirst().orElse(null);

        if (entry != null) {
            int playerCount = 0;
            for (int integer : entry.typeList.stream().map(serverType -> BukkitMain.getPlugin().getServerManager().getBalancer(serverType).getTotalNumber()).collect(Collectors.toList()))
                playerCount += integer;
            entry.hologram.setDisplayName("§e" + playerCount + " jogadores!");
        }
    }

    public void createCharacter(String displayName, String skinName, String configName, Character.Interact interact, ServerType... serverType) {
        new Character(displayName, skinName, BukkitMain.getPlugin().getLocationFromConfig(configName), interact);

        Hologram hologram = BukkitMain.getPlugin().getHologramController().createHologram(displayName, BukkitMain.getPlugin().getLocationFromConfig(configName), SimpleHologram.class);

        int playerCount = 0;

        for (int integer : Arrays.asList(serverType).stream()
                .map(sT -> BukkitMain.getPlugin().getServerManager().getBalancer(sT).getTotalNumber())
                .collect(Collectors.toList())) {
            playerCount += integer;
        }

        Hologram hologramLine = hologram.addLine(Arrays.asList(serverType).stream().map(sT -> BukkitMain.getPlugin().getServerManager().getBalancer(sT).getTotalNumber()).collect(Collectors.toList()).isEmpty() ? "§cNenhum servidor disponível!" : "§e" + playerCount + " jogadores!");

        hologramList.add(new HologramInfo(Arrays.asList(serverType), hologramLine));
        hologram.spawn();
        BukkitMain.getPlugin().getHologramController().registerHologram(hologram);
    }

    @AllArgsConstructor
    public class HologramInfo {

        private List<ServerType> typeList;
        private Hologram hologram;

    }
}