package net.minecraft.client.multiplayer;

import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;
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
        SkinManager var1 = var0.getSkinManager();
        CompletableFuture<PlayerSkin> var2 = var1.getOrLoad(param0);
        boolean var3 = !var0.isLocalPlayer(param0.getId());
        PlayerSkin var4 = DefaultPlayerSkin.get(param0);
        return () -> {
            PlayerSkin var0x = var2.getNow(var4);
            return var3 && !var0x.secure() ? var4 : var0x;
        };
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
        this.messageValidator = param0.createMessageValidator(ProfilePublicKey.EXPIRY_GRACE_PERIOD);
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
