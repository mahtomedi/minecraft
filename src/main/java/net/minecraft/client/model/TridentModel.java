package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TridentModel extends Model {
    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/trident.png");
    private final ModelPart pole = new ModelPart(32, 32, 0, 6);

    public TridentModel() {
        super(RenderType::entitySolid);
        this.pole.addBox(-0.5F, 2.0F, -0.5F, 1.0F, 25.0F, 1.0F, 0.0F);
        ModelPart var0 = new ModelPart(32, 32, 4, 0);
        var0.addBox(-1.5F, 0.0F, -0.5F, 3.0F, 2.0F, 1.0F);
        this.pole.addChild(var0);
        ModelPart var1 = new ModelPart(32, 32, 4, 3);
        var1.addBox(-2.5F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F);
        this.pole.addChild(var1);
        ModelPart var2 = new ModelPart(32, 32, 0, 0);
        var2.addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, 0.0F);
        this.pole.addChild(var2);
        ModelPart var3 = new ModelPart(32, 32, 4, 3);
        var3.mirror = true;
        var3.addBox(1.5F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F);
        this.pole.addChild(var3);
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6) {
        this.pole.render(param0, param1, 0.0625F, param2, param3, null, param4, param5, param6);
    }
}
