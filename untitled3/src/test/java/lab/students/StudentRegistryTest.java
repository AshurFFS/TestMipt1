package lab.students;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class StudentRegistryTest {

    private final Student anna = new Student(1, "Anna", 5.0);
    private final Student boris = new Student(2, "Boris", 7.5);
    private final Student vera = new Student(3, "Vera", 10.0);
    private final Student gleb = new Student(4, "Gleb", 0.0);
    private final Student dina = new Student(5, "Dina", 7.5);

    private List<Student> all;
    private HashMap<Integer, Student> hashMap;
    private TreeMap<Integer, Student> treeMap;

    @BeforeEach
    void setUp() {
        all = List.of(anna, boris, vera, gleb, dina);
        hashMap = StudentRegistry.createHashMap(all);
        treeMap = StudentRegistry.createDescendingTreeMap(all);
    }

    // ---------- Student ----------

    @Test
    void studentEqualsAndHashCodeAreConsistent() {
        Student a = new Student(1, "Anna", 5.0);
        Student b = new Student(1, "Anna", 5.0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Student(2, "Anna", 5.0));
        assertNotEquals(a, new Student(1, "Anna", 5.5));
        assertEquals(1, new HashSet<>(List.of(a, b)).size());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void studentRejectsNonPositiveId(int id) {
        assertThrows(IllegalArgumentException.class, () -> new Student(id, "X", 5.0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void studentRejectsBlankName(String name) {
        assertThrows(IllegalArgumentException.class, () -> new Student(1, name, 5.0));
    }

    @Test
    void studentRejectsNullName() {
        assertThrows(NullPointerException.class, () -> new Student(1, null, 5.0));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, 10.01, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void studentRejectsGradeOutOfRange(double grade) {
        assertThrows(IllegalArgumentException.class, () -> new Student(1, "X", grade));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 10.0, 5.5})
    void studentAcceptsBoundaryGrades(double grade) {
        assertEquals(grade, new Student(1, "X", grade).grade());
    }

    // ---------- создание карт ----------

    @Test
    void hashMapIsKeyedById() {
        assertEquals(5, hashMap.size());
        for (Student s : all) {
            assertEquals(s, hashMap.get(s.id()));
        }
    }

    @Test
    void treeMapKeysAreDescending() {
        assertEquals(List.of(5, 4, 3, 2, 1), new ArrayList<>(treeMap.keySet()));
    }

    @Test
    void duplicateIdIsRejectedWhenBuildingMaps() {
        List<Student> dup = List.of(anna, new Student(1, "Other", 3.0));
        assertThrows(IllegalArgumentException.class, () -> StudentRegistry.createHashMap(dup));
        assertThrows(IllegalArgumentException.class, () -> StudentRegistry.createDescendingTreeMap(dup));
    }

    // ---------- findStudentsByGradeRange ----------

    private static Set<Student> asSet(List<Student> list) {
        return new HashSet<>(list); // порядок обхода HashMap не гарантирован
    }

    @Test
    void rangeIncludesBothBounds() {
        List<Student> result = StudentRegistry.findStudentsByGradeRange(hashMap, 5.0, 7.5);
        assertEquals(Set.of(anna, boris, dina), asSet(result));
        assertEquals(3, result.size());
    }

    @Test
    void rangeCoveringWholeScaleReturnsEveryone() {
        List<Student> result = StudentRegistry.findStudentsByGradeRange(hashMap, 0.0, 10.0);
        assertEquals(Set.of(anna, boris, vera, gleb, dina), asSet(result));
    }

    @Test
    void rangeWithEqualBoundsMatchesExactGrade() {
        assertEquals(Set.of(boris, dina),
                asSet(StudentRegistry.findStudentsByGradeRange(hashMap, 7.5, 7.5)));
        assertEquals(Set.of(vera),
                asSet(StudentRegistry.findStudentsByGradeRange(hashMap, 10.0, 10.0)));
        assertEquals(Set.of(gleb),
                asSet(StudentRegistry.findStudentsByGradeRange(hashMap, 0.0, 0.0)));
    }

    @Test
    void rangeWithNoMatchesReturnsEmptyList() {
        assertTrue(StudentRegistry.findStudentsByGradeRange(hashMap, 8.0, 9.0).isEmpty());
    }

    @Test
    void rangeOnEmptyMapReturnsEmptyList() {
        assertTrue(StudentRegistry.findStudentsByGradeRange(new HashMap<>(), 0.0, 10.0).isEmpty());
    }

    @Test
    void rangeWorksWithTreeMapToo() {
        assertEquals(Set.of(anna, boris, dina),
                asSet(StudentRegistry.findStudentsByGradeRange(treeMap, 5.0, 7.5)));
    }

    @ParameterizedTest
    @CsvSource({
            "7.0, 5.0",     // min > max
            "-0.1, 5.0",    // min ниже допустимого
            "5.0, 10.1",    // max выше допустимого
            "-1.0, 11.0",   // обе границы вне диапазона
            "NaN, 5.0",
            "5.0, NaN"
    })
    void rangeRejectsInvalidBounds(double min, double max) {
        assertThrows(IllegalArgumentException.class,
                () -> StudentRegistry.findStudentsByGradeRange(hashMap, min, max));
    }

    @Test
    void rangeReturnsNewMutableListAndDoesNotModifyMap() {
        Map<Integer, Student> snapshot = new HashMap<>(hashMap);
        List<Student> result = StudentRegistry.findStudentsByGradeRange(hashMap, 0.0, 10.0);
        result.clear();
        result.add(anna); // список изменяемый
        assertEquals(snapshot, hashMap);
        List<Student> second = StudentRegistry.findStudentsByGradeRange(hashMap, 0.0, 10.0);
        assertNotSame(result, second);
        assertEquals(5, second.size());
    }

    // ---------- getTopNStudentsById ----------

    @Test
    void topNReturnsLargestIdsInDescendingOrder() {
        assertEquals(List.of(dina, gleb, vera), StudentRegistry.getTopNStudentsById(treeMap, 3));
    }

    @Test
    void topNOfOneReturnsStudentWithLargestId() {
        assertEquals(List.of(dina), StudentRegistry.getTopNStudentsById(treeMap, 1));
    }

    @Test
    void topNEqualToSizeReturnsAll() {
        assertEquals(List.of(dina, gleb, vera, boris, anna),
                StudentRegistry.getTopNStudentsById(treeMap, 5));
    }

    @Test
    void topNGreaterThanSizeReturnsAll() {
        assertEquals(List.of(dina, gleb, vera, boris, anna),
                StudentRegistry.getTopNStudentsById(treeMap, 100));
        assertEquals(5, StudentRegistry.getTopNStudentsById(treeMap, Integer.MAX_VALUE).size());
    }

    @Test
    void topNZeroReturnsEmptyList() {
        assertTrue(StudentRegistry.getTopNStudentsById(treeMap, 0).isEmpty());
    }

    @Test
    void topNOnEmptyMapReturnsEmptyList() {
        TreeMap<Integer, Student> empty = StudentRegistry.createDescendingTreeMap(List.of());
        assertTrue(StudentRegistry.getTopNStudentsById(empty, 3).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, Integer.MIN_VALUE})
    void topNRejectsNegativeN(int n) {
        assertThrows(IllegalArgumentException.class, () -> StudentRegistry.getTopNStudentsById(treeMap, n));
    }

    @Test
    void topNRejectsMapWithoutDescendingOrder() {
        // new TreeMap<>(sortedMap) сохранил бы компаратор, поэтому копируем через putAll
        NavigableMap<Integer, Student> ascending = new TreeMap<>();
        ascending.putAll(treeMap);
        assertThrows(IllegalArgumentException.class, () -> StudentRegistry.getTopNStudentsById(ascending, 2));
    }

    @Test
    void topNReturnsNewMutableListAndDoesNotModifyMap() {
        Map<Integer, Student> snapshot = new TreeMap<>(treeMap);
        List<Student> result = StudentRegistry.getTopNStudentsById(treeMap, 2);
        result.clear();
        result.add(anna); // список изменяемый
        assertEquals(snapshot, new TreeMap<>(treeMap));
        assertEquals(5, treeMap.size());
        assertEquals(List.of(5, 4, 3, 2, 1), new ArrayList<>(treeMap.keySet()));
    }
}
