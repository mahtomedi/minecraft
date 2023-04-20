package net.minecraft.client.gui.screens.social;

import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
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
    protected void enableScissor(GuiGraphics param0) {
        param0.enableScissor(this.x0, this.y0 + 4, this.x1, this.y1);
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
                boolean var3 = var2.hasVerifiableChat();
                param1.put(var1, new PlayerEntry(this.minecraft, this.socialInteractionsScreen, var1, var2.getProfile().getName(), var2::getSkinLocation, var3));
            }
        }

    }

    private void updatePlayersFromChatLog(Map<UUID, PlayerEntry> param0, boolean param1) {
        Collection<GameProfile> var0 = collectProfilesFromChatLog(this.minecraft.getReportingContext().chatLog());
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
                                Suppliers.memoize(() -> this.minecraft.getSkinManager().getInsecureSkinLocation(var1)),
                                true
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

    private static Collection<GameProfile> collectProfilesFromChatLog(ChatLog param0) {
        Set<GameProfile> var0 = new ObjectLinkedOpenHashSet<>();

        for(int var1 = param0.end(); var1 >= param0.start(); --var1) {
            LoggedChatEvent var2 = param0.lookup(var1);
            if (var2 instanceof LoggedChatMessage.Player var3 && var3.message().hasSignature()) {
                var0.add(var3.profile());
            }
        }

        return var0;
    }

    private void sortPlayerEntries() {
        this.players.sort(Comparator.<PlayerEntry, Integer>comparing(param0 -> {
            if (param0.getPlayerId().equals(this.minecraft.getUser().getProfileId())) {
                return 0;
            } else if (param0.getPlayerId().version() == 2) {
                return 4;
            } else if (this.minecraft.getReportingContext().hasDraftReportFor(param0.getPlayerId())) {
                return 1;
            } else {
                return param0.hasRecentMessages() ? 2 : 3;
            }
        }).thenComparing(param0 -> {
            if (!param0.getPlayerName().isBlank()) {
                int var0 = param0.getPlayerName().codePointAt(0);
                if (var0 == 95 || var0 >= 97 && var0 <= 122 || var0 >= 65 && var0 <= 90 || var0 >= 48 && var0 <= 57) {
                    return 0;
                }
            }

            return 1;
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
            boolean var2 = param0.hasVerifiableChat();
            PlayerEntry var3 = new PlayerEntry(
                this.minecraft, this.socialInteractionsScreen, param0.getProfile().getId(), param0.getProfile().getName(), param0::getSkinLocation, var2
            );
            this.addEntry(var3);
            this.players.add(var3);
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
