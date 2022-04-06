package net.minecraft.client.player;

import net.minecraft.client.Options;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyboardInput extends Input {
    private final Options options;

    public KeyboardInput(Options param0) {
        this.options = param0;
    }

    private static float calculateImpulse(boolean param0, boolean param1) {
        if (param0 == param1) {
            return 0.0F;
        } else {
            return param0 ? 1.0F : -1.0F;
        }
    }

    @Override
    public void tick(boolean param0, float param1) {
        this.up = this.options.keyUp.isDown();
        this.down = this.options.keyDown.isDown();
        this.left = this.options.keyLeft.isDown();
        this.right = this.options.keyRight.isDown();
        this.forwardImpulse = calculateImpulse(this.up, this.down);
        this.leftImpulse = calculateImpulse(this.left, this.right);
        this.jumping = this.options.keyJump.isDown();
        this.shiftKeyDown = this.options.keyShift.isDown();
        if (param0) {
            this.leftImpulse *= param1;
            this.forwardImpulse *= param1;
        }

    }
}
