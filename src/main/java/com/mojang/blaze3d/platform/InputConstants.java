package com.mojang.blaze3d.platform;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.LazyLoadedValue;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharModsCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWDropCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;

@OnlyIn(Dist.CLIENT)
public class InputConstants {
    @Nullable
    private static final MethodHandle GLFW_RAW_MOUSE_MOTION_SUPPORTED;
    private static final int GLFW_RAW_MOUSE_MOTION;
    public static final int KEY_0 = 48;
    public static final int KEY_1 = 49;
    public static final int KEY_2 = 50;
    public static final int KEY_3 = 51;
    public static final int KEY_4 = 52;
    public static final int KEY_5 = 53;
    public static final int KEY_6 = 54;
    public static final int KEY_7 = 55;
    public static final int KEY_8 = 56;
    public static final int KEY_9 = 57;
    public static final int KEY_A = 65;
    public static final int KEY_B = 66;
    public static final int KEY_C = 67;
    public static final int KEY_D = 68;
    public static final int KEY_E = 69;
    public static final int KEY_F = 70;
    public static final int KEY_G = 71;
    public static final int KEY_H = 72;
    public static final int KEY_I = 73;
    public static final int KEY_J = 74;
    public static final int KEY_K = 75;
    public static final int KEY_L = 76;
    public static final int KEY_M = 77;
    public static final int KEY_N = 78;
    public static final int KEY_O = 79;
    public static final int KEY_P = 80;
    public static final int KEY_Q = 81;
    public static final int KEY_R = 82;
    public static final int KEY_S = 83;
    public static final int KEY_T = 84;
    public static final int KEY_U = 85;
    public static final int KEY_V = 86;
    public static final int KEY_W = 87;
    public static final int KEY_X = 88;
    public static final int KEY_Y = 89;
    public static final int KEY_Z = 90;
    public static final int KEY_F1 = 290;
    public static final int KEY_F2 = 291;
    public static final int KEY_F3 = 292;
    public static final int KEY_F4 = 293;
    public static final int KEY_F5 = 294;
    public static final int KEY_F6 = 295;
    public static final int KEY_F7 = 296;
    public static final int KEY_F8 = 297;
    public static final int KEY_F9 = 298;
    public static final int KEY_F10 = 299;
    public static final int KEY_F11 = 300;
    public static final int KEY_F12 = 301;
    public static final int KEY_F13 = 302;
    public static final int KEY_F14 = 303;
    public static final int KEY_F15 = 304;
    public static final int KEY_F16 = 305;
    public static final int KEY_F17 = 306;
    public static final int KEY_F18 = 307;
    public static final int KEY_F19 = 308;
    public static final int KEY_F20 = 309;
    public static final int KEY_F21 = 310;
    public static final int KEY_F22 = 311;
    public static final int KEY_F23 = 312;
    public static final int KEY_F24 = 313;
    public static final int KEY_F25 = 314;
    public static final int KEY_NUMLOCK = 282;
    public static final int KEY_NUMPAD0 = 320;
    public static final int KEY_NUMPAD1 = 321;
    public static final int KEY_NUMPAD2 = 322;
    public static final int KEY_NUMPAD3 = 323;
    public static final int KEY_NUMPAD4 = 324;
    public static final int KEY_NUMPAD5 = 325;
    public static final int KEY_NUMPAD6 = 326;
    public static final int KEY_NUMPAD7 = 327;
    public static final int KEY_NUMPAD8 = 328;
    public static final int KEY_NUMPAD9 = 329;
    public static final int KEY_NUMPADCOMMA = 330;
    public static final int KEY_NUMPADENTER = 335;
    public static final int KEY_NUMPADEQUALS = 336;
    public static final int KEY_DOWN = 264;
    public static final int KEY_LEFT = 263;
    public static final int KEY_RIGHT = 262;
    public static final int KEY_UP = 265;
    public static final int KEY_ADD = 334;
    public static final int KEY_APOSTROPHE = 39;
    public static final int KEY_BACKSLASH = 92;
    public static final int KEY_COMMA = 44;
    public static final int KEY_EQUALS = 61;
    public static final int KEY_GRAVE = 96;
    public static final int KEY_LBRACKET = 91;
    public static final int KEY_MINUS = 45;
    public static final int KEY_MULTIPLY = 332;
    public static final int KEY_PERIOD = 46;
    public static final int KEY_RBRACKET = 93;
    public static final int KEY_SEMICOLON = 59;
    public static final int KEY_SLASH = 47;
    public static final int KEY_SPACE = 32;
    public static final int KEY_TAB = 258;
    public static final int KEY_LALT = 342;
    public static final int KEY_LCONTROL = 341;
    public static final int KEY_LSHIFT = 340;
    public static final int KEY_LWIN = 343;
    public static final int KEY_RALT = 346;
    public static final int KEY_RCONTROL = 345;
    public static final int KEY_RSHIFT = 344;
    public static final int KEY_RWIN = 347;
    public static final int KEY_RETURN = 257;
    public static final int KEY_ESCAPE = 256;
    public static final int KEY_BACKSPACE = 259;
    public static final int KEY_DELETE = 261;
    public static final int KEY_END = 269;
    public static final int KEY_HOME = 268;
    public static final int KEY_INSERT = 260;
    public static final int KEY_PAGEDOWN = 267;
    public static final int KEY_PAGEUP = 266;
    public static final int KEY_CAPSLOCK = 280;
    public static final int KEY_PAUSE = 284;
    public static final int KEY_SCROLLLOCK = 281;
    public static final int KEY_PRINTSCREEN = 283;
    public static final int PRESS = 1;
    public static final int RELEASE = 0;
    public static final int REPEAT = 2;
    public static final int MOUSE_BUTTON_LEFT = 0;
    public static final int MOUSE_BUTTON_MIDDLE = 2;
    public static final int MOUSE_BUTTON_RIGHT = 1;
    public static final int MOD_CONTROL = 2;
    public static final int CURSOR = 208897;
    public static final int CURSOR_DISABLED = 212995;
    public static final int CURSOR_NORMAL = 212993;
    public static final InputConstants.Key UNKNOWN;

    public static InputConstants.Key getKey(int param0, int param1) {
        return param0 == -1 ? InputConstants.Type.SCANCODE.getOrCreate(param1) : InputConstants.Type.KEYSYM.getOrCreate(param0);
    }

    public static InputConstants.Key getKey(String param0) {
        if (InputConstants.Key.NAME_MAP.containsKey(param0)) {
            return InputConstants.Key.NAME_MAP.get(param0);
        } else {
            for(InputConstants.Type var0 : InputConstants.Type.values()) {
                if (param0.startsWith(var0.defaultPrefix)) {
                    String var1 = param0.substring(var0.defaultPrefix.length() + 1);
                    return var0.getOrCreate(Integer.parseInt(var1));
                }
            }

            throw new IllegalArgumentException("Unknown key name: " + param0);
        }
    }

    public static boolean isKeyDown(long param0, int param1) {
        return GLFW.glfwGetKey(param0, param1) == 1;
    }

    public static void setupKeyboardCallbacks(long param0, GLFWKeyCallbackI param1, GLFWCharModsCallbackI param2) {
        GLFW.glfwSetKeyCallback(param0, param1);
        GLFW.glfwSetCharModsCallback(param0, param2);
    }

    public static void setupMouseCallbacks(
        long param0, GLFWCursorPosCallbackI param1, GLFWMouseButtonCallbackI param2, GLFWScrollCallbackI param3, GLFWDropCallbackI param4
    ) {
        GLFW.glfwSetCursorPosCallback(param0, param1);
        GLFW.glfwSetMouseButtonCallback(param0, param2);
        GLFW.glfwSetScrollCallback(param0, param3);
        GLFW.glfwSetDropCallback(param0, param4);
    }

    public static void grabOrReleaseMouse(long param0, int param1, double param2, double param3) {
        GLFW.glfwSetCursorPos(param0, param2, param3);
        GLFW.glfwSetInputMode(param0, 208897, param1);
    }

    public static boolean isRawMouseInputSupported() {
        try {
            return GLFW_RAW_MOUSE_MOTION_SUPPORTED != null && (boolean)GLFW_RAW_MOUSE_MOTION_SUPPORTED.invokeExact();
        } catch (Throwable var1) {
            throw new RuntimeException(var1);
        }
    }

    public static void updateRawMouseInput(long param0, boolean param1) {
        if (isRawMouseInputSupported()) {
            GLFW.glfwSetInputMode(param0, GLFW_RAW_MOUSE_MOTION, param1 ? 1 : 0);
        }

    }

    static {
        Lookup var0 = MethodHandles.lookup();
        MethodType var1 = MethodType.methodType(Boolean.TYPE);
        MethodHandle var2 = null;
        int var3 = 0;

        try {
            var2 = var0.findStatic(GLFW.class, "glfwRawMouseMotionSupported", var1);
            MethodHandle var4 = var0.findStaticGetter(GLFW.class, "GLFW_RAW_MOUSE_MOTION", Integer.TYPE);
            var3 = (int)var4.invokeExact();
        } catch (NoSuchFieldException | NoSuchMethodException var51) {
        } catch (Throwable var6) {
            throw new RuntimeException(var6);
        }

        GLFW_RAW_MOUSE_MOTION_SUPPORTED = var2;
        GLFW_RAW_MOUSE_MOTION = var3;
        UNKNOWN = InputConstants.Type.KEYSYM.getOrCreate(-1);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Key {
        private final String name;
        private final InputConstants.Type type;
        private final int value;
        private final LazyLoadedValue<Component> displayName;
        private static final Map<String, InputConstants.Key> NAME_MAP = Maps.newHashMap();

        private Key(String param0, InputConstants.Type param1, int param2) {
            this.name = param0;
            this.type = param1;
            this.value = param2;
            this.displayName = new LazyLoadedValue<>(() -> param1.displayTextSupplier.apply(param2, param0));
            NAME_MAP.put(param0, this);
        }

        public InputConstants.Type getType() {
            return this.type;
        }

        public int getValue() {
            return this.value;
        }

        public String getName() {
            return this.name;
        }

        public Component getDisplayName() {
            return this.displayName.get();
        }

        public OptionalInt getNumericKeyValue() {
            if (this.value >= 48 && this.value <= 57) {
                return OptionalInt.of(this.value - 48);
            } else {
                return this.value >= 320 && this.value <= 329 ? OptionalInt.of(this.value - 320) : OptionalInt.empty();
            }
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                InputConstants.Key var0 = (InputConstants.Key)param0;
                return this.value == var0.value && this.type == var0.type;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.type, this.value);
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Type {
        KEYSYM("key.keyboard", (param0, param1) -> {
            String var0 = GLFW.glfwGetKeyName(param0, -1);
            return (Component)(var0 != null ? new TextComponent(var0) : new TranslatableComponent(param1));
        }),
        SCANCODE("scancode", (param0, param1) -> {
            String var0 = GLFW.glfwGetKeyName(-1, param0);
            return (Component)(var0 != null ? new TextComponent(var0) : new TranslatableComponent(param1));
        }),
        MOUSE(
            "key.mouse",
            (param0, param1) -> Language.getInstance().has(param1) ? new TranslatableComponent(param1) : new TranslatableComponent("key.mouse", param0 + 1)
        );

        private final Int2ObjectMap<InputConstants.Key> map = new Int2ObjectOpenHashMap<>();
        private final String defaultPrefix;
        private final BiFunction<Integer, String, Component> displayTextSupplier;

        private static void addKey(InputConstants.Type param0, String param1, int param2) {
            InputConstants.Key var0 = new InputConstants.Key(param1, param0, param2);
            param0.map.put(param2, var0);
        }

        private Type(String param0, BiFunction<Integer, String, Component> param1) {
            this.defaultPrefix = param0;
            this.displayTextSupplier = param1;
        }

        public InputConstants.Key getOrCreate(int param0) {
            return this.map.computeIfAbsent(param0, param0x -> {
                int var0 = param0x;
                if (this == MOUSE) {
                    var0 = param0x + 1;
                }

                String var1x = this.defaultPrefix + "." + var0;
                return new InputConstants.Key(var1x, this, param0x);
            });
        }

        static {
            addKey(KEYSYM, "key.keyboard.unknown", -1);
            addKey(MOUSE, "key.mouse.left", 0);
            addKey(MOUSE, "key.mouse.right", 1);
            addKey(MOUSE, "key.mouse.middle", 2);
            addKey(MOUSE, "key.mouse.4", 3);
            addKey(MOUSE, "key.mouse.5", 4);
            addKey(MOUSE, "key.mouse.6", 5);
            addKey(MOUSE, "key.mouse.7", 6);
            addKey(MOUSE, "key.mouse.8", 7);
            addKey(KEYSYM, "key.keyboard.0", 48);
            addKey(KEYSYM, "key.keyboard.1", 49);
            addKey(KEYSYM, "key.keyboard.2", 50);
            addKey(KEYSYM, "key.keyboard.3", 51);
            addKey(KEYSYM, "key.keyboard.4", 52);
            addKey(KEYSYM, "key.keyboard.5", 53);
            addKey(KEYSYM, "key.keyboard.6", 54);
            addKey(KEYSYM, "key.keyboard.7", 55);
            addKey(KEYSYM, "key.keyboard.8", 56);
            addKey(KEYSYM, "key.keyboard.9", 57);
            addKey(KEYSYM, "key.keyboard.a", 65);
            addKey(KEYSYM, "key.keyboard.b", 66);
            addKey(KEYSYM, "key.keyboard.c", 67);
            addKey(KEYSYM, "key.keyboard.d", 68);
            addKey(KEYSYM, "key.keyboard.e", 69);
            addKey(KEYSYM, "key.keyboard.f", 70);
            addKey(KEYSYM, "key.keyboard.g", 71);
            addKey(KEYSYM, "key.keyboard.h", 72);
            addKey(KEYSYM, "key.keyboard.i", 73);
            addKey(KEYSYM, "key.keyboard.j", 74);
            addKey(KEYSYM, "key.keyboard.k", 75);
            addKey(KEYSYM, "key.keyboard.l", 76);
            addKey(KEYSYM, "key.keyboard.m", 77);
            addKey(KEYSYM, "key.keyboard.n", 78);
            addKey(KEYSYM, "key.keyboard.o", 79);
            addKey(KEYSYM, "key.keyboard.p", 80);
            addKey(KEYSYM, "key.keyboard.q", 81);
            addKey(KEYSYM, "key.keyboard.r", 82);
            addKey(KEYSYM, "key.keyboard.s", 83);
            addKey(KEYSYM, "key.keyboard.t", 84);
            addKey(KEYSYM, "key.keyboard.u", 85);
            addKey(KEYSYM, "key.keyboard.v", 86);
            addKey(KEYSYM, "key.keyboard.w", 87);
            addKey(KEYSYM, "key.keyboard.x", 88);
            addKey(KEYSYM, "key.keyboard.y", 89);
            addKey(KEYSYM, "key.keyboard.z", 90);
            addKey(KEYSYM, "key.keyboard.f1", 290);
            addKey(KEYSYM, "key.keyboard.f2", 291);
            addKey(KEYSYM, "key.keyboard.f3", 292);
            addKey(KEYSYM, "key.keyboard.f4", 293);
            addKey(KEYSYM, "key.keyboard.f5", 294);
            addKey(KEYSYM, "key.keyboard.f6", 295);
            addKey(KEYSYM, "key.keyboard.f7", 296);
            addKey(KEYSYM, "key.keyboard.f8", 297);
            addKey(KEYSYM, "key.keyboard.f9", 298);
            addKey(KEYSYM, "key.keyboard.f10", 299);
            addKey(KEYSYM, "key.keyboard.f11", 300);
            addKey(KEYSYM, "key.keyboard.f12", 301);
            addKey(KEYSYM, "key.keyboard.f13", 302);
            addKey(KEYSYM, "key.keyboard.f14", 303);
            addKey(KEYSYM, "key.keyboard.f15", 304);
            addKey(KEYSYM, "key.keyboard.f16", 305);
            addKey(KEYSYM, "key.keyboard.f17", 306);
            addKey(KEYSYM, "key.keyboard.f18", 307);
            addKey(KEYSYM, "key.keyboard.f19", 308);
            addKey(KEYSYM, "key.keyboard.f20", 309);
            addKey(KEYSYM, "key.keyboard.f21", 310);
            addKey(KEYSYM, "key.keyboard.f22", 311);
            addKey(KEYSYM, "key.keyboard.f23", 312);
            addKey(KEYSYM, "key.keyboard.f24", 313);
            addKey(KEYSYM, "key.keyboard.f25", 314);
            addKey(KEYSYM, "key.keyboard.num.lock", 282);
            addKey(KEYSYM, "key.keyboard.keypad.0", 320);
            addKey(KEYSYM, "key.keyboard.keypad.1", 321);
            addKey(KEYSYM, "key.keyboard.keypad.2", 322);
            addKey(KEYSYM, "key.keyboard.keypad.3", 323);
            addKey(KEYSYM, "key.keyboard.keypad.4", 324);
            addKey(KEYSYM, "key.keyboard.keypad.5", 325);
            addKey(KEYSYM, "key.keyboard.keypad.6", 326);
            addKey(KEYSYM, "key.keyboard.keypad.7", 327);
            addKey(KEYSYM, "key.keyboard.keypad.8", 328);
            addKey(KEYSYM, "key.keyboard.keypad.9", 329);
            addKey(KEYSYM, "key.keyboard.keypad.add", 334);
            addKey(KEYSYM, "key.keyboard.keypad.decimal", 330);
            addKey(KEYSYM, "key.keyboard.keypad.enter", 335);
            addKey(KEYSYM, "key.keyboard.keypad.equal", 336);
            addKey(KEYSYM, "key.keyboard.keypad.multiply", 332);
            addKey(KEYSYM, "key.keyboard.keypad.divide", 331);
            addKey(KEYSYM, "key.keyboard.keypad.subtract", 333);
            addKey(KEYSYM, "key.keyboard.down", 264);
            addKey(KEYSYM, "key.keyboard.left", 263);
            addKey(KEYSYM, "key.keyboard.right", 262);
            addKey(KEYSYM, "key.keyboard.up", 265);
            addKey(KEYSYM, "key.keyboard.apostrophe", 39);
            addKey(KEYSYM, "key.keyboard.backslash", 92);
            addKey(KEYSYM, "key.keyboard.comma", 44);
            addKey(KEYSYM, "key.keyboard.equal", 61);
            addKey(KEYSYM, "key.keyboard.grave.accent", 96);
            addKey(KEYSYM, "key.keyboard.left.bracket", 91);
            addKey(KEYSYM, "key.keyboard.minus", 45);
            addKey(KEYSYM, "key.keyboard.period", 46);
            addKey(KEYSYM, "key.keyboard.right.bracket", 93);
            addKey(KEYSYM, "key.keyboard.semicolon", 59);
            addKey(KEYSYM, "key.keyboard.slash", 47);
            addKey(KEYSYM, "key.keyboard.space", 32);
            addKey(KEYSYM, "key.keyboard.tab", 258);
            addKey(KEYSYM, "key.keyboard.left.alt", 342);
            addKey(KEYSYM, "key.keyboard.left.control", 341);
            addKey(KEYSYM, "key.keyboard.left.shift", 340);
            addKey(KEYSYM, "key.keyboard.left.win", 343);
            addKey(KEYSYM, "key.keyboard.right.alt", 346);
            addKey(KEYSYM, "key.keyboard.right.control", 345);
            addKey(KEYSYM, "key.keyboard.right.shift", 344);
            addKey(KEYSYM, "key.keyboard.right.win", 347);
            addKey(KEYSYM, "key.keyboard.enter", 257);
            addKey(KEYSYM, "key.keyboard.escape", 256);
            addKey(KEYSYM, "key.keyboard.backspace", 259);
            addKey(KEYSYM, "key.keyboard.delete", 261);
            addKey(KEYSYM, "key.keyboard.end", 269);
            addKey(KEYSYM, "key.keyboard.home", 268);
            addKey(KEYSYM, "key.keyboard.insert", 260);
            addKey(KEYSYM, "key.keyboard.page.down", 267);
            addKey(KEYSYM, "key.keyboard.page.up", 266);
            addKey(KEYSYM, "key.keyboard.caps.lock", 280);
            addKey(KEYSYM, "key.keyboard.pause", 284);
            addKey(KEYSYM, "key.keyboard.scroll.lock", 281);
            addKey(KEYSYM, "key.keyboard.menu", 348);
            addKey(KEYSYM, "key.keyboard.print.screen", 283);
            addKey(KEYSYM, "key.keyboard.world.1", 161);
            addKey(KEYSYM, "key.keyboard.world.2", 162);
        }
    }
}
