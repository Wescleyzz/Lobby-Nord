package br.saintmc.lobby.inventory;

import br.saintmc.commons.bukkit.BukkitMain;
import br.saintmc.commons.bukkit.api.item.ItemBuilder;
import br.saintmc.commons.bukkit.api.menu.MenuInventory;
import br.saintmc.commons.bukkit.api.menu.MenuUpdateHandler;
import br.saintmc.commons.bukkit.api.menu.click.ClickType;
import br.saintmc.commons.bukkit.api.menu.click.MenuClickHandler;
import br.saintmc.commons.bukkit.utils.string.StringLoreUtils;
import br.saintmc.commons.core.account.Member;
import br.saintmc.commons.core.account.group.Group;
import br.saintmc.commons.core.server.ServerType;
import br.saintmc.lobby.Lobby;
import br.saintmc.lobby.inventory.gladiator.GladiatorInventory;
import br.saintmc.lobby.inventory.kitpvp.KitPvPInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import lombok.RequiredArgsConstructor;

public class ServerInventory {

    public ServerInventory(Player player) {
        MenuInventory menuInventory = new MenuInventory("§7Servidores", 3);

        createItens(player, menuInventory);

        menuInventory.setUpdateHandler(new MenuUpdateHandler() {

            @Override
            public void onUpdate(Player player, MenuInventory menu) {
                createItens(player, menuInventory);
            }
        });

        menuInventory.open(player);
    }

    public void createItens(Player player, MenuInventory menuInventory) {
        menuInventory.setItem(10,
                new ItemBuilder().name("§b§lKitPvP").type(Material.DIAMOND_SWORD).lore(StringLoreUtils.getLore(30,
                        "\n§7Novo servidor de KitPvP com sopa feito para todos usarem estratégias e lutarem sem armudura em um estilo mais Hardcore simulando um HG!\n§f\n§a"
                                + (BukkitMain.getPlugin().getServerManager().getBalancer(ServerType.SIMULATOR)
                                .getTotalNumber()
                                + BukkitMain.getPlugin().getServerManager().getBalancer(ServerType.FULLIRON)
                                .getTotalNumber())
                                + " jogadores online!"))
                        .build(),
                new MenuClickHandler() {

                    @Override
                    public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
                        if (type == ClickType.LEFT) {
                            new KitPvPInventory(p);
                            return;
                        }

                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("PVP");
                        player.sendPluginMessage(Lobby.getPlugin(), "BungeeCord", out.toByteArray());
                        player.closeInventory();
                    }
                });

        menuInventory.setItem(12,
                new ItemBuilder().name("§e§lHungerGames").type(Material.MUSHROOM_SOUP).lore(StringLoreUtils.getLore(30,
                        "\n§7Seja o ultimo sobrevivente em uma batalha brutal com kits onde apenas um será o campeão!\n§f\n§a"
                                + (BukkitMain.getPlugin().getServerManager().getBalancer(ServerType.HUNGERGAMES)
                                .getTotalNumber())
                                + " jogadores online!"))
                        .build(),
                new MenuClickHandler() {

                    @Override
                    public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
                        if (type == ClickType.LEFT) {
                            ByteArrayDataOutput out = ByteStreams.newDataOutput();
                            out.writeUTF("HGLobby");
                            player.sendPluginMessage(Lobby.getPlugin(), "BungeeCord", out.toByteArray());
                            player.closeInventory();
                            return;
                        }

                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("HGLobby");
                        player.sendPluginMessage(Lobby.getPlugin(), "BungeeCord", out.toByteArray());
                        player.closeInventory();
                    }
                });

        menuInventory.setItem(14,
                new ItemBuilder().name("§3§lGladiator").type(Material.IRON_FENCE).lore(StringLoreUtils.getLore(30,
                        "\n§7Neste modo de jogo você pode desafiar seus amigos ou inimigos para uma batalha mortal!\n§f\n§a"
                                + (BukkitMain.getPlugin().getServerManager().getBalancer(ServerType.GLADIATOR)
                                .getTotalNumber())
                                + " jogadores online!"))
                        .build(),
                new MenuClickHandler() {

                    @Override
                    public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
                        if (type == ClickType.LEFT) {
                            new GladiatorInventory(p);
                            return;
                        }
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Gladiator");
                        player.sendPluginMessage(Lobby.getPlugin(), "BungeeCord", out.toByteArray());
                        player.closeInventory();
                    }
                });

        menuInventory.setItem(16, new ItemBuilder().name("§e§lSkyWars").type(Material.GRASS)
                .lore(StringLoreUtils.getLore(30, "\n§7Neste modo de jogo você batalhará nos céus!\n\n"
                        + "§7Este modo está em desenvolvimento e BUGS podem acontecer!\n§f\n§a"
                        + (BukkitMain.getPlugin().getServerManager().getBalancer(ServerType.SW_SOLO).getTotalNumber()
                        + BukkitMain.getPlugin().getServerManager().getBalancer(ServerType.SW_TEAM)
                        .getTotalNumber()
                        + BukkitMain.getPlugin().getServerManager().getBalancer(ServerType.SW_SQUAD)
                        .getTotalNumber())
                        + " jogadores online!"))
                .build(), new MenuClickHandler() {

            @Override
            public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
                if (type == ClickType.RIGHT && Member.hasGroupPermission(p.getUniqueId(), Group.TRIAL)) {
                    player.sendMessage("§cNenhum servidor de Sky Wars disponível no momento!");
                    player.closeInventory();
                    return;
                }

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("SWSolo");
                player.sendPluginMessage(Lobby.getPlugin(), "BungeeCord", out.toByteArray());
                player.closeInventory();
            }
        });
    }

    @RequiredArgsConstructor
    public static class SendClick implements MenuClickHandler {

        private final String serverId;

        @Override
        public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
            BukkitMain.getPlugin().sendPlayer(p, serverId);
        }
    }
}
