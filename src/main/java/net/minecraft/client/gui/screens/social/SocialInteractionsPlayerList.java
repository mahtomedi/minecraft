package net.minecraft.client.gui.screens.social;

import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.multiplayer.ClientPacketListener;
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

    public void updatePlayerList(Collection<UUID> param0, double param1, boolean param2) {
        Map<UUID, PlayerEntry> var0 = new HashMap<>();
        this.addOnlinePlayers(param0, var0);
        this.updatePlayersFromChatLog(var0, param2);
        this.updateFiltersAndScroll(var0.values(), param1);
    }

    private void addOnlinePlayers(Collection<UUID> param0, Map<UUID, PlayerEntry> param1) {
        ClientPacketListener var0 = this.minecraft.player.connection;

        for(UUID var1 : param0) {
            PlayerInfo var2 = var0.getPlayerInfo(var1);
            if (var2 != null) {
                UUID var3 = var2.getProfile().getId();
                param1.put(var3, new PlayerEntry(this.minecraft, this.socialInteractionsScreen, var3, var2.getProfile().getName(), var2::getSkinLocation));
            }
        }

    }

    private void updatePlayersFromChatLog(Map<UUID, PlayerEntry> param0, boolean param1) {
        Collection<GameProfile> var0 = this.minecraft.getReportingContext().chatLog().selectAllDescending().distinctGameProfiles();
        Iterator var4 = var0.iterator();

        while(true) {
            PlayerEntry var2;
            do {
                if (!var4.hasNext()) {
                    return;
                }

                GameProfile var1 = (GameProfile)var4.next();
                if (param1) {
                    var2 = param0.computeIfAbsent(
                        var1.getId(),
                        param1x -> {
                            PlayerEntry var0x = new PlayerEntry(
                                this.minecraft,
                                this.socialInteractionsScreen,
                                var1.getId(),
                                var1.getName(),
                                Suppliers.memoize(() -> this.minecraft.getSkinManager().getInsecureSkinLocation(var1))
                            );
                            var0x.setRemoved(true);
                            return var0x;
                        }
                    );
                    break;
                }

                var2 = param0.get(var1.getId());
            } while(var2 == null);

            var2.setHasRecentMessages(true);
        }
    }

    private void sortPlayerEntries() {
        this.players.sort(Comparator.<PlayerEntry, Integer>comparing(param0 -> {
            if (param0.getPlayerId().equals(this.minecraft.getUser().getProfileId())) {
                return 0;
            } else if (param0.getPlayerId().version() == 2) {
                return 3;
            } else {
                return param0.hasRecentMessages() ? 1 : 2;
            }
        }).thenComparing(param0 -> {
            int var0 = param0.getPlayerName().codePointAt(0);
            return var0 != 95 && (var0 < 97 || var0 > 122) && (var0 < 65 || var0 > 90) && (var0 < 48 || var0 > 57) ? 1 : 0;
        }).thenComparing(PlayerEntry::getPlayerName, String::compareToIgnoreCase));
    }

    private void updateFiltersAndScroll(Collection<PlayerEntry> param0, double param1) {
        this.players.clear();
        this.players.addAll(param0);
        this.sortPlayerEntries();
        this.updateFilteredPlayers();
        this.replaceEntries(this.players);
        this.setScrollAmount(param1);
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
