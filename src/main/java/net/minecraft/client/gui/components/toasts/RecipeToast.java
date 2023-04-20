package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeToast implements Toast {
    private static final long DISPLAY_TIME = 5000L;
    private static final Component TITLE_TEXT = Component.translatable("recipe.toast.title");
    private static final Component DESCRIPTION_TEXT = Component.translatable("recipe.toast.description");
    private final List<Recipe<?>> recipes = Lists.newArrayList();
    private long lastChanged;
    private boolean changed;

    public RecipeToast(Recipe<?> param0) {
        this.recipes.add(param0);
    }

    @Override
    public Toast.Visibility render(GuiGraphics param0, ToastComponent param1, long param2) {
        if (this.changed) {
            this.lastChanged = param2;
            this.changed = false;
        }

        if (this.recipes.isEmpty()) {
            return Toast.Visibility.HIDE;
        } else {
            param0.blit(TEXTURE, 0, 0, 0, 32, this.width(), this.height());
            param0.drawString(param1.getMinecraft().font, TITLE_TEXT, 30, 7, -11534256, false);
            param0.drawString(param1.getMinecraft().font, DESCRIPTION_TEXT, 30, 18, -16777216, false);
            Recipe<?> var0 = this.recipes
                .get(
                    (int)(
                        (double)param2
                            / Math.max(1.0, 5000.0 * param1.getNotificationDisplayTimeMultiplier() / (double)this.recipes.size())
                            % (double)this.recipes.size()
                    )
                );
            ItemStack var1 = var0.getToastSymbol();
            param0.pose().pushPose();
            param0.pose().scale(0.6F, 0.6F, 1.0F);
            param0.renderFakeItem(var1, 3, 3);
            param0.pose().popPose();
            param0.renderFakeItem(var0.getResultItem(param1.getMinecraft().level.registryAccess()), 8, 8);
            return (double)(param2 - this.lastChanged) >= 5000.0 * param1.getNotificationDisplayTimeMultiplier()
                ? Toast.Visibility.HIDE
                : Toast.Visibility.SHOW;
        }
    }

    private void addItem(Recipe<?> param0) {
        this.recipes.add(param0);
        this.changed = true;
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
