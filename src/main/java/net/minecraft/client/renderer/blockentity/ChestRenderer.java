package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.Calendar;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
public class ChestRenderer<T extends BlockEntity & LidBlockEntity> extends BlockEntityRenderer<T> {
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

    public ChestRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
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
    public void render(T param0, double param1, double param2, double param3, float param4, PoseStack param5, MultiBufferSource param6, int param7, int param8) {
        BlockState var0 = param0.hasLevel() ? param0.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        ChestType var1 = var0.hasProperty(ChestBlock.TYPE) ? var0.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        boolean var2 = var1 != ChestType.SINGLE;
        ResourceLocation var3;
        if (this.xmasTextures) {
            var3 = var2 ? CHEST_LARGE_XMAS_LOCATION : CHEST_XMAS_LOCATION;
        } else if (param0 instanceof TrappedChestBlockEntity) {
            var3 = var2 ? CHEST_LARGE_TRAP_LOCATION : CHEST_TRAP_LOCATION;
        } else if (param0 instanceof EnderChestBlockEntity) {
            var3 = ENDER_CHEST_LOCATION;
        } else {
            var3 = var2 ? CHEST_LARGE_LOCATION : CHEST_LOCATION;
        }

        param5.pushPose();
        float var7 = var0.getValue(ChestBlock.FACING).toYRot();
        param5.translate(0.5, 0.5, 0.5);
        param5.mulPose(Vector3f.YP.rotationDegrees(-var7));
        param5.translate(-0.5, -0.5, -0.5);
        float var8 = param0.getOpenNess(param4);
        var8 = 1.0F - var8;
        var8 = 1.0F - var8 * var8 * var8;
        VertexConsumer var9 = param6.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        TextureAtlasSprite var10 = this.getSprite(var3);
        if (var2) {
            if (var1 == ChestType.LEFT) {
                param5.translate(-1.0, 0.0, 0.0);
            }

            this.render(param5, var9, this.doubleLid, this.doubleLock, this.doubleBottom, var8, param7, param8, var10);
        } else {
            this.render(param5, var9, this.lid, this.lock, this.bottom, var8, param7, param8, var10);
        }

        param5.popPose();
    }

    private void render(
        PoseStack param0,
        VertexConsumer param1,
        ModelPart param2,
        ModelPart param3,
        ModelPart param4,
        float param5,
        int param6,
        int param7,
        TextureAtlasSprite param8
    ) {
        param2.xRot = -(param5 * (float) (Math.PI / 2));
        param3.xRot = param2.xRot;
        param2.render(param0, param1, 0.0625F, param6, param7, param8);
        param3.render(param0, param1, 0.0625F, param6, param7, param8);
        param4.render(param0, param1, 0.0625F, param6, param7, param8);
    }
}
