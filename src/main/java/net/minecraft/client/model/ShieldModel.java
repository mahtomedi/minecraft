package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShieldModel extends Model {
    private final ModelPart plate;
    private final ModelPart handle;

    public ShieldModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        this.plate = new ModelPart(this, 0, 0);
        this.plate.addBox(-6.0F, -11.0F, -2.0F, 12.0F, 22.0F, 1.0F, 0.0F);
        this.handle = new ModelPart(this, 26, 0);
        this.handle.addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 6.0F, 0.0F);
    }

    public void render(PoseStack param0, VertexConsumer param1, int param2) {
        this.plate.render(param0, param1, 0.0625F, param2, null);
        this.handle.render(param0, param1, 0.0625F, param2, null);
    }
}
