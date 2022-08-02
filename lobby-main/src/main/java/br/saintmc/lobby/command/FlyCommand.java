package br.saintmc.lobby.command;

import br.saintmc.commons.bukkit.account.BukkitMember;
import br.saintmc.commons.core.account.group.Group;
import br.saintmc.commons.core.command.CommandArgs;
import br.saintmc.commons.core.command.CommandClass;
import br.saintmc.commons.core.command.CommandFramework;
import org.bukkit.entity.Player;
public class FlyCommand implements CommandClass {

    @CommandFramework.Command(name = "fly", groupToUse = Group.LIGHT)
    public void flyCommand(CommandArgs cmdArgs) {
        if (!cmdArgs.isPlayer())
            return;
        Player player = ((BukkitMember) cmdArgs.getSender()).getPlayer();
        player.setAllowFlight(!player.getAllowFlight());
        player.sendMessage(player.getAllowFlight() ? "§aVocê ativou o fly!" : "§cVocê desativou o fly!");
        if (player.getAllowFlight())
            player.setFlying(true);
    }
}
