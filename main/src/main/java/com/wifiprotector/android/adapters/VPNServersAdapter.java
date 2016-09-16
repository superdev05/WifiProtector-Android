package com.wifiprotector.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wifiprotector.android.model.VPNCountry;

import java.util.Collections;
import java.util.List;

import de.blinkt.openvpn.R;

public class VPNServersAdapter extends ArrayAdapter<VPNCountry> {

    public static final int AUTO_SELECTION_POSITION = 0;

    public VPNServersAdapter(Context context, List<VPNCountry> countries) {
        super(context, android.R.layout.simple_list_item_1, countries != null ? countries : Collections.<VPNCountry>emptyList());
    }

    @Override
    public int getCount() {
        if (super.getCount() == 0) {
            return 0;
        }
        return super.getCount() + 1;
    }

    @Override
    public VPNCountry getItem(int position) {
        if (position == 0) {
            return new VPNCountry();
        }
        return super.getItem(position - 1);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent, R.layout.cell_drop_down_server);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent, R.layout.cell_server);
    }

    private View createView(int position, View convertView, ViewGroup parent, int viewResource) {
        ViewHolder holder;

        if (convertView == null || convertView.getTag() == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(viewResource, parent, false);

            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == AUTO_SELECTION_POSITION) {
            holder.textView.setText(getContext().getString(R.string.auto_selection));
            holder.imageView.setVisibility(View.GONE);
        } else {
            VPNCountry country = getItem(position);
            holder.textView.setText(country.getDisplayName());
            holder.imageView.setVisibility(View.VISIBLE);
            holder.imageView.setImageResource(getDrawableResForCountry(country.getCountryCode()));
        }

        return convertView;
    }

    private int getDrawableResForCountry(String countryCode) {
        return getContext().getResources().getIdentifier(countryCode.toLowerCase(), "drawable", getContext().getPackageName());
    }

    private static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}

