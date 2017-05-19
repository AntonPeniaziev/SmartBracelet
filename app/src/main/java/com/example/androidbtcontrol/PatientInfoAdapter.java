package com.example.androidbtcontrol;

/**
 * Created by Sapir Eltanani on 08/05/2017.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Sapir Eltanani on 08/05/2017.
 */

public class PatientInfoAdapter extends BaseAdapter {
    Context _context;
    String _patientMac;
    ArrayList<Treatment> _treatments;
    private static LayoutInflater inflater = null;
    int _listRow;
    //PatientInfo _patientInfo;


    public PatientInfoAdapter(Context context, int layoutId) {
        // TODO Auto-generated constructor stub
        _context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _listRow=layoutId;
        //_patientInfo = data;
    }

    @Override
    public int getCount() {
        return (_treatments == null) ? 0 : _treatments.size();
    }

    @Override
    public Object getItem(int i) {
        return (_treatments == null) ? null : _treatments.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null) {
            vi = inflater.inflate(_listRow, null);
        }

        TextView treatment = (TextView) vi.findViewById(R.id.treatment);
        if (_treatments != null) {
            treatment.setText(_treatments.get(position).getName());
        }

        return vi;
    }

    public void setData(ArrayList<Treatment> treatments) {
        this._treatments = treatments;
    }

    public void setMac(String mac) {
        this._patientMac = mac;
    }
}

