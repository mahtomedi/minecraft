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

    @Override
    public void tick(boolean param0, boolean param1) {
        this.up = this.options.keyUp.isDown();
        this.down = this.options.keyDown.isDown();
        this.left = this.options.keyLeft.isDown();
        this.right = this.options.keyRight.isDown();
        this.forwardImpulse = this.up == this.down ? 0.0F : (float)(this.up ? 1 : -1);
        this.leftImpulse = this.left == this.right ? 0.0F : (float)(this.left ? 1 : -1);
        this.jumping = this.options.keyJump.isDown();
        this.sneakKeyDown = this.options.keySneak.isDown();
        if (!param1 && (this.sneakKeyDown || param0)) {
            this.leftImpulse = (float)((double)this.leftImpulse * 0.3);
            this.forwardImpulse = (float)((double)this.forwardImpulse * 0.3);
        }

    }
}
