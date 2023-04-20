package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class DebugRenderer {
    public final PathfindingRenderer pathfindingRenderer = new PathfindingRenderer();
    public final DebugRenderer.SimpleDebugRenderer waterDebugRenderer;
    public final DebugRenderer.SimpleDebugRenderer chunkBorderRenderer;
    public final DebugRenderer.SimpleDebugRenderer heightMapRenderer;
    public final DebugRenderer.SimpleDebugRenderer collisionBoxRenderer;
    public final DebugRenderer.SimpleDebugRenderer neighborsUpdateRenderer;
    public final StructureRenderer structureRenderer;
    public final DebugRenderer.SimpleDebugRenderer lightDebugRenderer;
    public final DebugRenderer.SimpleDebugRenderer worldGenAttemptRenderer;
    public final DebugRenderer.SimpleDebugRenderer solidFaceRenderer;
    public final DebugRenderer.SimpleDebugRenderer chunkRenderer;
    public final BrainDebugRenderer brainDebugRenderer;
    public final VillageSectionsDebugRenderer villageSectionsDebugRenderer;
    public final BeeDebugRenderer beeDebugRenderer;
    public final RaidDebugRenderer raidDebugRenderer;
    public final GoalSelectorDebugRenderer goalSelectorRenderer;
    public final GameTestDebugRenderer gameTestDebugRenderer;
    public final GameEventListenerRenderer gameEventListenerRenderer;
    public final LightSectionDebugRenderer skyLightSectionDebugRenderer;
    private boolean renderChunkborder;

    public DebugRenderer(Minecraft param0) {
        this.waterDebugRenderer = new WaterDebugRenderer(param0);
        this.chunkBorderRenderer = new ChunkBorderRenderer(param0);
        this.heightMapRenderer = new HeightMapRenderer(param0);
        this.collisionBoxRenderer = new CollisionBoxRenderer(param0);
        this.neighborsUpdateRenderer = new NeighborsUpdateRenderer(param0);
        this.structureRenderer = new StructureRenderer(param0);
        this.lightDebugRenderer = new LightDebugRenderer(param0);
        this.worldGenAttemptRenderer = new WorldGenAttemptRenderer();
        this.solidFaceRenderer = new SolidFaceRenderer(param0);
        this.chunkRenderer = new ChunkDebugRenderer(param0);
        this.brainDebugRenderer = new BrainDebugRenderer(param0);
        this.villageSectionsDebugRenderer = new VillageSectionsDebugRenderer();
        this.beeDebugRenderer = new BeeDebugRenderer(param0);
        this.raidDebugRenderer = new RaidDebugRenderer(param0);
        this.goalSelectorRenderer = new GoalSelectorDebugRenderer(param0);
        this.gameTestDebugRenderer = new GameTestDebugRenderer();
        this.gameEventListenerRenderer = new GameEventListenerRenderer(param0);
        this.skyLightSectionDebugRenderer = new LightSectionDebugRenderer(param0, LightLayer.SKY);
    }

    public void clear() {
        this.pathfindingRenderer.clear();
        this.waterDebugRenderer.clear();
        this.chunkBorderRenderer.clear();
        this.heightMapRenderer.clear();
        this.collisionBoxRenderer.clear();
        this.neighborsUpdateRenderer.clear();
        this.structureRenderer.clear();
        this.lightDebugRenderer.clear();
        this.worldGenAttemptRenderer.clear();
        this.solidFaceRenderer.clear();
        this.chunkRenderer.clear();
        this.brainDebugRenderer.clear();
        this.villageSectionsDebugRenderer.clear();
        this.beeDebugRenderer.clear();
        this.raidDebugRenderer.clear();
        this.goalSelectorRenderer.clear();
        this.gameTestDebugRenderer.clear();
        this.gameEventListenerRenderer.clear();
        this.skyLightSectionDebugRenderer.clear();
    }

    public boolean switchRenderChunkborder() {
        this.renderChunkborder = !this.renderChunkborder;
        return this.renderChunkborder;
    }

    public void render(PoseStack param0, MultiBufferSource.BufferSource param1, double param2, double param3, double param4) {
        if (this.renderChunkborder && !Minecraft.getInstance().showOnlyReducedInfo()) {
            this.chunkBorderRenderer.render(param0, param1, param2, param3, param4);
        }

        this.gameTestDebugRenderer.render(param0, param1, param2, param3, param4);
    }

    public static Optional<Entity> getTargetedEntity(@Nullable Entity param0, int param1) {
        if (param0 == null) {
            return Optional.empty();
        } else {
            Vec3 var0 = param0.getEyePosition();
            Vec3 var1 = param0.getViewVector(1.0F).scale((double)param1);
            Vec3 var2 = var0.add(var1);
            AABB var3 = param0.getBoundingBox().expandTowards(var1).inflate(1.0);
            int var4 = param1 * param1;
            Predicate<Entity> var5 = param0x -> !param0x.isSpectator() && param0x.isPickable();
            EntityHitResult var6 = ProjectileUtil.getEntityHitResult(param0, var0, var2, var3, var5, (double)var4);
            if (var6 == null) {
                return Optional.empty();
            } else {
                return var0.distanceToSqr(var6.getLocation()) > (double)var4 ? Optional.empty() : Optional.of(var6.getEntity());
            }
        }
    }

    public static void renderFilledBox(
        PoseStack param0, MultiBufferSource param1, BlockPos param2, BlockPos param3, float param4, float param5, float param6, float param7
    ) {
        Camera var0 = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (var0.isInitialized()) {
            Vec3 var1 = var0.getPosition().reverse();
            AABB var2 = new AABB(param2, param3).move(var1);
            renderFilledBox(param0, param1, var2, param4, param5, param6, param7);
        }
    }

    public static void renderFilledBox(
        PoseStack param0, MultiBufferSource param1, BlockPos param2, float param3, float param4, float param5, float param6, float param7
    ) {
        Camera var0 = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (var0.isInitialized()) {
            Vec3 var1 = var0.getPosition().reverse();
            AABB var2 = new AABB(param2).move(var1).inflate((double)param3);
            renderFilledBox(param0, param1, var2, param4, param5, param6, param7);
        }
    }

    public static void renderFilledBox(PoseStack param0, MultiBufferSource param1, AABB param2, float param3, float param4, float param5, float param6) {
        renderFilledBox(param0, param1, param2.minX, param2.minY, param2.minZ, param2.maxX, param2.maxY, param2.maxZ, param3, param4, param5, param6);
    }

    public static void renderFilledBox(
        PoseStack param0,
        MultiBufferSource param1,
        double param2,
        double param3,
        double param4,
        double param5,
        double param6,
        double param7,
        float param8,
        float param9,
        float param10,
        float param11
    ) {
        VertexConsumer var0 = param1.getBuffer(RenderType.debugFilledBox());
        LevelRenderer.addChainedFilledBoxVertices(param0, var0, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11);
    }

    public static void renderFloatingText(PoseStack param0, MultiBufferSource param1, String param2, int param3, int param4, int param5, int param6) {
        renderFloatingText(param0, param1, param2, (double)param3 + 0.5, (double)param4 + 0.5, (double)param5 + 0.5, param6);
    }

    public static void renderFloatingText(PoseStack param0, MultiBufferSource param1, String param2, double param3, double param4, double param5, int param6) {
        renderFloatingText(param0, param1, param2, param3, param4, param5, param6, 0.02F);
    }

    public static void renderFloatingText(
        PoseStack param0, MultiBufferSource param1, String param2, double param3, double param4, double param5, int param6, float param7
    ) {
        renderFloatingText(param0, param1, param2, param3, param4, param5, param6, param7, true, 0.0F, false);
    }

    public static void renderFloatingText(
        PoseStack param0,
        MultiBufferSource param1,
        String param2,
        double param3,
        double param4,
        double param5,
        int param6,
        float param7,
        boolean param8,
        float param9,
        boolean param10
    ) {
        Minecraft var0 = Minecraft.getInstance();
        Camera var1 = var0.gameRenderer.getMainCamera();
        if (var1.isInitialized() && var0.getEntityRenderDispatcher().options != null) {
            Font var2 = var0.font;
            double var3 = var1.getPosition().x;
            double var4 = var1.getPosition().y;
            double var5 = var1.getPosition().z;
            param0.pushPose();
            param0.translate((float)(param3 - var3), (float)(param4 - var4) + 0.07F, (float)(param5 - var5));
            param0.mulPoseMatrix(new Matrix4f().rotation(var1.rotation()));
            param0.scale(-param7, -param7, param7);
            float var6 = param8 ? (float)(-var2.width(param2)) / 2.0F : 0.0F;
            var6 -= param9 / param7;
            var2.drawInBatch(
                param2, var6, 0.0F, param6, false, param0.last().pose(), param1, param10 ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, 15728880
            );
            param0.popPose();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface SimpleDebugRenderer {
        void render(PoseStack var1, MultiBufferSource var2, double var3, double var5, double var7);

        default void clear() {
        }
    }
}
