package net.minecraft.client;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyMapping implements Comparable<KeyMapping> {
    private static final Map<String, KeyMapping> ALL = Maps.newHashMap();
    private static final Map<InputConstants.Key, KeyMapping> MAP = Maps.newHashMap();
    private static final Set<String> CATEGORIES = Sets.newHashSet();
    public static final String CATEGORY_MOVEMENT = "key.categories.movement";
    public static final String CATEGORY_MISC = "key.categories.misc";
    public static final String CATEGORY_MULTIPLAYER = "key.categories.multiplayer";
    public static final String CATEGORY_GAMEPLAY = "key.categories.gameplay";
    public static final String CATEGORY_INVENTORY = "key.categories.inventory";
    public static final String CATEGORY_INTERFACE = "key.categories.ui";
    public static final String CATEGORY_CREATIVE = "key.categories.creative";
    private static final Map<String, Integer> CATEGORY_SORT_ORDER = Util.make(Maps.newHashMap(), param0 -> {
        param0.put("key.categories.movement", 1);
        param0.put("key.categories.gameplay", 2);
        param0.put("key.categories.inventory", 3);
        param0.put("key.categories.creative", 4);
        param0.put("key.categories.multiplayer", 5);
        param0.put("key.categories.ui", 6);
        param0.put("key.categories.misc", 7);
    });
    private final String name;
    private final InputConstants.Key defaultKey;
    private final String category;
    private InputConstants.Key key;
    private boolean isDown;
    private int clickCount;

    public static void click(InputConstants.Key param0) {
        KeyMapping var0 = MAP.get(param0);
        if (var0 != null) {
            ++var0.clickCount;
        }

    }

    public static void set(InputConstants.Key param0, boolean param1) {
        KeyMapping var0 = MAP.get(param0);
        if (var0 != null) {
            var0.setDown(param1);
        }

    }

    public static void setAll() {
        for(KeyMapping var0 : ALL.values()) {
            if (var0.key.getType() == InputConstants.Type.KEYSYM && var0.key.getValue() != InputConstants.UNKNOWN.getValue()) {
                var0.setDown(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), var0.key.getValue()));
            }
        }

    }

    public static void releaseAll() {
        for(KeyMapping var0 : ALL.values()) {
            var0.release();
        }

    }

    public static void resetMapping() {
        MAP.clear();

        for(KeyMapping var0 : ALL.values()) {
            MAP.put(var0.key, var0);
        }

    }

    public KeyMapping(String param0, int param1, String param2) {
        this(param0, InputConstants.Type.KEYSYM, param1, param2);
    }

    public KeyMapping(String param0, InputConstants.Type param1, int param2, String param3) {
        this.name = param0;
        this.key = param1.getOrCreate(param2);
        this.defaultKey = this.key;
        this.category = param3;
        ALL.put(param0, this);
        MAP.put(this.key, this);
        CATEGORIES.add(param3);
    }

    public boolean isDown() {
        return this.isDown;
    }

    public String getCategory() {
        return this.category;
    }

    public boolean consumeClick() {
        if (this.clickCount == 0) {
            return false;
        } else {
            --this.clickCount;
            return true;
        }
    }

    private void release() {
        this.clickCount = 0;
        this.setDown(false);
    }

    public String getName() {
        return this.name;
    }

    public InputConstants.Key getDefaultKey() {
        return this.defaultKey;
    }

    public void setKey(InputConstants.Key param0) {
        this.key = param0;
    }

    public int compareTo(KeyMapping param0) {
        return this.category.equals(param0.category)
            ? I18n.get(this.name).compareTo(I18n.get(param0.name))
            : CATEGORY_SORT_ORDER.get(this.category).compareTo(CATEGORY_SORT_ORDER.get(param0.category));
    }

    public static Supplier<Component> createNameSupplier(String param0) {
        KeyMapping var0 = ALL.get(param0);
        return var0 == null ? () -> new TranslatableComponent(param0) : var0::getTranslatedKeyMessage;
    }

    public boolean same(KeyMapping param0) {
        return this.key.equals(param0.key);
    }

    public boolean isUnbound() {
        return this.key.equals(InputConstants.UNKNOWN);
    }

    public boolean matches(int param0, int param1) {
        if (param0 == InputConstants.UNKNOWN.getValue()) {
            return this.key.getType() == InputConstants.Type.SCANCODE && this.key.getValue() == param1;
        } else {
            return this.key.getType() == InputConstants.Type.KEYSYM && this.key.getValue() == param0;
        }
    }

    public boolean matchesMouse(int param0) {
        return this.key.getType() == InputConstants.Type.MOUSE && this.key.getValue() == param0;
    }

    public Component getTranslatedKeyMessage() {
        return this.key.getDisplayName();
    }

    public boolean isDefault() {
        return this.key.equals(this.defaultKey);
    }

    public String saveString() {
        return this.key.getName();
    }

    public void setDown(boolean param0) {
        this.isDown = param0;
    }
}
