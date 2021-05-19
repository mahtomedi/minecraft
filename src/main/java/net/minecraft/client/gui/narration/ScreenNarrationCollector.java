package net.minecraft.client.gui.narration;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScreenNarrationCollector {
    int generation;
    final Map<ScreenNarrationCollector.EntryKey, ScreenNarrationCollector.NarrationEntry> entries = Maps.newTreeMap(
        Comparator.<ScreenNarrationCollector.EntryKey, NarratedElementType>comparing(param0 -> param0.type).thenComparing(param0 -> param0.depth)
    );

    public void update(Consumer<NarrationElementOutput> param0) {
        ++this.generation;
        param0.accept(new ScreenNarrationCollector.Output(0));
    }

    public String collectNarrationText(boolean param0) {
        final StringBuilder var0 = new StringBuilder();
        Consumer<String> var1 = new Consumer<String>() {
            private boolean firstEntry = true;

            public void accept(String param0) {
                if (!this.firstEntry) {
                    var0.append(". ");
                }

                this.firstEntry = false;
                var0.append(param0);
            }
        };
        this.entries.forEach((param2, param3) -> {
            if (param3.generation == this.generation && (param0 || !param3.alreadyNarrated)) {
                param3.contents.getText(var1);
                param3.alreadyNarrated = true;
            }

        });
        return var0.toString();
    }

    @OnlyIn(Dist.CLIENT)
    static class EntryKey {
        final NarratedElementType type;
        final int depth;

        EntryKey(NarratedElementType param0, int param1) {
            this.type = param0;
            this.depth = param1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class NarrationEntry {
        NarrationThunk<?> contents = NarrationThunk.EMPTY;
        int generation = -1;
        boolean alreadyNarrated;

        public ScreenNarrationCollector.NarrationEntry update(int param0, NarrationThunk<?> param1) {
            if (!this.contents.equals(param1)) {
                this.contents = param1;
                this.alreadyNarrated = false;
            } else if (this.generation + 1 != param0) {
                this.alreadyNarrated = false;
            }

            this.generation = param0;
            return this;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class Output implements NarrationElementOutput {
        private final int depth;

        Output(int param0) {
            this.depth = param0;
        }

        @Override
        public void add(NarratedElementType param0, NarrationThunk<?> param1) {
            ScreenNarrationCollector.this.entries
                .computeIfAbsent(new ScreenNarrationCollector.EntryKey(param0, this.depth), param0x -> new ScreenNarrationCollector.NarrationEntry())
                .update(ScreenNarrationCollector.this.generation, param1);
        }

        @Override
        public NarrationElementOutput nest() {
            return ScreenNarrationCollector.this.new Output(this.depth + 1);
        }
    }
}
