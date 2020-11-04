package net.minecraft.client.tutorial;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FindTreeTutorialStepInstance implements TutorialStepInstance {
    private static final Set<Block> TREE_BLOCKS = Sets.newHashSet(
        Blocks.OAK_LOG,
        Blocks.SPRUCE_LOG,
        Blocks.BIRCH_LOG,
        Blocks.JUNGLE_LOG,
        Blocks.ACACIA_LOG,
        Blocks.DARK_OAK_LOG,
        Blocks.WARPED_STEM,
        Blocks.CRIMSON_STEM,
        Blocks.OAK_WOOD,
        Blocks.SPRUCE_WOOD,
        Blocks.BIRCH_WOOD,
        Blocks.JUNGLE_WOOD,
        Blocks.ACACIA_WOOD,
        Blocks.DARK_OAK_WOOD,
        Blocks.WARPED_HYPHAE,
        Blocks.CRIMSON_HYPHAE,
        Blocks.OAK_LEAVES,
        Blocks.SPRUCE_LEAVES,
        Blocks.BIRCH_LEAVES,
        Blocks.JUNGLE_LEAVES,
        Blocks.ACACIA_LEAVES,
        Blocks.DARK_OAK_LEAVES,
        Blocks.NETHER_WART_BLOCK,
        Blocks.WARPED_WART_BLOCK
    );
    private static final Component TITLE = new TranslatableComponent("tutorial.find_tree.title");
    private static final Component DESCRIPTION = new TranslatableComponent("tutorial.find_tree.description");
    private final Tutorial tutorial;
    private TutorialToast toast;
    private int timeWaiting;

    public FindTreeTutorialStepInstance(Tutorial param0) {
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
                    for(Block var1 : TREE_BLOCKS) {
                        if (var0.getInventory().contains(new ItemStack(var1))) {
                            this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
                            return;
                        }
                    }

                    if (hasPunchedTreesPreviously(var0)) {
                        this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
                        return;
                    }
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
            if (TREE_BLOCKS.contains(var0.getBlock())) {
                this.tutorial.setStep(TutorialSteps.PUNCH_TREE);
            }
        }

    }

    @Override
    public void onGetItem(ItemStack param0) {
        for(Block var0 : TREE_BLOCKS) {
            if (param0.is(var0.asItem())) {
                this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
                return;
            }
        }

    }

    public static boolean hasPunchedTreesPreviously(LocalPlayer param0) {
        for(Block var0 : TREE_BLOCKS) {
            if (param0.getStats().getValue(Stats.BLOCK_MINED.get(var0)) > 0) {
                return true;
            }
        }

        return false;
    }
}
