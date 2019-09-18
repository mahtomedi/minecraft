package net.minecraft.client.model;

import java.util.function.Consumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Model implements Consumer<ModelPart> {
    public int texWidth = 64;
    public int texHeight = 32;

    public void accept(ModelPart param0) {
    }
}
