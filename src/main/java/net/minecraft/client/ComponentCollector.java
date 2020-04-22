package net.minecraft.client;

import javax.annotation.Nullable;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ComponentCollector {
    private boolean singleComponent = true;
    @Nullable
    private MutableComponent collector;

    public void append(MutableComponent param0) {
        if (this.collector == null) {
            this.collector = param0;
        } else {
            if (this.singleComponent) {
                this.collector = new TextComponent("").append(this.collector);
                this.singleComponent = false;
            }

            this.collector.append(param0);
        }

    }

    @Nullable
    public MutableComponent getResult() {
        return this.collector;
    }

    public MutableComponent getResultOrEmpty() {
        return (MutableComponent)(this.collector != null ? this.collector : new TextComponent(""));
    }
}
