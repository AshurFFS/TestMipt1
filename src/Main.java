import java.math.BigDecimal;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        scanner.useLocale(Locale.forLanguageTag("ru")); // десятичная запятая: 500,50

        Account account = new Account(11111, 000, new BigDecimal("10000.00"), BankType.AUM);

        System.out.println("Добро пожаловать в банкомат!");

        System.out.print("Введите номер карты: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Ошибка доступа: номер карты должен быть целым числом.");
            return;
        }
        int cardNumber = scanner.nextInt();

        System.out.print("Введите пин-код: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Ошибка доступа: пин-код должен быть целым числом.");
            return;
        }
        int pin = scanner.nextInt();

        if (cardNumber != account.getCardNumber() || pin != account.getPin()) {
            System.out.println("Ошибка доступа: неверный номер карты или пин-код.");
            return;
        }

        System.out.println("Авторизация успешна. " + account);

        CashMachine machine = new CashMachine();
        BigDecimal balance = account.getBalance();

        System.out.print("Введите сумму для внесения (например, 500,50): ");
        if (!scanner.hasNextBigDecimal()) {
            System.out.println("Некорректная сумма.");
            return;
        }
        BigDecimal depositAmount = scanner.nextBigDecimal();
        balance = machine.deposit(balance, depositAmount);
        System.out.println("Баланс после внесения: " + balance.toPlainString() + " руб.");

        System.out.print("Введите сумму для снятия (например, 3000,00): ");
        if (!scanner.hasNextBigDecimal()) {
            System.out.println("Некорректная сумма.");
            return;
        }
        BigDecimal withdrawAmount = scanner.nextBigDecimal();
        balance = machine.withdraw(balance, withdrawAmount, account.getBankType());
        System.out.println("Баланс после снятия: " + balance.toPlainString() + " руб.");
    }
}
