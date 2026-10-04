import java.math.BigDecimal;
import java.math.RoundingMode;

public class CashMachine implements WithdrawalOperations, DepositOperations {

    @Override
    public BigDecimal deposit(BigDecimal balance, BigDecimal amount) {
        if (balance == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return balance;
        }
        return balance.add(amount).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal withdraw(BigDecimal balance, BigDecimal amount, BankType bankType) {
        if (balance == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Некорректная сумма снятия.");
            return balance;
        }

        BigDecimal commission = applyCommission(amount, bankType);
        BigDecimal total = amount.add(commission);

        if (total.compareTo(balance) > 0) {
            System.out.println("Недостаточно средств на счете.");
            return balance;
        }
        return balance.subtract(total).setScale(2, RoundingMode.HALF_UP);
    }
}
