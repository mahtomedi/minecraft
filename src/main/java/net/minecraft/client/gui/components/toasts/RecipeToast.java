package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeToast implements Toast {
    private final List<Recipe<?>> recipes = Lists.newArrayList();
    private long lastChanged;
    private boolean changed;

    public RecipeToast(Recipe<?> param0) {
        this.recipes.add(param0);
    }

    @Override
    public Toast.Visibility render(ToastComponent param0, long param1) {
        if (this.changed) {
            this.lastChanged = param1;
            this.changed = false;
        }

        if (this.recipes.isEmpty()) {
            return Toast.Visibility.HIDE;
        } else {
            param0.getMinecraft().getTextureManager().bind(TEXTURE);
            RenderSystem.color3f(1.0F, 1.0F, 1.0F);
            param0.blit(0, 0, 0, 32, 160, 32);
            param0.getMinecraft().font.draw(I18n.get("recipe.toast.title"), 30.0F, 7.0F, -11534256);
            param0.getMinecraft().font.draw(I18n.get("recipe.toast.description"), 30.0F, 18.0F, -16777216);
            Lighting.turnOnGui();
            Recipe<?> var0 = this.recipes.get((int)(param1 / (5000L / (long)this.recipes.size()) % (long)this.recipes.size()));
            ItemStack var1 = var0.getToastSymbol();
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.6F, 0.6F, 1.0F);
            param0.getMinecraft().getItemRenderer().renderAndDecorateItem(null, var1, 3, 3);
            RenderSystem.popMatrix();
            param0.getMinecraft().getItemRenderer().renderAndDecorateItem(null, var0.getResultItem(), 8, 8);
            return param1 - this.lastChanged >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        }
    }

    public void addItem(Recipe<?> param0) {
        if (this.recipes.add(param0)) {
            this.changed = true;
        }

    }

    public static void addOrUpdate(ToastComponent param0, Recipe<?> param1) {
        RecipeToast var0 = param0.getToast(RecipeToast.class, NO_TOKEN);
        if (var0 == null) {
            param0.addToast(new RecipeToast(param1));
        } else {
            var0.addItem(param1);
        }

    }
}
