package net.minecraft.util;

public class Tuple<A, B> {
    private A a;
    private B b;

    public Tuple(A param0, B param1) {
        this.a = param0;
        this.b = param1;
    }

    public A getA() {
        return this.a;
    }

    public B getB() {
        return this.b;
    }
}
