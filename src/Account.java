import java.math.BigDecimal;
import java.math.RoundingMode;

public class Account {
    public int cardNumber;
    public int pin;
    public BigDecimal balance;
    public BankType bankType;

    public Account(int cardNumber, int pin, BigDecimal balance, BankType bankType) {
        if (cardNumber >= 10000 && cardNumber <= 99999) {
            this.cardNumber = cardNumber;
        } else {
            this.cardNumber = 0;
        }

        if (pin >= 100 && pin <= 999) {
            this.pin = pin;
        } else {
            this.pin = 0;
        }

        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            this.balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            this.balance = balance.setScale(2, RoundingMode.HALF_UP);
        }

        if (bankType == null) {
            this.bankType = BankType.NEO;
        } else {
            this.bankType = bankType;
        }
    }

    public int getCardNumber() {
        return cardNumber;
    }

    public int getPin() {
        return pin;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BankType getBankType() {
        return bankType;
    }

    @Override
    public String toString() {
        return bankType.getRussianName() + " Карта: " + cardNumber + ", Баланс: " + balance.toPlainString() + " руб.";
    }
}

