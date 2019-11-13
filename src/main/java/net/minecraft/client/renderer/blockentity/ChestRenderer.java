package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.Calendar;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
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
    public static final ResourceLocation CHEST_TRAP_LOCATION = new ResourceLocation("entity/chest/trapped");
    public static final ResourceLocation CHEST_TRAP_LOCATION_LEFT = new ResourceLocation("entity/chest/trapped_left");
    public static final ResourceLocation CHEST_TRAP_LOCATION_RIGHT = new ResourceLocation("entity/chest/trapped_right");
    public static final ResourceLocation CHEST_XMAS_LOCATION = new ResourceLocation("entity/chest/christmas");
    public static final ResourceLocation CHEST_XMAS_LOCATION_LEFT = new ResourceLocation("entity/chest/christmas_left");
    public static final ResourceLocation CHEST_XMAS_LOCATION_RIGHT = new ResourceLocation("entity/chest/christmas_right");
    public static final ResourceLocation CHEST_LOCATION = new ResourceLocation("entity/chest/normal");
    public static final ResourceLocation CHEST_LOCATION_LEFT = new ResourceLocation("entity/chest/normal_left");
    public static final ResourceLocation CHEST_LOCATION_RIGHT = new ResourceLocation("entity/chest/normal_right");
    public static final ResourceLocation ENDER_CHEST_LOCATION = new ResourceLocation("entity/chest/ender");
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final ModelPart doubleLeftLid;
    private final ModelPart doubleLeftBottom;
    private final ModelPart doubleLeftLock;
    private final ModelPart doubleRightLid;
    private final ModelPart doubleRightBottom;
    private final ModelPart doubleRightLock;
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
        this.lock.addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
        this.lock.y = 8.0F;
        this.doubleLeftBottom = new ModelPart(64, 64, 0, 19);
        this.doubleLeftBottom.addBox(1.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F, 0.0F);
        this.doubleLeftLid = new ModelPart(64, 64, 0, 0);
        this.doubleLeftLid.addBox(1.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F, 0.0F);
        this.doubleLeftLid.y = 9.0F;
        this.doubleLeftLid.z = 1.0F;
        this.doubleLeftLock = new ModelPart(64, 64, 0, 0);
        this.doubleLeftLock.addBox(15.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F, 0.0F);
        this.doubleLeftLock.y = 8.0F;
        this.doubleRightBottom = new ModelPart(64, 64, 0, 19);
        this.doubleRightBottom.addBox(0.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F, 0.0F);
        this.doubleRightLid = new ModelPart(64, 64, 0, 0);
        this.doubleRightLid.addBox(0.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F, 0.0F);
        this.doubleRightLid.y = 9.0F;
        this.doubleRightLid.z = 1.0F;
        this.doubleRightLock = new ModelPart(64, 64, 0, 0);
        this.doubleRightLock.addBox(0.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F, 0.0F);
        this.doubleRightLock.y = 8.0F;
    }

    @Override
    public void render(T param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        Level var0 = param0.getLevel();
        boolean var1 = var0 != null;
        BlockState var2 = var1 ? param0.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        ChestType var3 = var2.hasProperty(ChestBlock.TYPE) ? var2.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        boolean var4 = var3 != ChestType.SINGLE;
        ResourceLocation var5;
        if (this.xmasTextures) {
            var5 = this.chooseTexture(var3, CHEST_XMAS_LOCATION, CHEST_XMAS_LOCATION_LEFT, CHEST_XMAS_LOCATION_RIGHT);
        } else if (param0 instanceof TrappedChestBlockEntity) {
            var5 = this.chooseTexture(var3, CHEST_TRAP_LOCATION, CHEST_TRAP_LOCATION_LEFT, CHEST_TRAP_LOCATION_RIGHT);
        } else if (param0 instanceof EnderChestBlockEntity) {
            var5 = ENDER_CHEST_LOCATION;
        } else {
            var5 = this.chooseTexture(var3, CHEST_LOCATION, CHEST_LOCATION_LEFT, CHEST_LOCATION_RIGHT);
        }

        param2.pushPose();
        float var9 = var2.getValue(ChestBlock.FACING).toYRot();
        param2.translate(0.5, 0.5, 0.5);
        param2.mulPose(Vector3f.YP.rotationDegrees(-var9));
        param2.translate(-0.5, -0.5, -0.5);
        float var10;
        if (var1) {
            var10 = ChestBlock.getCombinedOpenness(param0, var2, var0, param0.getBlockPos(), param1);
        } else {
            var10 = param0.getOpenNess(param1);
        }

        var10 = 1.0F - var10;
        var10 = 1.0F - var10 * var10 * var10;
        TextureAtlasSprite var12 = this.getSprite(var5);
        if (var4) {
            VertexConsumer var13 = param3.getBuffer(RenderType.blockentityCutout());
            if (var3 == ChestType.LEFT) {
                this.render(param2, var13, this.doubleRightLid, this.doubleRightLock, this.doubleRightBottom, var10, param4, param5, var12);
            } else {
                this.render(param2, var13, this.doubleLeftLid, this.doubleLeftLock, this.doubleLeftBottom, var10, param4, param5, var12);
            }
        } else {
            VertexConsumer var14 = param3.getBuffer(RenderType.blockentitySolid());
            this.render(param2, var14, this.lid, this.lock, this.bottom, var10, param4, param5, var12);
        }

        param2.popPose();
    }

    private ResourceLocation chooseTexture(ChestType param0, ResourceLocation param1, ResourceLocation param2, ResourceLocation param3) {
        switch(param0) {
            case LEFT:
                return param3;
            case RIGHT:
                return param2;
            case SINGLE:
            default:
                return param1;
        }
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
        param2.render(param0, param1, param6, param7, param8);
        param3.render(param0, param1, param6, param7, param8);
        param4.render(param0, param1, param6, param7, param8);
    }
}
