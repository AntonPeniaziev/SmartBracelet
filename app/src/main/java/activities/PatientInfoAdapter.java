package activities;

/**
 * Created by Sapir Eltanani on 08/05/2017.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.SmartBracelet.R;
import logic.Treatment;

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
    public Boolean setDiseable;
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
    public Treatment getItem(int i) {
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
        TextView treatmentName = (TextView) vi.findViewById(R.id.treatment_name);
        TextView treatmentTime = (TextView) vi.findViewById(R.id.treatment_time);
        Button removeTreatment = (Button) vi.findViewById(R.id.treatmentRemove);
        Typeface army_font = Typeface.createFromAsset(_context.getAssets(), "fonts/Assistant-Bold.ttf");
        //treatment.setTypeface(army_font);
        treatmentName.setTypeface(army_font);
        treatmentTime.setTypeface(army_font);
        removeTreatment.setTypeface(army_font);

        setOnclickRemoveTreatment(removeTreatment, _treatments.get(position));

        if (_treatments != null) {
            // treatment.setText(_treatments.get(position).getLastTime()+ " " + _treatments.get(position).getName()  + " " );
            treatmentTime.setText(_treatments.get(position).getLastTime() + " ");
            treatmentName.setText(_treatments.get(position).getName() + " ");
        }
        if(setDiseable){
            setTextViewDisabled();
        }
        return vi;
    }

    public void setData(ArrayList<Treatment> treatments) {
        this._treatments = treatments;
    }

    public void setMac(String mac) {
        this._patientMac = mac;
    }



//    public void setOnClickEditText(final TextView treatmentName){
//        treatmentName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                PatientInfoActivity instance = PatientInfoActivity.getInstance();
//                instance.createDialogEditTreatment(treatmentName);
//            }
//        });
//
//    }


    public void setTextViewEnabled() {
        // Create a border programmatically
        setDiseable = false;
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(Color.WHITE);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(2f);

        if(views != null) {
            for(int i=0; i< views.size(); ++i){
                LinearLayout layout = (LinearLayout) views.get(i).findViewById(R.id.treatmentInfo);
               layout.setEnabled(false);
                layout.setClickable(false);
                layout.setBackground(shape);
                RelativeLayout layout1 = (RelativeLayout) views.get(i).findViewById(R.id.row);
                layout1.setEnabled(false);
                layout1.setClickable(false);
                Button removeButton = (Button) views.get(i).findViewById(R.id.treatmentRemove);
                removeButton.setFocusable(false);
                //TextView treatmentName = (TextView) views.get(i).findViewById(R.id.treatment_name);
//                treatmentName.setEnabled(true);
//                treatmentName.setFocusable(true);
//                treatmentName.setClickable(true);
//                treatmentName.setBackground(shape);
                //setOnClickEditText(treatmentName);
            }
        }
    }


    public void setTextViewDisabled() {
        if(views != null) {
            for(int i=0; i< views.size(); ++i){
                LinearLayout layout = (LinearLayout) views.get(i).findViewById(R.id.treatmentInfo);
                layout.setBackgroundResource(0);
                layout.setEnabled(true);
                layout.setClickable(true);
                RelativeLayout layout1 = (RelativeLayout) views.get(i).findViewById(R.id.row);
                layout1.setEnabled(true);
                layout1.setClickable(true);
                Button removeButton = (Button) views.get(i).findViewById(R.id.treatmentRemove);
                removeButton.setFocusable(true);
            }
        }
    }


    public ArrayList<Treatment> getTreatments(){
        return _treatments;
    }

    void setOnclickRemoveTreatment(Button removeButton, final Treatment treatment){
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "Delete treatment?";
                String title = "Smart Bracelet";
                DialogInterface.OnClickListener clickYes = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TentActivity.updateTreatment(_patientMac, treatment, null);
                    }
                };

                DialogInterface.OnClickListener clickNo = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                };

                android.support.v7.app.AlertDialog.Builder dlgAlert  = new android.support.v7.app.AlertDialog.Builder(PatientInfoActivity.getInstance());
                dlgAlert.setMessage(message);
                dlgAlert.setTitle(title);
                dlgAlert.setPositiveButton("Yes",clickYes);
                dlgAlert.setNegativeButton("No", clickNo);
                dlgAlert.show();
            }
        });
    }


}

