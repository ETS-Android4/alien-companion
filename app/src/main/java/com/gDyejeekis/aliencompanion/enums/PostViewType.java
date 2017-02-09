package com.gDyejeekis.aliencompanion.enums;

import com.gDyejeekis.aliencompanion.R;

/**
 * Created by sound on 8/28/2015.
 */
public enum PostViewType {

    /**
     * View type values must correspond to their order in menu layout (except cardDetails)
     */
    list(0), listReversed(1), smallCards(2), cards(3), classic(4), gallery(5), cardDetails(99);

    private final int value;

    public int value() {
        return value;
    }

    PostViewType(int value) {
        this.value = value;
    }

    public static int getLayoutResource(int value) {
        switch (value) {
            case 0:
                return R.layout.post_list_item;
            case 1:
                return R.layout.post_list_item_reversed;
            case 2:
                return R.layout.small_card_new;
            case 3:
                return R.layout.post_list_item_card;
            case 4:
                return R.layout.post_list_item_classic;
            case 5:
                return R.layout.post_list_item_gallery;
            case 99:
                return R.layout.post_details_card;
            default:
                throw new IllegalArgumentException("No corresponding layout found for this viewtype");
        }
    }

    public static int getLayoutResource(PostViewType viewType) {
        return getLayoutResource(viewType.value());
    }

    public static boolean hasVisibleListDivider(int value) {
        return value == 0 || value == 1;
    }

    public static boolean hasVisibleListDivider(PostViewType viewType) {
        return hasVisibleListDivider(viewType.value());
    }

}
