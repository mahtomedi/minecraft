package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Calendar;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChestRenderer<T extends BlockEntity & LidBlockEntity> extends BatchedBlockEntityRenderer<T> {
    public static final ResourceLocation CHEST_LARGE_TRAP_LOCATION = new ResourceLocation("entity/chest/trapped_double");
    public static final ResourceLocation CHEST_LARGE_XMAS_LOCATION = new ResourceLocation("entity/chest/christmas_double");
    public static final ResourceLocation CHEST_LARGE_LOCATION = new ResourceLocation("entity/chest/normal_double");
    public static final ResourceLocation CHEST_TRAP_LOCATION = new ResourceLocation("entity/chest/trapped");
    public static final ResourceLocation CHEST_XMAS_LOCATION = new ResourceLocation("entity/chest/christmas");
    public static final ResourceLocation CHEST_LOCATION = new ResourceLocation("entity/chest/normal");
    public static final ResourceLocation ENDER_CHEST_LOCATION = new ResourceLocation("entity/chest/ender");
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final ModelPart doubleLid;
    private final ModelPart doubleBottom;
    private final ModelPart doubleLock;
    private boolean xmasTextures;

    public ChestRenderer() {
        Calendar var0 = Calendar.getInstance();
        if (var0.get(2) + 1 == 12 && var0.get(5) >= 24 && var0.get(5) <= 26) {
            this.xmasTextures = true;
        }

        this.bottom = new ModelPart(64, 64, 0, 19);
        this.bottom.addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F, 0.0F);
        this.lid = new ModelPart(64, 64, 0, 0);
        this.lid.addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F, 0.0F);
        this.lid.y = 9.0F;
        this.lid.z = 1.0F;
        this.lock = new ModelPart(64, 64, 0, 0);
        this.lock.addBox(7.0F, -2.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
        this.lock.y = 9.0F;
        this.doubleBottom = new ModelPart(128, 64, 0, 19);
        this.doubleBottom.addBox(1.0F, 0.0F, 1.0F, 30.0F, 10.0F, 14.0F, 0.0F);
        this.doubleLid = new ModelPart(128, 64, 0, 0);
        this.doubleLid.addBox(1.0F, 0.0F, 0.0F, 30.0F, 5.0F, 14.0F, 0.0F);
        this.doubleLid.y = 9.0F;
        this.doubleLid.z = 1.0F;
        this.doubleLock = new ModelPart(128, 64, 0, 0);
        this.doubleLock.addBox(15.0F, -2.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
        this.doubleLock.y = 9.0F;
    }

    @Override
    protected void renderToBuffer(
        T param0, double param1, double param2, double param3, float param4, int param5, RenderType param6, BufferBuilder param7, int param8, int param9
    ) {
        BlockState var0 = param0.hasLevel() ? param0.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        ChestType var1 = var0.hasProperty(ChestBlock.TYPE) ? var0.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        if (var1 != ChestType.LEFT) {
            boolean var2 = var1 != ChestType.SINGLE;
            ResourceLocation var3;
            if (param5 >= 0) {
                var3 = ModelBakery.DESTROY_STAGES.get(param5);
            } else if (this.xmasTextures) {
                var3 = var2 ? CHEST_LARGE_XMAS_LOCATION : CHEST_XMAS_LOCATION;
            } else if (param0 instanceof TrappedChestBlockEntity) {
                var3 = var2 ? CHEST_LARGE_TRAP_LOCATION : CHEST_TRAP_LOCATION;
            } else if (param0 instanceof EnderChestBlockEntity) {
                var3 = ENDER_CHEST_LOCATION;
            } else {
                var3 = var2 ? CHEST_LARGE_LOCATION : CHEST_LOCATION;
            }

            param7.pushPose();
            float var8 = var0.getValue(ChestBlock.FACING).toYRot();
            param7.translate(0.5, 0.5, 0.5);
            param7.multiplyPose(new Quaternion(Vector3f.YP, -var8, true));
            param7.translate(-0.5, -0.5, -0.5);
            float var9 = param0.getOpenNess(param4);
            var9 = 1.0F - var9;
            var9 = 1.0F - var9 * var9 * var9;
            TextureAtlasSprite var10 = this.getSprite(var3);
            if (var2) {
                this.render(param7, this.doubleLid, this.doubleLock, this.doubleBottom, var9, param8, param9, var10);
            } else {
                this.render(param7, this.lid, this.lock, this.bottom, var9, param8, param9, var10);
            }

            param7.popPose();
        }
    }

    private void render(
        BufferBuilder param0, ModelPart param1, ModelPart param2, ModelPart param3, float param4, int param5, int param6, TextureAtlasSprite param7
    ) {
        param1.xRot = -(param4 * (float) (Math.PI / 2));
        param2.xRot = param1.xRot;
        param1.render(param0, 0.0625F, param5, param6, param7);
        param2.render(param0, 0.0625F, param5, param6, param7);
        param3.render(param0, 0.0625F, param5, param6, param7);
    }
}
