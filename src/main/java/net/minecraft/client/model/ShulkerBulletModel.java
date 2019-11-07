package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBulletModel<T extends Entity> extends ListModel<T> {
    private final ModelPart main;

    public ShulkerBulletModel() {
        this.texWidth = 64;
        this.texHeight = 32;
        this.main = new ModelPart(this);
        this.main.texOffs(0, 0).addBox(-4.0F, -4.0F, -1.0F, 8.0F, 8.0F, 2.0F, 0.0F);
        this.main.texOffs(0, 10).addBox(-1.0F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F, 0.0F);
        this.main.texOffs(20, 0).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 2.0F, 8.0F, 0.0F);
        this.main.setPos(0.0F, 0.0F, 0.0F);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.main);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.main.yRot = param4 * (float) (Math.PI / 180.0);
        this.main.xRot = param5 * (float) (Math.PI / 180.0);
    }
}
