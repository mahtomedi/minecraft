package com.mojang.blaze3d.platform;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageARBCallback;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.KHRDebug;

@OnlyIn(Dist.CLIENT)
public class GlDebug {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<Integer> DEBUG_LEVELS = ImmutableList.of(37190, 37191, 37192, 33387);
    private static final List<Integer> DEBUG_LEVELS_ARB = ImmutableList.of(37190, 37191, 37192);

    private static String printUnknownToken(int param0) {
        return "Unknown (0x" + Integer.toHexString(param0).toUpperCase() + ")";
    }

    public static String sourceToString(int param0) {
        switch(param0) {
            case 33350:
                return "API";
            case 33351:
                return "WINDOW SYSTEM";
            case 33352:
                return "SHADER COMPILER";
            case 33353:
                return "THIRD PARTY";
            case 33354:
                return "APPLICATION";
            case 33355:
                return "OTHER";
            default:
                return printUnknownToken(param0);
        }
    }

    public static String typeToString(int param0) {
        switch(param0) {
            case 33356:
                return "ERROR";
            case 33357:
                return "DEPRECATED BEHAVIOR";
            case 33358:
                return "UNDEFINED BEHAVIOR";
            case 33359:
                return "PORTABILITY";
            case 33360:
                return "PERFORMANCE";
            case 33361:
                return "OTHER";
            case 33384:
                return "MARKER";
            default:
                return printUnknownToken(param0);
        }
    }

    public static String severityToString(int param0) {
        switch(param0) {
            case 33387:
                return "NOTIFICATION";
            case 37190:
                return "HIGH";
            case 37191:
                return "MEDIUM";
            case 37192:
                return "LOW";
            default:
                return printUnknownToken(param0);
        }
    }

    private static void printDebugLog(int param0, int param1, int param2, int param3, int param4, long param5, long param6) {
        LOGGER.info(
            "OpenGL debug message, id={}, source={}, type={}, severity={}, message={}",
            param2,
            sourceToString(param0),
            typeToString(param1),
            severityToString(param3),
            GLDebugMessageCallback.getMessage(param4, param5)
        );
    }

    public static void enableDebugCallback(int param0, boolean param1) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        if (param0 > 0) {
            GLCapabilities var0 = GL.getCapabilities();
            if (var0.GL_KHR_debug) {
                GL11.glEnable(37600);
                if (param1) {
                    GL11.glEnable(33346);
                }

                for(int var1 = 0; var1 < DEBUG_LEVELS.size(); ++var1) {
                    boolean var2 = var1 < param0;
                    KHRDebug.glDebugMessageControl(4352, 4352, DEBUG_LEVELS.get(var1), (int[])null, var2);
                }

                KHRDebug.glDebugMessageCallback(GLX.make(GLDebugMessageCallback.create(GlDebug::printDebugLog), DebugMemoryUntracker::untrack), 0L);
            } else if (var0.GL_ARB_debug_output) {
                if (param1) {
                    GL11.glEnable(33346);
                }

                for(int var3 = 0; var3 < DEBUG_LEVELS_ARB.size(); ++var3) {
                    boolean var4 = var3 < param0;
                    ARBDebugOutput.glDebugMessageControlARB(4352, 4352, DEBUG_LEVELS_ARB.get(var3), (int[])null, var4);
                }

                ARBDebugOutput.glDebugMessageCallbackARB(GLX.make(GLDebugMessageARBCallback.create(GlDebug::printDebugLog), DebugMemoryUntracker::untrack), 0L);
            }

        }
    }
}
