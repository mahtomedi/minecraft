package com.mojang.realmsclient;

import java.util.Arrays;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyCombo {
    private final char[] chars;
    private int matchIndex;
    private final Runnable onCompletion;

    public KeyCombo(char[] param0, Runnable param1) {
        this.onCompletion = param1;
        if (param0.length < 1) {
            throw new IllegalArgumentException("Must have at least one char");
        } else {
            this.chars = param0;
        }
    }

    public KeyCombo(char[] param0) {
        this(param0, () -> {
        });
    }

    public boolean keyPressed(char param0) {
        if (param0 == this.chars[this.matchIndex++]) {
            if (this.matchIndex == this.chars.length) {
                this.reset();
                this.onCompletion.run();
                return true;
            }
        } else {
            this.reset();
        }

        return false;
    }

    public void reset() {
        this.matchIndex = 0;
    }

    @Override
    public String toString() {
        return "KeyCombo{chars=" + Arrays.toString(this.chars) + ", matchIndex=" + this.matchIndex + '}';
    }
}
