package org.example;

public enum Specialty {
    IPZ("Інженерія програмного забезпечення"),
    KN("Комп’ютерні науки"),
    PM("Прикладна математика");

    private final String displayName;

    Specialty(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
