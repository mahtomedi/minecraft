package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BookModel extends Model {
    private final ModelPart leftLid = new ModelPart(64, 32, 0, 0).addBox(-6.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F);
    private final ModelPart rightLid = new ModelPart(64, 32, 16, 0).addBox(0.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F);
    private final ModelPart leftPages;
    private final ModelPart rightPages;
    private final ModelPart flipPage1;
    private final ModelPart flipPage2;
    private final ModelPart seam = new ModelPart(64, 32, 12, 0).addBox(-1.0F, -5.0F, 0.0F, 2.0F, 10.0F, 0.005F);
    private final List<ModelPart> parts;

    public BookModel() {
        this.leftPages = new ModelPart(64, 32, 0, 10).addBox(0.0F, -4.0F, -0.99F, 5.0F, 8.0F, 1.0F);
        this.rightPages = new ModelPart(64, 32, 12, 10).addBox(0.0F, -4.0F, -0.01F, 5.0F, 8.0F, 1.0F);
        this.flipPage1 = new ModelPart(64, 32, 24, 10).addBox(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.005F);
        this.flipPage2 = new ModelPart(64, 32, 24, 10).addBox(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.005F);
        this.parts = ImmutableList.of(this.leftLid, this.rightLid, this.seam, this.leftPages, this.rightPages, this.flipPage1, this.flipPage2);
        this.leftLid.setPos(0.0F, 0.0F, -1.0F);
        this.rightLid.setPos(0.0F, 0.0F, 1.0F);
        this.seam.yRot = (float) (Math.PI / 2);
    }

    public void render(PoseStack param0, VertexConsumer param1, float param2, int param3, TextureAtlasSprite param4) {
        this.parts.forEach(param5 -> param5.render(param0, param1, param2, param3, param4));
    }

    public void setupAnim(float param0, float param1, float param2, float param3) {
        float var0 = (Mth.sin(param0 * 0.02F) * 0.1F + 1.25F) * param3;
        this.leftLid.yRot = (float) Math.PI + var0;
        this.rightLid.yRot = -var0;
        this.leftPages.yRot = var0;
        this.rightPages.yRot = -var0;
        this.flipPage1.yRot = var0 - var0 * 2.0F * param1;
        this.flipPage2.yRot = var0 - var0 * 2.0F * param2;
        this.leftPages.x = Mth.sin(var0);
        this.rightPages.x = Mth.sin(var0);
        this.flipPage1.x = Mth.sin(var0);
        this.flipPage2.x = Mth.sin(var0);
    }
}
