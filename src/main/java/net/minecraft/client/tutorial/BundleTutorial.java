package net.minecraft.client.tutorial;

import javax.annotation.Nullable;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BundleTutorial {
    private final Tutorial tutorial;
    private final Options options;
    @Nullable
    private TutorialToast toast;

    public BundleTutorial(Tutorial param0, Options param1) {
        this.tutorial = param0;
        this.options = param1;
    }

    private void showToast() {
        if (this.toast != null) {
            this.tutorial.removeTimedToast(this.toast);
        }

        Component var0 = Component.translatable("tutorial.bundleInsert.title");
        Component var1 = Component.translatable("tutorial.bundleInsert.description");
        this.toast = new TutorialToast(TutorialToast.Icons.RIGHT_CLICK, var0, var1, true);
        this.tutorial.addTimedToast(this.toast, 160);
    }

    private void clearToast() {
        if (this.toast != null) {
            this.tutorial.removeTimedToast(this.toast);
            this.toast = null;
        }

        if (!this.options.hideBundleTutorial) {
            this.options.hideBundleTutorial = true;
            this.options.save();
        }

    }

    public void onInventoryAction(ItemStack param0, ItemStack param1, ClickAction param2) {
        if (!this.options.hideBundleTutorial) {
            if (!param0.isEmpty() && param1.is(Items.BUNDLE)) {
                if (param2 == ClickAction.PRIMARY) {
                    this.showToast();
                } else if (param2 == ClickAction.SECONDARY) {
                    this.clearToast();
                }
            } else if (param0.is(Items.BUNDLE) && !param1.isEmpty() && param2 == ClickAction.SECONDARY) {
                this.clearToast();
            }

        }
    }
}
