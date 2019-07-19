package net.minecraft.advancements;

import java.util.Collection;

public interface RequirementsStrategy {
    RequirementsStrategy AND = param0 -> {
        String[][] var0 = new String[param0.size()][];
        int var1 = 0;

        for(String var2 : param0) {
            var0[var1++] = new String[]{var2};
        }

        return var0;
    };
    RequirementsStrategy OR = param0 -> new String[][]{param0.toArray(new String[0])};

    String[][] createRequirements(Collection<String> var1);
}
