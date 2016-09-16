package com.wifiprotector.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.wifiprotector.android.activities.MainActivity;
import com.wifiprotector.android.adapters.VPNServersAdapter;
import com.wifiprotector.android.log.SecuredLogManager;
import com.wifiprotector.android.model.VPNCountry;

import java.util.ArrayList;
import java.util.List;

import de.blinkt.openvpn.R;

/**
 * Created by panda on 5/13/15.
 */
public class ReportDialog extends Dialog{

    private Context mainContext;
    private EditText editEmailAddress;
    private Spinner spinnerCountries;
    private EditText editComment;
    private TextView textError;


    private RadioGroup radioCategories;
    private RadioGroup radioFrequency;

    private Button  btnSend;

    private MainActivity gActivity;
    private List<VPNCountry> vpnCountries;// = new ArrayList<VPNCountry>();

    public ReportDialog(Context context) {
        super(context);

        mainContext = context;
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.report_dialog);

        editEmailAddress = (EditText) findViewById(R.id.editMailAddress);
        editComment = (EditText) findViewById(R.id.editComment);
        spinnerCountries = (Spinner) findViewById(R.id.spinnerCountries);
        radioCategories = (RadioGroup) findViewById(R.id.rGroupCategory);
        radioFrequency = (RadioGroup) findViewById(R.id.rGroupFrequency);
        btnSend = (Button) findViewById(R.id.btnSend);
        textError = (TextView) findViewById(R.id.textError);
        textError.setText("");
        btnSend.setOnClickListener(m_sendClickListener);

    }

    public void setActivity(MainActivity activity){
        gActivity = activity;
    }

    public void setVPNCountries(List<VPNCountry> countries){
        vpnCountries = countries;
        initializeSpinner();
    }

    private void initializeSpinner(){
        VPNServersAdapter serversAdapter = new VPNServersAdapter(gActivity, vpnCountries);
        spinnerCountries.setAdapter(serversAdapter);

       // spinnerCountries.setSelection(VPNServersAdapter.AUTO_SELECTION_POSITION);
    }

    View.OnClickListener m_sendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendLogs();

        }
    };

    private void sendLogs(){
        String strComment = editComment.getText().toString();
        String strEmail = editEmailAddress.getText().toString();

        if(strEmail == null || strEmail.length() == 0){
            textError.setText("Please type email address!");
            return;
        }

        if(strEmail.contains("@") == false){
            textError.setText("Invalid email address!");
            return;
        }

        SecuredLogManager.getLogManager().sendLog(strEmail, strComment);

        dismiss();
    }

}
