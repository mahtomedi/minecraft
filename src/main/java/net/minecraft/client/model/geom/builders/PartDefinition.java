package net.minecraft.client.model.geom.builders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PartDefinition {
    private final List<CubeDefinition> cubes;
    private final PartPose partPose;
    private final Map<String, PartDefinition> children = Maps.newHashMap();

    PartDefinition(List<CubeDefinition> param0, PartPose param1) {
        this.cubes = param0;
        this.partPose = param1;
    }

    public PartDefinition addOrReplaceChild(String param0, CubeListBuilder param1, PartPose param2) {
        PartDefinition var0 = new PartDefinition(param1.getCubes(), param2);
        PartDefinition var1 = this.children.put(param0, var0);
        if (var1 != null) {
            var0.children.putAll(var1.children);
        }

        return var0;
    }

    public ModelPart bake(int param0, int param1) {
        Object2ObjectArrayMap<String, ModelPart> var0 = this.children
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(Entry::getKey, param2 -> param2.getValue().bake(param0, param1), (param0x, param1x) -> param0x, Object2ObjectArrayMap::new)
            );
        List<ModelPart.Cube> var1 = this.cubes.stream().map(param2 -> param2.bake(param0, param1)).collect(ImmutableList.toImmutableList());
        ModelPart var2 = new ModelPart(var1, var0);
        var2.setInitialPose(this.partPose);
        var2.loadPose(this.partPose);
        return var2;
    }

    public PartDefinition getChild(String param0) {
        return this.children.get(param0);
    }
}
