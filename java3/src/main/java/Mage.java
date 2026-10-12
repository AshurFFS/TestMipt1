public class Mage extends Hero {
    private static final int SPELL_COST = 10;
    private static final int SKILL_COST = 25;
    private static final int STAFF_MANA_RESTORE = 5;

    private final int maxMana;
    private int mana;

    public Mage(String name, int maxHealth, int baseAttack, int maxMana) {
        super(name, maxHealth, baseAttack);
        if (maxMana < MIN_STAT_VALUE) {
            throw new IllegalArgumentException("Мана не может быть меньше " + MIN_STAT_VALUE);
        }
        this.maxMana = maxMana;
        this.mana = maxMana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getMana() {
        return mana;
    }

    @Override
    public void attack(Hero target) {
        if (mana >= SPELL_COST) {
            int damage = getBaseAttack() * 2;
            mana -= SPELL_COST;
            System.out.println(getName() + " выпускает магический выстрел в " + target.getName()
                    + " (урон: " + damage + ", мана: -" + SPELL_COST + ")");
            target.takeDamage(damage);
        } else {
            int damage = getBaseAttack() / 2;
            mana = Math.min(maxMana, mana + STAFF_MANA_RESTORE);
            System.out.println(getName() + " бьёт посохом " + target.getName()
                    + " (урон: " + damage + ", мана: +" + STAFF_MANA_RESTORE + ")");
            target.takeDamage(damage);
        }
    }

    @Override
    public ActionType makeTurn(Hero target) {
        if (canCast()) {
            castSpecialSkill(target);
            return ActionType.SPECIAL_SKILL;
        }
        if (needsRest()) {
            rest();
            return ActionType.REST;
        }
        attack(target);
        return ActionType.BASE_ATTACK;
    }

    @Override
    public boolean canCast() {
        return mana >= SKILL_COST;
    }

    @Override
    public void castSpecialSkill(Hero target) {
        if (!canCast()) {
            System.out.println(getName() + " не хватает маны: заклинание «Огненная глыба» сорвалось!");
            return;
        }
        int damage = getBaseAttack() * 3;
        mana -= SKILL_COST;
        System.out.println(getName() + " бросает Огненную глыбу в " + target.getName()
                + " (урон: " + damage + ", мана: -" + SKILL_COST + ")");
        target.takeDamage(damage);
    }

    @Override
    public boolean needsRest() {
        return mana == 0 || getHealth() * 100 < getMaxHealth() * 30;
    }

    @Override
    public void rest() {
        mana = maxMana;
        int amount = getBaseAttack();
        heal(amount);
        System.out.println(getName() + " уходит в Исцеляющую медитацию: мана полностью восстановлена, +"
                + amount + " здоровья");
    }
}