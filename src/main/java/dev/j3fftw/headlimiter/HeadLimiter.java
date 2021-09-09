package dev.j3fftw.headlimiter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import me.mrCookieSlime.Slimefun.api.BlockStorage;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.GitHubBuildsUpdater;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class HeadLimiter extends JavaPlugin implements Listener {

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("HeadLimiter-pool-%d").build();

    private final ExecutorService executorService = Executors.newFixedThreadPool(
        this.getConfig().getInt("thread-pool-size", 4), threadFactory
    );

    @Override
    public void onEnable() {
        if (!new File(getDataFolder(), "config.yml").exists())
            saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);

        new Metrics(this, 9968);

        if (getConfig().getBoolean("auto-update") && getDescription().getVersion().startsWith("DEV - ")) {
            new GitHubBuildsUpdater(this, getFile(), "ybw0014/HeadLimiter/master").start();
        }
    }

    private boolean isCargo(SlimefunItem sfItem) {
        return sfItem.isItem(SlimefunItems.CARGO_INPUT_NODE)
            || sfItem.isItem(SlimefunItems.CARGO_OUTPUT_NODE)
            || sfItem.isItem(SlimefunItems.CARGO_OUTPUT_NODE_2)
            || sfItem.isItem(SlimefunItems.CARGO_CONNECTOR_NODE)
            || sfItem.isItem(SlimefunItems.CARGO_MANAGER);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent e) {
        final SlimefunItem sfItem = SlimefunItem.getByItem(e.getItemInHand());
        if (!e.isCancelled()
            && (e.getBlock().getType() == Material.PLAYER_HEAD || e.getBlock().getType() == Material.PLAYER_WALL_HEAD)
            && sfItem != null && isCargo(sfItem)
        ) {
            final Block block = e.getBlock();
            final BlockState[] te = block.getChunk().getTileEntities();
            executorService.submit(() -> {
                int i = 0;
                for (BlockState bs : te) {
                    final SlimefunItem slimefunItem = BlockStorage.check(bs.getLocation());
                    if (slimefunItem != null && isCargo(slimefunItem))
                        i++;
                }

                final int threshold = this.getConfig().getInt("amount");
                if (i >= threshold) {
                    Bukkit.getScheduler().runTask(this, () -> {
                        if (block.getType() != Material.AIR) {
                            block.setType(Material.AIR);
                            if (!e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                                block.getWorld().dropItemNaturally(block.getLocation(), sfItem.getItem());
                            }
                        }
                    });
                    BlockStorage.clearBlockInfo(block.getLocation());
                    e.getPlayer().sendMessage(ChatColor.RED + "你已达到该区块货运节点的上限");
                }
            });
        }
    }
}
