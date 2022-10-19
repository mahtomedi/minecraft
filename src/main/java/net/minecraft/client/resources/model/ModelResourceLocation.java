package net.minecraft.client.resources.model;

import com.google.common.annotations.VisibleForTesting;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelResourceLocation extends ResourceLocation {
    @VisibleForTesting
    static final char VARIANT_SEPARATOR = '#';
    private final String variant;

    private ModelResourceLocation(String param0, String param1, String param2, @Nullable ResourceLocation.Dummy param3) {
        super(param0, param1, param3);
        this.variant = param2;
    }

    public ModelResourceLocation(String param0, String param1, String param2) {
        super(param0, param1);
        this.variant = lowercaseVariant(param2);
    }

    public ModelResourceLocation(ResourceLocation param0, String param1) {
        this(param0.getNamespace(), param0.getPath(), lowercaseVariant(param1), null);
    }

    public static ModelResourceLocation vanilla(String param0, String param1) {
        return new ModelResourceLocation("minecraft", param0, param1);
    }

    private static String lowercaseVariant(String param0) {
        return param0.toLowerCase(Locale.ROOT);
    }

    public String getVariant() {
        return this.variant;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 instanceof ModelResourceLocation var0 && super.equals(param0) ? this.variant.equals(var0.variant) : false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + this.variant.hashCode();
    }

    @Override
    public String toString() {
        return super.toString() + "#" + this.variant;
    }
}
