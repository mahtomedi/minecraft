package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BedRenderer extends BatchedBlockEntityRenderer<BedBlockEntity> {
    public static final ResourceLocation[] TEXTURES = Arrays.stream(DyeColor.values())
        .sorted(Comparator.comparingInt(DyeColor::getId))
        .map(param0 -> new ResourceLocation("entity/bed/" + param0.getName()))
        .toArray(param0 -> new ResourceLocation[param0]);
    private final ModelPart headPiece;
    private final ModelPart footPiece;
    private final ModelPart[] legs = new ModelPart[4];

    public BedRenderer() {
        this.headPiece = new ModelPart(64, 64, 0, 0);
        this.headPiece.addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F, 0.0F);
        this.footPiece = new ModelPart(64, 64, 0, 22);
        this.footPiece.addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F, 0.0F);
        this.legs[0] = new ModelPart(64, 64, 50, 0);
        this.legs[1] = new ModelPart(64, 64, 50, 6);
        this.legs[2] = new ModelPart(64, 64, 50, 12);
        this.legs[3] = new ModelPart(64, 64, 50, 18);
        this.legs[0].addBox(0.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F);
        this.legs[1].addBox(0.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F);
        this.legs[2].addBox(-16.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F);
        this.legs[3].addBox(-16.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F);
        this.legs[0].xRot = (float) (Math.PI / 2);
        this.legs[1].xRot = (float) (Math.PI / 2);
        this.legs[2].xRot = (float) (Math.PI / 2);
        this.legs[3].xRot = (float) (Math.PI / 2);
        this.legs[0].zRot = 0.0F;
        this.legs[1].zRot = (float) (Math.PI / 2);
        this.legs[2].zRot = (float) (Math.PI * 3.0 / 2.0);
        this.legs[3].zRot = (float) Math.PI;
    }

    protected void renderToBuffer(
        BedBlockEntity param0,
        double param1,
        double param2,
        double param3,
        float param4,
        int param5,
        RenderType param6,
        BufferBuilder param7,
        int param8,
        int param9
    ) {
        ResourceLocation var0;
        if (param5 >= 0) {
            var0 = ModelBakery.DESTROY_STAGES.get(param5);
        } else {
            var0 = TEXTURES[param0.getColor().getId()];
        }

        this.doRender(param7, var0, param0, param8, param9);
    }

    public void doRender(BufferBuilder param0, ResourceLocation param1, BedBlockEntity param2, int param3, int param4) {
        if (param2.hasLevel()) {
            BlockState var0 = param2.getBlockState();
            this.renderPiece(param0, var0.getValue(BedBlock.PART) == BedPart.HEAD, var0.getValue(BedBlock.FACING), param1, param3, param4, false);
        } else {
            this.renderPiece(param0, true, Direction.SOUTH, param1, param3, param4, false);
            this.renderPiece(param0, false, Direction.SOUTH, param1, param3, param4, true);
        }

    }

    private void renderPiece(BufferBuilder param0, boolean param1, Direction param2, ResourceLocation param3, int param4, int param5, boolean param6) {
        this.headPiece.visible = param1;
        this.footPiece.visible = !param1;
        this.legs[0].visible = !param1;
        this.legs[1].visible = param1;
        this.legs[2].visible = !param1;
        this.legs[3].visible = param1;
        param0.pushPose();
        param0.translate(0.0, 0.5625, param6 ? -1.0 : 0.0);
        param0.multiplyPose(new Quaternion(Vector3f.XP, 90.0F, true));
        param0.translate(0.5, 0.5, 0.5);
        param0.multiplyPose(new Quaternion(Vector3f.ZP, 180.0F + param2.toYRot(), true));
        param0.translate(-0.5, -0.5, -0.5);
        TextureAtlasSprite var0 = this.getSprite(param3);
        this.headPiece.render(param0, 0.0625F, param4, param5, var0);
        this.footPiece.render(param0, 0.0625F, param4, param5, var0);
        this.legs[0].render(param0, 0.0625F, param4, param5, var0);
        this.legs[1].render(param0, 0.0625F, param4, param5, var0);
        this.legs[2].render(param0, 0.0625F, param4, param5, var0);
        this.legs[3].render(param0, 0.0625F, param4, param5, var0);
        param0.popPose();
    }
}
