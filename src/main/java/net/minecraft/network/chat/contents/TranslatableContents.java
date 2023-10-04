package net.minecraft.network.chat.contents;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

public class TranslatableContents implements ComponentContents {
    public static final Object[] NO_ARGS = new Object[0];
    private static final Codec<Object> PRIMITIVE_ARG_CODEC = ExtraCodecs.validate(ExtraCodecs.JAVA, TranslatableContents::filterAllowedArguments);
    private static final Codec<Object> ARG_CODEC = Codec.either(PRIMITIVE_ARG_CODEC, ComponentSerialization.CODEC)
        .xmap(
            param0 -> param0.map(param0x -> param0x, param0x -> Objects.requireNonNullElse(param0x.tryCollapseToString(), param0x)),
            param0 -> param0 instanceof Component var0 ? Either.right(var0) : Either.left(param0)
        );
    public static final MapCodec<TranslatableContents> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    Codec.STRING.fieldOf("translate").forGetter(param0x -> param0x.key),
                    Codec.STRING.optionalFieldOf("fallback").forGetter(param0x -> Optional.ofNullable(param0x.fallback)),
                    ExtraCodecs.strictOptionalField(ARG_CODEC.listOf(), "with").forGetter(param0x -> adjustArgs(param0x.args))
                )
                .apply(param0, TranslatableContents::create)
    );
    public static final ComponentContents.Type<TranslatableContents> TYPE = new ComponentContents.Type<>(CODEC, "translatable");
    private static final FormattedText TEXT_PERCENT = FormattedText.of("%");
    private static final FormattedText TEXT_NULL = FormattedText.of("null");
    private final String key;
    @Nullable
    private final String fallback;
    private final Object[] args;
    @Nullable
    private Language decomposedWith;
    private List<FormattedText> decomposedParts = ImmutableList.of();
    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    private static DataResult<Object> filterAllowedArguments(@Nullable Object param0) {
        return !isAllowedPrimitiveArgument(param0) ? DataResult.error(() -> "This value needs to be parsed as component") : DataResult.success(param0);
    }

    public static boolean isAllowedPrimitiveArgument(@Nullable Object param0) {
        return param0 instanceof Number || param0 instanceof Boolean || param0 instanceof String;
    }

    private static Optional<List<Object>> adjustArgs(Object[] param0) {
        return param0.length == 0 ? Optional.empty() : Optional.of(Arrays.asList(param0));
    }

    private static Object[] adjustArgs(Optional<List<Object>> param0) {
        return param0.<Object[]>map(param0x -> param0x.isEmpty() ? NO_ARGS : param0x.toArray()).orElse(NO_ARGS);
    }

    private static TranslatableContents create(String param0, Optional<String> param1, Optional<List<Object>> param2) {
        return new TranslatableContents(param0, param1.orElse(null), adjustArgs(param2));
    }

    public TranslatableContents(String param0, @Nullable String param1, Object[] param2) {
        this.key = param0;
        this.fallback = param1;
        this.args = param2;
    }

    @Override
    public ComponentContents.Type<?> type() {
        return TYPE;
    }

    private void decompose() {
        Language var0 = Language.getInstance();
        if (var0 != this.decomposedWith) {
            this.decomposedWith = var0;
            String var1 = this.fallback != null ? var0.getOrDefault(this.key, this.fallback) : var0.getOrDefault(this.key);

            try {
                Builder<FormattedText> var2 = ImmutableList.builder();
                this.decomposeTemplate(var1, var2::add);
                this.decomposedParts = var2.build();
            } catch (TranslatableFormatException var4) {
                this.decomposedParts = ImmutableList.of(FormattedText.of(var1));
            }

        }
    }

    private void decomposeTemplate(String param0, Consumer<FormattedText> param1) {
        Matcher var0 = FORMAT_PATTERN.matcher(param0);

        try {
            int var1 = 0;

            int var2;
            int var4;
            for(var2 = 0; var0.find(var2); var2 = var4) {
                int var3 = var0.start();
                var4 = var0.end();
                if (var3 > var2) {
                    String var5 = param0.substring(var2, var3);
                    if (var5.indexOf(37) != -1) {
                        throw new IllegalArgumentException();
                    }

                    param1.accept(FormattedText.of(var5));
                }

                String var6 = var0.group(2);
                String var7 = param0.substring(var3, var4);
                if ("%".equals(var6) && "%%".equals(var7)) {
                    param1.accept(TEXT_PERCENT);
                } else {
                    if (!"s".equals(var6)) {
                        throw new TranslatableFormatException(this, "Unsupported format: '" + var7 + "'");
                    }

                    String var8 = var0.group(1);
                    int var9 = var8 != null ? Integer.parseInt(var8) - 1 : var1++;
                    param1.accept(this.getArgument(var9));
                }
            }

            if (var2 < param0.length()) {
                String var10 = param0.substring(var2);
                if (var10.indexOf(37) != -1) {
                    throw new IllegalArgumentException();
                }

                param1.accept(FormattedText.of(var10));
            }

        } catch (IllegalArgumentException var12) {
            throw new TranslatableFormatException(this, var12);
        }
    }

    private FormattedText getArgument(int param0) {
        if (param0 >= 0 && param0 < this.args.length) {
            Object var0 = this.args[param0];
            if (var0 instanceof Component) {
                return (Component)var0;
            } else {
                return var0 == null ? TEXT_NULL : FormattedText.of(var0.toString());
            }
        } else {
            throw new TranslatableFormatException(this, param0);
        }
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> param0, Style param1) {
        this.decompose();

        for(FormattedText var0 : this.decomposedParts) {
            Optional<T> var1 = var0.visit(param0, param1);
            if (var1.isPresent()) {
                return var1;
            }
        }

        return Optional.empty();
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> param0) {
        this.decompose();

        for(FormattedText var0 : this.decomposedParts) {
            Optional<T> var1 = var0.visit(param0);
            if (var1.isPresent()) {
                return var1;
            }
        }

        return Optional.empty();
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack param0, @Nullable Entity param1, int param2) throws CommandSyntaxException {
        Object[] var0 = new Object[this.args.length];

        for(int var1 = 0; var1 < var0.length; ++var1) {
            Object var2 = this.args[var1];
            if (var2 instanceof Component var3) {
                var0[var1] = ComponentUtils.updateForEntity(param0, var3, param1, param2);
            } else {
                var0[var1] = var2;
            }
        }

        return MutableComponent.create(new TranslatableContents(this.key, this.fallback, var0));
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            if (param0 instanceof TranslatableContents var0
                && Objects.equals(this.key, var0.key)
                && Objects.equals(this.fallback, var0.fallback)
                && Arrays.equals(this.args, var0.args)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        int var0 = Objects.hashCode(this.key);
        var0 = 31 * var0 + Objects.hashCode(this.fallback);
        return 31 * var0 + Arrays.hashCode(this.args);
    }

    @Override
    public String toString() {
        return "translation{key='"
            + this.key
            + "'"
            + (this.fallback != null ? ", fallback='" + this.fallback + "'" : "")
            + ", args="
            + Arrays.toString(this.args)
            + "}";
    }

    public String getKey() {
        return this.key;
    }

    @Nullable
    public String getFallback() {
        return this.fallback;
    }

    public Object[] getArgs() {
        return this.args;
    }
}
