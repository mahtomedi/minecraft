package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockEntityRenderDispatcher implements ResourceManagerReloadListener {
    private Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = ImmutableMap.of();
    private final Font font;
    private final EntityModelSet entityModelSet;
    public Level level;
    public Camera camera;
    public HitResult cameraHitResult;
    private final Supplier<BlockRenderDispatcher> blockRenderDispatcher;

    public BlockEntityRenderDispatcher(Font param0, EntityModelSet param1, Supplier<BlockRenderDispatcher> param2) {
        this.font = param0;
        this.entityModelSet = param1;
        this.blockRenderDispatcher = param2;
    }

    @Nullable
    public <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E param0) {
        return (BlockEntityRenderer<E>)this.renderers.get(param0.getType());
    }

    public void prepare(Level param0, Camera param1, HitResult param2) {
        if (this.level != param0) {
            this.setLevel(param0);
        }

        this.camera = param1;
        this.cameraHitResult = param2;
    }

    public <E extends BlockEntity> void render(E param0, float param1, PoseStack param2, MultiBufferSource param3) {
        BlockEntityRenderer<E> var0 = this.getRenderer(param0);
        if (var0 != null) {
            if (param0.hasLevel() && param0.getType().isValid(param0.getBlockState())) {
                if (var0.shouldRender(param0, this.camera.getPosition())) {
                    tryRender(param0, () -> setupAndRender(var0, param0, param1, param2, param3));
                }
            }
        }
    }

    private static <T extends BlockEntity> void setupAndRender(
        BlockEntityRenderer<T> param0, T param1, float param2, PoseStack param3, MultiBufferSource param4
    ) {
        Level var0 = param1.getLevel();
        int var1;
        if (var0 != null) {
            var1 = LevelRenderer.getLightColor(var0, param1.getBlockPos());
        } else {
            var1 = 15728880;
        }

        param0.render(param1, param2, param3, param4, var1, OverlayTexture.NO_OVERLAY);
    }

    public <E extends BlockEntity> boolean renderItem(E param0, PoseStack param1, MultiBufferSource param2, int param3, int param4) {
        BlockEntityRenderer<E> var0 = this.getRenderer(param0);
        if (var0 == null) {
            return true;
        } else {
            tryRender(param0, () -> var0.render(param0, 0.0F, param1, param2, param3, param4));
            return false;
        }
    }

    private static void tryRender(BlockEntity param0, Runnable param1) {
        try {
            param1.run();
        } catch (Throwable var5) {
            CrashReport var1 = CrashReport.forThrowable(var5, "Rendering Block Entity");
            CrashReportCategory var2 = var1.addCategory("Block Entity Details");
            param0.fillCrashReportCategory(var2);
            throw new ReportedException(var1);
        }
    }

    public void setLevel(@Nullable Level param0) {
        this.level = param0;
        if (param0 == null) {
            this.camera = null;
        }

    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        BlockEntityRendererProvider.Context var0 = new BlockEntityRendererProvider.Context(
            this, this.blockRenderDispatcher.get(), this.entityModelSet, this.font
        );
        this.renderers = BlockEntityRenderers.createEntityRenderers(var0);
    }
}
