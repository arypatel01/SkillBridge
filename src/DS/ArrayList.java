package DS;

import java.util.*;
import java.util.function.Consumer;

public class ArrayList<E> implements Iterable<E>{

    private Object[] data;
    private int size;

    private static final int DEFAULT_CAPACITY = 10;

    public ArrayList() {
        data = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    public void add(E element) {
        if (size == data.length) {
            grow();
        }
        data[size] = element;
        size++;
    }

    @SuppressWarnings("unchecked")
    public E get(int index) {
        checkIndex(index);
        return (E) data[index];
    }

    @SuppressWarnings("unchecked")
    public E remove(int index) {
        checkIndex(index);
        E removed = (E) data[index];

        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(data, index + 1, data, index, numMoved);
        }
        data[--size] = null;
        return removed;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(E element) {
        return indexOf(element) >= 0;
    }

    public int indexOf(E element) {
        for (int i = 0; i < size; i++) {
            if (element == null ? data[i] == null : element.equals(data[i])) {
                return i;
            }
        }
        return -1;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            data[i] = null;
        }
        size = 0;
    }

    private void grow() {
        data = Arrays.copyOf(data, data.length * 2);
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(data[i]);
            if (i != size - 1) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            @SuppressWarnings("unchecked")
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return (E) data[currentIndex++];
            }

            @Override
            public void remove() {
                if (currentIndex <= 0) {
                    throw new IllegalStateException("next() has not been called yet");
                }
                ArrayList.this.remove(currentIndex - 1);
                currentIndex--;
            }
        };
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator<E> spliterator() {
        return Iterable.super.spliterator();
    }
}