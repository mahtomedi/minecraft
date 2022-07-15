package com.mojang.blaze3d.platform;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVidMode.Buffer;

@OnlyIn(Dist.CLIENT)
public final class VideoMode {
    private final int width;
    private final int height;
    private final int redBits;
    private final int greenBits;
    private final int blueBits;
    private final int refreshRate;
    private static final Pattern PATTERN = Pattern.compile("(\\d+)x(\\d+)(?:@(\\d+)(?::(\\d+))?)?");

    public VideoMode(int param0, int param1, int param2, int param3, int param4, int param5) {
        this.width = param0;
        this.height = param1;
        this.redBits = param2;
        this.greenBits = param3;
        this.blueBits = param4;
        this.refreshRate = param5;
    }

    public VideoMode(Buffer param0) {
        this.width = param0.width();
        this.height = param0.height();
        this.redBits = param0.redBits();
        this.greenBits = param0.greenBits();
        this.blueBits = param0.blueBits();
        this.refreshRate = param0.refreshRate();
    }

    public VideoMode(GLFWVidMode param0) {
        this.width = param0.width();
        this.height = param0.height();
        this.redBits = param0.redBits();
        this.greenBits = param0.greenBits();
        this.blueBits = param0.blueBits();
        this.refreshRate = param0.refreshRate();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getRedBits() {
        return this.redBits;
    }

    public int getGreenBits() {
        return this.greenBits;
    }

    public int getBlueBits() {
        return this.blueBits;
    }

    public int getRefreshRate() {
        return this.refreshRate;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            VideoMode var0 = (VideoMode)param0;
            return this.width == var0.width
                && this.height == var0.height
                && this.redBits == var0.redBits
                && this.greenBits == var0.greenBits
                && this.blueBits == var0.blueBits
                && this.refreshRate == var0.refreshRate;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.width, this.height, this.redBits, this.greenBits, this.blueBits, this.refreshRate);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%sx%s@%s (%sbit)", this.width, this.height, this.refreshRate, this.redBits + this.greenBits + this.blueBits);
    }

    public static Optional<VideoMode> read(@Nullable String param0) {
        if (param0 == null) {
            return Optional.empty();
        } else {
            try {
                Matcher var0 = PATTERN.matcher(param0);
                if (var0.matches()) {
                    int var1 = Integer.parseInt(var0.group(1));
                    int var2 = Integer.parseInt(var0.group(2));
                    String var3 = var0.group(3);
                    int var4;
                    if (var3 == null) {
                        var4 = 60;
                    } else {
                        var4 = Integer.parseInt(var3);
                    }

                    String var6 = var0.group(4);
                    int var7;
                    if (var6 == null) {
                        var7 = 24;
                    } else {
                        var7 = Integer.parseInt(var6);
                    }

                    int var9 = var7 / 3;
                    return Optional.of(new VideoMode(var1, var2, var9, var9, var9, var4));
                }
            } catch (Exception var91) {
            }

            return Optional.empty();
        }
    }

    public String write() {
        return String.format(Locale.ROOT, "%sx%s@%s:%s", this.width, this.height, this.refreshRate, this.redBits + this.greenBits + this.blueBits);
    }
}
