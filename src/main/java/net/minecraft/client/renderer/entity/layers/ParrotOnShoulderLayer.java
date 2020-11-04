package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParrotOnShoulderLayer<T extends Player> extends RenderLayer<T, PlayerModel<T>> {
    private final ParrotModel model;

    public ParrotOnShoulderLayer(RenderLayerParent<T, PlayerModel<T>> param0, EntityModelSet param1) {
        super(param0);
        this.model = new ParrotModel(param1.getLayer(ModelLayers.PARROT));
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        this.render(param0, param1, param2, param3, param4, param5, param8, param9, true);
        this.render(param0, param1, param2, param3, param4, param5, param8, param9, false);
    }

    private void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, boolean param8
    ) {
        CompoundTag var0 = param8 ? param3.getShoulderEntityLeft() : param3.getShoulderEntityRight();
        EntityType.byString(var0.getString("id")).filter(param0x -> param0x == EntityType.PARROT).ifPresent(param10 -> {
            param0.pushPose();
            param0.translate(param8 ? 0.4F : -0.4F, param3.isCrouching() ? -1.3F : -1.5, 0.0);
            VertexConsumer var0x = param1.getBuffer(this.model.renderType(ParrotRenderer.PARROT_LOCATIONS[var0.getInt("Variant")]));
            this.model.renderOnShoulder(param0, var0x, param2, OverlayTexture.NO_OVERLAY, param4, param5, param6, param7, param3.tickCount);
            param0.popPose();
        });
    }
}
