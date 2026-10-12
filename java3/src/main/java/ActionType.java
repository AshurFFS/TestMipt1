public enum ActionType {
    BASE_ATTACK("базовая атака"),
    SPECIAL_SKILL("особое умение"),
    REST("отдых");

    private final String title;

    ActionType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}