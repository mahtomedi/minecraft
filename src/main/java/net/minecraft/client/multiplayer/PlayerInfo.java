package net.minecraft.client.multiplayer;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.logging.LogUtils;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CryptException;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PlayerInfo {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GameProfile profile;
    private final Map<Type, ResourceLocation> textureLocations = Maps.newEnumMap(Type.class);
    private GameType gameMode;
    private int latency;
    private boolean pendingTextures;
    @Nullable
    private String skinModel;
    @Nullable
    private Component tabListDisplayName;
    private int lastHealth;
    private int displayHealth;
    private long lastHealthTime;
    private long healthBlinkTime;
    private long renderVisibilityId;
    @Nullable
    private final ProfilePublicKey profilePublicKey;
    private final SignedMessageValidator messageValidator;

    public PlayerInfo(ClientboundPlayerInfoPacket.PlayerUpdate param0, SignatureValidator param1) {
        this.profile = param0.getProfile();
        this.gameMode = param0.getGameMode();
        this.latency = param0.getLatency();
        this.tabListDisplayName = param0.getDisplayName();
        ProfilePublicKey var0 = null;

        try {
            ProfilePublicKey.Data var1 = param0.getProfilePublicKey();
            if (var1 != null) {
                var0 = ProfilePublicKey.createValidated(param1, this.profile.getId(), var1);
            }
        } catch (InsecurePublicKeyException | CryptException var5) {
            LOGGER.error("Failed to retrieve publicKey property for profile {}", this.profile.getId(), var5);
        }

        this.profilePublicKey = var0;
        this.messageValidator = SignedMessageValidator.create(var0);
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    @Nullable
    public ProfilePublicKey getProfilePublicKey() {
        return this.profilePublicKey;
    }

    public SignedMessageValidator getMessageValidator() {
        return this.messageValidator;
    }

    @Nullable
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

    public boolean isCapeLoaded() {
        return this.getCapeLocation() != null;
    }

    public boolean isSkinLoaded() {
        return this.getSkinLocation() != null;
    }

    public String getModelName() {
        return this.skinModel == null ? DefaultPlayerSkin.getSkinModelName(this.profile.getId()) : this.skinModel;
    }

    public ResourceLocation getSkinLocation() {
        this.registerTextures();
        return MoreObjects.firstNonNull(this.textureLocations.get(Type.SKIN), DefaultPlayerSkin.getDefaultSkin(this.profile.getId()));
    }

    @Nullable
    public ResourceLocation getCapeLocation() {
        this.registerTextures();
        return this.textureLocations.get(Type.CAPE);
    }

    @Nullable
    public ResourceLocation getElytraLocation() {
        this.registerTextures();
        return this.textureLocations.get(Type.ELYTRA);
    }

    @Nullable
    public PlayerTeam getTeam() {
        return Minecraft.getInstance().level.getScoreboard().getPlayersTeam(this.getProfile().getName());
    }

    protected void registerTextures() {
        synchronized(this) {
            if (!this.pendingTextures) {
                this.pendingTextures = true;
                Minecraft.getInstance().getSkinManager().registerSkins(this.profile, (param0, param1, param2) -> {
                    this.textureLocations.put(param0, param1);
                    if (param0 == Type.SKIN) {
                        this.skinModel = param2.getMetadata("model");
                        if (this.skinModel == null) {
                            this.skinModel = "default";
                        }
                    }

                }, true);
            }

        }
    }

    public void setTabListDisplayName(@Nullable Component param0) {
        this.tabListDisplayName = param0;
    }

    @Nullable
    public Component getTabListDisplayName() {
        return this.tabListDisplayName;
    }

    public int getLastHealth() {
        return this.lastHealth;
    }

    public void setLastHealth(int param0) {
        this.lastHealth = param0;
    }

    public int getDisplayHealth() {
        return this.displayHealth;
    }

    public void setDisplayHealth(int param0) {
        this.displayHealth = param0;
    }

    public long getLastHealthTime() {
        return this.lastHealthTime;
    }

    public void setLastHealthTime(long param0) {
        this.lastHealthTime = param0;
    }

    public long getHealthBlinkTime() {
        return this.healthBlinkTime;
    }

    public void setHealthBlinkTime(long param0) {
        this.healthBlinkTime = param0;
    }

    public long getRenderVisibilityId() {
        return this.renderVisibilityId;
    }

    public void setRenderVisibilityId(long param0) {
        this.renderVisibilityId = param0;
    }
}
