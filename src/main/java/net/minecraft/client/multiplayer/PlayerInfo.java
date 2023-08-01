package net.minecraft.client.multiplayer;

import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerInfo {
    private final GameProfile profile;
    private final Supplier<PlayerSkin> skinLookup;
    private GameType gameMode = GameType.DEFAULT_MODE;
    private int latency;
    @Nullable
    private Component tabListDisplayName;
    @Nullable
    private RemoteChatSession chatSession;
    private SignedMessageValidator messageValidator;

    public PlayerInfo(GameProfile param0, boolean param1) {
        this.profile = param0;
        this.messageValidator = fallbackMessageValidator(param1);
        Supplier<Supplier<PlayerSkin>> var0 = Suppliers.memoize(() -> createSkinLookup(param0));
        this.skinLookup = () -> var0.get().get();
    }

    private static Supplier<PlayerSkin> createSkinLookup(GameProfile param0) {
        Minecraft var0 = Minecraft.getInstance();
        CompletableFuture<PlayerSkin> var1 = loadSkin(param0, var0.getSkinManager(), var0.getMinecraftSessionService());
        boolean var2 = !var0.isLocalPlayer(param0.getId());
        PlayerSkin var3 = DefaultPlayerSkin.get(param0);
        return () -> {
            PlayerSkin var0x = var1.getNow(var3);
            return var2 && !var0x.secure() ? var3 : var0x;
        };
    }

    private static CompletableFuture<PlayerSkin> loadSkin(GameProfile param0, SkinManager param1, MinecraftSessionService param2) {
        CompletableFuture<GameProfile> var0;
        if (param1.hasSecureTextureData(param0)) {
            var0 = CompletableFuture.completedFuture(param0);
        } else {
            var0 = CompletableFuture.supplyAsync(() -> fillProfileProperties(param0, param2), Util.ioPool());
        }

        return var0.thenCompose(param1::getOrLoad);
    }

    private static GameProfile fillProfileProperties(GameProfile param0, MinecraftSessionService param1) {
        Minecraft var0 = Minecraft.getInstance();
        param0.getProperties().clear();
        if (var0.isLocalPlayer(param0.getId())) {
            param0.getProperties().putAll(var0.getProfileProperties());
        } else {
            GameProfile var1 = param1.fetchProfile(param0.getId(), true);
            if (var1 != null) {
                var1.getProperties().putAll(var1.getProperties());
            }
        }

        return param0;
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    @Nullable
    public RemoteChatSession getChatSession() {
        return this.chatSession;
    }

    public SignedMessageValidator getMessageValidator() {
        return this.messageValidator;
    }

    public boolean hasVerifiableChat() {
        return this.chatSession != null;
    }

    protected void setChatSession(RemoteChatSession param0) {
        this.chatSession = param0;
        this.messageValidator = param0.createMessageValidator();
    }

    protected void clearChatSession(boolean param0) {
        this.chatSession = null;
        this.messageValidator = fallbackMessageValidator(param0);
    }

    private static SignedMessageValidator fallbackMessageValidator(boolean param0) {
        return param0 ? SignedMessageValidator.REJECT_ALL : SignedMessageValidator.ACCEPT_UNSIGNED;
    }

    public GameType getGameMode() {
        return this.gameMode;
    }

    protected void setGameMode(GameType param0) {
        this.gameMode = param0;
    }

    public int getLatency() {
        return this.latency;
    }

    protected void setLatency(int param0) {
        this.latency = param0;
    }

    public PlayerSkin getSkin() {
        return this.skinLookup.get();
    }

    @Nullable
    public PlayerTeam getTeam() {
        return Minecraft.getInstance().level.getScoreboard().getPlayersTeam(this.getProfile().getName());
    }

    public void setTabListDisplayName(@Nullable Component param0) {
        this.tabListDisplayName = param0;
    }

    @Nullable
    public Component getTabListDisplayName() {
        return this.tabListDisplayName;
    }
}
