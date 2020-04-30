package ru.itmo.java;

import java.util.Map;
import java.util.Arrays;

import static java.util.Optional.ofNullable;

public class HashTable {

    private final static int RESIZE_FACTOR = 2;
    private final static int PROBING_INTERVAL = 1;
    private final static float INITIAL_LOAD_FACTOR = 0.5f;
    private final static int MAX_SIZE = Integer.MAX_VALUE;

    private Entry[] elements;
    private boolean[] deletedElements;
    private int size = 0;
    private int capacity;
    private float loadFactor;
    private int threshold;

    public HashTable(int capacity) {
        this(capacity, INITIAL_LOAD_FACTOR);
    }

    public HashTable(int capacity, float loadFactor) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Invalid capacity");
        }

        if (loadFactor > 1.0f) {
            throw new IllegalArgumentException("Invalid loadFactor");
        }

        this.elements = new Entry[capacity];
        this.deletedElements = new boolean[capacity];
        this.capacity = capacity;
        this.loadFactor = loadFactor;
        this.threshold = (int)(capacity * loadFactor);
    }

    public Object put(Object key, Object value) {
        if (size == MAX_SIZE) {
            throw new RuntimeException("You cannot put more object");
        }

        Entry newElement = new Entry(key, value);
        int foundIndex = getIndexByKey(key);

        if (elements[foundIndex] == null) {

            foundIndex = getHash(key);
            while (elements[foundIndex] != null) {
                foundIndex = (foundIndex + PROBING_INTERVAL) % capacity;
            }

            deletedElements[foundIndex] = false;

            elements[foundIndex] = newElement;
            ++size;

            if (size >= threshold) {
                resize();
            }

            return null;
        }
        Object oldValue = elements[foundIndex].value;
        elements[foundIndex] = newElement;
        return oldValue;
    }

    public Object get(Object key) {
        return ofNullable(elements[getIndexByKey(key)]);
    }

    public Object remove(Object key) {
        int foundIndex = getIndexByKey(key);

        if (elements[foundIndex] == null) {
            return null;
        }

        deletedElements[foundIndex] = true;
        Object oldValue = elements[foundIndex].value;
        --size;
        elements[foundIndex] = null;
        return oldValue;
    }

    public int size() {
        return size;
    }

    private void resize() {
        if (size >= threshold) {
            capacity *= RESIZE_FACTOR;
            threshold = (int) (capacity * loadFactor);

            Entry[] oldElements = elements;
            elements = new Entry[capacity];
            deletedElements = new boolean[capacity];
            size = 0;
            for (Entry currentElement : oldElements) {
                if (currentElement != null) {
                    put(currentElement.key, currentElement.value);
                }
            }
        }
    }

    private int getHash(Object key) {
        return Math.abs(key.hashCode()) % capacity;
    }

    private int getIndexByKey(Object key) {
        int hashKey = getHash(key);
        while (deletedElements[hashKey]
                || (elements[hashKey] != null
                && !elements[hashKey].key.equals(key))
        ) {
            hashKey = (hashKey + PROBING_INTERVAL) % capacity;
        }
        return hashKey;
    }

    private static class Entry {

        private Object key;
        private Object value;

        public Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("key=%s, value=%s", key.toString(), value.toString());
        }

    }

}
