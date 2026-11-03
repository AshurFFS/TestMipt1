package lab.students;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Реестр студентов и операции выборки по нему.
 */
public final class StudentRegistry {

    private StudentRegistry() {
    }

    /**
     * Создаёт {@link HashMap}, где ключом является {@link Student#id()}.
     *
     * @param students студенты для добавления
     * @return новая карта {@code id -> студент}
     * @throws IllegalArgumentException если среди студентов есть повторяющийся id
     */
    public static HashMap<Integer, Student> createHashMap(Collection<Student> students) {
        HashMap<Integer, Student> map = new HashMap<>();
        for (Student s : students) {
            if (map.putIfAbsent(s.id(), s) != null) {
                throw new IllegalArgumentException("duplicate id: " + s.id());
            }
        }
        return map;
    }

    /**
     * Создаёт {@link TreeMap} с сортировкой ключей по убыванию, где ключом является {@link Student#id()}.
     *
     * @param students студенты для добавления
     * @return новая карта {@code id -> студент}, упорядоченная по убыванию id
     * @throws IllegalArgumentException если среди студентов есть повторяющийся id
     */
    public static TreeMap<Integer, Student> createDescendingTreeMap(Collection<Student> students) {
        TreeMap<Integer, Student> map = new TreeMap<>(Collections.reverseOrder());
        for (Student s : students) {
            if (map.putIfAbsent(s.id(), s) != null) {
                throw new IllegalArgumentException("duplicate id: " + s.id());
            }
        }
        return map;
    }

    /**
     * Возвращает студентов, чья оценка лежит в замкнутом диапазоне {@code [minGrade, maxGrade]}.
     * Переданная карта не изменяется. Порядок результата определяется порядком обхода карты.
     *
     * @param students карта студентов
     * @param minGrade нижняя граница (включительно)
     * @param maxGrade верхняя граница (включительно)
     * @return новый изменяемый {@link ArrayList}
     * @throws IllegalArgumentException если {@code minGrade > maxGrade}, любая граница выходит
     *                                  за {@code [0.0, 10.0]} или является NaN
     */
    public static List<Student> findStudentsByGradeRange(
            Map<Integer, Student> students,
            double minGrade,
            double maxGrade) {
        if (!isValidGrade(minGrade) || !isValidGrade(maxGrade)) {
            throw new IllegalArgumentException(
                    "grade bounds must be in [0.0, 10.0]: " + minGrade + ", " + maxGrade);
        }
        if (minGrade > maxGrade) {
            throw new IllegalArgumentException(
                    "minGrade must not exceed maxGrade: " + minGrade + " > " + maxGrade);
        }
        List<Student> result = new ArrayList<>();
        for (Student s : students.values()) {
            if (s.grade() >= minGrade && s.grade() <= maxGrade) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Возвращает не более {@code n} студентов с наибольшими id в порядке убывания id.
     * Карта должна быть создана с сортировкой ключей по убыванию (например, через
     * {@link #createDescendingTreeMap(Collection)}). Переданная карта не изменяется.
     *
     * @param students карта с ключами по убыванию
     * @param n        максимальное число студентов в результате
     * @return новый изменяемый {@link ArrayList}
     * @throws IllegalArgumentException если {@code n < 0} либо порядок ключей карты не убывающий
     */
    public static List<Student> getTopNStudentsById(
            NavigableMap<Integer, Student> students,
            int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative: " + n);
        }
        if (!isDescending(students)) {
            throw new IllegalArgumentException("map must be sorted by keys in descending order");
        }
        List<Student> result = new ArrayList<>();
        if (n == 0) {
            return result;
        }
        for (Student s : students.values()) {
            if (result.size() == n) {
                break;
            }
            result.add(s);
        }
        return result;
    }

    private static boolean isValidGrade(double grade) {
        return !Double.isNaN(grade) && grade >= Student.MIN_GRADE && grade <= Student.MAX_GRADE;
    }

    /** Проверяет, что компаратор карты ставит больший ключ раньше меньшего. */
    private static boolean isDescending(NavigableMap<Integer, Student> map) {
        return map.comparator() != null && map.comparator().compare(1, 2) > 0;
    }
}
