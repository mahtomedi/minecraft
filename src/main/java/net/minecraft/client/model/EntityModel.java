package net.minecraft.client.model;

import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class EntityModel<T extends Entity> extends Model {
    public float attackTime;
    public boolean riding;
    public boolean young = true;

    protected EntityModel() {
        this(RenderType::entityCutoutNoCull);
    }

    protected EntityModel(Function<ResourceLocation, RenderType> param0) {
        super(param0);
    }

    public abstract void setupAnim(T var1, float var2, float var3, float var4, float var5, float var6, float var7);

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
    }

    public void copyPropertiesTo(EntityModel<T> param0) {
        param0.attackTime = this.attackTime;
        param0.riding = this.riding;
        param0.young = this.young;
    }
}
