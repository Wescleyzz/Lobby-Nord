package br.saintmc.lobby.event;

import br.saintmc.commons.bukkit.event.player.PlayerCancellableEvent;
import org.bukkit.entity.Player;

public class PlayerItemReceiveEvent extends PlayerCancellableEvent {

    public PlayerItemReceiveEvent(Player player) {
        super(player);
    }
}
