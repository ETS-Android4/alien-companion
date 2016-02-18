package com.gDyejeekis.aliencompanion.enums;

/**
 * Created by sound on 2/18/2016.
 */
public enum DaysEnum {

    MONDAY("mon"),
    TUESDAY("tue"),
    WEDNESDAY("wed"),
    THURSDAY("thu"),
    FRIDAY("fri"),
    SATURDAY("sat"),
    SUNDAY("sun");

    private final String value;

    DaysEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
