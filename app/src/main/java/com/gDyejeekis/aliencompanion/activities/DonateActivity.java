package com.gDyejeekis.aliencompanion.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Purchase;
import com.gDyejeekis.aliencompanion.BuildConfig;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.InfoDialogFragment;
import com.gDyejeekis.aliencompanion.models.Donation;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.adapters.DonationListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by George on 1/22/2018.
 */

public class DonateActivity extends ToolbarActivity implements View.OnClickListener {

    public static final String TAG = "DonateActivity";

    public static final float[] DONATION_AMOUNTS = {0.99f, 1.99f, 2.99f, 3.99f, 4.99f, 5.99f, 6.99f, 7.99f, 8.99f, 9.99f};

    public static final String DONATIONS_DB_NODE = "donations";
    public static final String PROD_DONATIONS_DB_NODE = DONATIONS_DB_NODE + "/donations-prod";
    public static final String TEST_DONATIONS_DB_NODE = DONATIONS_DB_NODE + "/donations-test";

    public static final int BILLING_REQUEST_CODE = 15273;

    private EditText nameField;
    private EditText messageField;
    private TextView amountText;
    private CheckBox makePublic;
    private LinearLayout layoutPastDonations;
    private ListView donationsListView;
    private int amountIndex;
    private ImageView decrAmountBtn;
    private ImageView incrAmountBtn;
    private Button donateButton;

    private DatabaseReference database;

    private IabHelper iabHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);
        initToolbar();
        initFields();
        initDonationForm();
        refreshDonationsList();
        bindBillingService();
    }

    private void bindBillingService() {
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh no, there was a problem.
                    Log.e(TAG, "Error setting up In-app Billing: " + result);
                    setDonationFormEnabled(false);
                    ToastUtils.showSnackbar(DonateActivity.this.getCurrentFocus(),
                            "There was an error connecting to Google Play services. You won't be able to donate at this time.",
                            Snackbar.LENGTH_LONG);
                }
                // Hooray, IAB is fully set up!
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_donate, menu);
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (iabHelper != null) {
            iabHelper.disposeWhenFinished();
            iabHelper = null;
        }
    }

    private void initFields() {
        nameField = findViewById(R.id.editText_donate_name);
        messageField = findViewById(R.id.editText_donate_message);
        amountText = findViewById(R.id.textView_donate_amount);
        makePublic = findViewById(R.id.checkBox_donate_public);
        layoutPastDonations = findViewById(R.id.layout_past_donations);
        donationsListView = findViewById(R.id.listView_donations);
        decrAmountBtn = findViewById(R.id.imageView_donate_decrease_amount);
        incrAmountBtn = findViewById(R.id.imageView_donate_increase_amount);
        decrAmountBtn.setOnClickListener(this);
        incrAmountBtn.setOnClickListener(this);
        donateButton = findViewById(R.id.button_donate);
        donateButton.setOnClickListener(this);

        String currentNode = BuildConfig.DEBUG ? TEST_DONATIONS_DB_NODE : PROD_DONATIONS_DB_NODE;
        database = FirebaseDatabase.getInstance().getReference(currentNode);

        String base64EncodedPublicKey = "";
        // TODO compute your public key and store it in base64EncodedPublicKey
        iabHelper = new IabHelper(this, base64EncodedPublicKey);
    }

    private void updateModifyAmountButtons() {
        styleModifyAmountButton(decrAmountBtn);
        styleModifyAmountButton(incrAmountBtn);
    }

    private void styleModifyAmountButton(ImageView imageView) {
        boolean increase = imageView.getId()==R.id.imageView_donate_increase_amount;
        boolean enabled = increase ? amountIndex < DONATION_AMOUNTS.length-1 : amountIndex > 0;
        int drawable;
        float alpha;
        switch (MyApplication.currentBaseTheme) {
            case MyApplication.LIGHT_THEME:
                drawable = increase ? R.drawable.ic_add_circle_outline_black_24dp : R.drawable.ic_remove_circle_outline_black_24dp;
                alpha = enabled ? 0.54f : 0.27f;
                break;
            case MyApplication.DARK_THEME_LOW_CONTRAST:
                drawable = increase ? R.drawable.ic_add_circle_outline_white_24dp : R.drawable.ic_remove_circle_outline_white_24dp;
                alpha = enabled ? 0.6f : 0.3f;
                break;
            default:
                drawable = increase ? R.drawable.ic_add_circle_outline_white_24dp : R.drawable.ic_remove_circle_outline_white_24dp;
                alpha = enabled ? 1f : 0.5f;
                break;
        }
        imageView.setImageResource(drawable);
        imageView.setAlpha(alpha);
    }

    private void initDonationForm() {
        amountIndex = 1;
        nameField.setText("");
        messageField.setText("");
        amountText.setText(String.valueOf(DONATION_AMOUNTS[amountIndex]));
        makePublic.setChecked(true);
        updateModifyAmountButtons();
    }

    private void setDonationFormEnabled(boolean flag) {
        nameField.setEnabled(flag);
        messageField.setEnabled(flag);
        makePublic.setEnabled(flag);
        decrAmountBtn.setEnabled(flag);
        incrAmountBtn.setEnabled(flag);
        donateButton.setEnabled(flag);
    }

    private void startGooglePlayPurchase() {
        setDonationFormEnabled(false);
        //ToastUtils.showToast(this, "Just a moment..");
        final IabHelper.OnIabPurchaseFinishedListener listener = new IabHelper.OnIabPurchaseFinishedListener() {
            @Override
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                if (result.isSuccess()) {
                    Log.d(TAG, "Iab purchase finished successfully");
                    consumePurchase(purchase);
                    onSuccessfulPurchase(purchase);
                } else {
                    Log.e(TAG, "Error completing Iab purchase: " + result);
                    onFailedPurchase();
                }
                setDonationFormEnabled(true);
            }
        };
        try {
            iabHelper.launchPurchaseFlow(this, getItemSKU(), BILLING_REQUEST_CODE, listener);
        } catch (Exception e) {
            Log.e(TAG, "Exception thrown during launchPurchaseFlow()");
            e.printStackTrace();
            onFailedPurchase();
            setDonationFormEnabled(true);
        }
    }

    private String getItemSKU() {
        return "donation_" + String.valueOf(DONATION_AMOUNTS[amountIndex]);
    }

    private void consumePurchase(Purchase purchase) {
        Log.d(TAG, "Consuming purchase..");
        final IabHelper.OnConsumeFinishedListener listener = new IabHelper.OnConsumeFinishedListener() {
            @Override
            public void onConsumeFinished(Purchase purchase, IabResult result) {
                if (result.isSuccess()) {
                    Log.d(TAG, "Purchase consumed successfully");
                    // provision the in-app purchase to the user
                    // (for example, credit 50 gold coins to player's character)
                }
                else {
                    // handle error
                    Log.e(TAG, "Error consuming purchase: " + result);
                }
            }
        };
        try {
            iabHelper.consumeAsync(purchase, listener);
        } catch (Exception e) {
            Log.e(TAG, "Exception thrown during consumeAsync()");
            e.printStackTrace();
        }
    }

    /* TODO
       Security Recommendation: When you receive the purchase response from Google Play,
       ensure that you check the returned data signature and the orderId. Verify that the
       orderId exists and is a unique value that you have not previously processed. For
       added security, you should perform purchase validation on your own secure server.
    */

    private void onSuccessfulPurchase(Purchase purchase) {
        //String donationId = database.push().getKey(); // generate firebase key as donationId
        Donation donation = new Donation(purchase.getPurchaseTime(), nameField.getText().toString(),
                messageField.getText().toString(), DONATION_AMOUNTS[amountIndex], makePublic.isChecked());
        database.child(purchase.getOrderId()).setValue(donation);
        initDonationForm();
        refreshDonationsList();
        final String message =
                "Donation received! Thanks for your support :)";
        ToastUtils.showToast(this, message, Toast.LENGTH_LONG);
    }

    private void onFailedPurchase() {
        final String message =
                "There was an error processing your donation (you have not been charged)";
        ToastUtils.showToast(this, message, Toast.LENGTH_LONG);
    }

    private void refreshDonationsList() {
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Donation> donations = parseFirebaseDonationData(dataSnapshot);
                setPastDonations(donations);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                Log.e(TAG, databaseError.getDetails());
            }
        });
    }

    private List<Donation> parseFirebaseDonationData(DataSnapshot dataSnapshot) {
        List<Donation> donations = new ArrayList<>();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            Donation donation = ds.getValue(Donation.class);
            if (donation!=null && donation.showToPublic())
                donations.add(donation);
        }
        Collections.sort(donations, new Comparator<Donation>() {
            @Override
            public int compare(Donation d1, Donation d2) {
                if (d1.createdAt == d2.createdAt)
                    return 0;
                return d1.createdAt > d2.createdAt ? -1 : 1;
            }
        });
        return donations;
    }

    private void setPastDonations(List<Donation> donations) {
        if (donations == null || donations.isEmpty()) {
            layoutPastDonations.setVisibility(View.GONE);
        } else {
            layoutPastDonations.setVisibility(View.VISIBLE);
            donationsListView.setAdapter(new DonationListAdapter(this, donations));
            //GeneralUtils.setListViewHeightBasedOnChildren(listView);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_donate_decrease_amount:
                decreaseAmount();
                updateModifyAmountButtons();
                break;
            case R.id.imageView_donate_increase_amount:
                increaseAmount();
                updateModifyAmountButtons();
                break;
            case R.id.button_donate:
                GeneralUtils.hideSoftKeyboard(this);
                startGooglePlayPurchase();
                break;
        }
    }

    private void increaseAmount() {
        if (amountIndex < DONATION_AMOUNTS.length-1) {
            amountIndex++;
            amountText.setText(String.valueOf(DONATION_AMOUNTS[amountIndex]));
        }
    }

    private void decreaseAmount() {
        if (amountIndex > 0) {
            amountIndex--;
            amountText.setText(String.valueOf(DONATION_AMOUNTS[amountIndex]));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_donate_info) {
            InfoDialogFragment.showDialog(getSupportFragmentManager(),
                    getResources().getString(R.string.about_donations_title),
                    getResources().getString(R.string.about_donations));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
