package lab.students;

/**
 * Неизменяемая модель студента.
 *
 * @param id    положительный уникальный идентификатор
 * @param name  непустое имя
 * @param grade оценка в диапазоне от 0.0 до 10.0 включительно
 */
public record Student(int id, String name, double grade) {

    /** Минимально допустимая оценка. */
    public static final double MIN_GRADE = 0.0;

    /** Максимально допустимая оценка. */
    public static final double MAX_GRADE = 10.0;

    /**
     * Создаёт студента, проверяя инварианты.
     *
     * @throws IllegalArgumentException если {@code id <= 0}, имя пустое или состоит из пробелов,
     *                                  либо оценка вне диапазона {@code [0.0, 10.0]} или равна NaN
     * @throws NullPointerException     если {@code name == null}
     */
    public Student {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive: " + id);
        }
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (Double.isNaN(grade) || grade < MIN_GRADE || grade > MAX_GRADE) {
            throw new IllegalArgumentException("grade must be in [0.0, 10.0]: " + grade);
        }
    }
}
