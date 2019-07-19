package net.minecraft.network.chat;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.world.entity.Entity;

public class TranslatableComponent extends BaseComponent implements ContextAwareComponent {
    private static final Language DEFAULT_LANGUAGE = new Language();
    private static final Language LANGUAGE = Language.getInstance();
    private final String key;
    private final Object[] args;
    private final Object decomposeLock = new Object();
    private long decomposedLanguageTime = -1L;
    protected final List<Component> decomposedParts = Lists.newArrayList();
    public static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    public TranslatableComponent(String param0, Object... param1) {
        this.key = param0;
        this.args = param1;

        for(int var0 = 0; var0 < param1.length; ++var0) {
            Object var1 = param1[var0];
            if (var1 instanceof Component) {
                Component var2 = ((Component)var1).deepCopy();
                this.args[var0] = var2;
                var2.getStyle().inheritFrom(this.getStyle());
            } else if (var1 == null) {
                this.args[var0] = "null";
            }
        }

    }

    @VisibleForTesting
    synchronized void decompose() {
        synchronized(this.decomposeLock) {
            long var0 = LANGUAGE.getLastUpdateTime();
            if (var0 == this.decomposedLanguageTime) {
                return;
            }

            this.decomposedLanguageTime = var0;
            this.decomposedParts.clear();
        }

        try {
            this.decomposeTemplate(LANGUAGE.getElement(this.key));
        } catch (TranslatableFormatException var6) {
            this.decomposedParts.clear();

            try {
                this.decomposeTemplate(DEFAULT_LANGUAGE.getElement(this.key));
            } catch (TranslatableFormatException var5) {
                throw var6;
            }
        }

    }

    protected void decomposeTemplate(String param0) {
        Matcher var0 = FORMAT_PATTERN.matcher(param0);

        try {
            int var1 = 0;

            int var2;
            int var4;
            for(var2 = 0; var0.find(var2); var2 = var4) {
                int var3 = var0.start();
                var4 = var0.end();
                if (var3 > var2) {
                    Component var5 = new TextComponent(String.format(param0.substring(var2, var3)));
                    var5.getStyle().inheritFrom(this.getStyle());
                    this.decomposedParts.add(var5);
                }

                String var6 = var0.group(2);
                String var7 = param0.substring(var3, var4);
                if ("%".equals(var6) && "%%".equals(var7)) {
                    Component var8 = new TextComponent("%");
                    var8.getStyle().inheritFrom(this.getStyle());
                    this.decomposedParts.add(var8);
                } else {
                    if (!"s".equals(var6)) {
                        throw new TranslatableFormatException(this, "Unsupported format: '" + var7 + "'");
                    }

                    String var9 = var0.group(1);
                    int var10 = var9 != null ? Integer.parseInt(var9) - 1 : var1++;
                    if (var10 < this.args.length) {
                        this.decomposedParts.add(this.getComponent(var10));
                    }
                }
            }

            if (var2 < param0.length()) {
                Component var11 = new TextComponent(String.format(param0.substring(var2)));
                var11.getStyle().inheritFrom(this.getStyle());
                this.decomposedParts.add(var11);
            }

        } catch (IllegalFormatException var111) {
            throw new TranslatableFormatException(this, var111);
        }
    }

    private Component getComponent(int param0) {
        if (param0 >= this.args.length) {
            throw new TranslatableFormatException(this, param0);
        } else {
            Object var0 = this.args[param0];
            Component var1;
            if (var0 instanceof Component) {
                var1 = (Component)var0;
            } else {
                var1 = new TextComponent(var0 == null ? "null" : var0.toString());
                var1.getStyle().inheritFrom(this.getStyle());
            }

            return var1;
        }
    }

    @Override
    public Component setStyle(Style param0) {
        super.setStyle(param0);

        for(Object var0 : this.args) {
            if (var0 instanceof Component) {
                ((Component)var0).getStyle().inheritFrom(this.getStyle());
            }
        }

        if (this.decomposedLanguageTime > -1L) {
            for(Component var1 : this.decomposedParts) {
                var1.getStyle().inheritFrom(param0);
            }
        }

        return this;
    }

    @Override
    public Stream<Component> stream() {
        this.decompose();
        return Streams.concat(this.decomposedParts.stream(), this.siblings.stream()).flatMap(Component::stream);
    }

    @Override
    public String getContents() {
        this.decompose();
        StringBuilder var0 = new StringBuilder();

        for(Component var1 : this.decomposedParts) {
            var0.append(var1.getContents());
        }

        return var0.toString();
    }

    public TranslatableComponent copy() {
        Object[] var0 = new Object[this.args.length];

        for(int var1 = 0; var1 < this.args.length; ++var1) {
            if (this.args[var1] instanceof Component) {
                var0[var1] = ((Component)this.args[var1]).deepCopy();
            } else {
                var0[var1] = this.args[var1];
            }
        }

        return new TranslatableComponent(this.key, var0);
    }

    @Override
    public Component resolve(@Nullable CommandSourceStack param0, @Nullable Entity param1, int param2) throws CommandSyntaxException {
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
            + '\''
            + ", args="
            + Arrays.toString(this.args)
            + ", siblings="
            + this.siblings
            + ", style="
            + this.getStyle()
            + '}';
    }

    public String getKey() {
        return this.key;
    }

    public Object[] getArgs() {
        return this.args;
    }
}
