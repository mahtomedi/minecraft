package net.minecraft.client.model.geom.builders;

import com.mojang.math.Vector3f;
import javax.annotation.Nullable;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class CubeDefinition {
    @Nullable
    private final String comment;
    private final Vector3f origin;
    private final Vector3f dimensions;
    private final CubeDeformation grow;
    private final boolean mirror;
    private final UVPair texCoord;
    private final UVPair texScale;

    protected CubeDefinition(
        @Nullable String param0,
        float param1,
        float param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        CubeDeformation param9,
        boolean param10,
        float param11,
        float param12
    ) {
        this.comment = param0;
        this.texCoord = new UVPair(param1, param2);
        this.origin = new Vector3f(param3, param4, param5);
        this.dimensions = new Vector3f(param6, param7, param8);
        this.grow = param9;
        this.mirror = param10;
        this.texScale = new UVPair(param11, param12);
    }

    public ModelPart.Cube bake(int param0, int param1) {
        return new ModelPart.Cube(
            (int)this.texCoord.u(),
            (int)this.texCoord.v(),
            this.origin.x(),
            this.origin.y(),
            this.origin.z(),
            this.dimensions.x(),
            this.dimensions.y(),
            this.dimensions.z(),
            this.grow.growX,
            this.grow.growY,
            this.grow.growZ,
            this.mirror,
            (float)param0 * this.texScale.u(),
            (float)param1 * this.texScale.v()
        );
    }
}
