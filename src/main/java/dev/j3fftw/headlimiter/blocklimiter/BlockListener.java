package dev.j3fftw.headlimiter.blocklimiter;

import javax.annotation.Nonnull;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.event.SlimefunChunkDataLoadEvent;

import dev.j3fftw.headlimiter.HeadLimiter;
import dev.j3fftw.headlimiter.Utils;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockBreakEvent;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockPlaceEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.ChunkPosition;

@EnableAsync
public class BlockListener implements Listener {

    public BlockListener(@Nonnull HeadLimiter headLimiter) {
        headLimiter.getServer().getPluginManager().registerEvents(this, headLimiter);
    }

    @EventHandler
    @Async
    public void onSlimefunChunkLoad(@Nonnull SlimefunChunkDataLoadEvent event) {
        BlockLimiter blockLimiter = HeadLimiter.getInstance().getBlockLimiter();
        ChunkPosition chunkPos = new ChunkPosition(event.getChunk());
        
        for (SlimefunBlockData blockData : event.getChunkData().getAllBlockData()) {
            String id = blockData.getSfId();
            ChunkContent content = blockLimiter.getChunkContent(chunkPos);
            if (content == null) {
                content = new ChunkContent();
                content.incrementAmount(id);
                blockLimiter.setChunkContent(chunkPos, content);
            } else {
                content.incrementAmount(id);
            }
        }
    }

    @EventHandler
    @Async
    public void onSlimefunItemPlaced(@Nonnull SlimefunBlockPlaceEvent event) {
    	int total = Utils.countTotal(event.getBlockPlaced().getChunk());
    	int limit = Utils.getMaxHeads(event.getPlayer());
        if (total > limit) {
        	event.setCancelled(true);
        	event.getPlayer().sendMessage(ChatColor.RED + "这个区块已经有 " + total + "个粘液科技方块了。");
        	event.getPlayer().sendMessage(ChatColor.RED + "这个区块中的粘液科技机器/物品已经达到你的上限了。");
            event.getPlayer().sendMessage(ChatColor.RED + "你不能在该区块中放置更多粘液科技机器/物品了。");
        }

        
    }

    @EventHandler
    @Async
    public void onSlimefunItemBroken(@Nonnull SlimefunBlockBreakEvent event) {
        SlimefunItem slimefunItem = event.getSlimefunItem();
        String slimefunItemId = slimefunItem.getId();
        int definedLimit = BlockLimiter.getInstance().getPlayerLimitByItem(event.getPlayer(), slimefunItem);
        if (definedLimit == -1) {
            // No limit has been set, nothing required for HeadLimiter
            return;
        }

        ChunkPosition chunkPosition = new ChunkPosition(event.getBlockBroken().getChunk());
        ChunkContent content = BlockLimiter.getInstance().getChunkContent(chunkPosition);

        if (content == null) {
            // Content is null so no blocks are currently in this chunk, shouldn't be possible, but never mind
            return;
        }

        // This chunk can take more of the specified item type
        content.decrementAmount(slimefunItemId);

    }

}
