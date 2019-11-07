package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Random;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GhastModel<T extends Entity> extends ListModel<T> {
    private final ModelPart[] tentacles = new ModelPart[9];
    private final ImmutableList<ModelPart> parts;

    public GhastModel() {
        Builder<ModelPart> var0 = ImmutableList.builder();
        ModelPart var1 = new ModelPart(this, 0, 0);
        var1.addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F);
        var1.y = 17.6F;
        var0.add(var1);
        Random var2 = new Random(1660L);

        for(int var3 = 0; var3 < this.tentacles.length; ++var3) {
            this.tentacles[var3] = new ModelPart(this, 0, 0);
            float var4 = (((float)(var3 % 3) - (float)(var3 / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
            float var5 = ((float)(var3 / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
            int var6 = var2.nextInt(7) + 8;
            this.tentacles[var3].addBox(-1.0F, 0.0F, -1.0F, 2.0F, (float)var6, 2.0F);
            this.tentacles[var3].x = var4;
            this.tentacles[var3].z = var5;
            this.tentacles[var3].y = 24.6F;
            var0.add(this.tentacles[var3]);
        }

        this.parts = var0.build();
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        for(int var0 = 0; var0 < this.tentacles.length; ++var0) {
            this.tentacles[var0].xRot = 0.2F * Mth.sin(param3 * 0.3F + (float)var0) + 0.4F;
        }

    }

    @Override
    public Iterable<ModelPart> parts() {
        return this.parts;
    }
}
