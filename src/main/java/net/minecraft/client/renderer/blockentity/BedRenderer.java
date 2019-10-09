package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
public class BedRenderer extends BlockEntityRenderer<BedBlockEntity> {
    public static final ResourceLocation[] TEXTURES = Arrays.stream(DyeColor.values())
        .sorted(Comparator.comparingInt(DyeColor::getId))
        .map(param0 -> new ResourceLocation("entity/bed/" + param0.getName()))
        .toArray(param0 -> new ResourceLocation[param0]);
    private final ModelPart headPiece;
    private final ModelPart footPiece;
    private final ModelPart[] legs = new ModelPart[4];

    public BedRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
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

    public void render(
        BedBlockEntity param0, double param1, double param2, double param3, float param4, PoseStack param5, MultiBufferSource param6, int param7, int param8
    ) {
        ResourceLocation var0 = TEXTURES[param0.getColor().getId()];
        VertexConsumer var1 = param6.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        if (param0.hasLevel()) {
            BlockState var2 = param0.getBlockState();
            this.renderPiece(param5, var1, var2.getValue(BedBlock.PART) == BedPart.HEAD, var2.getValue(BedBlock.FACING), var0, param7, param8, false);
        } else {
            this.renderPiece(param5, var1, true, Direction.SOUTH, var0, param7, param8, false);
            this.renderPiece(param5, var1, false, Direction.SOUTH, var0, param7, param8, true);
        }

    }

    private void renderPiece(
        PoseStack param0, VertexConsumer param1, boolean param2, Direction param3, ResourceLocation param4, int param5, int param6, boolean param7
    ) {
        this.headPiece.visible = param2;
        this.footPiece.visible = !param2;
        this.legs[0].visible = !param2;
        this.legs[1].visible = param2;
        this.legs[2].visible = !param2;
        this.legs[3].visible = param2;
        param0.pushPose();
        param0.translate(0.0, 0.5625, param7 ? -1.0 : 0.0);
        param0.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        param0.translate(0.5, 0.5, 0.5);
        param0.mulPose(Vector3f.ZP.rotationDegrees(180.0F + param3.toYRot()));
        param0.translate(-0.5, -0.5, -0.5);
        TextureAtlasSprite var0 = this.getSprite(param4);
        this.headPiece.render(param0, param1, 0.0625F, param5, param6, var0);
        this.footPiece.render(param0, param1, 0.0625F, param5, param6, var0);
        this.legs[0].render(param0, param1, 0.0625F, param5, param6, var0);
        this.legs[1].render(param0, param1, 0.0625F, param5, param6, var0);
        this.legs[2].render(param0, param1, 0.0625F, param5, param6, var0);
        this.legs[3].render(param0, param1, 0.0625F, param5, param6, var0);
        param0.popPose();
    }
}
