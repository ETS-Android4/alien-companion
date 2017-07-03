package com.gDyejeekis.aliencompanion.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.models.filters.DomainFilter;
import com.gDyejeekis.aliencompanion.models.filters.Filter;
import com.gDyejeekis.aliencompanion.models.filters.FilterProfile;
import com.gDyejeekis.aliencompanion.models.filters.FlairFilter;
import com.gDyejeekis.aliencompanion.models.filters.SelfTextFilter;
import com.gDyejeekis.aliencompanion.models.filters.SubredditFilter;
import com.gDyejeekis.aliencompanion.models.filters.TitleFilter;
import com.gDyejeekis.aliencompanion.models.filters.UserFilter;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.adapters.RemovableItemListAdapter;

import java.util.List;

/**
 * Created by George on 6/21/2017.
 */

public class EditFilterProfileActivity extends ToolbarActivity implements View.OnClickListener, TextView.OnEditorActionListener{

    private FilterProfile profile;
    private boolean isNewProfile;
    private EditText nameField;
    private EditText domainField;
    private EditText titleField;
    private EditText flairField;
    private EditText selfTextField;
    private EditText subredditField;
    private EditText userField;
    private EditText subRestrField;
    private EditText multiRestrField;
    private ListView domains;
    private ListView titles;
    private ListView flairs;
    private ListView selfTexts;
    private ListView subreddits;
    private ListView users;
    private ListView subRestrctions;
    private ListView multiRestrctions;

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_filter_profile);
        initToolbar();

        initFields();
        initProfile((FilterProfile) getIntent().getSerializableExtra("profile"));
    }

    private void initFields() {
        nameField = (EditText) findViewById(R.id.editText_profile_name);
        domainField = (EditText) findViewById(R.id.editText_domain_filter);
        titleField = (EditText) findViewById(R.id.editText_title_filter);
        flairField = (EditText) findViewById(R.id.editText_flair_filter);
        selfTextField = (EditText) findViewById(R.id.editText_self_text_filter);
        subredditField = (EditText) findViewById(R.id.editText_subreddit_filter);
        userField = (EditText) findViewById(R.id.editText_user_filter);
        subRestrField = (EditText) findViewById(R.id.editText_subreddit_restrction);
        multiRestrField = (EditText) findViewById(R.id.editText_multireddit_restrction);
        domains = (ListView) findViewById(R.id.listView_domain_filters);
        titles = (ListView) findViewById(R.id.listView_title_filters);
        flairs = (ListView) findViewById(R.id.listView_flair_filters);
        selfTexts = (ListView) findViewById(R.id.listView_self_text_filters);
        subreddits = (ListView) findViewById(R.id.listView_subreddit_filters);
        users = (ListView) findViewById(R.id.listView_user_filters);
        subRestrctions = (ListView) findViewById(R.id.listView_subreddit_restrictions);
        multiRestrctions = (ListView) findViewById(R.id.listView_multireddit_restrictions);
        ImageView addDomain = (ImageView) findViewById(R.id.button_add_domain_filter);
        ImageView addTitle = (ImageView) findViewById(R.id.button_add_title_filter);
        ImageView addSelfText = (ImageView) findViewById(R.id.button_add_self_text_filter);
        ImageView addFlair = (ImageView) findViewById(R.id.button_add_flair_filter);
        ImageView addSubreddit = (ImageView) findViewById(R.id.button_add_subreddit_filter);
        ImageView addUser = (ImageView) findViewById(R.id.button_add_user_filter);
        ImageView addSubRestr = (ImageView) findViewById(R.id.button_add_subreddit_restrction);
        ImageView addMultiRestr = (ImageView) findViewById(R.id.button_add_multireddit_restrction);
        Button saveButton = (Button) findViewById(R.id.button_save_changes);

        domainField.setOnEditorActionListener(this);
        titleField.setOnEditorActionListener(this);
        flairField.setOnEditorActionListener(this);
        selfTextField.setOnEditorActionListener(this);
        subredditField.setOnEditorActionListener(this);
        userField.setOnEditorActionListener(this);
        subRestrField.setOnEditorActionListener(this);
        multiRestrField.setOnEditorActionListener(this);

        addDomain.setOnClickListener(this);
        addTitle.setOnClickListener(this);
        addSelfText.setOnClickListener(this);
        addFlair.setOnClickListener(this);
        addSubreddit.setOnClickListener(this);
        addUser.setOnClickListener(this);
        addSubRestr.setOnClickListener(this);
        addMultiRestr.setOnClickListener(this);
        saveButton.setOnClickListener(this);

    }

    private void initProfile(FilterProfile profile) {
        isNewProfile = (profile==null);

        if(isNewProfile) {
            this.profile = new FilterProfile();
            getSupportActionBar().setTitle("Create filter profile");
            nameField.requestFocus();
        }
        else {
            this.profile = profile;
            getSupportActionBar().setTitle("Edit filter profile");
            nameField.setText(profile.getName());
        }
        refreshFilters();
        refreshSubredditRestrctions();
        refreshMultiredditRestrctions();
    }

    private void refreshFilters() {
        refreshDomainFilters();
        refreshTitleFilters();
        refreshFlairFilters();
        refreshSelfTextFilters();
        refreshSubredditFilters();
        refreshUserFilters();
    }

    private void refreshFilters(Class<? extends Filter> cls) {
        if(cls == DomainFilter.class) {
            refreshDomainFilters();
        }
        else if(cls == TitleFilter.class) {
            refreshTitleFilters();
        }
        else if(cls == FlairFilter.class) {
            refreshFlairFilters();
        }
        else if(cls == SelfTextFilter.class) {
            refreshSelfTextFilters();
        }
        else if(cls == SubredditFilter.class) {
            refreshSubredditFilters();
        }
        else if(cls == UserFilter.class) {
            refreshUserFilters();
        }
    }

    private void refreshFilterList(Class<? extends Filter> cls, ListView listView) {
        List<String> filters = profile.getFilterStrings(cls);
        if(filters == null || filters.isEmpty()) {
            listView.setVisibility(View.GONE);
        }
        else {
            listView.setVisibility(View.VISIBLE);
            listView.setAdapter(new RemovableItemListAdapter(this, filters, RemovableItemListAdapter.FILTERS));
            GeneralUtils.setListViewHeightBasedOnChildren(listView);
        }
    }

    private void refreshDomainFilters() {
        refreshFilterList(DomainFilter.class, domains);
    }

    private void refreshFlairFilters() {
        refreshFilterList(FlairFilter.class, flairs);
    }

    private void refreshTitleFilters() {
        refreshFilterList(TitleFilter.class, titles);
    }

    private void refreshSelfTextFilters() {
        refreshFilterList(SelfTextFilter.class, selfTexts);
    }

    private void refreshSubredditFilters() {
        refreshFilterList(SubredditFilter.class, subreddits);
    }

    private void refreshUserFilters() {
        refreshFilterList(UserFilter.class, users);
    }

    private void refreshSubredditRestrctions() {
        if(profile.getSubredditRestrictions() == null || profile.getSubredditRestrictions().isEmpty()) {
            subRestrctions.setVisibility(View.GONE);
        }
        else {
            subRestrctions.setVisibility(View.VISIBLE);
            subRestrctions.setAdapter(new RemovableItemListAdapter(this, profile.getSubredditRestrictions(), RemovableItemListAdapter.SUBREDDIT_RESTRICTIONS));
            GeneralUtils.setListViewHeightBasedOnChildren(subRestrctions);
        }
    }

    private void refreshMultiredditRestrctions() {
        if(profile.getMultiredditRestrictions() == null || profile.getMultiredditRestrictions().isEmpty()) {
            multiRestrctions.setVisibility(View.GONE);
        }
        else {
            multiRestrctions.setVisibility(View.VISIBLE);
            multiRestrctions.setAdapter(new RemovableItemListAdapter(this, profile.getMultiredditRestrictions(), RemovableItemListAdapter.MULTIREDDIT_RESTRCTIONS));
            GeneralUtils.setListViewHeightBasedOnChildren(multiRestrctions);
        }
    }

    private void addFilter(Class<? extends Filter> cls, EditText field, String hint, String warning) {
        String filterText = field.getText().toString();
        filterText = filterText.replaceAll("\\s","");
        if(filterText.isEmpty()) {
            field.setText("");
            field.setHint(warning);
            field.setHintTextColor(Color.RED);
        }
        else if(profile.containsFilter(cls, filterText)) {
            clearField(field, hint);
            ToastUtils.showSnackbarOverToast(this, "Filter already in list");
        }
        else {
            clearField(field, hint);
            profile.addFilter(cls, filterText);
            refreshFilters(cls);
        }
    }

    private void clearField(EditText field, String hint) {
        field.setText("");
        field.setHint(hint);
        field.setHintTextColor(MyApplication.textHintColor);
    }

    private void addDomainFilter() {
        addFilter(DomainFilter.class, domainField, "domain", "enter domain");
    }

    private void addFlairFilter() {
        addFilter(FlairFilter.class, flairField, "flair", "enter flair");
    }

    private void addTitleFilter() {
        addFilter(TitleFilter.class, titleField, "keyword / phrase", "enter a keyword or phrase");
    }

    private void addSelfTextFilter() {
        addFilter(SelfTextFilter.class, selfTextField, "keyword / phrase", "enter a keyword or phrase");
    }

    private void addSubredditFilter() {
        addFilter(SubredditFilter.class, subredditField, "subreddit", "enter subreddit");
    }

    private void addUserFilter() {
        addFilter(UserFilter.class, userField, "user", "enter user");
    }

    private void addSubredditRestrction() {
        String restriction = subRestrField.getText().toString();
        restriction = restriction.replaceAll("\\s","");
        if(restriction.isEmpty()) {
            subRestrField.setText("");
            subRestrField.setHint("enter subreddit");
            subRestrField.setHintTextColor(Color.RED);
        }
        else if(profile.containsSubredditRestriction(restriction)) {
            clearField(subRestrField, "subreddit");
            ToastUtils.showSnackbarOverToast(this, "Subreddit already in list");
        }
        else {
            clearField(subRestrField, "subreddit");
            profile.addSubredditRestriction(restriction);
            refreshSubredditRestrctions();
        }
    }

    private void addMultiredditRestrction() {
        String restriction = multiRestrField.getText().toString();
        restriction = restriction.replaceAll("\\s","");
        if(restriction.isEmpty()) {
            multiRestrField.setText("");
            multiRestrField.setHint("enter multireddit");
            multiRestrField.setHintTextColor(Color.RED);
        }
        else if(profile.containsMultiredditRestrction(restriction)) {
            clearField(multiRestrField, "multireddit");
            ToastUtils.showSnackbarOverToast(this, "Multireddit already in list");
        }
        else {
            clearField(multiRestrField, "multireddit");
            profile.addMultiredditRestriction(restriction);
            refreshMultiredditRestrctions();
        }
    }

    public void removeFilter(int index) {
        Filter filter = profile.getFilters().get(index);
        profile.removeFilter(filter);
        refreshFilters(filter.getClass());
    }

    public void removeSubRestriction(int index) {
        profile.removeSubredditRestrction(index);
        refreshSubredditRestrctions();
    }

    public void removeMultiRestriction(int index) {
        profile.removeMultiredditRestrction(index);
        refreshMultiredditRestrctions();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add_domain_filter:
                addDomainFilter();
                break;
            case R.id.button_add_title_filter:
                addTitleFilter();
                break;
            case R.id.button_add_flair_filter:
                addFlairFilter();
                break;
            case R.id.button_add_self_text_filter:
                addSelfTextFilter();
                break;
            case R.id.button_add_subreddit_filter:
                addSubredditFilter();
                break;
            case R.id.button_add_user_filter:
                addUserFilter();
                break;
            case R.id.button_add_subreddit_restrction:
                addSubredditRestrction();
                break;
            case R.id.button_add_multireddit_restrction:
                addMultiredditRestrction();
                break;
            case R.id.button_save_changes:
                String name = nameField.getText().toString();
                if(name.trim().isEmpty()) {
                    if(isNewProfile) {
                        profile.setName(getIntent().getStringExtra("defaultName"));
                    }
                } else {
                    profile.setName(name);
                }

                profile.save(this, isNewProfile);
                finish();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.editText_domain_filter:
                addDomainFilter();
                return true;
            case R.id.editText_title_filter:
                addTitleFilter();
                return true;
            case R.id.editText_flair_filter:
                addFlairFilter();
                return true;
            case R.id.editText_self_text_filter:
                addSelfTextFilter();
                return true;
            case R.id.editText_subreddit_filter:
                addSubredditFilter();
                return true;
            case R.id.editText_user_filter:
                addUserFilter();
                return true;
            case R.id.editText_subreddit_restrction:
                addSubredditRestrction();
                return true;
            case R.id.editText_multireddit_restrction:
                addMultiredditRestrction();
                return true;
        }
        return false;
    }

}
