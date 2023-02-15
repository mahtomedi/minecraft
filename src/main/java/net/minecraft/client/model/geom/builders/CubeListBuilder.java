package net.minecraft.client.model.geom.builders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CubeListBuilder {
    private static final Set<Direction> ALL_VISIBLE = EnumSet.allOf(Direction.class);
    private final List<CubeDefinition> cubes = Lists.newArrayList();
    private int xTexOffs;
    private int yTexOffs;
    private boolean mirror;

    public CubeListBuilder texOffs(int param0, int param1) {
        this.xTexOffs = param0;
        this.yTexOffs = param1;
        return this;
    }

    public CubeListBuilder mirror() {
        return this.mirror(true);
    }

    public CubeListBuilder mirror(boolean param0) {
        this.mirror = param0;
        return this;
    }

    public CubeListBuilder addBox(
        String param0, float param1, float param2, float param3, int param4, int param5, int param6, CubeDeformation param7, int param8, int param9
    ) {
        this.texOffs(param8, param9);
        this.cubes
            .add(
                new CubeDefinition(
                    param0,
                    (float)this.xTexOffs,
                    (float)this.yTexOffs,
                    param1,
                    param2,
                    param3,
                    (float)param4,
                    (float)param5,
                    (float)param6,
                    param7,
                    this.mirror,
                    1.0F,
                    1.0F,
                    ALL_VISIBLE
                )
            );
        return this;
    }

    public CubeListBuilder addBox(String param0, float param1, float param2, float param3, int param4, int param5, int param6, int param7, int param8) {
        this.texOffs(param7, param8);
        this.cubes
            .add(
                new CubeDefinition(
                    param0,
                    (float)this.xTexOffs,
                    (float)this.yTexOffs,
                    param1,
                    param2,
                    param3,
                    (float)param4,
                    (float)param5,
                    (float)param6,
                    CubeDeformation.NONE,
                    this.mirror,
                    1.0F,
                    1.0F,
                    ALL_VISIBLE
                )
            );
        return this;
    }

    public CubeListBuilder addBox(float param0, float param1, float param2, float param3, float param4, float param5) {
        this.cubes
            .add(
                new CubeDefinition(
                    null,
                    (float)this.xTexOffs,
                    (float)this.yTexOffs,
                    param0,
                    param1,
                    param2,
                    param3,
                    param4,
                    param5,
                    CubeDeformation.NONE,
                    this.mirror,
                    1.0F,
                    1.0F,
                    ALL_VISIBLE
                )
            );
        return this;
    }

    public CubeListBuilder addBox(float param0, float param1, float param2, float param3, float param4, float param5, Set<Direction> param6) {
        this.cubes
            .add(
                new CubeDefinition(
                    null,
                    (float)this.xTexOffs,
                    (float)this.yTexOffs,
                    param0,
                    param1,
                    param2,
                    param3,
                    param4,
                    param5,
                    CubeDeformation.NONE,
                    this.mirror,
                    1.0F,
                    1.0F,
                    param6
                )
            );
        return this;
    }

    public CubeListBuilder addBox(String param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.cubes
            .add(
                new CubeDefinition(
                    param0,
                    (float)this.xTexOffs,
                    (float)this.yTexOffs,
                    param1,
                    param2,
                    param3,
                    param4,
                    param5,
                    param6,
                    CubeDeformation.NONE,
                    this.mirror,
                    1.0F,
                    1.0F,
                    ALL_VISIBLE
                )
            );
        return this;
    }

    public CubeListBuilder addBox(String param0, float param1, float param2, float param3, float param4, float param5, float param6, CubeDeformation param7) {
        this.cubes
            .add(
                new CubeDefinition(
                    param0,
                    (float)this.xTexOffs,
                    (float)this.yTexOffs,
                    param1,
                    param2,
                    param3,
                    param4,
                    param5,
                    param6,
                    param7,
                    this.mirror,
                    1.0F,
                    1.0F,
                    ALL_VISIBLE
                )
            );
        return this;
    }

    public CubeListBuilder addBox(float param0, float param1, float param2, float param3, float param4, float param5, boolean param6) {
        this.cubes
            .add(
                new CubeDefinition(
                    null,
                    (float)this.xTexOffs,
                    (float)this.yTexOffs,
                    param0,
                    param1,
                    param2,
                    param3,
                    param4,
                    param5,
                    CubeDeformation.NONE,
                    param6,
                    1.0F,
                    1.0F,
                    ALL_VISIBLE
                )
            );
        return this;
    }

    public CubeListBuilder addBox(
        float param0, float param1, float param2, float param3, float param4, float param5, CubeDeformation param6, float param7, float param8
    ) {
        this.cubes
            .add(
                new CubeDefinition(
                    null,
                    (float)this.xTexOffs,
                    (float)this.yTexOffs,
                    param0,
                    param1,
                    param2,
                    param3,
                    param4,
                    param5,
                    param6,
                    this.mirror,
                    param7,
                    param8,
                    ALL_VISIBLE
                )
            );
        return this;
    }

    public CubeListBuilder addBox(float param0, float param1, float param2, float param3, float param4, float param5, CubeDeformation param6) {
        this.cubes
            .add(
                new CubeDefinition(
                    null,
                    (float)this.xTexOffs,
                    (float)this.yTexOffs,
                    param0,
                    param1,
                    param2,
                    param3,
                    param4,
                    param5,
                    param6,
                    this.mirror,
                    1.0F,
                    1.0F,
                    ALL_VISIBLE
                )
            );
        return this;
    }

    public List<CubeDefinition> getCubes() {
        return ImmutableList.copyOf(this.cubes);
    }

    public static CubeListBuilder create() {
        return new CubeListBuilder();
    }
}
