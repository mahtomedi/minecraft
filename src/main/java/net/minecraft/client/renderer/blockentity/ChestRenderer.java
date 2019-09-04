package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Calendar;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.LargeChestModel;
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
    private static final ResourceLocation CHEST_LARGE_TRAP_LOCATION = new ResourceLocation("textures/entity/chest/trapped_double.png");
    private static final ResourceLocation CHEST_LARGE_XMAS_LOCATION = new ResourceLocation("textures/entity/chest/christmas_double.png");
    private static final ResourceLocation CHEST_LARGE_LOCATION = new ResourceLocation("textures/entity/chest/normal_double.png");
    private static final ResourceLocation CHEST_TRAP_LOCATION = new ResourceLocation("textures/entity/chest/trapped.png");
    private static final ResourceLocation CHEST_XMAS_LOCATION = new ResourceLocation("textures/entity/chest/christmas.png");
    private static final ResourceLocation CHEST_LOCATION = new ResourceLocation("textures/entity/chest/normal.png");
    private static final ResourceLocation ENDER_CHEST_LOCATION = new ResourceLocation("textures/entity/chest/ender.png");
    private final ChestModel chestModel = new ChestModel();
    private final ChestModel largeChestModel = new LargeChestModel();
    private boolean xmasTextures;

    public ChestRenderer() {
        Calendar var0 = Calendar.getInstance();
        if (var0.get(2) + 1 == 12 && var0.get(5) >= 24 && var0.get(5) <= 26) {
            this.xmasTextures = true;
        }

    }

    @Override
    public void render(T param0, double param1, double param2, double param3, float param4, int param5) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(515);
        RenderSystem.depthMask(true);
        BlockState var0 = param0.hasLevel() ? param0.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        ChestType var1 = var0.hasProperty(ChestBlock.TYPE) ? var0.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        if (var1 != ChestType.LEFT) {
            boolean var2 = var1 != ChestType.SINGLE;
            ChestModel var3 = this.getChestModelAndBindTexture(param0, param5, var2);
            if (param5 >= 0) {
                RenderSystem.matrixMode(5890);
                RenderSystem.pushMatrix();
                RenderSystem.scalef(var2 ? 8.0F : 4.0F, 4.0F, 1.0F);
                RenderSystem.translatef(0.0625F, 0.0625F, 0.0625F);
                RenderSystem.matrixMode(5888);
            } else {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            }

            RenderSystem.pushMatrix();
            RenderSystem.enableRescaleNormal();
            RenderSystem.translatef((float)param1, (float)param2 + 1.0F, (float)param3 + 1.0F);
            RenderSystem.scalef(1.0F, -1.0F, -1.0F);
            float var4 = var0.getValue(ChestBlock.FACING).toYRot();
            if ((double)Math.abs(var4) > 1.0E-5) {
                RenderSystem.translatef(0.5F, 0.5F, 0.5F);
                RenderSystem.rotatef(var4, 0.0F, 1.0F, 0.0F);
                RenderSystem.translatef(-0.5F, -0.5F, -0.5F);
            }

            this.rotateLid(param0, param4, var3);
            var3.render();
            RenderSystem.disableRescaleNormal();
            RenderSystem.popMatrix();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            if (param5 >= 0) {
                RenderSystem.matrixMode(5890);
                RenderSystem.popMatrix();
                RenderSystem.matrixMode(5888);
            }

        }
    }

    private ChestModel getChestModelAndBindTexture(T param0, int param1, boolean param2) {
        ResourceLocation var0;
        if (param1 >= 0) {
            var0 = BREAKING_LOCATIONS[param1];
        } else if (this.xmasTextures) {
            var0 = param2 ? CHEST_LARGE_XMAS_LOCATION : CHEST_XMAS_LOCATION;
        } else if (param0 instanceof TrappedChestBlockEntity) {
            var0 = param2 ? CHEST_LARGE_TRAP_LOCATION : CHEST_TRAP_LOCATION;
        } else if (param0 instanceof EnderChestBlockEntity) {
            var0 = ENDER_CHEST_LOCATION;
        } else {
            var0 = param2 ? CHEST_LARGE_LOCATION : CHEST_LOCATION;
        }

        this.bindTexture(var0);
        return param2 ? this.largeChestModel : this.chestModel;
    }

    private void rotateLid(T param0, float param1, ChestModel param2) {
        float var0 = param0.getOpenNess(param1);
        var0 = 1.0F - var0;
        var0 = 1.0F - var0 * var0 * var0;
        param2.getLid().xRot = -(var0 * (float) (Math.PI / 2));
    }
}
