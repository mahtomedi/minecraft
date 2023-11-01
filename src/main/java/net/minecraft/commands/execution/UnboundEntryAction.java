package net.minecraft.commands.execution;

@FunctionalInterface
public interface UnboundEntryAction<T> {
    void execute(T var1, ExecutionContext<T> var2, Frame var3);

    default EntryAction<T> bind(T param0) {
        return (param1, param2) -> this.execute(param0, param1, param2);
    }
}
