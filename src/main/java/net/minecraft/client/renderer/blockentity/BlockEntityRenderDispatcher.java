package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockEntityRenderDispatcher {
    private final Map<Class<? extends BlockEntity>, BlockEntityRenderer<? extends BlockEntity>> renderers = Maps.newHashMap();
    public static final BlockEntityRenderDispatcher instance = new BlockEntityRenderDispatcher();
    private Font font;
    public static double xOff;
    public static double yOff;
    public static double zOff;
    public TextureManager textureManager;
    public Level level;
    public Camera camera;
    public HitResult cameraHitResult;

    private BlockEntityRenderDispatcher() {
        this.renderers.put(SignBlockEntity.class, new SignRenderer());
        this.renderers.put(SpawnerBlockEntity.class, new SpawnerRenderer());
        this.renderers.put(PistonMovingBlockEntity.class, new PistonHeadRenderer());
        this.renderers.put(ChestBlockEntity.class, new ChestRenderer());
        this.renderers.put(EnderChestBlockEntity.class, new ChestRenderer());
        this.renderers.put(EnchantmentTableBlockEntity.class, new EnchantTableRenderer());
        this.renderers.put(LecternBlockEntity.class, new LecternRenderer());
        this.renderers.put(TheEndPortalBlockEntity.class, new TheEndPortalRenderer());
        this.renderers.put(TheEndGatewayBlockEntity.class, new TheEndGatewayRenderer());
        this.renderers.put(BeaconBlockEntity.class, new BeaconRenderer());
        this.renderers.put(SkullBlockEntity.class, new SkullBlockRenderer());
        this.renderers.put(BannerBlockEntity.class, new BannerRenderer());
        this.renderers.put(StructureBlockEntity.class, new StructureBlockRenderer());
        this.renderers.put(ShulkerBoxBlockEntity.class, new ShulkerBoxRenderer(new ShulkerModel()));
        this.renderers.put(BedBlockEntity.class, new BedRenderer());
        this.renderers.put(ConduitBlockEntity.class, new ConduitRenderer());
        this.renderers.put(BellBlockEntity.class, new BellRenderer());
        this.renderers.put(CampfireBlockEntity.class, new CampfireRenderer());

        for(BlockEntityRenderer<?> var0 : this.renderers.values()) {
            var0.init(this);
        }

    }

    public <T extends BlockEntity> BlockEntityRenderer<T> getRenderer(Class<? extends BlockEntity> param0) {
        BlockEntityRenderer<? extends BlockEntity> var0 = this.renderers.get(param0);
        if (var0 == null && param0 != BlockEntity.class) {
            var0 = this.getRenderer((Class<? extends BlockEntity>)param0.getSuperclass());
            this.renderers.put(param0, var0);
        }

        return var0;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityRenderer<T> getRenderer(@Nullable BlockEntity param0) {
        return param0 == null ? null : this.getRenderer(param0.getClass());
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

    public void render(BlockEntity param0, float param1, int param2) {
        if (param0.distanceToSqr(this.camera.getPosition().x, this.camera.getPosition().y, this.camera.getPosition().z) < param0.getViewDistance()) {
            Lighting.turnOn();
            int var0 = this.level.getLightColor(param0.getBlockPos());
            int var1 = var0 % 65536;
            int var2 = var0 / 65536;
            RenderSystem.glMultiTexCoord2f(33985, (float)var1, (float)var2);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos var3 = param0.getBlockPos();
            this.render(param0, (double)var3.getX() - xOff, (double)var3.getY() - yOff, (double)var3.getZ() - zOff, param1, param2, false);
        }

    }

    public void render(BlockEntity param0, double param1, double param2, double param3, float param4) {
        this.render(param0, param1, param2, param3, param4, -1, false);
    }

    public void renderItem(BlockEntity param0) {
        this.render(param0, 0.0, 0.0, 0.0, 0.0F, -1, true);
    }

    public void render(BlockEntity param0, double param1, double param2, double param3, float param4, int param5, boolean param6) {
        BlockEntityRenderer<BlockEntity> var0 = this.getRenderer(param0);
        if (var0 != null) {
            try {
                if (param6 || param0.hasLevel() && param0.getType().isValid(param0.getBlockState().getBlock())) {
                    var0.render(param0, param1, param2, param3, param4, param5);
                }
            } catch (Throwable var15) {
                CrashReport var2 = CrashReport.forThrowable(var15, "Rendering Block Entity");
                CrashReportCategory var3 = var2.addCategory("Block Entity Details");
                param0.fillCrashReportCategory(var3);
                throw new ReportedException(var2);
            }
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
