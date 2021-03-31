package net.minecraft.stats;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeBook {
    protected final Set<ResourceLocation> known = Sets.newHashSet();
    protected final Set<ResourceLocation> highlight = Sets.newHashSet();
    private final RecipeBookSettings bookSettings = new RecipeBookSettings();

    public void copyOverData(RecipeBook param0) {
        this.known.clear();
        this.highlight.clear();
        this.bookSettings.replaceFrom(param0.bookSettings);
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

    public void remove(Recipe<?> param0) {
        this.remove(param0.getId());
    }

    protected void remove(ResourceLocation param0) {
        this.known.remove(param0);
        this.highlight.remove(param0);
    }

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

    public boolean isOpen(RecipeBookType param0) {
        return this.bookSettings.isOpen(param0);
    }

    public void setOpen(RecipeBookType param0, boolean param1) {
        this.bookSettings.setOpen(param0, param1);
    }

    public boolean isFiltering(RecipeBookMenu<?> param0) {
        return this.isFiltering(param0.getRecipeBookType());
    }

    public boolean isFiltering(RecipeBookType param0) {
        return this.bookSettings.isFiltering(param0);
    }

    public void setFiltering(RecipeBookType param0, boolean param1) {
        this.bookSettings.setFiltering(param0, param1);
    }

    public void setBookSettings(RecipeBookSettings param0) {
        this.bookSettings.replaceFrom(param0);
    }

    public RecipeBookSettings getBookSettings() {
        return this.bookSettings.copy();
    }

    public void setBookSetting(RecipeBookType param0, boolean param1, boolean param2) {
        this.bookSettings.setOpen(param0, param1);
        this.bookSettings.setFiltering(param0, param2);
    }
}
