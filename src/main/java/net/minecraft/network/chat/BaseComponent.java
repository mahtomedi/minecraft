package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class BaseComponent implements Component {
    protected final List<Component> siblings = Lists.newArrayList();
    private Style style;

    @Override
    public Component append(Component param0) {
        param0.getStyle().inheritFrom(this.getStyle());
        this.siblings.add(param0);
        return this;
    }

    @Override
    public List<Component> getSiblings() {
        return this.siblings;
    }

    @Override
    public Component setStyle(Style param0) {
        this.style = param0;

        for(Component var0 : this.siblings) {
            var0.getStyle().inheritFrom(this.getStyle());
        }

        return this;
    }

    @Override
    public Style getStyle() {
        if (this.style == null) {
            this.style = new Style();

            for(Component var0 : this.siblings) {
                var0.getStyle().inheritFrom(this.style);
            }
        }

        return this.style;
    }

    @Override
    public Stream<Component> stream() {
        return Streams.concat(Stream.of(this), this.siblings.stream().flatMap(Component::stream));
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof BaseComponent)) {
            return false;
        } else {
            BaseComponent var0 = (BaseComponent)param0;
            return this.siblings.equals(var0.siblings) && this.getStyle().equals(var0.getStyle());
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
