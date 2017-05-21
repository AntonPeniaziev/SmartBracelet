package com.example.androidbtcontrol;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;

import java.util.concurrent.ExecutionException;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    EditText _usernameText;
    EditText _passwordText;
    Button _loginButton;
    String _errorMsg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


        if (username.isEmpty()) {
            _errorMsg = "Enter a valid username";
            valid = false;
        } else {
            _usernameText.setError(null);
            //TODO: check if the username exist in web and if not get the username in
            try {
                valid = new LoginTask().execute(username).get();

            } catch (InterruptedException e) {
                Toast.makeText(getBaseContext(),  "Something is wrong. try again soon", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(getBaseContext(),  "Something is wrong. try again soon", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (MongoTimeoutException e) {
                Toast.makeText(getBaseContext(),  "Something is wrong. Check internet connection or try again soon", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

            if (!valid) {
                _errorMsg = "Username doesn't exist. try another user or check your INTERNET connection";
                return valid;
            }

            if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
                _errorMsg ="Password between 4 and 10 characters: numbers and letters";

                valid = false;
            } else {
                //TODO:: check the correctness of the password
                _passwordText.setError(null);
                try {
                    String[] userAndPass = {username, password};
                    valid = new LoginTask().execute(userAndPass).get();
                } catch (InterruptedException e) {
                    Toast.makeText(getBaseContext(),  "something is wrong. try again soon", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    Toast.makeText(getBaseContext(),  "something is wrong. try again soon", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (MongoTimeoutException e) {
                    Toast.makeText(getBaseContext(),  "Something is wrong. Check internet connection or try again soon", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                if (!valid) {
                    _errorMsg = "The user name or password for SmartBracelet is incorrect. Otherwise, check your INTERNET connection";
                    return valid;
                }

            }
        }
        return valid;
    }
}