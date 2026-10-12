public class Arena {
    private final Hero fighter1;
    private final Hero fighter2;
    private int roundCounter;

    public Arena(Hero fighter1, Hero fighter2) {
        if (fighter1 == null || fighter2 == null) {
            throw new IllegalArgumentException("Бойцы не могут быть null");
        }
        this.fighter1 = fighter1;
        this.fighter2 = fighter2;
    }

    public void startTournament() {
        while (fighter1.isAlive() && fighter2.isAlive()) {
            roundCounter++;
            System.out.println("\n===== Раунд " + roundCounter + " =====");

            Hero attacker;
            Hero defender;
            if (Math.random() < 0.5) {
                attacker = fighter1;
                defender = fighter2;
            } else {
                attacker = fighter2;
                defender = fighter1;
            }

            performTurn(attacker, defender);
            if (!defender.isAlive()) {
                break;
            }
            performTurn(defender, attacker);
            if (!attacker.isAlive()) {
                break;
            }

            printStatus(fighter1);
            printStatus(fighter2);
        }
        announceWinner();
    }

    private void performTurn(Hero actor, Hero target) {
        ActionType action = actor.makeTurn(target);
        System.out.println("Игрок " + actor.getName() + " совершил действие: " + action.getTitle());
    }

    private void printStatus(Hero hero) {
        System.out.println(hero.getName() + ": HP " + hero.getHealth() + "/" + hero.getMaxHealth());
    }

    private void announceWinner() {
        Hero winner = fighter1.isAlive() ? fighter1 : fighter2;
        System.out.println("\nПобедитель: " + winner.getName() + "!");
    }
}