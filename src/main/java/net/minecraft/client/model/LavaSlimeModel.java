package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LavaSlimeModel<T extends Slime> extends ListModel<T> {
    private final ModelPart[] bodyCubes = new ModelPart[8];
    private final ModelPart insideCube;
    private final ImmutableList<ModelPart> parts;

    public LavaSlimeModel() {
        for(int var0 = 0; var0 < this.bodyCubes.length; ++var0) {
            int var1 = 0;
            int var2 = var0;
            if (var0 == 2) {
                var1 = 24;
                var2 = 10;
            } else if (var0 == 3) {
                var1 = 24;
                var2 = 19;
            }

            this.bodyCubes[var0] = new ModelPart(this, var1, var2);
            this.bodyCubes[var0].addBox(-4.0F, (float)(16 + var0), -4.0F, 8.0F, 1.0F, 8.0F);
        }

        this.insideCube = new ModelPart(this, 0, 16);
        this.insideCube.addBox(-2.0F, 18.0F, -2.0F, 4.0F, 4.0F, 4.0F);
        Builder<ModelPart> var3 = ImmutableList.builder();
        var3.add(this.insideCube);
        var3.addAll(Arrays.asList(this.bodyCubes));
        this.parts = var3.build();
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        float var0 = Mth.lerp(param3, param0.oSquish, param0.squish);
        if (var0 < 0.0F) {
            var0 = 0.0F;
        }

        for(int var1 = 0; var1 < this.bodyCubes.length; ++var1) {
            this.bodyCubes[var1].y = (float)(-(4 - var1)) * var0 * 1.7F;
        }

    }

    public ImmutableList<ModelPart> parts() {
        return this.parts;
    }
}
