package br.saintmc.lobby.inventory.gladiator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import br.saintmc.commons.Commons;
import br.saintmc.commons.bukkit.BukkitMain;
import br.saintmc.commons.bukkit.api.item.ItemBuilder;
import br.saintmc.commons.bukkit.api.menu.MenuInventory;
import br.saintmc.commons.bukkit.api.menu.MenuUpdateHandler;
import br.saintmc.commons.core.account.Member;
import br.saintmc.commons.core.account.group.Group;
import br.saintmc.commons.core.server.ServerType;
import br.saintmc.commons.core.server.loadbalancer.server.SaintServer;
import br.saintmc.lobby.inventory.ServerInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GladiatorInventory {

    public GladiatorInventory(Player player) {
        Member member = Commons.getMemberManager().getMember(player.getUniqueId());

        List<SaintServer> serverList = new ArrayList<>(
                BukkitMain.getPlugin().getServerManager().getBalancer(ServerType.GLADIATOR).getList());

        MenuInventory menu = new MenuInventory("§7Servidores de Gladiator",
                2 + (serverList.size() == 0 ? 1 : (serverList.size() / 7) + 1));

        serverList.sort(new Comparator<SaintServer>() {

            @Override
            public int compare(SaintServer o1, SaintServer o2) {
                int object = Boolean.valueOf(o1.isFull()).compareTo(o2.isFull());

                if (object != 0)
                    return object;

                if (o1.getOnlinePlayers() < o2.getOnlinePlayers())
                    return 1;
                else if (o1.getOnlinePlayers() == o2.getOnlinePlayers())
                    return 0;

                return o1.getServerId().compareTo(o2.getServerId());
            }

        });

        create(serverList, member, menu);

        menu.setUpdateHandler(new MenuUpdateHandler() {

            @Override
            public void onUpdate(Player player, MenuInventory menu) {
                create(serverList, member, menu);
            }

        });

        menu.open(player);

    }

    public void create(List<SaintServer> serverList, Member member, MenuInventory menu) {
        int w = 10;

        for (SaintServer server : serverList) {
            if (!server.isJoinEnabled() && !member.hasGroupPermission(Group.BUILDER)
                    && !server.isInWhitelist(member.getPlayerName()))
                continue;

            ItemBuilder builder = new ItemBuilder();
            builder.type(Material.INK_SACK);

            if (server.isFull()) {
                builder.name("§e§l" + server.getServerId());
                builder.lore("\n§b§l" + server.getOnlinePlayers() + " §7jogadores conectados\n§cEsse servidor está lotado!");
                builder.durability(1);
            } else {
                builder.name("§e§l" + server.getServerId());
                builder.lore("\n§b§l" + server.getOnlinePlayers() + " §7jogadores conectados\n§aClique para conectar a esse servidor!");
                builder.durability(10);
            }

            builder.amount(server.getOnlinePlayers());

            if (w % 9 == 8) {
                w += 2;
            }

            menu.setItem(w, builder.build(), new ServerInventory.SendClick(server.getServerId()));
            w++;
        }
    }
}
