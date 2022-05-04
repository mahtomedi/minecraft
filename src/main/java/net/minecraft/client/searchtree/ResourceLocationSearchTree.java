package net.minecraft.client.searchtree;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ResourceLocationSearchTree<T> {
    static <T> ResourceLocationSearchTree<T> empty() {
        return new ResourceLocationSearchTree<T>() {
            @Override
            public List<T> searchNamespace(String param0) {
                return List.of();
            }

            @Override
            public List<T> searchPath(String param0) {
                return List.of();
            }
        };
    }

    static <T> ResourceLocationSearchTree<T> create(List<T> param0, Function<T, Stream<ResourceLocation>> param1) {
        if (param0.isEmpty()) {
            return empty();
        } else {
            final SuffixArray<T> var0 = new SuffixArray<>();
            final SuffixArray<T> var1 = new SuffixArray<>();

            for(T var2 : param0) {
                param1.apply(var2).forEach(param3 -> {
                    var0.add(var2, param3.getNamespace().toLowerCase(Locale.ROOT));
                    var1.add(var2, param3.getPath().toLowerCase(Locale.ROOT));
                });
            }

            var0.generate();
            var1.generate();
            return new ResourceLocationSearchTree<T>() {
                @Override
                public List<T> searchNamespace(String param0) {
                    return var0.search(param0);
                }

                @Override
                public List<T> searchPath(String param0) {
                    return var1.search(param0);
                }
            };
        }
    }

    List<T> searchNamespace(String var1);

    List<T> searchPath(String var1);
}
