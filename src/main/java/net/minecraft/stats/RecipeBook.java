package net.minecraft.stats;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RecipeBook {
    protected final Set<ResourceLocation> known = Sets.newHashSet();
    protected final Set<ResourceLocation> highlight = Sets.newHashSet();
    protected boolean guiOpen;
    protected boolean filteringCraftable;
    protected boolean furnaceGuiOpen;
    protected boolean furnaceFilteringCraftable;
    protected boolean blastingFurnaceGuiOpen;
    protected boolean blastingFurnaceFilteringCraftable;
    protected boolean smokerGuiOpen;
    protected boolean smokerFilteringCraftable;

    public void copyOverData(RecipeBook param0) {
        this.known.clear();
        this.highlight.clear();
        this.guiOpen = param0.guiOpen;
        this.filteringCraftable = param0.filteringCraftable;
        this.furnaceGuiOpen = param0.furnaceGuiOpen;
        this.furnaceFilteringCraftable = param0.furnaceFilteringCraftable;
        this.blastingFurnaceGuiOpen = param0.blastingFurnaceGuiOpen;
        this.blastingFurnaceFilteringCraftable = param0.blastingFurnaceFilteringCraftable;
        this.smokerGuiOpen = param0.smokerGuiOpen;
        this.smokerFilteringCraftable = param0.smokerFilteringCraftable;
        this.known.addAll(param0.known);
        this.highlight.addAll(param0.highlight);
    }

    public void add(Recipe<?> param0) {
        if (!param0.isSpecial()) {
            this.add(param0.getId());
        }

    }

    protected void add(ResourceLocation param0) {
        this.known.add(param0);
    }

    public boolean contains(@Nullable Recipe<?> param0) {
        return param0 == null ? false : this.known.contains(param0.getId());
    }

    public boolean contains(ResourceLocation param0) {
        return this.known.contains(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public void remove(Recipe<?> param0) {
        this.remove(param0.getId());
    }

    protected void remove(ResourceLocation param0) {
        this.known.remove(param0);
        this.highlight.remove(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean willHighlight(Recipe<?> param0) {
        return this.highlight.contains(param0.getId());
    }

    public void removeHighlight(Recipe<?> param0) {
        this.highlight.remove(param0.getId());
    }

    public void addHighlight(Recipe<?> param0) {
        this.addHighlight(param0.getId());
    }

    protected void addHighlight(ResourceLocation param0) {
        this.highlight.add(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isGuiOpen() {
        return this.guiOpen;
    }

    public void setGuiOpen(boolean param0) {
        this.guiOpen = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFilteringCraftable(RecipeBookMenu<?> param0) {
        if (param0 instanceof FurnaceMenu) {
            return this.furnaceFilteringCraftable;
        } else if (param0 instanceof BlastFurnaceMenu) {
            return this.blastingFurnaceFilteringCraftable;
        } else {
            return param0 instanceof SmokerMenu ? this.smokerFilteringCraftable : this.filteringCraftable;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFilteringCraftable() {
        return this.filteringCraftable;
    }

    public void setFilteringCraftable(boolean param0) {
        this.filteringCraftable = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFurnaceGuiOpen() {
        return this.furnaceGuiOpen;
    }

    public void setFurnaceGuiOpen(boolean param0) {
        this.furnaceGuiOpen = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFurnaceFilteringCraftable() {
        return this.furnaceFilteringCraftable;
    }

    public void setFurnaceFilteringCraftable(boolean param0) {
        this.furnaceFilteringCraftable = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isBlastingFurnaceGuiOpen() {
        return this.blastingFurnaceGuiOpen;
    }

    public void setBlastingFurnaceGuiOpen(boolean param0) {
        this.blastingFurnaceGuiOpen = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isBlastingFurnaceFilteringCraftable() {
        return this.blastingFurnaceFilteringCraftable;
    }

    public void setBlastingFurnaceFilteringCraftable(boolean param0) {
        this.blastingFurnaceFilteringCraftable = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isSmokerGuiOpen() {
        return this.smokerGuiOpen;
    }

    public void setSmokerGuiOpen(boolean param0) {
        this.smokerGuiOpen = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isSmokerFilteringCraftable() {
        return this.smokerFilteringCraftable;
    }

    public void setSmokerFilteringCraftable(boolean param0) {
        this.smokerFilteringCraftable = param0;
    }
}
