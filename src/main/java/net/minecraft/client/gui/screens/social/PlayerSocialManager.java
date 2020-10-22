package net.minecraft.client.gui.screens.social;

import com.google.common.collect.Sets;
import com.mojang.authlib.minecraft.SocialInteractionsService;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerSocialManager {
    private final Minecraft minecraft;
    private final Set<UUID> hiddenPlayers = Sets.newHashSet();
    private final SocialInteractionsService service;

    public PlayerSocialManager(Minecraft param0, SocialInteractionsService param1) {
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

    public boolean isBlocked(UUID param0) {
        return this.service.isBlockedPlayer(param0);
    }

    public Set<UUID> getHiddenPlayers() {
        return this.hiddenPlayers;
    }

    public void addPlayer(PlayerInfo param0) {
        Screen var0 = this.minecraft.screen;
        if (var0 instanceof SocialInteractionsScreen) {
            SocialInteractionsScreen var1 = (SocialInteractionsScreen)var0;
            var1.onAddPlayer(param0);
        }

    }

    public void removePlayer(UUID param0) {
        Screen var0 = this.minecraft.screen;
        if (var0 instanceof SocialInteractionsScreen) {
            SocialInteractionsScreen var1 = (SocialInteractionsScreen)var0;
            var1.onRemovePlayer(param0);
        }

    }
}
