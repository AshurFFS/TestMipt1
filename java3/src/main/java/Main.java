import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Hero first = createHero(scanner, 1);
        Hero second = createHero(scanner, 2);
        new Arena(first, second).startTournament();
    }

    private static Hero createHero(Scanner scanner, int number) {
        System.out.println("\n--- Создание персонажа " + number + " ---");
        while (true) {
            try {
                int type = readInt(scanner, "Тип (1 - Воин, 2 - Маг): ");
                System.out.print("Имя: ");
                String name = scanner.nextLine().trim();
                int maxHealth = readInt(scanner, "Максимальное здоровье: ");
                int baseAttack = readInt(scanner, "Базовая атака: ");

                if (type == 1) {
                    int armor = readInt(scanner, "Броня: ");
                    return new Warrior(name, maxHealth, baseAttack, armor);
                }
                if (type == 2) {
                    int maxMana = readInt(scanner, "Максимальная мана: ");
                    return new Mage(name, maxHealth, baseAttack, maxMana);
                }
                System.out.println("Неизвестный тип персонажа, попробуйте снова.");
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage() + ". Введите данные заново.");
            }
        }
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Нужно целое число.");
            }
        }
    }
}