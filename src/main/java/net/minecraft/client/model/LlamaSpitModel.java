package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaSpitModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart main = new ModelPart(this);

    public LlamaSpitModel() {
        this(0.0F);
    }

    public LlamaSpitModel(float param0) {
        int var0 = 2;
        this.main.texOffs(0, 0).addBox(-4.0F, 0.0F, 0.0F, 2, 2, 2, param0);
        this.main.texOffs(0, 0).addBox(0.0F, -4.0F, 0.0F, 2, 2, 2, param0);
        this.main.texOffs(0, 0).addBox(0.0F, 0.0F, -4.0F, 2, 2, 2, param0);
        this.main.texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 2, 2, 2, param0);
        this.main.texOffs(0, 0).addBox(2.0F, 0.0F, 0.0F, 2, 2, 2, param0);
        this.main.texOffs(0, 0).addBox(0.0F, 2.0F, 0.0F, 2, 2, 2, param0);
        this.main.texOffs(0, 0).addBox(0.0F, 0.0F, 2.0F, 2, 2, 2, param0);
        this.main.setPos(0.0F, 0.0F, 0.0F);
    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.main.render(param6);
    }
}
