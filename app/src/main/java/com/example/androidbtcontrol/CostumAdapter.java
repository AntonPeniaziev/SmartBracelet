package com.example.androidbtcontrol;

import android.content.Context;
import android.content.Intent;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Sapir Eltanani on 24/04/2017.
 */

public class CostumAdapter extends BaseAdapter {

    Context context;
    ArrayList<Patient> data;
    private static LayoutInflater inflater = null;
    int _listRow;

    public CostumAdapter(Context context, int layoutId) {
        // TODO Auto-generated constructor stub
        this.context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _listRow=layoutId;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return (data == null) ? 0 : data.size();
    }

    @Override
    public Patient getItem(int position) {
        // TODO Auto-generated method stub
        return (data == null) ? null : data.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null) {
            vi = inflater.inflate(_listRow, null);
        }

        TextView text = (TextView) vi.findViewById(R.id.braceletMAC);
        TextView bodyTemp = (TextView) vi.findViewById(R.id.bodyTemp);
        TextView bloodPressure = (TextView) vi.findViewById(R.id.bloodPressure);
        TextView json = (TextView) vi.findViewById(R.id.json);

        if (data != null) {
            text.setText(data.get(position).getBtMac());
            bodyTemp.setText(data.get(position).getBodyTemp());
            bloodPressure.setText(data.get(position).getBloodPressure());
            json.setText(data.get(position).getJson());
        }

        return vi;
    }

    public void setData(ArrayList<Patient> data) {
        this.data = data;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }
}
