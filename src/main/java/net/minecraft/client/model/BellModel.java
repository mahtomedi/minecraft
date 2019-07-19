package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BellModel extends Model {
    private final ModelPart bellBody;
    private final ModelPart bellBase;

    public BellModel() {
        this.texWidth = 32;
        this.texHeight = 32;
        this.bellBody = new ModelPart(this, 0, 0);
        this.bellBody.addBox(-3.0F, -6.0F, -3.0F, 6, 7, 6);
        this.bellBody.setPos(8.0F, 12.0F, 8.0F);
        this.bellBase = new ModelPart(this, 0, 13);
        this.bellBase.addBox(4.0F, 4.0F, 4.0F, 8, 2, 8);
        this.bellBase.setPos(-8.0F, -12.0F, -8.0F);
        this.bellBody.addChild(this.bellBase);
    }

    public void render(float param0, float param1, float param2) {
        this.bellBody.xRot = param0;
        this.bellBody.zRot = param1;
        this.bellBody.render(param2);
    }
}
