package br.saintmc.lobby.gamer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.EnumParticle;

@Getter
@Setter
public class Gamer {

    private final Player player;

    private boolean usingParticle = false;
    private boolean usingWing = false;

    private boolean seeing = true;
    private boolean flying = false;

    private EnumParticle wingParticle;
    private EnumParticle particle;

    public Gamer(Player player) {
        this.player = player;
    }

    public void changeHead(ItemStack itemStack) {
        player.getInventory().setHelmet(itemStack);
    }
}
