package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.GameRenderer;
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
    public Toast.Visibility render(PoseStack param0, ToastComponent param1, long param2) {
        if (this.changed) {
            this.lastChanged = param2;
            this.changed = false;
        }

        if (this.recipes.isEmpty()) {
            return Toast.Visibility.HIDE;
        } else {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            param1.blit(param0, 0, 0, 0, 32, this.width(), this.height());
            param1.getMinecraft().font.draw(param0, TITLE_TEXT, 30.0F, 7.0F, -11534256);
            param1.getMinecraft().font.draw(param0, DESCRIPTION_TEXT, 30.0F, 18.0F, -16777216);
            Recipe<?> var0 = this.recipes.get((int)(param2 / Math.max(1L, 5000L / (long)this.recipes.size()) % (long)this.recipes.size()));
            ItemStack var1 = var0.getToastSymbol();
            PoseStack var2 = RenderSystem.getModelViewStack();
            var2.pushPose();
            var2.scale(0.6F, 0.6F, 1.0F);
            RenderSystem.applyModelViewMatrix();
            param1.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(var1, 3, 3);
            var2.popPose();
            RenderSystem.applyModelViewMatrix();
            param1.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(var0.getResultItem(), 8, 8);
            return param2 - this.lastChanged >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
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
