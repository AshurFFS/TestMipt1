
import java.util.Objects;

public class Pair<K, V> {

    private final K key;
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }


    public Pair<V, K> swap() {
        return new Pair<>(value, key);
    }


    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "Pair{key=" + key + ", value=" + value + "}";
    }

    public static void main(String[] args) {
        final Pair<String, Integer> pair = new Pair<>("Age", 25);
        System.out.println(pair); // Pair{key=Age, value=25}

        final Pair<Integer, String> swapped = pair.swap();
        System.out.println(swapped); // Pair{key=25, value=Age}

        final Pair<String, String> created = Pair.of("name", "John");
        System.out.println(created); // Pair{key=name, value=John}
    }
}
