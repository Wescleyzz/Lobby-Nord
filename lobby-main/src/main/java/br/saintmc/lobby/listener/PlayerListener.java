package br.saintmc.lobby.listener;

import br.saintmc.commons.Commons;
import br.saintmc.commons.Constants;
import br.saintmc.commons.bukkit.BukkitMain;
import br.saintmc.commons.bukkit.account.BukkitMember;
import br.saintmc.commons.bukkit.api.item.ActionItemStack;
import br.saintmc.commons.bukkit.api.item.ItemBuilder;
import br.saintmc.commons.bukkit.api.tablist.TabListAPI;
import br.saintmc.commons.bukkit.event.player.account.PlayerChangeGroupEvent;
import br.saintmc.commons.bukkit.event.player.login.PlayerChangeLoginStatusEvent;
import br.saintmc.commons.bukkit.inventory.account.AccountInventory;
import br.saintmc.commons.core.account.Member;
import br.saintmc.commons.core.account.configuration.LoginConfiguration;
import br.saintmc.commons.core.account.group.Group;
import br.saintmc.commons.core.account.tag.Tag;
import br.saintmc.lobby.Lobby;
import br.saintmc.lobby.event.PlayerItemReceiveEvent;
import br.saintmc.lobby.gamer.Gamer;
import br.saintmc.lobby.inventory.ServerInventory;
import br.saintmc.lobby.inventory.tournament.TournamentInventory;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

@SuppressWarnings("deprecation")
public class PlayerListener implements Listener {

    @Getter
    private static PlayerListener playerListener;

    private ActionItemStack compass;
    private ActionItemStack lobbies;
    private ActionItemStack tournament;
    private ActionItemStack clanvsclan;

    public PlayerListener() {
        compass = new ActionItemStack(
                new ItemBuilder().name("§eSelecionar modo de jogo §7(Clique aqui)").type(Material.COMPASS).build(),
                new ActionItemStack.Interact(ActionItemStack.InteractType.CLICK) {

                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, ActionItemStack.ActionType action) {
                        new ServerInventory(player);
                        return false;
                    }
                });

        clanvsclan = new ActionItemStack(
                new ItemBuilder().name("§eSelecionar uma sala Clan x Clan §7(Clique aqui)").type(Material.ENDER_CHEST).build(),
                new ActionItemStack.Interact(ActionItemStack.InteractType.CLICK) {

                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, ActionItemStack.ActionType action) {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("ClanxClan");
                        player.sendPluginMessage(Lobby.getPlugin(), "BungeeCord", out.toByteArray());
                        player.closeInventory();
                        return false;
                    }
                });

        tournament = new ActionItemStack(
                new ItemBuilder().name("§eTorneio §7(Clique aqui)").glow().type(Material.BOOK).build(),
                new ActionItemStack.Interact(ActionItemStack.InteractType.CLICK) {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, ActionItemStack.ActionType action) {
                        new TournamentInventory(player, null, false, false);
                        return false;
                    }

                });

        playerListener = this;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        BukkitMember member = (BukkitMember) Commons.getMemberManager().getMember(event.getPlayer().getUniqueId());
        if (member.hasGroupPermission(Group.DONATOR)) {
            player.setAllowFlight(true);
            player.setFlying(true);
        } else {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
        for (Gamer gamer : Lobby.getPlugin().getPlayerManager().getGamers()) {
            if (!gamer.isSeeing()) {
                gamer.getPlayer().hidePlayer(player);
            }
        }
        player.teleport(member.getLoginConfiguration().isLogged() ? BukkitMain.getPlugin().getLocationFromConfig("spawn") : BukkitMain.getPlugin().getLocationFromConfig("login"));
        addItem(player, member);
        for (Player online : Bukkit.getOnlinePlayers()) {
            constructTabList(online);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChangeLoginStatus(PlayerChangeLoginStatusEvent event) {
        if (event.isLogged() || event.getMember().getLoginConfiguration().getAccountType() == LoginConfiguration.AccountType.ORIGINAL) {
            event.getPlayer().teleport(BukkitMain.getPlugin().getLocationFromConfig("spawn"));
            addItem(event.getPlayer(), event.getMember());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Lobby.getPlugin().getPlayerManager().removeGamer(e.getPlayer());
    }

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == SpawnReason.CUSTOM)
            return;

        if (e.getEntity() instanceof Player)
            return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity) && event.getDamager() != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        event.setCancelled(true);
        new BukkitRunnable() {

            @Override
            public void run() {
                event.getPlayer().updateInventory();
            }
        }.runTask(Lobby.getPlugin());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        BukkitMember player = (BukkitMember) Commons.getMemberManager()
                .getMember(e.getPlayer().getUniqueId());

        if (player.isBuildEnabled())
            if (player.hasGroupPermission(Group.DEV)) {
                e.setCancelled(false);
                return;
            }

        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockPlaceEvent e) {
        BukkitMember player = (BukkitMember) Commons.getMemberManager().getMember(e.getPlayer().getUniqueId());
        if (player.isBuildEnabled())
            if (player.hasGroupPermission(Group.DEV)) {
                e.setCancelled(false);
                return;
            }

        e.setCancelled(true);
    }

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) {
        BukkitMember player = (BukkitMember) Commons.getMemberManager().getMember(event.getPlayer().getUniqueId());
        if (player.isBuildEnabled())
            if (player.hasGroupPermission(Group.DEV)) {
                event.setCancelled(false);
                return;
            }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerChangeGroup(PlayerChangeGroupEvent event) {
        constructTabList(event.getPlayer());
    }

    @EventHandler
    public void onPlayerItemReceive(PlayerItemReceiveEvent event) {
        addItem(event.getPlayer(), Commons.getMemberManager().getMember(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        event.setFire(false);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().clear();
    }

    @EventHandler
    public void onLeaveDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFire(BlockIgniteEvent event) {
        event.setCancelled(true);
    }

    public void addItem(Player player, Member member) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);

        player.setHealth(20D);
        player.setFoodLevel(20);
        player.setLevel(-100);

        player.getInventory().setItem(4, compass.getItemStack());
        if (member.hasClan()) {
            player.getInventory().setItem(7, clanvsclan.getItemStack());
        }
        player.getInventory().setItem(0, new ActionItemStack(new ItemBuilder().name("§eSeu perfil §7(Clique aqui)").skin(member.getPlayerName()).durability(3).type(Material.SKULL_ITEM).build(), new ActionItemStack.Interact(ActionItemStack.InteractType.CLICK) {

                            @Override
                            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item,
                                                      ActionItemStack.ActionType action) {
                                new AccountInventory(player, member);
                                return false;
                            }

                        }).getItemStack());
        player.getInventory().setItem(8, tournament.getItemStack());
        player.updateInventory();
    }

    public TextComponent createClickable(String message, String hoverMessage, String url) {
        TextComponent textComponent = new TextComponent(message);

        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        textComponent
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverMessage)));

        return textComponent;
    }

    public void constructTabList(Player player) {
        int ping = 0;
        ping = ((CraftPlayer) player).getHandle().ping;
        int players = BukkitMain.getPlugin().getServerManager().getTotalNumber();
        Member member = Commons.getMemberManager().getMember(player.getUniqueId());
        Group group = member.getGroup();
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("§6§lSAINT");
        builder.append("\n");
        builder.append("\n");
        builder.append("§7Nickname: §e" + player.getName() + " §7Grupo: " + (group == Group.MEMBRO ? "§7§lMEMBRO" : Tag.valueOf(group.name()).getPrefix()));
        builder.append("\n");
        builder.append("§7Servidor: §e" + member.getServerId());
        builder.append("\n");
        StringBuilder footer = new StringBuilder();
        footer.append("\n");
        footer.append("§b" + Constants.WEBSITE);
        footer.append("\n");
        footer.append("§e" + Constants.DISCORD);
        footer.append("\n");
        TabListAPI.setHeaderAndFooter(player, builder.toString(), footer.toString());
    }
}
