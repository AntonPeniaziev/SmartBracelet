package com.example.androidbtcontrol;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends BaseAdapter {

    Context context;
    ArrayList<Patient> data;
    private static LayoutInflater inflater = null;
    int _listRow;

    public CustomAdapter(Context context, int layoutId) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null) {
            vi = inflater.inflate(_listRow, null);
        }
        TextView text = (TextView) vi.findViewById(R.id.braceletMAC);
        Typeface army_font = Typeface.createFromAsset(context.getAssets(),  "fonts/Army.ttf");
        text.setTypeface(army_font);
        TextView bodyTemp = (TextView) vi.findViewById(R.id.bodyTemp);
        TextView bloodPressure = (TextView) vi.findViewById(R.id.bloodPressure);
        TextView json = (TextView) vi.findViewById(R.id.json);

        if (data != null) {
            Button beepButton = (Button) vi.findViewById(R.id.beepBracelet);
            beepButton.setTypeface(army_font);
            setOnClickBeep(beepButton, (TentActivity)context, position);
            Button webInfo = (Button) vi.findViewById(R.id.webInfo);
            webInfo.setTypeface(army_font);
            setOnClickWeb(webInfo,(TentActivity)context, position);

            if (data.get(position).isConnected() == false) {
                text.setText("Bracelet: " + data.get(position).getBtMac() + "\n\n(CLICK TO CONNECT)");
                vi.setAlpha((float) 0.2);
                webInfo.setVisibility(View.GONE);
                beepButton.setVisibility(View.GONE);
                json.setVisibility(View.GONE);
                bodyTemp.setVisibility(View.GONE);
                bloodPressure.setVisibility(View.GONE);
            }
            else {
                vi.setAlpha((float) 1);
                webInfo.setVisibility(View.VISIBLE);
                beepButton.setVisibility(View.VISIBLE);
                json.setVisibility(View.VISIBLE);
                bodyTemp.setVisibility(View.VISIBLE);
                bloodPressure.setVisibility(View.VISIBLE);

                text.setText("Bracelet: " + data.get(position).getBtMac());
                bodyTemp.setText(data.get(position).getBodyTemp());
                bloodPressure.setText(data.get(position).getBloodPressure());
                String treatInfo = "";
                for (Treatment tr : data.get(position).getTreatmentsArray()) {
                    treatInfo += "Treatment received: " + tr.getName() + " at " + tr.getLastTime() + " of type " + tr.getType() + "\n";
                }
                json.setText(treatInfo);
            }
        }






        return vi;
    }

    private void setOnClickBeep(final Button btn, final TentActivity currActivity, final int position) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientBMac = data.get(position).getBtMac().toString();
                Toast.makeText(currActivity,
                        "beep sent to " + patientBMac,
                        Toast.LENGTH_SHORT).show();
                currActivity.getBt().addDataToBeSentByMac(patientBMac, "<6,0>");

            }
        });
    }

    private void setOnClickWeb(final Button btn, final TentActivity currActivity, final int position){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SeList<Treatment> patientTreatments = data.get(position).getTreatmentsArray();
//                new SendToMongodbTask().execute(patientTreatments);
                //Patient patient = data.get(position);


//                List<Patient> patient = new ArrayList<Patient>();
//                patient.add(data.get(position));
//                new SendToMongodbTask(currActivity).execute(patient);

//                TentActivity.updateTreatment(data.get(position).getBtMac(), data.get(position).getTreatmentsArray().get(0), "ss");
//                TentActivity.updateTreatment(data.get(position).getBtMac(), data.get(position).getTreatmentsArray().get(1), null);

            }
        });
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
