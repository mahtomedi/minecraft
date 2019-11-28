package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SignRenderer extends BlockEntityRenderer<SignBlockEntity> {
    private final SignRenderer.SignModel signModel = new SignRenderer.SignModel();

    public SignRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(SignBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        BlockState var0 = param0.getBlockState();
        param2.pushPose();
        float var1 = 0.6666667F;
        if (var0.getBlock() instanceof StandingSignBlock) {
            param2.translate(0.5, 0.5, 0.5);
            float var2 = -((float)(var0.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F);
            param2.mulPose(Vector3f.YP.rotationDegrees(var2));
            this.signModel.stick.visible = true;
        } else {
            param2.translate(0.5, 0.5, 0.5);
            float var3 = -var0.getValue(WallSignBlock.FACING).toYRot();
            param2.mulPose(Vector3f.YP.rotationDegrees(var3));
            param2.translate(0.0, -0.3125, -0.4375);
            this.signModel.stick.visible = false;
        }

        param2.pushPose();
        param2.scale(0.6666667F, -0.6666667F, -0.6666667F);
        Material var4 = getMaterial(var0.getBlock());
        VertexConsumer var5 = var4.buffer(param3, this.signModel::renderType);
        this.signModel.sign.render(param2, var5, param4, param5);
        this.signModel.stick.render(param2, var5, param4, param5);
        param2.popPose();
        Font var6 = this.renderer.getFont();
        float var7 = 0.010416667F;
        param2.translate(0.0, 0.33333334F, 0.046666667F);
        param2.scale(0.010416667F, -0.010416667F, 0.010416667F);
        int var8 = param0.getColor().getTextColor();
        double var9 = 0.4;
        int var10 = (int)((double)NativeImage.getR(var8) * 0.4);
        int var11 = (int)((double)NativeImage.getG(var8) * 0.4);
        int var12 = (int)((double)NativeImage.getB(var8) * 0.4);
        int var13 = NativeImage.combine(0, var12, var11, var10);

        for(int var14 = 0; var14 < 4; ++var14) {
            String var15 = param0.getRenderMessage(var14, param1x -> {
                List<Component> var0x = ComponentRenderUtils.wrapComponents(param1x, 90, var6, false, true);
                return var0x.isEmpty() ? "" : var0x.get(0).getColoredString();
            });
            if (var15 != null) {
                float var16 = (float)(-var6.width(var15) / 2);
                var6.drawInBatch(var15, var16, (float)(var14 * 10 - param0.messages.length * 5), var13, false, param2.last().pose(), param3, false, 0, param4);
            }
        }

        param2.popPose();
    }

    public static Material getMaterial(Block param0) {
        WoodType var0;
        if (param0 instanceof SignBlock) {
            var0 = ((SignBlock)param0).type();
        } else {
            var0 = WoodType.OAK;
        }

        return Sheets.signTexture(var0);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class SignModel extends Model {
        public final ModelPart sign = new ModelPart(64, 32, 0, 0);
        public final ModelPart stick;

        public SignModel() {
            super(RenderType::entityCutoutNoCull);
            this.sign.addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F, 0.0F);
            this.stick = new ModelPart(64, 32, 0, 14);
            this.stick.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F, 0.0F);
        }

        @Override
        public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
            this.sign.render(param0, param1, param2, param3, param4, param5, param6, param7);
            this.stick.render(param0, param1, param2, param3, param4, param5, param6, param7);
        }
    }
}
