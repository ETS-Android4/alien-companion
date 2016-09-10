package com.gDyejeekis.aliencompanion.enums;

/**
 * Created by George on 9/10/2016.
 */
public enum SettingsMenuType {

    headers("Settings"),
    appearance("Appearance"),
    navigation("Navigation"),
    posts("Posts"),
    comments("Comments"),
    sync("Sync options"),
    linkHandling("Link handling"),
    other("Other");

    private final String value;

    SettingsMenuType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
