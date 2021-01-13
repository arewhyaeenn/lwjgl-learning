package com.tcg.lwjgllearning.utils;

import java.util.LinkedList;

public class ArrayMap<T> {

    private final static int MINIMUM_INITIAL_CAPACITY = 32;
    private final static int MAX_CAPACITY = Integer.MAX_VALUE;
    private final static int HALF_CAPACITY = MAX_CAPACITY / 2;

    private Object[] values;
    private final LinkedList<Integer> openKeys;
    private int size;
    private int capacity;

    public ArrayMap(int initialCapacity) {
        this.capacity = Math.max(initialCapacity, MINIMUM_INITIAL_CAPACITY);
        this.size = 0;
        this.values = new Object[this.capacity];
        this.openKeys = new LinkedList<>();
    }

    public ArrayMap() {
        this(MINIMUM_INITIAL_CAPACITY);
    }

    public int add(T value) {
        if (value == null) return -1;
        if (this.size == this.capacity) {
            if (!this.increaseCapacity()) return -1;
        }

        int key;
        if (this.openKeys.size() > 0) {
            key = this.openKeys.pop();
        } else {
            key = this.size;
        }
        this.values[key] = value;
        this.size++;
        return key;
    }

    public T get(int key) {
        if (key >= this.capacity || key < 0) {
            return null;
        }
        return (T) this.values[key];
    }

    public T remove(int key) {
        T value = this.get(key);
        if (value != null) {
            this.values[key] = null;
            this.size--;
            this.openKeys.add(key);
        }
        return value;
    }

    private boolean increaseCapacity() {
        if (this.capacity == MAX_CAPACITY) return false;
        if (this.capacity >= HALF_CAPACITY) {
            this.capacity = MAX_CAPACITY;
        } else {
            this.capacity *= 2;
        }
        Object[] values = new Object[this.capacity];
        System.arraycopy(this.values, 0, values, 0, this.size);
        this.values = values;
        return true;
    }
}
