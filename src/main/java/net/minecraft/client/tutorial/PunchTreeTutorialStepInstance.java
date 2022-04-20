package net.minecraft.client.tutorial;

import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PunchTreeTutorialStepInstance implements TutorialStepInstance {
    private static final int HINT_DELAY = 600;
    private static final Component TITLE = Component.translatable("tutorial.punch_tree.title");
    private static final Component DESCRIPTION = Component.translatable("tutorial.punch_tree.description", Tutorial.key("attack"));
    private final Tutorial tutorial;
    private TutorialToast toast;
    private int timeWaiting;
    private int resetCount;

    public PunchTreeTutorialStepInstance(Tutorial param0) {
        this.tutorial = param0;
    }

    @Override
    public void tick() {
        ++this.timeWaiting;
        if (!this.tutorial.isSurvival()) {
            this.tutorial.setStep(TutorialSteps.NONE);
        } else {
            if (this.timeWaiting == 1) {
                LocalPlayer var0 = this.tutorial.getMinecraft().player;
                if (var0 != null) {
                    if (var0.getInventory().contains(ItemTags.LOGS)) {
                        this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
                        return;
                    }

                    if (FindTreeTutorialStepInstance.hasPunchedTreesPreviously(var0)) {
                        this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
                        return;
                    }
                }
            }

            if ((this.timeWaiting >= 600 || this.resetCount > 3) && this.toast == null) {
                this.toast = new TutorialToast(TutorialToast.Icons.TREE, TITLE, DESCRIPTION, true);
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
    public void onDestroyBlock(ClientLevel param0, BlockPos param1, BlockState param2, float param3) {
        boolean var0 = param2.is(BlockTags.LOGS);
        if (var0 && param3 > 0.0F) {
            if (this.toast != null) {
                this.toast.updateProgress(param3);
            }

            if (param3 >= 1.0F) {
                this.tutorial.setStep(TutorialSteps.OPEN_INVENTORY);
            }
        } else if (this.toast != null) {
            this.toast.updateProgress(0.0F);
        } else if (var0) {
            ++this.resetCount;
        }

    }

    @Override
    public void onGetItem(ItemStack param0) {
        if (param0.is(ItemTags.LOGS)) {
            this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
        }
    }
}
