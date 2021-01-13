package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SquidModel<T extends Entity> extends ListModel<T> {
    private final ModelPart body;
    private final ModelPart[] tentacles = new ModelPart[8];
    private final ImmutableList<ModelPart> parts;

    public SquidModel() {
        int var0 = -16;
        this.body = new ModelPart(this, 0, 0);
        this.body.addBox(-6.0F, -8.0F, -6.0F, 12.0F, 16.0F, 12.0F);
        this.body.y += 8.0F;

        for(int var1 = 0; var1 < this.tentacles.length; ++var1) {
            this.tentacles[var1] = new ModelPart(this, 48, 0);
            double var2 = (double)var1 * Math.PI * 2.0 / (double)this.tentacles.length;
            float var3 = (float)Math.cos(var2) * 5.0F;
            float var4 = (float)Math.sin(var2) * 5.0F;
            this.tentacles[var1].addBox(-1.0F, 0.0F, -1.0F, 2.0F, 18.0F, 2.0F);
            this.tentacles[var1].x = var3;
            this.tentacles[var1].z = var4;
            this.tentacles[var1].y = 15.0F;
            var2 = (double)var1 * Math.PI * -2.0 / (double)this.tentacles.length + (Math.PI / 2);
            this.tentacles[var1].yRot = (float)var2;
        }

        Builder<ModelPart> var5 = ImmutableList.builder();
        var5.add(this.body);
        var5.addAll(Arrays.asList(this.tentacles));
        this.parts = var5.build();
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        for(ModelPart var0 : this.tentacles) {
            var0.xRot = param3;
        }

    }

    @Override
    public Iterable<ModelPart> parts() {
        return this.parts;
    }
}
