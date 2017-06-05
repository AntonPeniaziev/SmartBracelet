package com.example.androidbtcontrol;

/**
 * Created by Sapir Eltanani on 08/05/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
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
    ArrayList<View> views;
    //PatientInfo _patientInfo;


    public PatientInfoAdapter(Context context, int layoutId) {
        // TODO Auto-generated constructor stub
        _context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _listRow = layoutId;
        //_patientInfo = data;
        views = new ArrayList<>();
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
        views.add(vi);
        //EditText treatment = (EditText) vi.findViewById(R.id.treatment);
        EditText treatmentName = (EditText) vi.findViewById(R.id.treatment_name);
        EditText treatmentTime = (EditText) vi.findViewById(R.id.treatment_time);
        Typeface army_font = Typeface.createFromAsset(_context.getAssets(), "fonts/Army.ttf");
        //treatment.setTypeface(army_font);
        treatmentName.setTypeface(army_font);
        treatmentTime.setTypeface(army_font);
        if (_treatments != null) {
            // treatment.setText(_treatments.get(position).getLastTime()+ " " + _treatments.get(position).getName()  + " " );
            treatmentTime.setText(_treatments.get(position).getLastTime() + " ");
            treatmentName.setText(_treatments.get(position).getName() + " ");
        }
        return vi;
    }

    public void setData(ArrayList<Treatment> treatments) {
        this._treatments = treatments;
    }

    public void setMac(String mac) {
        this._patientMac = mac;
    }


//    void setOnClickEditText(final EditText treatmentName){
//        treatmentName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
//
//    }

    public void setEditTextEnabled() {
        // Create a border programmatically
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(Color.WHITE);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(2f);

        if(views != null) {
            for(int i=0; i< views.size(); ++i){
                EditText treatmentName = (EditText) views.get(i).findViewById(R.id.treatment_name);
                treatmentName.setEnabled(true);
                treatmentName.setFocusable(true);
                treatmentName.setClickable(true);
                treatmentName.setBackground(shape);
               // setOnClickEditText(treatmentName);
            }
        }
    }
}

