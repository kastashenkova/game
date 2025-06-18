package org.example;

/**
 * The Specialty enum represents different academic specialties (majors) available.
 * Each specialty has a display name for user-friendly representation.
 */
public enum Specialty {
    /**
     * Software Engineering specialty.
     */
    IPZ("Інженерія програмного забезпечення"),
    /**
     * Computer Science specialty.
     */
    KN("Комп'ютерні науки"),
    /**
     * Applied Mathematics specialty.
     */
    PM("Прикладна математика");

    private final String displayName;

    /**
     * Constructs a Specialty enum with a given display name.
     * @param displayName The user-friendly name of the specialty.
     */
    Specialty(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the display name of the specialty.
     * @return The display name as a String.
     */
    @Override
    public String toString() {
        return displayName;
    }
}