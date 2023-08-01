package net.minecraft.client.gui.screens.social;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.UserApiService;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerSocialManager {
    private final Minecraft minecraft;
    private final Set<UUID> hiddenPlayers = Sets.newHashSet();
    private final UserApiService service;
    private final Map<String, UUID> discoveredNamesToUUID = Maps.newHashMap();
    private boolean onlineMode;
    private CompletableFuture<?> pendingBlockListRefresh = CompletableFuture.completedFuture(null);

    public PlayerSocialManager(Minecraft param0, UserApiService param1) {
        this.minecraft = param0;
        this.service = param1;
    }

    public void hidePlayer(UUID param0) {
        this.hiddenPlayers.add(param0);
    }

    public void showPlayer(UUID param0) {
        this.hiddenPlayers.remove(param0);
    }

    public boolean shouldHideMessageFrom(UUID param0) {
        return this.isHidden(param0) || this.isBlocked(param0);
    }

    public boolean isHidden(UUID param0) {
        return this.hiddenPlayers.contains(param0);
    }

    public void startOnlineMode() {
        this.onlineMode = true;
        this.pendingBlockListRefresh = this.pendingBlockListRefresh.thenRunAsync(this.service::refreshBlockList, Util.ioPool());
    }

    public void stopOnlineMode() {
        this.onlineMode = false;
    }

    public boolean isBlocked(UUID param0) {
        if (!this.onlineMode) {
            return false;
        } else {
            this.pendingBlockListRefresh.join();
            return this.service.isBlockedPlayer(param0);
        }
    }

    public Set<UUID> getHiddenPlayers() {
        return this.hiddenPlayers;
    }

    public UUID getDiscoveredUUID(String param0) {
        return this.discoveredNamesToUUID.getOrDefault(param0, Util.NIL_UUID);
    }

    public void addPlayer(PlayerInfo param0) {
        GameProfile var0 = param0.getProfile();
        this.discoveredNamesToUUID.put(var0.getName(), var0.getId());
        Screen var4 = this.minecraft.screen;
        if (var4 instanceof SocialInteractionsScreen var1) {
            var1.onAddPlayer(param0);
        }

    }

    public void removePlayer(UUID param0) {
        Screen var3 = this.minecraft.screen;
        if (var3 instanceof SocialInteractionsScreen var0) {
            var0.onRemovePlayer(param0);
        }

    }
}
