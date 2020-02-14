package net.minecraft.client.renderer.debug;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillageSectionsDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
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
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        this.doRender(param2, param3, param4);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    private void doRender(double param0, double param1, double param2) {
        BlockPos var0 = new BlockPos(param0, param1, param2);
        this.villageSections.forEach(param1x -> {
            if (var0.closerThan(param1x.center(), 60.0)) {
                highlightVillageSection(param1x);
            }

        });
    }

    private static void highlightVillageSection(SectionPos param0) {
        float var0 = 1.0F;
        BlockPos var1 = param0.center();
        BlockPos var2 = var1.offset(-1.0, -1.0, -1.0);
        BlockPos var3 = var1.offset(1.0, 1.0, 1.0);
        DebugRenderer.renderFilledBox(var2, var3, 0.2F, 1.0F, 0.2F, 0.15F);
    }
}
