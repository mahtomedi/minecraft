package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;

public abstract class BaseComponent implements MutableComponent {
    protected final List<Component> siblings = Lists.newArrayList();
    private Style style = Style.EMPTY;

    @Override
    public MutableComponent append(Component param0) {
        this.siblings.add(param0);
        return this;
    }

    @Override
    public String getContents() {
        return "";
    }

    @Override
    public List<Component> getSiblings() {
        return this.siblings;
    }

    @Override
    public MutableComponent setStyle(Style param0) {
        this.style = param0;
        return this;
    }

    @Override
    public Style getStyle() {
        return this.style;
    }

    public abstract BaseComponent toMutable();

    @Override
    public final MutableComponent mutableCopy() {
        BaseComponent var0 = this.toMutable();
        var0.siblings.addAll(this.siblings);
        var0.setStyle(this.style);
        return var0;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof BaseComponent)) {
            return false;
        } else {
            BaseComponent var0 = (BaseComponent)param0;
            return this.siblings.equals(var0.siblings) && Objects.equals(this.getStyle(), var0.getStyle());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getStyle(), this.siblings);
    }

    @Override
    public String toString() {
        return "BaseComponent{style=" + this.style + ", siblings=" + this.siblings + '}';
    }
}
