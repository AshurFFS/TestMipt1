package lab.array;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Реализация {@link CustomList} на основе массива {@code Object[]}
 * с автоматическим расширением ёмкости в 1.5 раза.
 * Значения {@code null} не допускаются.
 *
 * @param <E> тип элементов списка
 */
public class CustomArrayList<E> implements CustomList<E> {

    /** Ёмкость по умолчанию. */
    static final int DEFAULT_CAPACITY = 10;

    private Object[] elements;
    private int size;

    /**
     * Создаёт пустой список с начальной ёмкостью {@value #DEFAULT_CAPACITY}.
     */
    public CustomArrayList() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Создаёт пустой список с заданной начальной ёмкостью.
     *
     * @param initialCapacity начальная ёмкость, не меньше 1
     * @throws IllegalArgumentException если {@code initialCapacity < 1}
     */
    public CustomArrayList(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("Initial capacity must be positive: " + initialCapacity);
        }
        this.elements = new Object[initialCapacity];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(E element) {
        if (element == null) {
            throw new NullPointerException("null elements are not allowed");
        }
        if (size == elements.length) {
            grow();
        }
        elements[size++] = element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) {
        checkIndex(index);
        return (E) elements[index];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public E remove(int index) {
        checkIndex(index);
        E removed = (E) elements[index];
        int moved = size - index - 1;
        if (moved > 0) {
            System.arraycopy(elements, index + 1, elements, index, moved);
        }
        elements[--size] = null;
        return removed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Возвращает текущую ёмкость внутреннего массива.
     *
     * @return длина внутреннего массива
     */
    public int capacity() {
        return elements.length;
    }

    /**
     * Возвращает итератор по элементам от индекса 0 до {@code size - 1}.
     * Метод {@link Iterator#remove()} не поддерживается.
     *
     * @return итератор по списку
     */
    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    private void grow() {
        int oldCapacity = elements.length;
        int newCapacity = Math.max(oldCapacity + 1, oldCapacity + (oldCapacity >> 1));
        elements = Arrays.copyOf(elements, newCapacity);
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }

    private class Itr implements Iterator<E> {
        private int cursor;

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            return (E) elements[cursor++];
        }
    }
}
