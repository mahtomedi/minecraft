package net.minecraft.world.phys.shapes;

public interface BooleanOp {
    BooleanOp FALSE = (param0, param1) -> false;
    BooleanOp NOT_OR = (param0, param1) -> !param0 && !param1;
    BooleanOp ONLY_SECOND = (param0, param1) -> param1 && !param0;
    BooleanOp NOT_FIRST = (param0, param1) -> !param0;
    BooleanOp ONLY_FIRST = (param0, param1) -> param0 && !param1;
    BooleanOp NOT_SECOND = (param0, param1) -> !param1;
    BooleanOp NOT_SAME = (param0, param1) -> param0 != param1;
    BooleanOp NOT_AND = (param0, param1) -> !param0 || !param1;
    BooleanOp AND = (param0, param1) -> param0 && param1;
    BooleanOp SAME = (param0, param1) -> param0 == param1;
    BooleanOp SECOND = (param0, param1) -> param1;
    BooleanOp CAUSES = (param0, param1) -> !param0 || param1;
    BooleanOp FIRST = (param0, param1) -> param0;
    BooleanOp CAUSED_BY = (param0, param1) -> param0 || !param1;
    BooleanOp OR = (param0, param1) -> param0 || param1;
    BooleanOp TRUE = (param0, param1) -> true;

    boolean apply(boolean var1, boolean var2);
}
