package net.minecraft.client.searchtree;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface PlainTextSearchTree<T> {
    static <T> PlainTextSearchTree<T> empty() {
        return param0 -> List.of();
    }

    static <T> PlainTextSearchTree<T> create(List<T> param0, Function<T, Stream<String>> param1) {
        if (param0.isEmpty()) {
            return empty();
        } else {
            SuffixArray<T> var0 = new SuffixArray<>();

            for(T var1 : param0) {
                param1.apply(var1).forEach(param2 -> var0.add(var1, param2.toLowerCase(Locale.ROOT)));
            }

            var0.generate();
            return var0::search;
        }
    }

    List<T> search(String var1);
}
