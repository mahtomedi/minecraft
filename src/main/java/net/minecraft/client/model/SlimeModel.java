package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SlimeModel<T extends Entity> extends ListModel<T> {
    private final ModelPart cube;
    private final ModelPart eye0;
    private final ModelPart eye1;
    private final ModelPart mouth;

    public SlimeModel(int param0) {
        this.cube = new ModelPart(this, 0, param0);
        this.eye0 = new ModelPart(this, 32, 0);
        this.eye1 = new ModelPart(this, 32, 4);
        this.mouth = new ModelPart(this, 32, 8);
        if (param0 > 0) {
            this.cube.addBox(-3.0F, 17.0F, -3.0F, 6.0F, 6.0F, 6.0F);
            this.eye0.addBox(-3.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F);
            this.eye1.addBox(1.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F);
            this.mouth.addBox(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F);
        } else {
            this.cube.addBox(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F);
        }

    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.cube, this.eye0, this.eye1, this.mouth);
    }
}
