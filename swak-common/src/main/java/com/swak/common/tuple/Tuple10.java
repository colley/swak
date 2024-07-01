
package com.swak.common.tuple;

/**
 * Represents a list of 10 typed Objects.
 *
 * @since 3.0.0
 */
public final class Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> extends Tuple {
    private static final long serialVersionUID = 7824474731525795360L;
    private final T1 v1;
    private final T2 v2;
    private final T3 v3;
    private final T4 v4;
    private final T5 v5;
    private final T6 v6;
    private final T7 v7;
    private final T8 v8;
    private final T9 v9;
    private final T10 v10;

    public Tuple10(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10) {
        super(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);

        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
        this.v5 = v5;
        this.v6 = v6;
        this.v7 = v7;
        this.v8 = v8;
        this.v9 = v9;
        this.v10 = v10;
    }

    public Tuple10(Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> tuple) {
        this(tuple.v1, tuple.v2, tuple.v3, tuple.v4, tuple.v5, tuple.v6, tuple.v7, tuple.v8, tuple.v9, tuple.v10);
    }

    public T1 getV1() {
        return v1;
    }

    public T2 getV2() {
        return v2;
    }

    public T3 getV3() {
        return v3;
    }

    public T4 getV4() {
        return v4;
    }

    public T5 getV5() {
        return v5;
    }

    public T6 getV6() {
        return v6;
    }

    public T7 getV7() {
        return v7;
    }

    public T8 getV8() {
        return v8;
    }

    public T9 getV9() {
        return v9;
    }

    public T10 getV10() {
        return v10;
    }

    @Override
    public Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> clone() {
        return new Tuple10<>(this);
    }
}
