package net.minecraft.client.tutorial;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Tutorial {
    private final Minecraft minecraft;
    @Nullable
    private TutorialStepInstance instance;
    private final List<Tutorial.TimedToast> timedToasts = Lists.newArrayList();
    private final BundleTutorial bundleTutorial;

    public Tutorial(Minecraft param0, Options param1) {
        this.minecraft = param0;
        this.bundleTutorial = new BundleTutorial(this, param1);
    }

    public void onInput(Input param0) {
        if (this.instance != null) {
            this.instance.onInput(param0);
        }

    }

    public void onMouse(double param0, double param1) {
        if (this.instance != null) {
            this.instance.onMouse(param0, param1);
        }

    }

    public void onLookAt(@Nullable ClientLevel param0, @Nullable HitResult param1) {
        if (this.instance != null && param1 != null && param0 != null) {
            this.instance.onLookAt(param0, param1);
        }

    }

    public void onDestroyBlock(ClientLevel param0, BlockPos param1, BlockState param2, float param3) {
        if (this.instance != null) {
            this.instance.onDestroyBlock(param0, param1, param2, param3);
        }

    }

    public void onOpenInventory() {
        if (this.instance != null) {
            this.instance.onOpenInventory();
        }

    }

    public void onGetItem(ItemStack param0) {
        if (this.instance != null) {
            this.instance.onGetItem(param0);
        }

    }

    public void stop() {
        if (this.instance != null) {
            this.instance.clear();
            this.instance = null;
        }
    }

    public void start() {
        if (this.instance != null) {
            this.stop();
        }

        this.instance = this.minecraft.options.tutorialStep.create(this);
    }

    public void addTimedToast(TutorialToast param0, int param1) {
        this.timedToasts.add(new Tutorial.TimedToast(param0, param1));
        this.minecraft.getToasts().addToast(param0);
    }

    public void removeTimedToast(TutorialToast param0) {
        this.timedToasts.removeIf(param1 -> param1.toast == param0);
        param0.hide();
    }

    public void tick() {
        this.timedToasts.removeIf(param0 -> param0.updateProgress());
        if (this.instance != null) {
            if (this.minecraft.level != null) {
                this.instance.tick();
            } else {
                this.stop();
            }
        } else if (this.minecraft.level != null) {
            this.start();
        }

    }

    public void setStep(TutorialSteps param0) {
        this.minecraft.options.tutorialStep = param0;
        this.minecraft.options.save();
        if (this.instance != null) {
            this.instance.clear();
            this.instance = param0.create(this);
        }

    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    public boolean isSurvival() {
        if (this.minecraft.gameMode == null) {
            return false;
        } else {
            return this.minecraft.gameMode.getPlayerMode() == GameType.SURVIVAL;
        }
    }

    public static Component key(String param0) {
        return new KeybindComponent("key." + param0).withStyle(ChatFormatting.BOLD);
    }

    public void onInventoryAction(ItemStack param0, ItemStack param1, ClickAction param2) {
        this.bundleTutorial.onInventoryAction(param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    static final class TimedToast {
        private final TutorialToast toast;
        private final int durationTicks;
        private int progress;

        private TimedToast(TutorialToast param0, int param1) {
            this.toast = param0;
            this.durationTicks = param1;
        }

        private boolean updateProgress() {
            this.toast.updateProgress(Math.min((float)(++this.progress) / (float)this.durationTicks, 1.0F));
            if (this.progress > this.durationTicks) {
                this.toast.hide();
                return true;
            } else {
                return false;
            }
        }
    }
}
