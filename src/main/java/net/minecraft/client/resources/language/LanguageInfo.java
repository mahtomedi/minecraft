package net.minecraft.client.resources.language;

import com.mojang.bridge.game.Language;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LanguageInfo implements Language, Comparable<LanguageInfo> {
    private final String code;
    private final String region;
    private final String name;
    private final boolean bidirectional;

    public LanguageInfo(String param0, String param1, String param2, boolean param3) {
        this.code = param0;
        this.region = param1;
        this.name = param2;
        this.bidirectional = param3;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getRegion() {
        return this.region;
    }

    public boolean isBidirectional() {
        return this.bidirectional;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", this.name, this.region);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return !(param0 instanceof LanguageInfo) ? false : this.code.equals(((LanguageInfo)param0).code);
        }
    }

    @Override
    public int hashCode() {
        return this.code.hashCode();
    }

    public int compareTo(LanguageInfo param0) {
        return this.code.compareTo(param0.code);
    }
}
