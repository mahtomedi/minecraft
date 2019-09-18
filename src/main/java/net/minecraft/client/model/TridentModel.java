package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TridentModel extends Model {
    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/trident.png");
    private final ModelPart pole;

    public TridentModel() {
        this.texWidth = 32;
        this.texHeight = 32;
        this.pole = new ModelPart(this, 0, 0);
        this.pole.addBox(-0.5F, -4.0F, -0.5F, 1.0F, 31.0F, 1.0F, 0.0F);
        ModelPart var0 = new ModelPart(this, 4, 0);
        var0.addBox(-1.5F, 0.0F, -0.5F, 3.0F, 2.0F, 1.0F);
        this.pole.addChild(var0);
        ModelPart var1 = new ModelPart(this, 4, 3);
        var1.addBox(-2.5F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F);
        this.pole.addChild(var1);
        ModelPart var2 = new ModelPart(this, 4, 3);
        var2.mirror = true;
        var2.addBox(1.5F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F);
        this.pole.addChild(var2);
    }

    public void render() {
        this.pole.render(0.0625F);
    }
}
