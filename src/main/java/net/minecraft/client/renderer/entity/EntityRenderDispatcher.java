package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class EntityRenderDispatcher implements ResourceManagerReloadListener {
    private static final RenderType SHADOW_RENDER_TYPE = RenderType.entityShadow(new ResourceLocation("textures/misc/shadow.png"));
    private static final float MAX_SHADOW_RADIUS = 32.0F;
    private static final float SHADOW_POWER_FALLOFF_Y = 0.5F;
    private Map<EntityType<?>, EntityRenderer<?>> renderers = ImmutableMap.of();
    private Map<PlayerSkin.Model, EntityRenderer<? extends Player>> playerRenderers = Map.of();
    public final TextureManager textureManager;
    private Level level;
    public Camera camera;
    private Quaternionf cameraOrientation;
    public Entity crosshairPickEntity;
    private final ItemRenderer itemRenderer;
    private final BlockRenderDispatcher blockRenderDispatcher;
    private final ItemInHandRenderer itemInHandRenderer;
    private final Font font;
    public final Options options;
    private final EntityModelSet entityModels;
    private boolean shouldRenderShadow = true;
    private boolean renderHitBoxes;

    public <E extends Entity> int getPackedLightCoords(E param0, float param1) {
        return this.getRenderer(param0).getPackedLightCoords(param0, param1);
    }

    public EntityRenderDispatcher(
        Minecraft param0, TextureManager param1, ItemRenderer param2, BlockRenderDispatcher param3, Font param4, Options param5, EntityModelSet param6
    ) {
        this.textureManager = param1;
        this.itemRenderer = param2;
        this.itemInHandRenderer = new ItemInHandRenderer(param0, this, param2);
        this.blockRenderDispatcher = param3;
        this.font = param4;
        this.options = param5;
        this.entityModels = param6;
    }

    public <T extends Entity> EntityRenderer<? super T> getRenderer(T param0) {
        if (param0 instanceof AbstractClientPlayer var0) {
            PlayerSkin.Model var1 = var0.getSkin().model();
            EntityRenderer<? extends Player> var2 = this.playerRenderers.get(var1);
            return var2 != null ? var2 : this.playerRenderers.get(PlayerSkin.Model.WIDE);
        } else {
            return (EntityRenderer<? super T>)this.renderers.get(param0.getType());
        }
    }

    public void prepare(Level param0, Camera param1, Entity param2) {
        this.level = param0;
        this.camera = param1;
        this.cameraOrientation = param1.rotation();
        this.crosshairPickEntity = param2;
    }

    public void overrideCameraOrientation(Quaternionf param0) {
        this.cameraOrientation = param0;
    }

    public void setRenderShadow(boolean param0) {
        this.shouldRenderShadow = param0;
    }

    public void setRenderHitBoxes(boolean param0) {
        this.renderHitBoxes = param0;
    }

    public boolean shouldRenderHitBoxes() {
        return this.renderHitBoxes;
    }

    public <E extends Entity> boolean shouldRender(E param0, Frustum param1, double param2, double param3, double param4) {
        EntityRenderer<? super E> var0 = this.getRenderer(param0);
        return var0.shouldRender(param0, param1, param2, param3, param4);
    }

    public <E extends Entity> void render(
        E param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7, int param8
    ) {
        EntityRenderer<? super E> var0 = this.getRenderer(param0);

        try {
            Vec3 var1 = var0.getRenderOffset(param0, param5);
            double var2 = param1 + var1.x();
            double var3 = param2 + var1.y();
            double var4 = param3 + var1.z();
            param6.pushPose();
            param6.translate(var2, var3, var4);
            var0.render(param0, param4, param5, param6, param7, param8);
            if (param0.displayFireAnimation()) {
                this.renderFlame(param6, param7, param0, Mth.rotationAroundAxis(Mth.Y_AXIS, this.cameraOrientation, new Quaternionf()));
            }

            param6.translate(-var1.x(), -var1.y(), -var1.z());
            if (this.options.entityShadows().get() && this.shouldRenderShadow && var0.shadowRadius > 0.0F && !param0.isInvisible()) {
                double var5 = this.distanceToSqr(param0.getX(), param0.getY(), param0.getZ());
                float var6 = (float)((1.0 - var5 / 256.0) * (double)var0.shadowStrength);
                if (var6 > 0.0F) {
                    renderShadow(param6, param7, param0, var6, param5, this.level, Math.min(var0.shadowRadius, 32.0F));
                }
            }

            if (this.renderHitBoxes && !param0.isInvisible() && !Minecraft.getInstance().showOnlyReducedInfo()) {
                renderHitbox(param6, param7.getBuffer(RenderType.lines()), param0, param5);
            }

            param6.popPose();
        } catch (Throwable var24) {
            CrashReport var8 = CrashReport.forThrowable(var24, "Rendering entity in world");
            CrashReportCategory var9 = var8.addCategory("Entity being rendered");
            param0.fillCrashReportCategory(var9);
            CrashReportCategory var10 = var8.addCategory("Renderer details");
            var10.setDetail("Assigned renderer", var0);
            var10.setDetail("Location", CrashReportCategory.formatLocation(this.level, param1, param2, param3));
            var10.setDetail("Rotation", param4);
            var10.setDetail("Delta", param5);
            throw new ReportedException(var8);
        }
    }

    private static void renderHitbox(PoseStack param0, VertexConsumer param1, Entity param2, float param3) {
        AABB var0 = param2.getBoundingBox().move(-param2.getX(), -param2.getY(), -param2.getZ());
        LevelRenderer.renderLineBox(param0, param1, var0, 1.0F, 1.0F, 1.0F, 1.0F);
        if (param2 instanceof EnderDragon) {
            double var1 = -Mth.lerp((double)param3, param2.xOld, param2.getX());
            double var2 = -Mth.lerp((double)param3, param2.yOld, param2.getY());
            double var3 = -Mth.lerp((double)param3, param2.zOld, param2.getZ());

            for(EnderDragonPart var4 : ((EnderDragon)param2).getSubEntities()) {
                param0.pushPose();
                double var5 = var1 + Mth.lerp((double)param3, var4.xOld, var4.getX());
                double var6 = var2 + Mth.lerp((double)param3, var4.yOld, var4.getY());
                double var7 = var3 + Mth.lerp((double)param3, var4.zOld, var4.getZ());
                param0.translate(var5, var6, var7);
                LevelRenderer.renderLineBox(param0, param1, var4.getBoundingBox().move(-var4.getX(), -var4.getY(), -var4.getZ()), 0.25F, 1.0F, 0.0F, 1.0F);
                param0.popPose();
            }
        }

        if (param2 instanceof LivingEntity) {
            float var8 = 0.01F;
            LevelRenderer.renderLineBox(
                param0,
                param1,
                var0.minX,
                (double)(param2.getEyeHeight() - 0.01F),
                var0.minZ,
                var0.maxX,
                (double)(param2.getEyeHeight() + 0.01F),
                var0.maxZ,
                1.0F,
                0.0F,
                0.0F,
                1.0F
            );
        }

        Entity var9 = param2.getVehicle();
        if (var9 != null) {
            float var10 = Math.min(var9.getBbWidth(), param2.getBbWidth()) / 2.0F;
            float var11 = 0.0625F;
            Vec3 var12 = var9.getPassengerRidingPosition(param2).subtract(param2.position());
            LevelRenderer.renderLineBox(
                param0,
                param1,
                var12.x - (double)var10,
                var12.y,
                var12.z - (double)var10,
                var12.x + (double)var10,
                var12.y + 0.0625,
                var12.z + (double)var10,
                1.0F,
                1.0F,
                0.0F,
                1.0F
            );
        }

        Vec3 var13 = param2.getViewVector(param3);
        Matrix4f var14 = param0.last().pose();
        Matrix3f var15 = param0.last().normal();
        param1.vertex(var14, 0.0F, param2.getEyeHeight(), 0.0F).color(0, 0, 255, 255).normal(var15, (float)var13.x, (float)var13.y, (float)var13.z).endVertex();
        param1.vertex(var14, (float)(var13.x * 2.0), (float)((double)param2.getEyeHeight() + var13.y * 2.0), (float)(var13.z * 2.0))
            .color(0, 0, 255, 255)
            .normal(var15, (float)var13.x, (float)var13.y, (float)var13.z)
            .endVertex();
    }

    private void renderFlame(PoseStack param0, MultiBufferSource param1, Entity param2, Quaternionf param3) {
        TextureAtlasSprite var0 = ModelBakery.FIRE_0.sprite();
        TextureAtlasSprite var1 = ModelBakery.FIRE_1.sprite();
        param0.pushPose();
        float var2 = param2.getBbWidth() * 1.4F;
        param0.scale(var2, var2, var2);
        float var3 = 0.5F;
        float var4 = 0.0F;
        float var5 = param2.getBbHeight() / var2;
        float var6 = 0.0F;
        param0.mulPose(param3);
        param0.translate(0.0F, 0.0F, -0.3F + (float)((int)var5) * 0.02F);
        float var7 = 0.0F;
        int var8 = 0;
        VertexConsumer var9 = param1.getBuffer(Sheets.cutoutBlockSheet());

        for(PoseStack.Pose var10 = param0.last(); var5 > 0.0F; ++var8) {
            TextureAtlasSprite var11 = var8 % 2 == 0 ? var0 : var1;
            float var12 = var11.getU0();
            float var13 = var11.getV0();
            float var14 = var11.getU1();
            float var15 = var11.getV1();
            if (var8 / 2 % 2 == 0) {
                float var16 = var14;
                var14 = var12;
                var12 = var16;
            }

            fireVertex(var10, var9, var3 - 0.0F, 0.0F - var6, var7, var14, var15);
            fireVertex(var10, var9, -var3 - 0.0F, 0.0F - var6, var7, var12, var15);
            fireVertex(var10, var9, -var3 - 0.0F, 1.4F - var6, var7, var12, var13);
            fireVertex(var10, var9, var3 - 0.0F, 1.4F - var6, var7, var14, var13);
            var5 -= 0.45F;
            var6 -= 0.45F;
            var3 *= 0.9F;
            var7 += 0.03F;
        }

        param0.popPose();
    }

    private static void fireVertex(PoseStack.Pose param0, VertexConsumer param1, float param2, float param3, float param4, float param5, float param6) {
        param1.vertex(param0.pose(), param2, param3, param4)
            .color(255, 255, 255, 255)
            .uv(param5, param6)
            .overlayCoords(0, 10)
            .uv2(240)
            .normal(param0.normal(), 0.0F, 1.0F, 0.0F)
            .endVertex();
    }

    private static void renderShadow(PoseStack param0, MultiBufferSource param1, Entity param2, float param3, float param4, LevelReader param5, float param6) {
        float var0 = param6;
        if (param2 instanceof Mob var1 && var1.isBaby()) {
            var0 = param6 * 0.5F;
        }

        double var2 = Mth.lerp((double)param4, param2.xOld, param2.getX());
        double var3 = Mth.lerp((double)param4, param2.yOld, param2.getY());
        double var4 = Mth.lerp((double)param4, param2.zOld, param2.getZ());
        float var5 = Math.min(param3 / 0.5F, var0);
        int var6 = Mth.floor(var2 - (double)var0);
        int var7 = Mth.floor(var2 + (double)var0);
        int var8 = Mth.floor(var3 - (double)var5);
        int var9 = Mth.floor(var3);
        int var10 = Mth.floor(var4 - (double)var0);
        int var11 = Mth.floor(var4 + (double)var0);
        PoseStack.Pose var12 = param0.last();
        VertexConsumer var13 = param1.getBuffer(SHADOW_RENDER_TYPE);
        BlockPos.MutableBlockPos var14 = new BlockPos.MutableBlockPos();

        for(int var15 = var10; var15 <= var11; ++var15) {
            for(int var16 = var6; var16 <= var7; ++var16) {
                var14.set(var16, 0, var15);
                ChunkAccess var17 = param5.getChunk(var14);

                for(int var18 = var8; var18 <= var9; ++var18) {
                    var14.setY(var18);
                    float var19 = param3 - (float)(var3 - (double)var14.getY()) * 0.5F;
                    renderBlockShadow(var12, var13, var17, param5, var14, var2, var3, var4, var0, var19);
                }
            }
        }

    }

    private static void renderBlockShadow(
        PoseStack.Pose param0,
        VertexConsumer param1,
        ChunkAccess param2,
        LevelReader param3,
        BlockPos param4,
        double param5,
        double param6,
        double param7,
        float param8,
        float param9
    ) {
        BlockPos var0 = param4.below();
        BlockState var1 = param2.getBlockState(var0);
        if (var1.getRenderShape() != RenderShape.INVISIBLE && param3.getMaxLocalRawBrightness(param4) > 3) {
            if (var1.isCollisionShapeFullBlock(param2, var0)) {
                VoxelShape var2 = var1.getShape(param2, var0);
                if (!var2.isEmpty()) {
                    float var3 = LightTexture.getBrightness(param3.dimensionType(), param3.getMaxLocalRawBrightness(param4));
                    float var4 = param9 * 0.5F * var3;
                    if (var4 >= 0.0F) {
                        if (var4 > 1.0F) {
                            var4 = 1.0F;
                        }

                        AABB var5 = var2.bounds();
                        double var6 = (double)param4.getX() + var5.minX;
                        double var7 = (double)param4.getX() + var5.maxX;
                        double var8 = (double)param4.getY() + var5.minY;
                        double var9 = (double)param4.getZ() + var5.minZ;
                        double var10 = (double)param4.getZ() + var5.maxZ;
                        float var11 = (float)(var6 - param5);
                        float var12 = (float)(var7 - param5);
                        float var13 = (float)(var8 - param6);
                        float var14 = (float)(var9 - param7);
                        float var15 = (float)(var10 - param7);
                        float var16 = -var11 / 2.0F / param8 + 0.5F;
                        float var17 = -var12 / 2.0F / param8 + 0.5F;
                        float var18 = -var14 / 2.0F / param8 + 0.5F;
                        float var19 = -var15 / 2.0F / param8 + 0.5F;
                        shadowVertex(param0, param1, var4, var11, var13, var14, var16, var18);
                        shadowVertex(param0, param1, var4, var11, var13, var15, var16, var19);
                        shadowVertex(param0, param1, var4, var12, var13, var15, var17, var19);
                        shadowVertex(param0, param1, var4, var12, var13, var14, var17, var18);
                    }

                }
            }
        }
    }

    private static void shadowVertex(
        PoseStack.Pose param0, VertexConsumer param1, float param2, float param3, float param4, float param5, float param6, float param7
    ) {
        Vector3f var0 = param0.pose().transformPosition(param3, param4, param5, new Vector3f());
        param1.vertex(var0.x(), var0.y(), var0.z(), 1.0F, 1.0F, 1.0F, param2, param6, param7, OverlayTexture.NO_OVERLAY, 15728880, 0.0F, 1.0F, 0.0F);
    }

    public void setLevel(@Nullable Level param0) {
        this.level = param0;
        if (param0 == null) {
            this.camera = null;
        }

    }

    public double distanceToSqr(Entity param0) {
        return this.camera.getPosition().distanceToSqr(param0.position());
    }

    public double distanceToSqr(double param0, double param1, double param2) {
        return this.camera.getPosition().distanceToSqr(param0, param1, param2);
    }

    public Quaternionf cameraOrientation() {
        return this.cameraOrientation;
    }

    public ItemInHandRenderer getItemInHandRenderer() {
        return this.itemInHandRenderer;
    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        EntityRendererProvider.Context var0 = new EntityRendererProvider.Context(
            this, this.itemRenderer, this.blockRenderDispatcher, this.itemInHandRenderer, param0, this.entityModels, this.font
        );
        this.renderers = EntityRenderers.createEntityRenderers(var0);
        this.playerRenderers = EntityRenderers.createPlayerRenderers(var0);
    }
}
