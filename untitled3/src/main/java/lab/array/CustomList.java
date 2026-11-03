package lab.array;

/**
 * Минимальный параметризованный список, допускающий последовательный обход.
 *
 * @param <E> тип элементов списка
 */
public interface CustomList<E> extends Iterable<E> {

    /**
     * Добавляет элемент в конец списка.
     *
     * @param element добавляемый элемент, не {@code null}
     * @throws NullPointerException если {@code element == null}
     */
    void add(E element);

    /**
     * Возвращает элемент по индексу.
     *
     * @param index индекс из диапазона {@code [0, size)}
     * @return элемент в позиции {@code index}
     * @throws IndexOutOfBoundsException если индекс вне диапазона
     */
    E get(int index);

    /**
     * Удаляет элемент по индексу, сдвигая хвост списка влево.
     *
     * @param index индекс из диапазона {@code [0, size)}
     * @return удалённый элемент
     * @throws IndexOutOfBoundsException если индекс вне диапазона
     */
    E remove(int index);

    /**
     * Возвращает количество элементов в списке.
     *
     * @return размер списка
     */
    int size();

    /**
     * Проверяет, пуст ли список.
     *
     * @return {@code true}, если в списке нет элементов
     */
    boolean isEmpty();
}
