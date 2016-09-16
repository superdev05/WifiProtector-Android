package com.wifiprotector.android.dialog.iap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wifiprotector.android.VPNApplication;
import com.wifiprotector.android.activities.BaseBillingActivity;
import com.wifiprotector.android.dialog.BaseDialogFragment;
import com.wifiprotector.android.iab.IabHelper;
import com.wifiprotector.android.iab.IabResult;
import com.wifiprotector.android.iab.Inventory;
import com.wifiprotector.android.iab.SkuDetails;
import com.wifiprotector.android.log.SecuredLogManager;

import java.util.LinkedList;
import java.util.List;

import de.blinkt.openvpn.R;
import timber.log.Timber;

public abstract class SubscriptionChooserDialogFragment extends BaseDialogFragment {

    private SubscribeListener listener;
    private SubscriptionAdapter adapter;
    private List<SkuDetails> skuDetails = new LinkedList<SkuDetails>();

    private IabHelper iabHelper;

    IabHelper.QueryInventoryFinishedListener gotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if(inventory == null) return;

            SkuDetails monthlySubscriptionDetails = inventory.getSkuDetails(BaseBillingActivity.SKU_FULL_VERSION_MONTHLY);
            SkuDetails yearlySubscriptionDetails = inventory.getSkuDetails(BaseBillingActivity.SKU_FULL_VERSION_YEARLY);
            skuDetails.clear();
            if (monthlySubscriptionDetails != null) {
                skuDetails.add(monthlySubscriptionDetails);
            }
            if (yearlySubscriptionDetails != null) {
                skuDetails.add(yearlySubscriptionDetails);
            }

            iabHelper.dispose();
            iabHelper = null;

            adapter.notifyDataSetChanged();
        }
    };

    public interface SubscribeListener {
        void onSubscriptionChosen(String sku);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (SubscribeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SubscribeListener");
        }

        initIab(activity);
    }

    private void initIab(Activity activity) {
        iabHelper = new IabHelper(activity, VPNApplication.getPublicLicenceKey());
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    SecuredLogManager.getLogManager().writeLog(3, "WFProctector", "Problem setting up In-app Billing: " + result);
                    return;
                }
                List<String> skus = new LinkedList<String>();
                skus.add(BaseBillingActivity.SKU_FULL_VERSION_MONTHLY);
                skus.add(BaseBillingActivity.SKU_FULL_VERSION_YEARLY);
                iabHelper.queryInventoryAsync(true, skus, gotInventoryListener);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new SubscriptionAdapter(getActivity(), skuDetails);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(buildView(R.layout.dialog_subscriptions));

        return builder.create();
    }

    protected View buildView(int resId) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(resId, null, false);

        ListView listView = (ListView) view.findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onSubscriptionChosen(skuDetails.get(position).getSku());
            }
        });

        return view;
    }

    private static class SubscriptionAdapter extends ArrayAdapter<SkuDetails> {

        public SubscriptionAdapter(Context context, List<SkuDetails> skuDetails) {
            super(context, android.R.layout.simple_list_item_1, skuDetails);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.cell_subscription, parent, false);
            } else {
                view = convertView;
            }
            TextView line1 = (TextView) view.findViewById(R.id.title);
            TextView line2 = (TextView) view.findViewById(R.id.description);

            line1.setText(getItem(position).getTitle() + ", " + getItem(position).getPrice());
            line2.setText(getItem(position).getDescription());

            return view;
        }
    }
}
