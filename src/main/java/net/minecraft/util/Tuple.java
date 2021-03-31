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

    public void setA(A param0) {
        this.a = param0;
    }

    public B getB() {
        return this.b;
    }

    public void setB(B param0) {
        this.b = param0;
    }
}
