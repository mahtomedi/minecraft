package net.minecraft.client.tutorial;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
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

    public Tutorial(Minecraft param0) {
        this.minecraft = param0;
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

    public void onLookAt(@Nullable MultiPlayerLevel param0, @Nullable HitResult param1) {
        if (this.instance != null && param1 != null && param0 != null) {
            this.instance.onLookAt(param0, param1);
        }

    }

    public void onDestroyBlock(MultiPlayerLevel param0, BlockPos param1, BlockState param2, float param3) {
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

    public void tick() {
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

    public GameType getGameMode() {
        return this.minecraft.gameMode == null ? GameType.NOT_SET : this.minecraft.gameMode.getPlayerMode();
    }

    public static Component key(String param0) {
        return new KeybindComponent("key." + param0).withStyle(ChatFormatting.BOLD);
    }
}
