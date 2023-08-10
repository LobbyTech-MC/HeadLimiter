package dev.j3fftw.headlimiter.blocklimiter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.xzavier0722.mc.plugin.slimefun4.storage.callback.IAsyncReadCallback;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.ChunkPosition;

import dev.j3fftw.headlimiter.HeadLimiter;

public final class BlockLimiter {

    private static BlockLimiter instance;
    private final HashSet<Group> groups = new HashSet<>();
    private final Map<ChunkPosition, ChunkContent> contentMap = new HashMap<>();

    public BlockLimiter(@Nonnull HeadLimiter headLimiter) {
        Preconditions.checkArgument(instance == null, "Cannot create a new instance of the BlockLimiter");
        instance = this;
        new BlockListener(headLimiter);
    }

    @Nullable
    public ChunkContent getChunkContent(@Nonnull ChunkPosition chunkPosition) {
        return contentMap.get(chunkPosition);
    }

    public Group getGroupByItem(@Nonnull SlimefunItem slimefunItem) {
        return getGroupByItem(slimefunItem.getId());
    }

    @Nullable
    public Group getGroupByItem(@Nonnull String itemId) {
        for (Group group : this.groups) {
            if (group.contains(itemId)) {
                return group;
            }
        }
        return null;
    }

    public int getPlayerLimitByItem(@Nonnull Player player, @Nonnull SlimefunItem slimefunItem) {
        return getPlayerLimitByItem(player, slimefunItem.getId());
    }

    public int getPlayerLimitByItem(@Nonnull Player player, @Nonnull String itemId) {
        Group group = getGroupByItem(itemId);
        if (group == null) {
            return -1;
        } else {
            return group.getPermissibleAmount(player);
        }
    }

    public Set<Group> getGroups() {
        return groups;
    }

    public void setChunkContent(@Nonnull ChunkPosition chunkPosition, @Nonnull ChunkContent content) {
        contentMap.put(chunkPosition, content);
    }

    @Nonnull
    public static BlockLimiter getInstance() {
        return instance;
    }
}
