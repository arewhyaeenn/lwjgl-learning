package com.tcg.lwjgllearning.math;

public interface Vector<T extends Vector<T>> {

    T copy();

    void set(T v);

    T addInPlace(T v);

    T addOutPlace(T v);

    T subInPlace(T v);

    T subOutPlace(T v);

    T scaleInPlace(T v);

    T scaleOutPlace(T v);

    T scalarScaleInPlace(float scalar);

    T scalarScaleOutPlace(float scalar);

    T scaleAndAddInPlace(float scalar, T v);

    T scaleAndAddOutPlace(float scalar, T v);

    T inverse();

    float squareMagnitude();

    float magnitude();

    T normalizeInPlace();

    T normalizeOutPlace();

    boolean isZero();

    default T rotateInPlace(Quaternion q) {
        throw new UnsupportedOperationException("This vector does not support quaternion rotation.");
    }

    default T rotateOutPlace(Quaternion q) {
        throw new UnsupportedOperationException("This vector does not support quaternion rotation.");
    }

    default T rotateInPlace(float delta) {
        throw new UnsupportedOperationException("This vector does not support angle rotation.");
    }

    default T rotateOutPlace(float delta) {
        throw new UnsupportedOperationException("This vector does not support angle rotation.");
    }

    default T sum(T[] vectors) {
        if (vectors.length == 0) throw new IllegalArgumentException("Sum function must take at least one vector.");
        T totalSum = vectors[0].copy();
        for (int i = 1; i < vectors.length; i++) {
            totalSum.addInPlace(vectors[i]);
        }
        return totalSum;
    }

    default T cross(T v) {
        throw new UnsupportedOperationException("This vector does not support the cross product.");
    }

    float dot(T v);

    default float cosAngleBetween(T v) {
        return this.dot(v) / (this.magnitude() * v.magnitude());
    }

    default float angleBetween(T v) {
        return MathUtils.acos(this.cosAngleBetween(v));
    }

    float[] asArray();

}
