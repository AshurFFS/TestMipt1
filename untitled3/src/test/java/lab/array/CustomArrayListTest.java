package lab.array;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class CustomArrayListTest {

    private CustomArrayList<Integer> list;

    @BeforeEach
    void setUp() {
        list = new CustomArrayList<>();
    }

    private void fill(int count) {
        for (int i = 0; i < count; i++) {
            list.add(i);
        }
    }

    @Test
    void newListIsEmpty() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    void addSingleElement() {
        list.add(42);
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        assertEquals(42, list.get(0));
    }

    @Test
    void addSeveralElementsKeepsOrder() {
        list.add(1);
        list.add(2);
        list.add(3);
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
    }

    @Test
    void addMoreThanDefaultCapacityGrowsWithoutDataLoss() {
        assertEquals(10, list.capacity());
        fill(11);
        assertEquals(15, list.capacity());
        assertEquals(11, list.size());
        for (int i = 0; i < 11; i++) {
            assertEquals(i, list.get(i));
        }
        fill(100);
        assertEquals(111, list.size());
        assertEquals(10, list.get(110));
    }

    @Test
    void growthFactorIsOnePointFive() {
        CustomArrayList<Integer> small = new CustomArrayList<>(4);
        for (int i = 0; i < 5; i++) {
            small.add(i);
        }
        assertEquals(6, small.capacity());
    }

    @Test
    void getWorksForFirstMiddleAndLast() {
        fill(7);
        assertEquals(0, list.get(0));
        assertEquals(3, list.get(3));
        assertEquals(6, list.get(6));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, 5, 6, Integer.MAX_VALUE, Integer.MIN_VALUE})
    void getRejectsInvalidIndex(int index) {
        fill(5);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(index));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, 5, 6, Integer.MAX_VALUE, Integer.MIN_VALUE})
    void removeRejectsInvalidIndex(int index) {
        fill(5);
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(index));
        assertEquals(5, list.size());
    }

    @Test
    void getAndRemoveRejectAnyIndexOnEmptyList() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
    }

    static Stream<Arguments> removalCases() {
        // индекс удаляемого элемента, ожидаемое значение, ожидаемое содержимое после удаления
        return Stream.of(
                Arguments.of(0, 0, List.of(1, 2, 3, 4)),
                Arguments.of(2, 2, List.of(0, 1, 3, 4)),
                Arguments.of(4, 4, List.of(0, 1, 2, 3))
        );
    }

    @ParameterizedTest
    @MethodSource("removalCases")
    void removeReturnsValueAndShiftsTail(int index, int expectedValue, List<Integer> expectedRest) {
        fill(5);
        assertEquals(expectedValue, list.remove(index));
        assertEquals(4, list.size());
        List<Integer> actual = new ArrayList<>();
        list.forEach(actual::add);
        assertEquals(expectedRest, actual);
    }

    @Test
    void removeLastElementLeavesEmptyList() {
        list.add(1);
        assertEquals(1, list.remove(0));
        assertTrue(list.isEmpty());
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
    }

    @Test
    void removeClearsFreedSlot() throws Exception {
        fill(3);
        list.remove(1);
        java.lang.reflect.Field f = CustomArrayList.class.getDeclaredField("elements");
        f.setAccessible(true);
        Object[] raw = (Object[]) f.get(list);
        assertNull(raw[2], "освободившаяся ячейка должна быть null");
        assertEquals(2, list.size());
    }

    @Test
    void addNullThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> list.add(null));
        assertEquals(0, list.size());
    }

    @Test
    void iteratorTraversesInOrder() {
        fill(25);
        List<Integer> seen = new ArrayList<>();
        for (int value : list) {
            seen.add(value);
        }
        assertEquals(25, seen.size());
        for (int i = 0; i < 25; i++) {
            assertEquals(i, seen.get(i));
        }
    }

    @Test
    void iteratorOverEmptyListHasNoElements() {
        Iterator<Integer> it = list.iterator();
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void nextOnExhaustedIteratorThrowsNoSuchElementException() {
        fill(2);
        Iterator<Integer> it = list.iterator();
        it.next();
        it.next();
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void invalidInitialCapacityIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new CustomArrayList<Integer>(0));
    }
}
