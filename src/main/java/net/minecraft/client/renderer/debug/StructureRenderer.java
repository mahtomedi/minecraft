package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<DimensionType, Map<String, BoundingBox>> postMainBoxes = Maps.newIdentityHashMap();
    private final Map<DimensionType, Map<String, BoundingBox>> postPiecesBoxes = Maps.newIdentityHashMap();
    private final Map<DimensionType, Map<String, Boolean>> startPiecesMap = Maps.newIdentityHashMap();

    public StructureRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    public void addBoundingBox(BoundingBox param0, List<BoundingBox> param1, List<Boolean> param2, DimensionType param3) {
        if (!this.postMainBoxes.containsKey(param3)) {
            this.postMainBoxes.put(param3, Maps.newHashMap());
        }

        if (!this.postPiecesBoxes.containsKey(param3)) {
            this.postPiecesBoxes.put(param3, Maps.newHashMap());
            this.startPiecesMap.put(param3, Maps.newHashMap());
        }

        this.postMainBoxes.get(param3).put(param0.toString(), param0);

        for(int var0 = 0; var0 < param1.size(); ++var0) {
            BoundingBox var1 = param1.get(var0);
            Boolean var2 = param2.get(var0);
            this.postPiecesBoxes.get(param3).put(var1.toString(), var1);
            this.startPiecesMap.get(param3).put(var1.toString(), var2);
        }

    }

    @Override
    public void clear() {
        this.postMainBoxes.clear();
        this.postPiecesBoxes.clear();
        this.startPiecesMap.clear();
    }
}
