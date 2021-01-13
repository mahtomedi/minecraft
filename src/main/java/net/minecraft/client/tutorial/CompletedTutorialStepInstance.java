package net.minecraft.client.tutorial;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CompletedTutorialStepInstance implements TutorialStepInstance {
    private final Tutorial tutorial;

    public CompletedTutorialStepInstance(Tutorial param0) {
        this.tutorial = param0;
    }
}
