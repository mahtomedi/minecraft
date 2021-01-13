package net.minecraft.client.player;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractClientPlayer extends Player {
    private PlayerInfo playerInfo;
    public float elytraRotX;
    public float elytraRotY;
    public float elytraRotZ;
    public final ClientLevel clientLevel;

    public AbstractClientPlayer(ClientLevel param0, GameProfile param1) {
        super(param0, param0.getSharedSpawnPos(), param0.getSharedSpawnAngle(), param1);
        this.clientLevel = param0;
    }

    @Override
    public boolean isSpectator() {
        PlayerInfo var0 = Minecraft.getInstance().getConnection().getPlayerInfo(this.getGameProfile().getId());
        return var0 != null && var0.getGameMode() == GameType.SPECTATOR;
    }

    @Override
    public boolean isCreative() {
        PlayerInfo var0 = Minecraft.getInstance().getConnection().getPlayerInfo(this.getGameProfile().getId());
        return var0 != null && var0.getGameMode() == GameType.CREATIVE;
    }

    public boolean isCapeLoaded() {
        return this.getPlayerInfo() != null;
    }

    @Nullable
    protected PlayerInfo getPlayerInfo() {
        if (this.playerInfo == null) {
            this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getUUID());
        }

        return this.playerInfo;
    }

    public boolean isSkinLoaded() {
        PlayerInfo var0 = this.getPlayerInfo();
        return var0 != null && var0.isSkinLoaded();
    }

    public ResourceLocation getSkinTextureLocation() {
        PlayerInfo var0 = this.getPlayerInfo();
        return var0 == null ? DefaultPlayerSkin.getDefaultSkin(this.getUUID()) : var0.getSkinLocation();
    }

    @Nullable
    public ResourceLocation getCloakTextureLocation() {
        PlayerInfo var0 = this.getPlayerInfo();
        return var0 == null ? null : var0.getCapeLocation();
    }

    public boolean isElytraLoaded() {
        return this.getPlayerInfo() != null;
    }

    @Nullable
    public ResourceLocation getElytraTextureLocation() {
        PlayerInfo var0 = this.getPlayerInfo();
        return var0 == null ? null : var0.getElytraLocation();
    }

    public static HttpTexture registerSkinTexture(ResourceLocation param0, String param1) {
        TextureManager var0 = Minecraft.getInstance().getTextureManager();
        AbstractTexture var1 = var0.getTexture(param0);
        if (var1 == null) {
            var1 = new HttpTexture(
                null,
                String.format("http://skins.minecraft.net/MinecraftSkins/%s.png", StringUtil.stripColor(param1)),
                DefaultPlayerSkin.getDefaultSkin(createPlayerUUID(param1)),
                true,
                null
            );
            var0.register(param0, var1);
        }

        return (HttpTexture)var1;
    }

    public static ResourceLocation getSkinLocation(String param0) {
        return new ResourceLocation("skins/" + Hashing.sha1().hashUnencodedChars(StringUtil.stripColor(param0)));
    }

    public String getModelName() {
        PlayerInfo var0 = this.getPlayerInfo();
        return var0 == null ? DefaultPlayerSkin.getSkinModelName(this.getUUID()) : var0.getModelName();
    }

    public float getFieldOfViewModifier() {
        float var0 = 1.0F;
        if (this.abilities.flying) {
            var0 *= 1.1F;
        }

        var0 = (float)((double)var0 * ((this.getAttributeValue(Attributes.MOVEMENT_SPEED) / (double)this.abilities.getWalkingSpeed() + 1.0) / 2.0));
        if (this.abilities.getWalkingSpeed() == 0.0F || Float.isNaN(var0) || Float.isInfinite(var0)) {
            var0 = 1.0F;
        }

        if (this.isUsingItem() && this.getUseItem().getItem() == Items.BOW) {
            int var1 = this.getTicksUsingItem();
            float var2 = (float)var1 / 20.0F;
            if (var2 > 1.0F) {
                var2 = 1.0F;
            } else {
                var2 *= var2;
            }

            var0 *= 1.0F - var2 * 0.15F;
        }

        return Mth.lerp(Minecraft.getInstance().options.fovEffectScale, 1.0F, var0);
    }
}
