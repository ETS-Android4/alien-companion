package com.gDyejeekis.aliencompanion.enums;

/**
 * Created by sound on 8/28/2015.
 */
public enum PostViewType {
    list(0), listReversed(1), smallCards(2), cards(3), cardDetails(4), classic(5), gallery(6);

    private final int value;

    public int value() {
        return value;
    }

    PostViewType(int value) {
        this.value = value;
    }
}
