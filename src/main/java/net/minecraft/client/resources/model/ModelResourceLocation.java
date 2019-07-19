package net.minecraft.client.resources.model;

import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelResourceLocation extends ResourceLocation {
    private final String variant;

    protected ModelResourceLocation(String[] param0) {
        super(param0);
        this.variant = param0[2].toLowerCase(Locale.ROOT);
    }

    public ModelResourceLocation(String param0) {
        this(decompose(param0));
    }

    public ModelResourceLocation(ResourceLocation param0, String param1) {
        this(param0.toString(), param1);
    }

    public ModelResourceLocation(String param0, String param1) {
        this(decompose(param0 + '#' + param1));
    }

    protected static String[] decompose(String param0) {
        String[] var0 = new String[]{null, param0, ""};
        int var1 = param0.indexOf(35);
        String var2 = param0;
        if (var1 >= 0) {
            var0[2] = param0.substring(var1 + 1, param0.length());
            if (var1 > 1) {
                var2 = param0.substring(0, var1);
            }
        }

        System.arraycopy(ResourceLocation.decompose(var2, ':'), 0, var0, 0, 2);
        return var0;
    }

    public String getVariant() {
        return this.variant;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 instanceof ModelResourceLocation && super.equals(param0)) {
            ModelResourceLocation var0 = (ModelResourceLocation)param0;
            return this.variant.equals(var0.variant);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + this.variant.hashCode();
    }

    @Override
    public String toString() {
        return super.toString() + '#' + this.variant;
    }
}
