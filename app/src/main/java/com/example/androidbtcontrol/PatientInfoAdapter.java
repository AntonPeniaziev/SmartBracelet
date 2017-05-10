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


/**
 * Created by Sapir Eltanani on 08/05/2017.
 */

public class PatientInfoAdapter extends BaseAdapter {
    Context _context;
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
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null) {
            vi = inflater.inflate(_listRow, null);
        }

        TextView braceletID = (TextView) vi.findViewById(R.id.braceletID);
       // braceletID.setText(_patientInfo.getID());
        return vi;
    }
}

