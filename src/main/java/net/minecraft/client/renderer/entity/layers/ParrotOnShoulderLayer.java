package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParrotOnShoulderLayer<T extends Player> extends RenderLayer<T, PlayerModel<T>> {
    private final ParrotModel model = new ParrotModel();

    public ParrotOnShoulderLayer(RenderLayerParent<T, PlayerModel<T>> param0) {
        super(param0);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        GlStateManager.enableRescaleNormal();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.render(param0, param1, param2, param3, param5, param6, param7, true);
        this.render(param0, param1, param2, param3, param5, param6, param7, false);
        GlStateManager.disableRescaleNormal();
    }

    private void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, boolean param7) {
        CompoundTag var0 = param7 ? param0.getShoulderEntityLeft() : param0.getShoulderEntityRight();
        EntityType.byString(var0.getString("id")).filter(param0x -> param0x == EntityType.PARROT).ifPresent(param8 -> {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(param7 ? 0.4F : -0.4F, param0.isVisuallySneaking() ? -1.3F : -1.5F, 0.0F);
            this.bindTexture(ParrotRenderer.PARROT_LOCATIONS[var0.getInt("Variant")]);
            this.model.renderOnShoulder(param1, param2, param4, param5, param6, param0.tickCount);
            GlStateManager.popMatrix();
        });
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
