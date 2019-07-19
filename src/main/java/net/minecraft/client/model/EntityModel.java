package net.minecraft.client.model;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class EntityModel<T extends Entity> extends Model {
    public float attackTime;
    public boolean riding;
    public boolean young = true;

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
    }

    public void copyPropertiesTo(EntityModel<T> param0) {
        param0.attackTime = this.attackTime;
        param0.riding = this.riding;
        param0.young = this.young;
    }
}
