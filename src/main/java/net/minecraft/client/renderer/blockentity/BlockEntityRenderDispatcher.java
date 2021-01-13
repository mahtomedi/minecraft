package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockEntityRenderDispatcher {
    private final Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = Maps.newHashMap();
    public static final BlockEntityRenderDispatcher instance = new BlockEntityRenderDispatcher();
    private final BufferBuilder singleRenderBuffer = new BufferBuilder(256);
    private Font font;
    public TextureManager textureManager;
    public Level level;
    public Camera camera;
    public HitResult cameraHitResult;

    private BlockEntityRenderDispatcher() {
        this.register(BlockEntityType.SIGN, new SignRenderer(this));
        this.register(BlockEntityType.MOB_SPAWNER, new SpawnerRenderer(this));
        this.register(BlockEntityType.PISTON, new PistonHeadRenderer(this));
        this.register(BlockEntityType.CHEST, new ChestRenderer<>(this));
        this.register(BlockEntityType.ENDER_CHEST, new ChestRenderer<>(this));
        this.register(BlockEntityType.TRAPPED_CHEST, new ChestRenderer<>(this));
        this.register(BlockEntityType.ENCHANTING_TABLE, new EnchantTableRenderer(this));
        this.register(BlockEntityType.LECTERN, new LecternRenderer(this));
        this.register(BlockEntityType.END_PORTAL, new TheEndPortalRenderer<>(this));
        this.register(BlockEntityType.END_GATEWAY, new TheEndGatewayRenderer(this));
        this.register(BlockEntityType.BEACON, new BeaconRenderer(this));
        this.register(BlockEntityType.SKULL, new SkullBlockRenderer(this));
        this.register(BlockEntityType.BANNER, new BannerRenderer(this));
        this.register(BlockEntityType.STRUCTURE_BLOCK, new StructureBlockRenderer(this));
        this.register(BlockEntityType.SHULKER_BOX, new ShulkerBoxRenderer(new ShulkerModel(), this));
        this.register(BlockEntityType.BED, new BedRenderer(this));
        this.register(BlockEntityType.CONDUIT, new ConduitRenderer(this));
        this.register(BlockEntityType.BELL, new BellRenderer(this));
        this.register(BlockEntityType.CAMPFIRE, new CampfireRenderer(this));
    }

    private <E extends BlockEntity> void register(BlockEntityType<E> param0, BlockEntityRenderer<E> param1) {
        this.renderers.put(param0, param1);
    }

    @Nullable
    public <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E param0) {
        return (BlockEntityRenderer<E>)this.renderers.get(param0.getType());
    }

    public void prepare(Level param0, TextureManager param1, Font param2, Camera param3, HitResult param4) {
        if (this.level != param0) {
            this.setLevel(param0);
        }

        this.textureManager = param1;
        this.camera = param3;
        this.font = param2;
        this.cameraHitResult = param4;
    }

    public <E extends BlockEntity> void render(E param0, float param1, PoseStack param2, MultiBufferSource param3) {
        if (Vec3.atCenterOf(param0.getBlockPos()).closerThan(this.camera.getPosition(), param0.getViewDistance())) {
            BlockEntityRenderer<E> var0 = this.getRenderer(param0);
            if (var0 != null) {
                if (param0.hasLevel() && param0.getType().isValid(param0.getBlockState().getBlock())) {
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

    public Font getFont() {
        return this.font;
    }
}
