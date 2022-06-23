package net.minecraft.client.gui.screens.social;

import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SocialInteractionsPlayerList extends ContainerObjectSelectionList<PlayerEntry> {
    private final SocialInteractionsScreen socialInteractionsScreen;
    private final List<PlayerEntry> players = Lists.newArrayList();
    @Nullable
    private String filter;

    public SocialInteractionsPlayerList(SocialInteractionsScreen param0, Minecraft param1, int param2, int param3, int param4, int param5, int param6) {
        super(param1, param2, param3, param4, param5, param6);
        this.socialInteractionsScreen = param0;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        double var0 = this.minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor(
            (int)((double)this.getRowLeft() * var0),
            (int)((double)(this.height - this.y1) * var0),
            (int)((double)(this.getScrollbarPosition() + 6) * var0),
            (int)((double)(this.height - (this.height - this.y1) - this.y0 - 4) * var0)
        );
        super.render(param0, param1, param2, param3);
        RenderSystem.disableScissor();
    }

    public void updatePlayerList(Collection<UUID> param0, double param1) {
        this.addOnlinePlayers(param0);
        this.updateFiltersAndScroll(param1);
    }

    public void updatePlayerListWithLog(Collection<UUID> param0, double param1) {
        this.addOnlinePlayers(param0);
        this.addPlayersFromLog(param0);
        this.updateFiltersAndScroll(param1);
    }

    private void addOnlinePlayers(Collection<UUID> param0) {
        this.players.clear();

        for(UUID var0 : param0) {
            PlayerInfo var1 = this.minecraft.player.connection.getPlayerInfo(var0);
            if (var1 != null) {
                this.players
                    .add(
                        new PlayerEntry(
                            this.minecraft, this.socialInteractionsScreen, var1.getProfile().getId(), var1.getProfile().getName(), var1::getSkinLocation
                        )
                    );
            }
        }

        this.players.sort((param0x, param1) -> param0x.getPlayerName().compareToIgnoreCase(param1.getPlayerName()));
    }

    private void addPlayersFromLog(Collection<UUID> param0) {
        for(GameProfile var1 : this.minecraft.getReportingContext().chatLog().selectAllDescending().distinctGameProfiles()) {
            if (!param0.contains(var1.getId())) {
                PlayerEntry var2 = new PlayerEntry(
                    this.minecraft,
                    this.socialInteractionsScreen,
                    var1.getId(),
                    var1.getName(),
                    Suppliers.memoize(() -> this.minecraft.getSkinManager().getInsecureSkinLocation(var1))
                );
                var2.setRemoved(true);
                this.players.add(var2);
            }
        }

    }

    private void updateFiltersAndScroll(double param0) {
        this.updateFilteredPlayers();
        this.replaceEntries(this.players);
        this.setScrollAmount(param0);
    }

    private void updateFilteredPlayers() {
        if (this.filter != null) {
            this.players.removeIf(param0 -> !param0.getPlayerName().toLowerCase(Locale.ROOT).contains(this.filter));
            this.replaceEntries(this.players);
        }

    }

    public void setFilter(String param0) {
        this.filter = param0;
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public void addPlayer(PlayerInfo param0, SocialInteractionsScreen.Page param1) {
        UUID var0 = param0.getProfile().getId();

        for(PlayerEntry var1 : this.players) {
            if (var1.getPlayerId().equals(var0)) {
                var1.setRemoved(false);
                return;
            }
        }

        if ((param1 == SocialInteractionsScreen.Page.ALL || this.minecraft.getPlayerSocialManager().shouldHideMessageFrom(var0))
            && (Strings.isNullOrEmpty(this.filter) || param0.getProfile().getName().toLowerCase(Locale.ROOT).contains(this.filter))) {
            PlayerEntry var2 = new PlayerEntry(
                this.minecraft, this.socialInteractionsScreen, param0.getProfile().getId(), param0.getProfile().getName(), param0::getSkinLocation
            );
            this.addEntry(var2);
            this.players.add(var2);
        }

    }

    public void removePlayer(UUID param0) {
        for(PlayerEntry var0 : this.players) {
            if (var0.getPlayerId().equals(param0)) {
                var0.setRemoved(true);
                return;
            }
        }

    }
}
