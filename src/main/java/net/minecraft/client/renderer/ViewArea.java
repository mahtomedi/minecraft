package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ViewArea {
    protected final LevelRenderer levelRenderer;
    protected final Level level;
    protected int sectionGridSizeY;
    protected int sectionGridSizeX;
    protected int sectionGridSizeZ;
    private int viewDistance;
    public SectionRenderDispatcher.RenderSection[] sections;

    public ViewArea(SectionRenderDispatcher param0, Level param1, int param2, LevelRenderer param3) {
        this.levelRenderer = param3;
        this.level = param1;
        this.setViewDistance(param2);
        this.createSections(param0);
    }

    protected void createSections(SectionRenderDispatcher param0) {
        if (!Minecraft.getInstance().isSameThread()) {
            throw new IllegalStateException("createSections called from wrong thread: " + Thread.currentThread().getName());
        } else {
            int var0 = this.sectionGridSizeX * this.sectionGridSizeY * this.sectionGridSizeZ;
            this.sections = new SectionRenderDispatcher.RenderSection[var0];

            for(int var1 = 0; var1 < this.sectionGridSizeX; ++var1) {
                for(int var2 = 0; var2 < this.sectionGridSizeY; ++var2) {
                    for(int var3 = 0; var3 < this.sectionGridSizeZ; ++var3) {
                        int var4 = this.getSectionIndex(var1, var2, var3);
                        this.sections[var4] = param0.new RenderSection(var4, var1 * 16, this.level.getMinBuildHeight() + var2 * 16, var3 * 16);
                    }
                }
            }

        }
    }

    public void releaseAllBuffers() {
        for(SectionRenderDispatcher.RenderSection var0 : this.sections) {
            var0.releaseBuffers();
        }

    }

    private int getSectionIndex(int param0, int param1, int param2) {
        return (param2 * this.sectionGridSizeY + param1) * this.sectionGridSizeX + param0;
    }

    protected void setViewDistance(int param0) {
        int var0 = param0 * 2 + 1;
        this.sectionGridSizeX = var0;
        this.sectionGridSizeY = this.level.getSectionsCount();
        this.sectionGridSizeZ = var0;
        this.viewDistance = param0;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public LevelHeightAccessor getLevelHeightAccessor() {
        return this.level;
    }

    public void repositionCamera(double param0, double param1) {
        int var0 = Mth.ceil(param0);
        int var1 = Mth.ceil(param1);

        for(int var2 = 0; var2 < this.sectionGridSizeX; ++var2) {
            int var3 = this.sectionGridSizeX * 16;
            int var4 = var0 - 8 - var3 / 2;
            int var5 = var4 + Math.floorMod(var2 * 16 - var4, var3);

            for(int var6 = 0; var6 < this.sectionGridSizeZ; ++var6) {
                int var7 = this.sectionGridSizeZ * 16;
                int var8 = var1 - 8 - var7 / 2;
                int var9 = var8 + Math.floorMod(var6 * 16 - var8, var7);

                for(int var10 = 0; var10 < this.sectionGridSizeY; ++var10) {
                    int var11 = this.level.getMinBuildHeight() + var10 * 16;
                    SectionRenderDispatcher.RenderSection var12 = this.sections[this.getSectionIndex(var2, var10, var6)];
                    BlockPos var13 = var12.getOrigin();
                    if (var5 != var13.getX() || var11 != var13.getY() || var9 != var13.getZ()) {
                        var12.setOrigin(var5, var11, var9);
                    }
                }
            }
        }

    }

    public void setDirty(int param0, int param1, int param2, boolean param3) {
        int var0 = Math.floorMod(param0, this.sectionGridSizeX);
        int var1 = Math.floorMod(param1 - this.level.getMinSection(), this.sectionGridSizeY);
        int var2 = Math.floorMod(param2, this.sectionGridSizeZ);
        SectionRenderDispatcher.RenderSection var3 = this.sections[this.getSectionIndex(var0, var1, var2)];
        var3.setDirty(param3);
    }

    @Nullable
    protected SectionRenderDispatcher.RenderSection getRenderSectionAt(BlockPos param0) {
        int var0 = Mth.floorDiv(param0.getY() - this.level.getMinBuildHeight(), 16);
        if (var0 >= 0 && var0 < this.sectionGridSizeY) {
            int var1 = Mth.positiveModulo(Mth.floorDiv(param0.getX(), 16), this.sectionGridSizeX);
            int var2 = Mth.positiveModulo(Mth.floorDiv(param0.getZ(), 16), this.sectionGridSizeZ);
            return this.sections[this.getSectionIndex(var1, var0, var2)];
        } else {
            return null;
        }
    }
}
