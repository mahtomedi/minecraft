package com.mojang.blaze3d.audio;

import java.nio.ByteBuffer;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.openal.AL10;

@OnlyIn(Dist.CLIENT)
public class SoundBuffer {
    @Nullable
    private ByteBuffer data;
    private final AudioFormat format;
    private boolean hasAlBuffer;
    private int alBuffer;

    public SoundBuffer(ByteBuffer param0, AudioFormat param1) {
        this.data = param0;
        this.format = param1;
    }

    OptionalInt getAlBuffer() {
        if (!this.hasAlBuffer) {
            if (this.data == null) {
                return OptionalInt.empty();
            }

            int var0 = OpenAlUtil.audioFormatToOpenAl(this.format);
            int[] var1 = new int[1];
            AL10.alGenBuffers(var1);
            if (OpenAlUtil.checkALError("Creating buffer")) {
                return OptionalInt.empty();
            }

            AL10.alBufferData(var1[0], var0, this.data, (int)this.format.getSampleRate());
            if (OpenAlUtil.checkALError("Assigning buffer data")) {
                return OptionalInt.empty();
            }

            this.alBuffer = var1[0];
            this.hasAlBuffer = true;
            this.data = null;
        }

        return OptionalInt.of(this.alBuffer);
    }

    public void discardAlBuffer() {
        if (this.hasAlBuffer) {
            AL10.alDeleteBuffers(new int[]{this.alBuffer});
            if (OpenAlUtil.checkALError("Deleting stream buffers")) {
                return;
            }
        }

        this.hasAlBuffer = false;
    }

    public OptionalInt releaseAlBuffer() {
        OptionalInt var0 = this.getAlBuffer();
        this.hasAlBuffer = false;
        return var0;
    }
}
