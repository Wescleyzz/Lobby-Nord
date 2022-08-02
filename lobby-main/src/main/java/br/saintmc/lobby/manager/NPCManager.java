package br.saintmc.lobby.manager;

import br.saintmc.commons.bukkit.BukkitMain;
import br.saintmc.commons.core.server.ServerType;
import br.saintmc.lobby.Lobby;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.api.events.NPCInteractEvent;
import net.jitse.npclib.api.skin.MineSkinFetcher;
import net.jitse.npclib.api.state.NPCSlot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class NPCManager implements Listener {

    private Lobby lobby;
    private NPCLib npcLib;

    private NPC hungergamesNPC;

    public NPCManager(Lobby lobby) {
        this.lobby = lobby;
        this.npcLib = new NPCLib(lobby);
        load();
        Bukkit.getPluginManager().registerEvents(this, lobby);
    }

    private void load() {
        int skinId = 277513;
        MineSkinFetcher.fetchSkinFromIdAsync(skinId, skin -> {
            hungergamesNPC = npcLib.createNPC(Arrays.asList(ChatColor.GOLD + "HungerGames", ChatColor.GRAY + "Pedrudo e LM"));
            hungergamesNPC.setLocation(new Location(Bukkit.getWorlds().get(0), -5, 90, -10, 0, 0));
            hungergamesNPC.setItem(NPCSlot.MAINHAND, new ItemStack(Material.MUSHROOM_SOUP));
            hungergamesNPC.setSkin(skin);
            hungergamesNPC.create();
        });
    }

    @EventHandler
    public void onNPCInteract(NPCInteractEvent event) {
        Player player = event.getWhoClicked();
        if (event.getNPC() == hungergamesNPC) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("HGLobby");
            player.sendPluginMessage(Lobby.getPlugin(), "BungeeCord", out.toByteArray());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Lobby.getPlugin(), () -> {
            Bukkit.getScheduler().runTask(Lobby.getPlugin(), () -> hungergamesNPC.show(event.getPlayer()));
        }, 60L);
    }
}
