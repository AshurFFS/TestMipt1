public class Warrior extends Hero {
    private final int armor;

    public Warrior(String name, int maxHealth, int baseAttack, int armor) {
        super(name, maxHealth, baseAttack);
        if (armor < 0) {
            throw new IllegalArgumentException("Броня не может быть отрицательной");
        }
        this.armor = armor;
    }

    public int getArmor() {
        return armor;
    }

    @Override
    public void takeDamage(int damage) {
        int finalDamage = Math.max(1, damage - armor);
        super.takeDamage(finalDamage);
    }

    @Override
    public void attack(Hero target) {
        System.out.println(getName() + " бьёт мечом " + target.getName()
                + " (урон: " + getBaseAttack() + ")");
        target.takeDamage(getBaseAttack());
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
        return getHealth() * 2 < getMaxHealth();
    }

    @Override
    public void castSpecialSkill(Hero target) {
        if (!canCast()) {
            System.out.println("Воин попытался ударить щитом, но потерял равновесие!");
            return;
        }
        int damage = getBaseAttack() + armor * 2;
        System.out.println(getName() + " бьёт щитом " + target.getName()
                + " (чистый урон: " + damage + ")");
        target.takeDamage(damage);
    }

    @Override
    public boolean needsRest() {
        return getHealth() * 100 < getMaxHealth() * 15;
    }

    @Override
    public void rest() {
        int amount = armor * 2;
        heal(amount);
        System.out.println(getName() + " переводит дух (Второе дыхание) и восстанавливает "
                + amount + " здоровья");
    }
}