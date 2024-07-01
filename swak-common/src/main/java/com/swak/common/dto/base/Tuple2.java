package com.swak.common.dto.base;

public class Tuple2<T1, T2>  {
    /** Field 0 of the tuple. */
    public T1 v1;
    /** Field 1 of the tuple. */
    public T2 v2;

    /** Creates a new tuple where all fields are null. */
    public Tuple2() {}

    /**
     * Creates a new tuple and assigns the given values to the tuple's fields.
     */
    public Tuple2(T1 v1, T2 v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public <T> T getField(int pos) {
        switch (pos) {
            case 0:
                return (T) this.v1;
            case 1:
                return (T) this.v2;
            default:
                throw new IndexOutOfBoundsException(String.valueOf(pos));
        }
    }

    public <T> void setField(T value, int pos) {
        switch (pos) {
            case 0:
                this.v1 = (T1) value;
                break;
            case 1:
                this.v2 = (T2) value;
                break;
            default:
                throw new IndexOutOfBoundsException(String.valueOf(pos));
        }
    }

    /**
     * Sets new values to all fields of the tuple.
     *
     * @param v1 The value for field 0
     * @param v2 The value for field 1
     */
    public void setFields(T1 v1, T2 v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    /**
     * Returns a shallow copy of the tuple with swapped values.
     *
     * @return shallow copy of the tuple with swapped values
     */
    public Tuple2<T2, T1> swap() {
        return new Tuple2<>(v2, v1);
    }


    public T1 getV1() {
        return this.v1;
    }

    public T2 getV2() {
        return this.v2;
    }

    /**
     * Shallow tuple copy.
     *
     * @return A new Tuple with the same fields as this.
     */
    public Tuple2<T1, T2> copy() {
        return new Tuple2<>(this.v1, this.v2);
    }

    /**
     * Creates a new tuple and assigns the given values to the tuple's fields. This is more
     * convenient than using the constructor, because the compiler can infer the generic type
     * arguments implicitly. For example: {@code Tuple3.of(n, x, s)} instead of {@code new
     * Tuple3<Integer, Double, String>(n, x, s)}
     */
    public static <T1, T2> Tuple2<T1, T2> of(T1 v1, T2 v2) {
        return new Tuple2<>(v1, v2);
    }
}