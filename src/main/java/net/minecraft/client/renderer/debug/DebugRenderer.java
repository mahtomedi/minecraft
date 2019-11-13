package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DebugRenderer {
    public final PathfindingRenderer pathfindingRenderer = new PathfindingRenderer();
    public final DebugRenderer.SimpleDebugRenderer waterDebugRenderer;
    public final DebugRenderer.SimpleDebugRenderer chunkBorderRenderer;
    public final DebugRenderer.SimpleDebugRenderer heightMapRenderer;
    public final DebugRenderer.SimpleDebugRenderer collisionBoxRenderer;
    public final DebugRenderer.SimpleDebugRenderer neighborsUpdateRenderer;
    public final CaveDebugRenderer caveRenderer;
    public final StructureRenderer structureRenderer;
    public final DebugRenderer.SimpleDebugRenderer lightDebugRenderer;
    public final DebugRenderer.SimpleDebugRenderer worldGenAttemptRenderer;
    public final DebugRenderer.SimpleDebugRenderer solidFaceRenderer;
    public final DebugRenderer.SimpleDebugRenderer chunkRenderer;
    public final VillageDebugRenderer villageDebugRenderer;
    public final BeeDebugRenderer beeDebugRenderer;
    public final RaidDebugRenderer raidDebugRenderer;
    public final GoalSelectorDebugRenderer goalSelectorRenderer;
    public final GameTestDebugRenderer gameTestDebugRenderer;
    private boolean renderChunkborder;

    public DebugRenderer(Minecraft param0) {
        this.waterDebugRenderer = new WaterDebugRenderer(param0);
        this.chunkBorderRenderer = new ChunkBorderRenderer(param0);
        this.heightMapRenderer = new HeightMapRenderer(param0);
        this.collisionBoxRenderer = new CollisionBoxRenderer(param0);
        this.neighborsUpdateRenderer = new NeighborsUpdateRenderer(param0);
        this.caveRenderer = new CaveDebugRenderer();
        this.structureRenderer = new StructureRenderer(param0);
        this.lightDebugRenderer = new LightDebugRenderer(param0);
        this.worldGenAttemptRenderer = new WorldGenAttemptRenderer();
        this.solidFaceRenderer = new SolidFaceRenderer(param0);
        this.chunkRenderer = new ChunkDebugRenderer(param0);
        this.villageDebugRenderer = new VillageDebugRenderer(param0);
        this.beeDebugRenderer = new BeeDebugRenderer(param0);
        this.raidDebugRenderer = new RaidDebugRenderer(param0);
        this.goalSelectorRenderer = new GoalSelectorDebugRenderer(param0);
        this.gameTestDebugRenderer = new GameTestDebugRenderer();
    }

    public void clear() {
        this.pathfindingRenderer.clear();
        this.waterDebugRenderer.clear();
        this.chunkBorderRenderer.clear();
        this.heightMapRenderer.clear();
        this.collisionBoxRenderer.clear();
        this.neighborsUpdateRenderer.clear();
        this.caveRenderer.clear();
        this.structureRenderer.clear();
        this.lightDebugRenderer.clear();
        this.worldGenAttemptRenderer.clear();
        this.solidFaceRenderer.clear();
        this.chunkRenderer.clear();
        this.villageDebugRenderer.clear();
        this.beeDebugRenderer.clear();
        this.raidDebugRenderer.clear();
        this.goalSelectorRenderer.clear();
        this.gameTestDebugRenderer.clear();
    }

    public boolean switchRenderChunkborder() {
        this.renderChunkborder = !this.renderChunkborder;
        return this.renderChunkborder;
    }

    public void render(PoseStack param0, MultiBufferSource.BufferSource param1, double param2, double param3, double param4, long param5) {
        if (this.renderChunkborder && !Minecraft.getInstance().showOnlyReducedInfo()) {
            this.chunkBorderRenderer.render(param0, param1, param2, param3, param4, param5);
        }

        this.gameTestDebugRenderer.render(param0, param1, param2, param3, param4, param5);
    }

    public static Optional<Entity> getTargetedEntity(@Nullable Entity param0, int param1) {
        if (param0 == null) {
            return Optional.empty();
        } else {
            Vec3 var0 = param0.getEyePosition(1.0F);
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

    public static void renderFilledBox(BlockPos param0, BlockPos param1, float param2, float param3, float param4, float param5) {
        Camera var0 = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (var0.isInitialized()) {
            Vec3 var1 = var0.getPosition().reverse();
            AABB var2 = new AABB(param0, param1).move(var1);
            renderFilledBox(var2, param2, param3, param4, param5);
        }
    }

    public static void renderFilledBox(BlockPos param0, float param1, float param2, float param3, float param4, float param5) {
        Camera var0 = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (var0.isInitialized()) {
            Vec3 var1 = var0.getPosition().reverse();
            AABB var2 = new AABB(param0).move(var1).inflate((double)param1);
            renderFilledBox(var2, param2, param3, param4, param5);
        }
    }

    public static void renderFilledBox(AABB param0, float param1, float param2, float param3, float param4) {
        renderFilledBox(param0.minX, param0.minY, param0.minZ, param0.maxX, param0.maxY, param0.maxZ, param1, param2, param3, param4);
    }

    public static void renderFilledBox(
        double param0, double param1, double param2, double param3, double param4, double param5, float param6, float param7, float param8, float param9
    ) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        var1.begin(5, DefaultVertexFormat.POSITION_COLOR);
        LevelRenderer.addChainedFilledBoxVertices(var1, param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
        var0.end();
    }

    public static void renderFloatingText(String param0, int param1, int param2, int param3, int param4) {
        renderFloatingText(param0, (double)param1 + 0.5, (double)param2 + 0.5, (double)param3 + 0.5, param4);
    }

    public static void renderFloatingText(String param0, double param1, double param2, double param3, int param4) {
        renderFloatingText(param0, param1, param2, param3, param4, 0.02F);
    }

    public static void renderFloatingText(String param0, double param1, double param2, double param3, int param4, float param5) {
        renderFloatingText(param0, param1, param2, param3, param4, param5, true, 0.0F, false);
    }

    public static void renderFloatingText(
        String param0, double param1, double param2, double param3, int param4, float param5, boolean param6, float param7, boolean param8
    ) {
        Minecraft var0 = Minecraft.getInstance();
        Camera var1 = var0.gameRenderer.getMainCamera();
        if (var1.isInitialized() && var0.getEntityRenderDispatcher().options != null) {
            Font var2 = var0.font;
            double var3 = var1.getPosition().x;
            double var4 = var1.getPosition().y;
            double var5 = var1.getPosition().z;
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float)(param1 - var3), (float)(param2 - var4) + 0.07F, (float)(param3 - var5));
            RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
            RenderSystem.scalef(param5, -param5, param5);
            RenderSystem.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
            RenderSystem.multMatrix(new Matrix4f(var1.rotation()));
            RenderSystem.enableTexture();
            if (param8) {
                RenderSystem.disableDepthTest();
            } else {
                RenderSystem.enableDepthTest();
            }

            RenderSystem.depthMask(true);
            RenderSystem.scalef(-1.0F, 1.0F, 1.0F);
            float var6 = param6 ? (float)(-var2.width(param0)) / 2.0F : 0.0F;
            var6 -= param7 / param5;
            var2.draw(param0, var6, 0.0F, param4);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableDepthTest();
            RenderSystem.popMatrix();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface SimpleDebugRenderer {
        void render(PoseStack var1, MultiBufferSource var2, double var3, double var5, double var7, long var9);

        default void clear() {
        }
    }
}
