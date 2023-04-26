package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DecoratedPotRenderer implements BlockEntityRenderer<DecoratedPotBlockEntity> {
    private static final String NECK = "neck";
    private static final String FRONT = "front";
    private static final String BACK = "back";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String TOP = "top";
    private static final String BOTTOM = "bottom";
    private final ModelPart neck;
    private final ModelPart frontSide;
    private final ModelPart backSide;
    private final ModelPart leftSide;
    private final ModelPart rightSide;
    private final ModelPart top;
    private final ModelPart bottom;
    private final Material baseMaterial = Objects.requireNonNull(Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.BASE));

    public DecoratedPotRenderer(BlockEntityRendererProvider.Context param0) {
        ModelPart var0 = param0.bakeLayer(ModelLayers.DECORATED_POT_BASE);
        this.neck = var0.getChild("neck");
        this.top = var0.getChild("top");
        this.bottom = var0.getChild("bottom");
        ModelPart var1 = param0.bakeLayer(ModelLayers.DECORATED_POT_SIDES);
        this.frontSide = var1.getChild("front");
        this.backSide = var1.getChild("back");
        this.leftSide = var1.getChild("left");
        this.rightSide = var1.getChild("right");
    }

    public static LayerDefinition createBaseLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        CubeDeformation var2 = new CubeDeformation(0.2F);
        CubeDeformation var3 = new CubeDeformation(-0.1F);
        var1.addOrReplaceChild(
            "neck",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(4.0F, 17.0F, 4.0F, 8.0F, 3.0F, 8.0F, var3)
                .texOffs(0, 5)
                .addBox(5.0F, 20.0F, 5.0F, 6.0F, 1.0F, 6.0F, var2),
            PartPose.offsetAndRotation(0.0F, 37.0F, 16.0F, (float) Math.PI, 0.0F, 0.0F)
        );
        CubeListBuilder var4 = CubeListBuilder.create().texOffs(-14, 13).addBox(0.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F);
        var1.addOrReplaceChild("top", var4, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, 0.0F, 0.0F));
        var1.addOrReplaceChild("bottom", var4, PartPose.offsetAndRotation(1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(var0, 32, 32);
    }

    public static LayerDefinition createSidesLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        CubeListBuilder var2 = CubeListBuilder.create().texOffs(1, 0).addBox(0.0F, 0.0F, 0.0F, 14.0F, 16.0F, 0.0F, EnumSet.of(Direction.NORTH));
        var1.addOrReplaceChild("back", var2, PartPose.offsetAndRotation(15.0F, 16.0F, 1.0F, 0.0F, 0.0F, (float) Math.PI));
        var1.addOrReplaceChild("left", var2, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, (float) (-Math.PI / 2), (float) Math.PI));
        var1.addOrReplaceChild("right", var2, PartPose.offsetAndRotation(15.0F, 16.0F, 15.0F, 0.0F, (float) (Math.PI / 2), (float) Math.PI));
        var1.addOrReplaceChild("front", var2, PartPose.offsetAndRotation(1.0F, 16.0F, 15.0F, (float) Math.PI, 0.0F, 0.0F));
        return LayerDefinition.create(var0, 16, 16);
    }

    @Nullable
    private static Material getMaterial(Item param0) {
        Material var0 = Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.getResourceKey(param0));
        if (var0 == null) {
            var0 = Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.getResourceKey(Items.BRICK));
        }

        return var0;
    }

    public void render(DecoratedPotBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        param2.pushPose();
        Direction var0 = param0.getDirection();
        param2.translate(0.5, 0.0, 0.5);
        param2.mulPose(Axis.YP.rotationDegrees(180.0F - var0.toYRot()));
        param2.translate(-0.5, 0.0, -0.5);
        VertexConsumer var1 = this.baseMaterial.buffer(param3, RenderType::entitySolid);
        this.neck.render(param2, var1, param4, param5);
        this.top.render(param2, var1, param4, param5);
        this.bottom.render(param2, var1, param4, param5);
        DecoratedPotBlockEntity.Decorations var2 = param0.getDecorations();
        this.renderSide(this.frontSide, param2, param3, param4, param5, getMaterial(var2.front()));
        this.renderSide(this.backSide, param2, param3, param4, param5, getMaterial(var2.back()));
        this.renderSide(this.leftSide, param2, param3, param4, param5, getMaterial(var2.left()));
        this.renderSide(this.rightSide, param2, param3, param4, param5, getMaterial(var2.right()));
        param2.popPose();
    }

    private void renderSide(ModelPart param0, PoseStack param1, MultiBufferSource param2, int param3, int param4, @Nullable Material param5) {
        if (param5 == null) {
            param5 = getMaterial(Items.BRICK);
        }

        if (param5 != null) {
            param0.render(param1, param5.buffer(param2, RenderType::entitySolid), param3, param4);
        }

    }
}
