package br.saintmc.lobby.listener;

import br.saintmc.commons.Commons;
import br.saintmc.commons.Constants;
import br.saintmc.commons.bukkit.BukkitMain;
import br.saintmc.commons.bukkit.api.scoreboard.Score;
import br.saintmc.commons.bukkit.api.scoreboard.Scoreboard;
import br.saintmc.commons.bukkit.api.scoreboard.impl.SimpleScoreboard;
import br.saintmc.commons.bukkit.event.player.PlayerScoreboardStateEvent;
import br.saintmc.commons.bukkit.event.player.account.PlayerChangeGroupEvent;
import br.saintmc.commons.bukkit.event.player.account.PlayerChangeLeagueEvent;
import br.saintmc.commons.bukkit.event.server.ServerPlayerJoinEvent;
import br.saintmc.commons.bukkit.event.server.ServerPlayerLeaveEvent;
import br.saintmc.commons.core.account.Member;
import br.saintmc.commons.core.account.clan.event.ClanEvent;
import br.saintmc.commons.core.account.group.Group;
import br.saintmc.commons.core.account.league.League;
import br.saintmc.commons.core.account.tag.Tag;
import br.saintmc.lobby.Lobby;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreboardListener implements Listener {

    public static final Scoreboard DEFAULT_SCOREBOARD;

    static {
        DEFAULT_SCOREBOARD = new SimpleScoreboard("§6§lSAINT");

        DEFAULT_SCOREBOARD.blankLine(9);
        DEFAULT_SCOREBOARD.setScore(8, new Score("Grupo: §7§lMEMBRO", "group"));
        DEFAULT_SCOREBOARD.setScore(7, new Score("Clan: §7-/-", "clan"));
        DEFAULT_SCOREBOARD.blankLine(6);
        DEFAULT_SCOREBOARD.setScore(5, new Score("§fJogadores: §e" + BukkitMain.getPlugin().getServerManager().getTotalNumber(), "online"));
        DEFAULT_SCOREBOARD.blankLine(4);
        DEFAULT_SCOREBOARD.setScore(3, new Score("§6" + Constants.SITE, "site"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        handleScoreboard(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(ServerPlayerJoinEvent e) {
        DEFAULT_SCOREBOARD.updateScore(
                new Score("Jogadores: §e" + BukkitMain.getPlugin().getServerManager().getTotalNumber(), "online"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(ServerPlayerLeaveEvent e) {
        DEFAULT_SCOREBOARD.updateScore(
                new Score("Jogadores: §e" + BukkitMain.getPlugin().getServerManager().getTotalNumber(), "online"));
    }

    @EventHandler
    public void onPlayerChangeGroup(PlayerChangeGroupEvent event) {
        new BukkitRunnable() {

            @Override
            public void run() {
                Group group = event.getGroup();

                DEFAULT_SCOREBOARD.updateScore(event.getPlayer(),
                        new Score("Grupo: §f§l"
                                + (group == Group.MEMBRO ? "§7§lMEMBRO" : Tag.valueOf(group.name()).getPrefix()),
                                "group"));
            }
        }.runTaskLater(Lobby.getPlugin(), 10l);
    }

    @EventHandler
    public void onPlayerScoreboardState(PlayerScoreboardStateEvent event) {
        if (event.isScoreboardEnabled())
            handleScoreboard(event.getPlayer());
    }

    private void handleScoreboard(Player player) {
        Member member = Commons.getMemberManager().getMember(player.getUniqueId());

        if (member == null) {
            player.kickPlayer("§cSua conta não foi carregada!");
            return;
        }

        DEFAULT_SCOREBOARD.createScoreboard(player);

        Group group = member.getGroup();
        League league = member.getLeague();

        DEFAULT_SCOREBOARD.updateScore(player,
                new Score("§fGrupo: §f§l" + (group == Group.MEMBRO ? "§7§lMEMBRO" : Tag.valueOf(group.name()).getPrefix()), "group"));
        DEFAULT_SCOREBOARD.updateScore(player,
                new Score("Clan: §7" + (member.hasClan() ? member.getClan().getClanAbbreviation() : "-/-"), "clan"));

        new BukkitRunnable() {

            @Override
            public void run() {
                DEFAULT_SCOREBOARD.updateScore(new Score("§fJogadores: §e" + BukkitMain.getPlugin().getServerManager().getTotalNumber(), "online"));
            }
        }.runTaskLater(Lobby.getPlugin(), 20l);
    }
}
