package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Camera;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConduitRenderer extends BlockEntityRenderer<ConduitBlockEntity> {
    private static final ResourceLocation SHELL_TEXTURE = new ResourceLocation("textures/entity/conduit/base.png");
    private static final ResourceLocation ACTIVE_SHELL_TEXTURE = new ResourceLocation("textures/entity/conduit/cage.png");
    private static final ResourceLocation WIND_TEXTURE = new ResourceLocation("textures/entity/conduit/wind.png");
    private static final ResourceLocation VERTICAL_WIND_TEXTURE = new ResourceLocation("textures/entity/conduit/wind_vertical.png");
    private static final ResourceLocation OPEN_EYE_TEXTURE = new ResourceLocation("textures/entity/conduit/open_eye.png");
    private static final ResourceLocation CLOSED_EYE_TEXTURE = new ResourceLocation("textures/entity/conduit/closed_eye.png");
    private final ConduitRenderer.ShellModel shellModel = new ConduitRenderer.ShellModel();
    private final ConduitRenderer.CageModel cageModel = new ConduitRenderer.CageModel();
    private final ConduitRenderer.WindModel windModel = new ConduitRenderer.WindModel();
    private final ConduitRenderer.EyeModel eyeModel = new ConduitRenderer.EyeModel();

    public void render(ConduitBlockEntity param0, double param1, double param2, double param3, float param4, int param5) {
        float var0 = (float)param0.tickCount + param4;
        if (!param0.isActive()) {
            float var1 = param0.getActiveRotation(0.0F);
            this.bindTexture(SHELL_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 0.5F, (float)param3 + 0.5F);
            GlStateManager.rotatef(var1, 0.0F, 1.0F, 0.0F);
            this.shellModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
            GlStateManager.popMatrix();
        } else if (param0.isActive()) {
            float var2 = param0.getActiveRotation(param4) * (180.0F / (float)Math.PI);
            float var3 = Mth.sin(var0 * 0.1F) / 2.0F + 0.5F;
            var3 = var3 * var3 + var3;
            this.bindTexture(ACTIVE_SHELL_TEXTURE);
            GlStateManager.disableCull();
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 0.3F + var3 * 0.2F, (float)param3 + 0.5F);
            GlStateManager.rotatef(var2, 0.5F, 1.0F, 0.5F);
            this.cageModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
            GlStateManager.popMatrix();
            int var4 = 3;
            int var5 = param0.tickCount / 3 % 22;
            this.windModel.setActiveAnim(var5);
            int var6 = param0.tickCount / 66 % 3;
            switch(var6) {
                case 0:
                    this.bindTexture(WIND_TEXTURE);
                    GlStateManager.pushMatrix();
                    GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 0.5F, (float)param3 + 0.5F);
                    this.windModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
                    GlStateManager.popMatrix();
                    GlStateManager.pushMatrix();
                    GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 0.5F, (float)param3 + 0.5F);
                    GlStateManager.scalef(0.875F, 0.875F, 0.875F);
                    GlStateManager.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
                    this.windModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
                    GlStateManager.popMatrix();
                    break;
                case 1:
                    this.bindTexture(VERTICAL_WIND_TEXTURE);
                    GlStateManager.pushMatrix();
                    GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 0.5F, (float)param3 + 0.5F);
                    GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                    this.windModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
                    GlStateManager.popMatrix();
                    GlStateManager.pushMatrix();
                    GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 0.5F, (float)param3 + 0.5F);
                    GlStateManager.scalef(0.875F, 0.875F, 0.875F);
                    GlStateManager.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
                    this.windModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
                    GlStateManager.popMatrix();
                    break;
                case 2:
                    this.bindTexture(WIND_TEXTURE);
                    GlStateManager.pushMatrix();
                    GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 0.5F, (float)param3 + 0.5F);
                    GlStateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
                    this.windModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
                    GlStateManager.popMatrix();
                    GlStateManager.pushMatrix();
                    GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 0.5F, (float)param3 + 0.5F);
                    GlStateManager.scalef(0.875F, 0.875F, 0.875F);
                    GlStateManager.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
                    this.windModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
                    GlStateManager.popMatrix();
            }

            Camera var7 = this.blockEntityRenderDispatcher.camera;
            if (param0.isHunting()) {
                this.bindTexture(OPEN_EYE_TEXTURE);
            } else {
                this.bindTexture(CLOSED_EYE_TEXTURE);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 0.3F + var3 * 0.2F, (float)param3 + 0.5F);
            GlStateManager.scalef(0.5F, 0.5F, 0.5F);
            GlStateManager.rotatef(-var7.getYRot(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotatef(var7.getXRot(), 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
            this.eyeModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.083333336F);
            GlStateManager.popMatrix();
        }

        super.render(param0, param1, param2, param3, param4, param5);
    }

    @OnlyIn(Dist.CLIENT)
    static class CageModel extends Model {
        private final ModelPart box;

        public CageModel() {
            this.texWidth = 32;
            this.texHeight = 16;
            this.box = new ModelPart(this, 0, 0);
            this.box.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
        }

        public void render(float param0, float param1, float param2, float param3, float param4, float param5) {
            this.box.render(param5);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class EyeModel extends Model {
        private final ModelPart eye;

        public EyeModel() {
            this.texWidth = 8;
            this.texHeight = 8;
            this.eye = new ModelPart(this, 0, 0);
            this.eye.addBox(-4.0F, -4.0F, 0.0F, 8, 8, 0, 0.01F);
        }

        public void render(float param0, float param1, float param2, float param3, float param4, float param5) {
            this.eye.render(param5);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class ShellModel extends Model {
        private final ModelPart box;

        public ShellModel() {
            this.texWidth = 32;
            this.texHeight = 16;
            this.box = new ModelPart(this, 0, 0);
            this.box.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6);
        }

        public void render(float param0, float param1, float param2, float param3, float param4, float param5) {
            this.box.render(param5);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class WindModel extends Model {
        private final ModelPart[] box = new ModelPart[22];
        private int activeAnim;

        public WindModel() {
            this.texWidth = 64;
            this.texHeight = 1024;

            for(int var0 = 0; var0 < 22; ++var0) {
                this.box[var0] = new ModelPart(this, 0, 32 * var0);
                this.box[var0].addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16);
            }

        }

        public void render(float param0, float param1, float param2, float param3, float param4, float param5) {
            this.box[this.activeAnim].render(param5);
        }

        public void setActiveAnim(int param0) {
            this.activeAnim = param0;
        }
    }
}
