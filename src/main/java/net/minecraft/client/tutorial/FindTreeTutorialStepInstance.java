package net.minecraft.client.tutorial;

import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FindTreeTutorialStepInstance implements TutorialStepInstance {
    private static final int HINT_DELAY = 6000;
    private static final Component TITLE = Component.translatable("tutorial.find_tree.title");
    private static final Component DESCRIPTION = Component.translatable("tutorial.find_tree.description");
    private final Tutorial tutorial;
    private TutorialToast toast;
    private int timeWaiting;

    public FindTreeTutorialStepInstance(Tutorial param0) {
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
                if (var0 != null && (hasCollectedTreeItems(var0) || hasPunchedTreesPreviously(var0))) {
                    this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
                    return;
                }
            }

            if (this.timeWaiting >= 6000 && this.toast == null) {
                this.toast = new TutorialToast(TutorialToast.Icons.TREE, TITLE, DESCRIPTION, false);
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
    public void onLookAt(ClientLevel param0, HitResult param1) {
        if (param1.getType() == HitResult.Type.BLOCK) {
            BlockState var0 = param0.getBlockState(((BlockHitResult)param1).getBlockPos());
            if (var0.is(BlockTags.COMPLETES_FIND_TREE_TUTORIAL)) {
                this.tutorial.setStep(TutorialSteps.PUNCH_TREE);
            }
        }

    }

    @Override
    public void onGetItem(ItemStack param0) {
        if (param0.is(ItemTags.COMPLETES_FIND_TREE_TUTORIAL)) {
            this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
        }

    }

    private static boolean hasCollectedTreeItems(LocalPlayer param0) {
        return param0.getInventory().hasAnyMatching(param0x -> param0x.is(ItemTags.COMPLETES_FIND_TREE_TUTORIAL));
    }

    public static boolean hasPunchedTreesPreviously(LocalPlayer param0) {
        for(Holder<Block> var0 : BuiltInRegistries.BLOCK.getTagOrEmpty(BlockTags.COMPLETES_FIND_TREE_TUTORIAL)) {
            Block var1 = var0.value();
            if (param0.getStats().getValue(Stats.BLOCK_MINED.get(var1)) > 0) {
                return true;
            }
        }

        return false;
    }
}
