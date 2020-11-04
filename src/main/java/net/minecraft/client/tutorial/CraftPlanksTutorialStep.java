package net.minecraft.client.tutorial;

import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CraftPlanksTutorialStep implements TutorialStepInstance {
    private static final Component CRAFT_TITLE = new TranslatableComponent("tutorial.craft_planks.title");
    private static final Component CRAFT_DESCRIPTION = new TranslatableComponent("tutorial.craft_planks.description");
    private final Tutorial tutorial;
    private TutorialToast toast;
    private int timeWaiting;

    public CraftPlanksTutorialStep(Tutorial param0) {
        this.tutorial = param0;
    }

    @Override
    public void tick() {
        ++this.timeWaiting;
        if (this.tutorial.getGameMode() != GameType.SURVIVAL) {
            this.tutorial.setStep(TutorialSteps.NONE);
        } else {
            if (this.timeWaiting == 1) {
                LocalPlayer var0 = this.tutorial.getMinecraft().player;
                if (var0 != null) {
                    if (var0.getInventory().contains(ItemTags.PLANKS)) {
                        this.tutorial.setStep(TutorialSteps.NONE);
                        return;
                    }

                    if (hasCraftedPlanksPreviously(var0, ItemTags.PLANKS)) {
                        this.tutorial.setStep(TutorialSteps.NONE);
                        return;
                    }
                }
            }

            if (this.timeWaiting >= 1200 && this.toast == null) {
                this.toast = new TutorialToast(TutorialToast.Icons.WOODEN_PLANKS, CRAFT_TITLE, CRAFT_DESCRIPTION, false);
                this.tutorial.getMinecraft().getToasts().addToast(this.toast);
            }

        }
    }

    @Override
    public void clear() {
        if (this.toast != null) {
            this.toast.hide();
            this.toast = null;
        }

    }

    @Override
    public void onGetItem(ItemStack param0) {
        if (param0.is(ItemTags.PLANKS)) {
            this.tutorial.setStep(TutorialSteps.NONE);
        }

    }

    public static boolean hasCraftedPlanksPreviously(LocalPlayer param0, Tag<Item> param1) {
        for(Item var0 : param1.getValues()) {
            if (param0.getStats().getValue(Stats.ITEM_CRAFTED.get(var0)) > 0) {
                return true;
            }
        }

        return false;
    }
}
