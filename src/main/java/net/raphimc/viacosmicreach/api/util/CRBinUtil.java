/*
 * This file is part of ViaCosmicReach - https://github.com/RaphiMC/ViaCosmicReach
 * Copyright (C) 2024-2025 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.viacosmicreach.api.util;

import finalforeach.cosmicreach.savelib.IByteArray;
import finalforeach.cosmicreach.savelib.utils.*;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.function.BiConsumer;

public class CRBinUtil {

    static {
        DynamicArrays.instantiator = new IDynamicArrayInstantiator() {

            @Override
            public <E> IDynamicArray<E> create(final Class<E> clazz) {
                return new DynamicArray<E>(clazz).new Impl();
            }

            @Override
            public <E> IDynamicArray<E> create(final Class<E> clazz, final int initialCapacity) {
                return new DynamicArray<E>(clazz, initialCapacity).new Impl();
            }

            @Override
            public IByteArray createByteArray() {
                return new DynamicByteArray();
            }

        };

        ObjectMaps.instantiator = new IObjectMapInstantiator() {

            @Override
            public <K, V> IObjectMap<K, V> create(final IObjectMap<K, V> srcMap) {
                return new ObjectMap<>(srcMap);
            }

            @Override
            public <K, V> IObjectMap<K, V> create() {
                return new ObjectMap<>();
            }

            @Override
            public <K> IObjectIntMap<K> createObjectIntMap() {
                return new ObjectIntMap<>();
            }

            @Override
            public <K> IObjectLongMap<K> createObjectLongMap() {
                return new ObjectLongMap<>();
            }

            @Override
            public <K> IObjectFloatMap<K> createObjectFloatMap() {
                return new ObjectFloatMap<>();
            }

        };
    }

    public static void initCrBin() {
    }

    private static class DynamicArray<E> extends Vector<E> {

        public DynamicArray(final Class<E> clazz) {
            this(clazz, 10);
        }

        public DynamicArray(final Class<E> clazz, final int initialCapacity) {
            DynamicArray.this.elementData = (Object[]) Array.newInstance(clazz, initialCapacity);
        }

        private class Impl implements IDynamicArray<E> {

            @Override
            public int size() {
                return DynamicArray.this.size();
            }

            @Override
            public void add(final E value) {
                DynamicArray.this.add(value);
            }

            @Override
            public E get(final int index) {
                return DynamicArray.this.get(index);
            }

            @Override
            public boolean contains(final E value, final boolean identity) {
                if (identity && value != null) {
                    return this.indexOf(value, true) >= 0;
                }

                return DynamicArray.this.contains(value);
            }

            @Override
            public int indexOf(final E value, final boolean identity) {
                if (identity && value != null) {
                    for (int i = 0; i < DynamicArray.this.elementCount; i++) {
                        if (DynamicArray.this.elementData[i] == value) {
                            return i;
                        }
                    }
                    return -1;
                }

                return DynamicArray.this.indexOf(value);
            }

            @Override
            public E[] items() {
                return (E[]) DynamicArray.this.elementData;
            }

            @Override
            public void clear() {
                DynamicArray.this.clear();
            }

            @Override
            public E removeIndex(final int index) {
                return DynamicArray.this.remove(index);
            }

            @Override
            public Iterator<E> iterator() {
                return DynamicArray.this.iterator();
            }

        }

    }

    private static class DynamicByteArray extends ByteArrayOutputStream implements IByteArray {

        @Override
        public byte[] toArray() {
            return super.toByteArray();
        }

        @Override
        public void addAll(final byte... bytes) {
            super.write(bytes, 0, bytes.length);
        }

        @Override
        public void set(final int index, final byte b) {
            super.buf[index] = b;
        }

        @Override
        public void add(final byte b) {
            super.write(b);
        }

        @Override
        public void addAll(final IByteArray byteArray) {
            super.write(byteArray.items(), 0, byteArray.size());
        }

        @Override
        public byte[] items() {
            return this.buf;
        }

    }

    private static class ObjectMap<K, V> extends HashMap<K, V> implements IObjectMap<K, V> {

        public ObjectMap() {
        }

        public ObjectMap(final IObjectMap<K, V> srcMap) {
            this.putAll(srcMap);
        }

        @Override
        public void putAll(final IObjectMap<K, V> srcMap) {
            srcMap.forEachEntry(super::put);
        }

        @Override
        public void forEachEntry(final BiConsumer<K, V> entryConsumer) {
            super.forEach(entryConsumer);
        }

    }

    private static class ObjectIntMap<K> extends HashMap<K, Integer> implements IObjectIntMap<K> {

        @Override
        public int get(final K k, final int i) {
            return super.getOrDefault(k, i);
        }

        @Override
        public void put(final K k, final int i) {
            super.put(k, i);
        }

    }

    private static class ObjectLongMap<K> extends HashMap<K, Long> implements IObjectLongMap<K> {

        @Override
        public long get(final K k, final long l) {
            return super.getOrDefault(k, l);
        }

        @Override
        public void put(final K k, final long l) {
            super.put(k, l);
        }

    }

    private static class ObjectFloatMap<K> extends HashMap<K, Float> implements IObjectFloatMap<K> {

        @Override
        public float get(final K k, final float v) {
            return super.getOrDefault(k, v);
        }

        @Override
        public void put(final K k, final float v) {
            super.put(k, v);
        }

    }

}
