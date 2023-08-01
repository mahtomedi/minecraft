package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractClientPlayer extends Player {
    @Nullable
    private PlayerInfo playerInfo;
    protected Vec3 deltaMovementOnPreviousTick = Vec3.ZERO;
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
        PlayerInfo var0 = this.getPlayerInfo();
        return var0 != null && var0.getGameMode() == GameType.SPECTATOR;
    }

    @Override
    public boolean isCreative() {
        PlayerInfo var0 = this.getPlayerInfo();
        return var0 != null && var0.getGameMode() == GameType.CREATIVE;
    }

    @Nullable
    protected PlayerInfo getPlayerInfo() {
        if (this.playerInfo == null) {
            this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getUUID());
        }

        return this.playerInfo;
    }

    @Override
    public void tick() {
        this.deltaMovementOnPreviousTick = this.getDeltaMovement();
        super.tick();
    }

    public Vec3 getDeltaMovementLerped(float param0) {
        return this.deltaMovementOnPreviousTick.lerp(this.getDeltaMovement(), (double)param0);
    }

    public PlayerSkin getSkin() {
        PlayerInfo var0 = this.getPlayerInfo();
        return var0 == null ? DefaultPlayerSkin.get(this.getUUID()) : var0.getSkin();
    }

    public float getFieldOfViewModifier() {
        float var0 = 1.0F;
        if (this.getAbilities().flying) {
            var0 *= 1.1F;
        }

        var0 *= ((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) / this.getAbilities().getWalkingSpeed() + 1.0F) / 2.0F;
        if (this.getAbilities().getWalkingSpeed() == 0.0F || Float.isNaN(var0) || Float.isInfinite(var0)) {
            var0 = 1.0F;
        }

        ItemStack var1 = this.getUseItem();
        if (this.isUsingItem()) {
            if (var1.is(Items.BOW)) {
                int var2 = this.getTicksUsingItem();
                float var3 = (float)var2 / 20.0F;
                if (var3 > 1.0F) {
                    var3 = 1.0F;
                } else {
                    var3 *= var3;
                }

                var0 *= 1.0F - var3 * 0.15F;
            } else if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && this.isScoping()) {
                return 0.1F;
            }
        }

        return Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get().floatValue(), 1.0F, var0);
    }
}
