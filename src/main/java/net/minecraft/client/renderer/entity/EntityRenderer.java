package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class EntityRenderer<T extends Entity> {
    private static final ResourceLocation SHADOW_LOCATION = new ResourceLocation("textures/misc/shadow.png");
    protected final EntityRenderDispatcher entityRenderDispatcher;
    protected float shadowRadius;
    protected float shadowStrength = 1.0F;
    protected boolean solidRender;

    protected EntityRenderer(EntityRenderDispatcher param0) {
        this.entityRenderDispatcher = param0;
    }

    public void setSolidRender(boolean param0) {
        this.solidRender = param0;
    }

    public boolean shouldRender(T param0, Culler param1, double param2, double param3, double param4) {
        if (!param0.shouldRender(param2, param3, param4)) {
            return false;
        } else if (param0.noCulling) {
            return true;
        } else {
            AABB var0 = param0.getBoundingBoxForCulling().inflate(0.5);
            if (var0.hasNaN() || var0.getSize() == 0.0) {
                var0 = new AABB(param0.x - 2.0, param0.y - 2.0, param0.z - 2.0, param0.x + 2.0, param0.y + 2.0, param0.z + 2.0);
            }

            return param1.isVisible(var0);
        }
    }

    public void render(T param0, double param1, double param2, double param3, float param4, float param5) {
        if (!this.solidRender) {
            this.renderName(param0, param1, param2, param3);
        }

    }

    protected int getTeamColor(T param0) {
        PlayerTeam var0 = (PlayerTeam)param0.getTeam();
        return var0 != null && var0.getColor().getColor() != null ? var0.getColor().getColor() : 16777215;
    }

    protected void renderName(T param0, double param1, double param2, double param3) {
        if (this.shouldShowName(param0)) {
            this.renderNameTag(param0, param0.getDisplayName().getColoredString(), param1, param2, param3, 64);
        }
    }

    protected boolean shouldShowName(T param0) {
        return param0.shouldShowName() && param0.hasCustomName();
    }

    protected void renderNameTags(T param0, double param1, double param2, double param3, String param4, double param5) {
        this.renderNameTag(param0, param4, param1, param2, param3, 64);
    }

    @Nullable
    protected abstract ResourceLocation getTextureLocation(T var1);

    protected boolean bindTexture(T param0) {
        ResourceLocation var0 = this.getTextureLocation(param0);
        if (var0 == null) {
            return false;
        } else {
            this.bindTexture(var0);
            return true;
        }
    }

    public void bindTexture(ResourceLocation param0) {
        this.entityRenderDispatcher.textureManager.bind(param0);
    }

    private void renderFlame(Entity param0, double param1, double param2, double param3, float param4) {
        RenderSystem.disableLighting();
        TextureAtlas var0 = Minecraft.getInstance().getTextureAtlas();
        TextureAtlasSprite var1 = var0.getSprite(ModelBakery.FIRE_0);
        TextureAtlasSprite var2 = var0.getSprite(ModelBakery.FIRE_1);
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)param1, (float)param2, (float)param3);
        float var3 = param0.getBbWidth() * 1.4F;
        RenderSystem.scalef(var3, var3, var3);
        Tesselator var4 = Tesselator.getInstance();
        BufferBuilder var5 = var4.getBuilder();
        float var6 = 0.5F;
        float var7 = 0.0F;
        float var8 = param0.getBbHeight() / var3;
        float var9 = (float)(param0.y - param0.getBoundingBox().minY);
        RenderSystem.rotatef(-this.entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
        RenderSystem.translatef(0.0F, 0.0F, -0.3F + (float)((int)var8) * 0.02F);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var10 = 0.0F;
        int var11 = 0;
        var5.begin(7, DefaultVertexFormat.POSITION_TEX);

        while(var8 > 0.0F) {
            TextureAtlasSprite var12 = var11 % 2 == 0 ? var1 : var2;
            this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
            float var13 = var12.getU0();
            float var14 = var12.getV0();
            float var15 = var12.getU1();
            float var16 = var12.getV1();
            if (var11 / 2 % 2 == 0) {
                float var17 = var15;
                var15 = var13;
                var13 = var17;
            }

            var5.vertex((double)(var6 - 0.0F), (double)(0.0F - var9), (double)var10).uv((double)var15, (double)var16).endVertex();
            var5.vertex((double)(-var6 - 0.0F), (double)(0.0F - var9), (double)var10).uv((double)var13, (double)var16).endVertex();
            var5.vertex((double)(-var6 - 0.0F), (double)(1.4F - var9), (double)var10).uv((double)var13, (double)var14).endVertex();
            var5.vertex((double)(var6 - 0.0F), (double)(1.4F - var9), (double)var10).uv((double)var15, (double)var14).endVertex();
            var8 -= 0.45F;
            var9 -= 0.45F;
            var6 *= 0.9F;
            var10 += 0.03F;
            ++var11;
        }

        var4.end();
        RenderSystem.popMatrix();
        RenderSystem.enableLighting();
    }

    private void renderShadow(Entity param0, double param1, double param2, double param3, float param4, float param5) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        this.entityRenderDispatcher.textureManager.bind(SHADOW_LOCATION);
        LevelReader var0 = this.getLevel();
        RenderSystem.depthMask(false);
        float var1 = this.shadowRadius;
        if (param0 instanceof Mob) {
            Mob var2 = (Mob)param0;
            if (var2.isBaby()) {
                var1 *= 0.5F;
            }
        }

        double var3 = Mth.lerp((double)param5, param0.xOld, param0.x);
        double var4 = Mth.lerp((double)param5, param0.yOld, param0.y);
        double var5 = Mth.lerp((double)param5, param0.zOld, param0.z);
        int var6 = Mth.floor(var3 - (double)var1);
        int var7 = Mth.floor(var3 + (double)var1);
        int var8 = Mth.floor(var4 - (double)var1);
        int var9 = Mth.floor(var4);
        int var10 = Mth.floor(var5 - (double)var1);
        int var11 = Mth.floor(var5 + (double)var1);
        double var12 = param1 - var3;
        double var13 = param2 - var4;
        double var14 = param3 - var5;
        Tesselator var15 = Tesselator.getInstance();
        BufferBuilder var16 = var15.getBuilder();
        var16.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);

        for(BlockPos var17 : BlockPos.betweenClosed(new BlockPos(var6, var8, var10), new BlockPos(var7, var9, var11))) {
            BlockPos var18 = var17.below();
            BlockState var19 = var0.getBlockState(var18);
            if (var19.getRenderShape() != RenderShape.INVISIBLE && var0.getMaxLocalRawBrightness(var17) > 3) {
                this.renderBlockShadow(var19, var0, var18, param1, param2, param3, var17, param4, var1, var12, var13, var14);
            }
        }

        var15.end();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    private LevelReader getLevel() {
        return this.entityRenderDispatcher.level;
    }

    private void renderBlockShadow(
        BlockState param0,
        LevelReader param1,
        BlockPos param2,
        double param3,
        double param4,
        double param5,
        BlockPos param6,
        float param7,
        float param8,
        double param9,
        double param10,
        double param11
    ) {
        if (param0.isCollisionShapeFullBlock(param1, param2)) {
            VoxelShape var0 = param0.getShape(this.getLevel(), param6.below());
            if (!var0.isEmpty()) {
                Tesselator var1 = Tesselator.getInstance();
                BufferBuilder var2 = var1.getBuilder();
                double var3 = ((double)param7 - (param4 - ((double)param6.getY() + param10)) / 2.0) * 0.5 * (double)this.getLevel().getBrightness(param6);
                if (!(var3 < 0.0)) {
                    if (var3 > 1.0) {
                        var3 = 1.0;
                    }

                    AABB var4 = var0.bounds();
                    double var5 = (double)param6.getX() + var4.minX + param9;
                    double var6 = (double)param6.getX() + var4.maxX + param9;
                    double var7 = (double)param6.getY() + var4.minY + param10 + 0.015625;
                    double var8 = (double)param6.getZ() + var4.minZ + param11;
                    double var9 = (double)param6.getZ() + var4.maxZ + param11;
                    float var10 = (float)((param3 - var5) / 2.0 / (double)param8 + 0.5);
                    float var11 = (float)((param3 - var6) / 2.0 / (double)param8 + 0.5);
                    float var12 = (float)((param5 - var8) / 2.0 / (double)param8 + 0.5);
                    float var13 = (float)((param5 - var9) / 2.0 / (double)param8 + 0.5);
                    var2.vertex(var5, var7, var8).uv((double)var10, (double)var12).color(1.0F, 1.0F, 1.0F, (float)var3).endVertex();
                    var2.vertex(var5, var7, var9).uv((double)var10, (double)var13).color(1.0F, 1.0F, 1.0F, (float)var3).endVertex();
                    var2.vertex(var6, var7, var9).uv((double)var11, (double)var13).color(1.0F, 1.0F, 1.0F, (float)var3).endVertex();
                    var2.vertex(var6, var7, var8).uv((double)var11, (double)var12).color(1.0F, 1.0F, 1.0F, (float)var3).endVertex();
                }
            }
        }
    }

    public static void render(AABB param0, double param1, double param2, double param3) {
        RenderSystem.disableTexture();
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        var1.offset(param1, param2, param3);
        var1.begin(7, DefaultVertexFormat.POSITION_NORMAL);
        var1.vertex(param0.minX, param0.maxY, param0.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        var1.vertex(param0.maxX, param0.maxY, param0.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        var1.vertex(param0.maxX, param0.minY, param0.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        var1.vertex(param0.minX, param0.minY, param0.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        var1.vertex(param0.minX, param0.minY, param0.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        var1.vertex(param0.maxX, param0.minY, param0.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        var1.vertex(param0.maxX, param0.maxY, param0.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        var1.vertex(param0.minX, param0.maxY, param0.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        var1.vertex(param0.minX, param0.minY, param0.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        var1.vertex(param0.maxX, param0.minY, param0.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        var1.vertex(param0.maxX, param0.minY, param0.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        var1.vertex(param0.minX, param0.minY, param0.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        var1.vertex(param0.minX, param0.maxY, param0.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        var1.vertex(param0.maxX, param0.maxY, param0.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        var1.vertex(param0.maxX, param0.maxY, param0.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        var1.vertex(param0.minX, param0.maxY, param0.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        var1.vertex(param0.minX, param0.minY, param0.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        var1.vertex(param0.minX, param0.maxY, param0.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        var1.vertex(param0.minX, param0.maxY, param0.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        var1.vertex(param0.minX, param0.minY, param0.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        var1.vertex(param0.maxX, param0.minY, param0.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        var1.vertex(param0.maxX, param0.maxY, param0.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        var1.vertex(param0.maxX, param0.maxY, param0.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        var1.vertex(param0.maxX, param0.minY, param0.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        var0.end();
        var1.offset(0.0, 0.0, 0.0);
        RenderSystem.enableTexture();
    }

    public void postRender(Entity param0, double param1, double param2, double param3, float param4, float param5) {
        if (this.entityRenderDispatcher.options != null) {
            if (this.entityRenderDispatcher.options.entityShadows
                && this.shadowRadius > 0.0F
                && !param0.isInvisible()
                && this.entityRenderDispatcher.shouldRenderShadow()) {
                double var0 = this.entityRenderDispatcher.distanceToSqr(param0.x, param0.y, param0.z);
                float var1 = (float)((1.0 - var0 / 256.0) * (double)this.shadowStrength);
                if (var1 > 0.0F) {
                    this.renderShadow(param0, param1, param2, param3, var1, param5);
                }
            }

            if (param0.displayFireAnimation() && !param0.isSpectator()) {
                this.renderFlame(param0, param1, param2, param3, param5);
            }

        }
    }

    public Font getFont() {
        return this.entityRenderDispatcher.getFont();
    }

    protected void renderNameTag(T param0, String param1, double param2, double param3, double param4, int param5) {
        double var0 = param0.distanceToSqr(this.entityRenderDispatcher.camera.getPosition());
        if (!(var0 > (double)(param5 * param5))) {
            float var1 = this.entityRenderDispatcher.playerRotY;
            float var2 = this.entityRenderDispatcher.playerRotX;
            float var3 = param0.getBbHeight() + 0.5F - (param0.isCrouching() ? 0.25F : 0.0F);
            int var4 = "deadmau5".equals(param1) ? -10 : 0;
            GameRenderer.renderNameTagInWorld(this.getFont(), param1, (float)param2, (float)param3 + var3, (float)param4, var4, var1, var2, param0.isDiscrete());
        }
    }

    public EntityRenderDispatcher getDispatcher() {
        return this.entityRenderDispatcher;
    }

    public boolean hasSecondPass() {
        return false;
    }

    public void renderSecondPass(T param0, double param1, double param2, double param3, float param4, float param5) {
    }

    public void setLightColor(T param0) {
        int var0 = param0.getLightColor();
        int var1 = var0 % 65536;
        int var2 = var0 / 65536;
        RenderSystem.glMultiTexCoord2f(33985, (float)var1, (float)var2);
    }
}
