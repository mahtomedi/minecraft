package net.minecraft.client.renderer.debug;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillageSectionsDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private static final int MAX_RENDER_DIST_FOR_VILLAGE_SECTIONS = 60;
    private final Set<SectionPos> villageSections = Sets.newHashSet();

    VillageSectionsDebugRenderer() {
    }

    @Override
    public void clear() {
        this.villageSections.clear();
    }

    public void setVillageSection(SectionPos param0) {
        this.villageSections.add(param0);
    }

    public void setNotVillageSection(SectionPos param0) {
        this.villageSections.remove(param0);
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        BlockPos var0 = BlockPos.containing(param2, param3, param4);
        this.villageSections.forEach(param3x -> {
            if (var0.closerThan(param3x.center(), 60.0)) {
                highlightVillageSection(param0, param1, param3x);
            }

        });
    }

    private static void highlightVillageSection(PoseStack param0, MultiBufferSource param1, SectionPos param2) {
        DebugRenderer.renderFilledUnitCube(param0, param1, param2.center(), 0.2F, 1.0F, 0.2F, 0.15F);
    }
}
