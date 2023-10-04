package com.mojang.blaze3d.audio;

import com.mojang.logging.LogUtils;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class OpenAlUtil {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static String alErrorToString(int param0) {
        switch(param0) {
            case 40961:
                return "Invalid name parameter.";
            case 40962:
                return "Invalid enumerated parameter value.";
            case 40963:
                return "Invalid parameter parameter value.";
            case 40964:
                return "Invalid operation.";
            case 40965:
                return "Unable to allocate memory.";
            default:
                return "An unrecognized error occurred.";
        }
    }

    static boolean checkALError(String param0) {
        int var0 = AL10.alGetError();
        if (var0 != 0) {
            LOGGER.error("{}: {}", param0, alErrorToString(var0));
            return true;
        } else {
            return false;
        }
    }

    private static String alcErrorToString(int param0) {
        switch(param0) {
            case 40961:
                return "Invalid device.";
            case 40962:
                return "Invalid context.";
            case 40963:
                return "Illegal enum.";
            case 40964:
                return "Invalid value.";
            case 40965:
                return "Unable to allocate memory.";
            default:
                return "An unrecognized error occurred.";
        }
    }

    static boolean checkALCError(long param0, String param1) {
        int var0 = ALC10.alcGetError(param0);
        if (var0 != 0) {
            LOGGER.error("{} ({}): {}", param1, param0, alcErrorToString(var0));
            return true;
        } else {
            return false;
        }
    }

    static int audioFormatToOpenAl(AudioFormat param0) {
        Encoding var0 = param0.getEncoding();
        int var1 = param0.getChannels();
        int var2 = param0.getSampleSizeInBits();
        if (var0.equals(Encoding.PCM_UNSIGNED) || var0.equals(Encoding.PCM_SIGNED)) {
            if (var1 == 1) {
                if (var2 == 8) {
                    return 4352;
                }

                if (var2 == 16) {
                    return 4353;
                }
            } else if (var1 == 2) {
                if (var2 == 8) {
                    return 4354;
                }

                if (var2 == 16) {
                    return 4355;
                }
            }
        }

        throw new IllegalArgumentException("Invalid audio format: " + param0);
    }
}
