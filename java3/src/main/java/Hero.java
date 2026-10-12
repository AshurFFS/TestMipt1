public abstract class Hero implements Castable, Restable {
    public static final int MIN_STAT_VALUE = 1;

    private final String name;
    private final int maxHealth;
    private final int baseAttack;
    private int health;

    protected Hero(String name, int maxHealth, int baseAttack) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        if (maxHealth < MIN_STAT_VALUE || baseAttack < MIN_STAT_VALUE) {
            throw new IllegalArgumentException("Характеристики не могут быть меньше " + MIN_STAT_VALUE);
        }
        this.name = name;
        this.maxHealth = maxHealth;
        this.baseAttack = baseAttack;
        this.health = maxHealth;
    }

    public String getName() {
        return name;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public int getHealth() {
        return health;
    }

    public abstract void attack(Hero target);

    public abstract ActionType makeTurn(Hero target);

    public void takeDamage(int damage) {
        if (damage <= 0 || !isAlive()) {
            return;
        }
        health = Math.max(0, health - damage);
        if (health == 0) {
            System.out.println(name + " пал в бою!");
        }
    }

    public void heal(int amount) {
        if (amount <= 0 || !isAlive()) {
            return;
        }
        health = Math.min(maxHealth, health + amount);
    }

    public boolean isAlive() {
        return health > 0;
    }
}