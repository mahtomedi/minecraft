package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ValueObject {
    @Override
    public String toString() {
        StringBuilder var0 = new StringBuilder("{");

        for(Field var1 : this.getClass().getFields()) {
            if (!isStatic(var1)) {
                try {
                    var0.append(getName(var1)).append("=").append(var1.get(this)).append(" ");
                } catch (IllegalAccessException var7) {
                }
            }
        }

        var0.deleteCharAt(var0.length() - 1);
        var0.append('}');
        return var0.toString();
    }

    private static String getName(Field param0) {
        SerializedName var0 = param0.getAnnotation(SerializedName.class);
        return var0 != null ? var0.value() : param0.getName();
    }

    private static boolean isStatic(Field param0) {
        return Modifier.isStatic(param0.getModifiers());
    }
}
