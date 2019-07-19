package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BookModel extends Model {
    private final ModelPart leftLid = new ModelPart(this).texOffs(0, 0).addBox(-6.0F, -5.0F, 0.0F, 6, 10, 0);
    private final ModelPart rightLid = new ModelPart(this).texOffs(16, 0).addBox(0.0F, -5.0F, 0.0F, 6, 10, 0);
    private final ModelPart leftPages;
    private final ModelPart rightPages;
    private final ModelPart flipPage1;
    private final ModelPart flipPage2;
    private final ModelPart seam = new ModelPart(this).texOffs(12, 0).addBox(-1.0F, -5.0F, 0.0F, 2, 10, 0);

    public BookModel() {
        this.leftPages = new ModelPart(this).texOffs(0, 10).addBox(0.0F, -4.0F, -0.99F, 5, 8, 1);
        this.rightPages = new ModelPart(this).texOffs(12, 10).addBox(0.0F, -4.0F, -0.01F, 5, 8, 1);
        this.flipPage1 = new ModelPart(this).texOffs(24, 10).addBox(0.0F, -4.0F, 0.0F, 5, 8, 0);
        this.flipPage2 = new ModelPart(this).texOffs(24, 10).addBox(0.0F, -4.0F, 0.0F, 5, 8, 0);
        this.leftLid.setPos(0.0F, 0.0F, -1.0F);
        this.rightLid.setPos(0.0F, 0.0F, 1.0F);
        this.seam.yRot = (float) (Math.PI / 2);
    }

    public void render(float param0, float param1, float param2, float param3, float param4, float param5) {
        this.setupAnim(param0, param1, param2, param3, param4, param5);
        this.leftLid.render(param5);
        this.rightLid.render(param5);
        this.seam.render(param5);
        this.leftPages.render(param5);
        this.rightPages.render(param5);
        this.flipPage1.render(param5);
        this.flipPage2.render(param5);
    }

    private void setupAnim(float param0, float param1, float param2, float param3, float param4, float param5) {
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
