package net.minecraft.client.model;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Model {
    public final List<ModelPart> cubes = Lists.newArrayList();
    public int texWidth = 64;
    public int texHeight = 32;

    public ModelPart getRandomModelPart(Random param0) {
        return this.cubes.get(param0.nextInt(this.cubes.size()));
    }
}
