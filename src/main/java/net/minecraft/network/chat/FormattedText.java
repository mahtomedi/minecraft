package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Unit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface FormattedText {
    Optional<Unit> STOP_ITERATION = Optional.of(Unit.INSTANCE);
    FormattedText EMPTY = new FormattedText() {
        @Override
        public <T> Optional<T> visit(FormattedText.ContentConsumer<T> param0) {
            return Optional.empty();
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> param0, Style param1) {
            return Optional.empty();
        }
    };

    <T> Optional<T> visit(FormattedText.ContentConsumer<T> var1);

    @OnlyIn(Dist.CLIENT)
    <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> var1, Style var2);

    static FormattedText of(final String param0) {
        return new FormattedText() {
            @Override
            public <T> Optional<T> visit(FormattedText.ContentConsumer<T> param0x) {
                return param0.accept(param0);
            }

            @OnlyIn(Dist.CLIENT)
            @Override
            public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> param0x, Style param1) {
                return param0.accept(param1, param0);
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    static FormattedText of(final String param0, final Style param1) {
        return new FormattedText() {
            @Override
            public <T> Optional<T> visit(FormattedText.ContentConsumer<T> param0x) {
                return param0.accept(param0);
            }

            @Override
            public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> param0x, Style param1x) {
                return param0.accept(param1.applyTo(param1), param0);
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    static FormattedText composite(FormattedText... param0) {
        return composite(ImmutableList.copyOf(param0));
    }

    @OnlyIn(Dist.CLIENT)
    static FormattedText composite(final List<FormattedText> param0) {
        return new FormattedText() {
            @Override
            public <T> Optional<T> visit(FormattedText.ContentConsumer<T> param0x) {
                for(FormattedText var0 : param0) {
                    Optional<T> var1 = var0.visit(param0);
                    if (var1.isPresent()) {
                        return var1;
                    }
                }

                return Optional.empty();
            }

            @Override
            public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> param0x, Style param1) {
                for(FormattedText var0 : param0) {
                    Optional<T> var1 = var0.visit(param0, param1);
                    if (var1.isPresent()) {
                        return var1;
                    }
                }

                return Optional.empty();
            }
        };
    }

    public interface ContentConsumer<T> {
        Optional<T> accept(String var1);
    }

    @OnlyIn(Dist.CLIENT)
    public interface StyledContentConsumer<T> {
        Optional<T> accept(Style var1, String var2);
    }
}
