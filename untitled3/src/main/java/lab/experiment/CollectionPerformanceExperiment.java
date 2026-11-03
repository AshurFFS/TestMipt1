package lab.experiment;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;


public final class CollectionPerformanceExperiment {

    /** Число элементов / операций в каждом сценарии. */
    static final int N = 10_000;

    /** Число прогревочных запусков перед замерами. */
    static final int WARMUP_RUNS = 3;

    /** Число замеров, по которым считается медиана. */
    static final int MEASURED_RUNS = 7;

    /** Приёмник контрольных сумм: не даёт JIT удалить измеряемый код. */
    private static volatile long sink;

    /**
     * Сценарий: как создать список и что именно измерять.
     *
     * @param name      название операции
     * @param prefilled заполнять ли список числами 0..N-1 перед замером
     * @param operation измеряемая операция; возвращает контрольную сумму
     */
    private record Scenario(String name, boolean prefilled, ToLongFunction<List<Integer>> operation) {
    }

    private CollectionPerformanceExperiment() {
    }

    /**
     * Точка входа: запускает все сценарии и печатает таблицу.
     *
     * @param args не используются
     */
    public static void main(String[] args) {
        // Явная UTF-8 кодировка, чтобы кириллица не зависела от настроек терминала
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.printf(Locale.ROOT, "Эксперимент: N = %d, прогрев = %d, замеров = %d (медиана)%n%n",
                N, WARMUP_RUNS, MEASURED_RUNS);
        String header = String.format(Locale.ROOT, "%-26s | %7s | %14s | %14s",
                "Операция", "N", "ArrayList, мс", "LinkedList, мс");
        out.println(header);
        out.println("-".repeat(header.length()));
        for (Scenario s : scenarios()) {
            double array = measureMedianMillis(s, ArrayList::new);
            double linked = measureMedianMillis(s, LinkedList::new);
            out.println(String.format(Locale.ROOT, "%-26s | %7d | %14.3f | %14.3f",
                    s.name(), N, array, linked));
        }
        out.println();
        out.println("Контрольная сумма (защита от удаления кода JIT): " + sink);
    }

    private static List<Scenario> scenarios() {
        return List.of(
                new Scenario("Добавление в конец", false, list -> {
                    for (int i = 0; i < N; i++) {
                        list.add(i);
                    }
                    return list.size() + list.get(list.size() - 1);
                }),
                new Scenario("Добавление в начало", false, list -> {
                    for (int i = 0; i < N; i++) {
                        list.add(0, i);
                    }
                    return list.size() + list.get(0);
                }),
                new Scenario("Вставка в середину", true, list -> {
                    for (int i = 0; i < N; i++) {
                        list.add(list.size() / 2, i);
                    }
                    return list.size() + list.get(list.size() / 2);
                }),
                new Scenario("Доступ по индексу (get)", true, list -> {
                    long sum = 0;
                    for (int i = 0; i < N; i++) {
                        sum += list.get(i);
                    }
                    return sum;
                }),
                new Scenario("Удаление из начала", true, list -> {
                    long sum = 0;
                    for (int i = 0; i < N; i++) {
                        sum += list.remove(0);
                    }
                    return sum + list.size();
                }),
                new Scenario("Удаление из конца", true, list -> {
                    long sum = 0;
                    for (int i = 0; i < N; i++) {
                        sum += list.remove(list.size() - 1);
                    }
                    return sum + list.size();
                })
        );
    }

    private static double measureMedianMillis(Scenario scenario, Supplier<List<Integer>> factory) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            sink += scenario.operation().applyAsLong(freshList(scenario, factory));
        }
        long[] times = new long[MEASURED_RUNS];
        for (int i = 0; i < MEASURED_RUNS; i++) {
            List<Integer> list = freshList(scenario, factory); // подготовка вне замера
            long start = System.nanoTime();
            long result = scenario.operation().applyAsLong(list);
            times[i] = System.nanoTime() - start;
            sink += result;
        }
        Arrays.sort(times);
        return times[MEASURED_RUNS / 2] / 1_000_000.0;
    }

    /** Создаёт новый список в одинаковом исходном состоянии для обеих реализаций. */
    private static List<Integer> freshList(Scenario scenario, Supplier<List<Integer>> factory) {
        List<Integer> list = factory.get();
        if (scenario.prefilled()) {
            for (int i = 0; i < N; i++) {
                list.add(i);
            }
        }
        return list;
    }
}
