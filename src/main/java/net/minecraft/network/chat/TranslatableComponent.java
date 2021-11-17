package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.world.entity.Entity;

public class TranslatableComponent extends BaseComponent implements ContextAwareComponent {
    private static final Object[] NO_ARGS = new Object[0];
    private static final FormattedText TEXT_PERCENT = FormattedText.of("%");
    private static final FormattedText TEXT_NULL = FormattedText.of("null");
    private final String key;
    private final Object[] args;
    @Nullable
    private Language decomposedWith;
    private final List<FormattedText> decomposedParts = Lists.newArrayList();
    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    public TranslatableComponent(String param0) {
        this.key = param0;
        this.args = NO_ARGS;
    }

    public TranslatableComponent(String param0, Object... param1) {
        this.key = param0;
        this.args = param1;
    }

    private void decompose() {
        Language var0 = Language.getInstance();
        if (var0 != this.decomposedWith) {
            this.decomposedWith = var0;
            this.decomposedParts.clear();
            String var1 = var0.getOrDefault(this.key);

            try {
                this.decomposeTemplate(var1);
            } catch (TranslatableFormatException var4) {
                this.decomposedParts.clear();
                this.decomposedParts.add(FormattedText.of(var1));
            }

        }
    }

    private void decomposeTemplate(String param0) {
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

                    this.decomposedParts.add(FormattedText.of(var5));
                }

                String var6 = var0.group(2);
                String var7 = param0.substring(var3, var4);
                if ("%".equals(var6) && "%%".equals(var7)) {
                    this.decomposedParts.add(TEXT_PERCENT);
                } else {
                    if (!"s".equals(var6)) {
                        throw new TranslatableFormatException(this, "Unsupported format: '" + var7 + "'");
                    }

                    String var8 = var0.group(1);
                    int var9 = var8 != null ? Integer.parseInt(var8) - 1 : var1++;
                    if (var9 < this.args.length) {
                        this.decomposedParts.add(this.getArgument(var9));
                    }
                }
            }

            if (var2 < param0.length()) {
                String var10 = param0.substring(var2);
                if (var10.indexOf(37) != -1) {
                    throw new IllegalArgumentException();
                }

                this.decomposedParts.add(FormattedText.of(var10));
            }

        } catch (IllegalArgumentException var111) {
            throw new TranslatableFormatException(this, var111);
        }
    }

    private FormattedText getArgument(int param0) {
        if (param0 >= this.args.length) {
            throw new TranslatableFormatException(this, param0);
        } else {
            Object var0 = this.args[param0];
            if (var0 instanceof Component) {
                return (Component)var0;
            } else {
                return var0 == null ? TEXT_NULL : FormattedText.of(var0.toString());
            }
        }
    }

    public TranslatableComponent plainCopy() {
        return new TranslatableComponent(this.key, this.args);
    }

    @Override
    public <T> Optional<T> visitSelf(FormattedText.StyledContentConsumer<T> param0, Style param1) {
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
    public <T> Optional<T> visitSelf(FormattedText.ContentConsumer<T> param0) {
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
            if (var2 instanceof Component) {
                var0[var1] = ComponentUtils.updateForEntity(param0, (Component)var2, param1, param2);
            } else {
                var0[var1] = var2;
            }
        }

        return new TranslatableComponent(this.key, var0);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof TranslatableComponent)) {
            return false;
        } else {
            TranslatableComponent var0 = (TranslatableComponent)param0;
            return Arrays.equals(this.args, var0.args) && this.key.equals(var0.key) && super.equals(param0);
        }
    }

    @Override
    public int hashCode() {
        int var0 = super.hashCode();
        var0 = 31 * var0 + this.key.hashCode();
        return 31 * var0 + Arrays.hashCode(this.args);
    }

    @Override
    public String toString() {
        return "TranslatableComponent{key='"
            + this.key
            + "', args="
            + Arrays.toString(this.args)
            + ", siblings="
            + this.siblings
            + ", style="
            + this.getStyle()
            + "}";
    }

    public String getKey() {
        return this.key;
    }

    public Object[] getArgs() {
        return this.args;
    }
}
