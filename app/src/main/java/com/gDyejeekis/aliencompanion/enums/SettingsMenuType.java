package com.gDyejeekis.aliencompanion.enums;

import android.preference.PreferenceFragment;

import com.gDyejeekis.aliencompanion.fragments.settings_fragments.AppearanceSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.CommentsSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.HeadersSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.LinkHandlingSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.NavigationSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.OtherSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.PostsCommentsSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.PostsSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.SyncSettingsFragment;

/**
 * Created by George on 9/10/2016.
 */
public enum SettingsMenuType {

    headers("Settings"),
    appearance("Appearance"),
    navigation("Navigation"),
    posts("Posts"),
    comments("Comments"),
    postsAndComments("Posts and comments"),
    sync("Sync options"),
    linkHandling("Link handling"),
    other("Other");

    public static PreferenceFragment getSettingsFragment(SettingsMenuType type) {
        switch (type) {
            case headers:
                return new HeadersSettingsFragment();
            case appearance:
                return new AppearanceSettingsFragment();
            case navigation:
                return new NavigationSettingsFragment();
            case posts:
                return new PostsSettingsFragment();
            case comments:
                return new CommentsSettingsFragment();
            case postsAndComments:
                return new PostsCommentsSettingsFragment();
            case sync:
                return new SyncSettingsFragment();
            case linkHandling:
                return new LinkHandlingSettingsFragment();
            case other:
                return new OtherSettingsFragment();
        }
        return null;
    }

    private final String value;

    SettingsMenuType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
