package net.minecraft.network.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;

public interface ComponentContents {
    ComponentContents EMPTY = new ComponentContents() {
        @Override
        public String toString() {
            return "empty";
        }
    };

    default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> param0, Style param1) {
        return Optional.empty();
    }

    default <T> Optional<T> visit(FormattedText.ContentConsumer<T> param0) {
        return Optional.empty();
    }

    default MutableComponent resolve(@Nullable CommandSourceStack param0, @Nullable Entity param1, int param2) throws CommandSyntaxException {
        return MutableComponent.create(this);
    }
}
