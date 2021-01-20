package net.minecraft.client.model.geom.builders;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerDefinition {
    private final MeshDefinition mesh;
    private final MaterialDefinition material;

    private LayerDefinition(MeshDefinition param0, MaterialDefinition param1) {
        this.mesh = param0;
        this.material = param1;
    }

    public ModelPart bakeRoot() {
        return this.mesh.getRoot().bake(this.material.xTexSize, this.material.yTexSize);
    }

    public static LayerDefinition create(MeshDefinition param0, int param1, int param2) {
        return new LayerDefinition(param0, new MaterialDefinition(param1, param2));
    }
}
