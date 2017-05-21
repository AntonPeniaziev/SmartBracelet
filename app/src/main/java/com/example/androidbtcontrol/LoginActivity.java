package com.example.androidbtcontrol;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    EditText _usernameText;
    EditText _passwordText;
    Button _loginButton;
    String _errorMsg;
    String _docID = "123";

    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (false == bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        setContentView(R.layout.activity_login);
        _usernameText = (EditText) findViewById(R.id.input_username);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _loginButton = (Button) findViewById(R.id.btn_login);
        _errorMsg="";

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");


        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.Base_Theme_AppCompat_Light_DarkActionBar);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        if (!validate()) {
            onLoginFailed();
            return;
        }

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onLoginSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        Intent tentIntent = new Intent(LoginActivity.this, TentActivity.class);
        tentIntent.putExtra("DOC_ID", _docID);
        startActivity(tentIntent);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(),  _errorMsg, Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        Boolean valid = true;

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        if (username.matches("[0-9]+") && username.length() > 2) {
            _docID = username; //TODO separate ID from name (maybe we need ID only, Android team expects to get an integer)
        }

        if (username.isEmpty()) {
            _errorMsg = "enter a valid username";
            valid = false;
        } else {
            _usernameText.setError(null);
            //TODO: check if the username exist in web and if not get the username in

            if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
                _errorMsg ="between 4 and 10 characters: numbers and letters";

                valid = false;
            } else {
                //TODO:: check the correctness of the password
                _passwordText.setError(null);
            }
        }
        return valid;
    }
}