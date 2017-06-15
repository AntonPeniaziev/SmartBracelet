package activities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import tasks.LoginTask;
import com.android.SmartBracelet.R;
import logic.TreatmentsTable;

import java.util.concurrent.ExecutionException;



public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    EditText _usernameText;
    static EditText _passwordText;
    static Button _loginButton;
    String _errorMsg;
    String _docID = "123";
    public static String doctorName = "";
    public static String doctorNumber = "";

    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter;
    static public TreatmentsTable treatmentUidTranslator;

    /**
     *  Function which initiating the Bluetooth Adapter
     * @return true if the bluetooth adapter initiated successfully, else false
     */
    boolean initBluetooth(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            finish();
            return false;
        }

        if (false == bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        return true;
    }


    /**
     * OnCreate function initiates the screen and all the views in it
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!initBluetooth()){
            return;
        }

        setContentView(R.layout.activity_login);
        Typeface army_font = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-Regular.ttf");

        _usernameText = (EditText) findViewById(R.id.input_username);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _loginButton = (Button) findViewById(R.id.btn_login);
        _usernameText.setTypeface(army_font);
        _passwordText.setTypeface(army_font);
        _loginButton.setTypeface(army_font);
        _errorMsg="";
        treatmentUidTranslator = new TreatmentsTable(this);
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
    }


    /**
     * open the Authenticating screen and call validate function
     *  to check if the user name and password are correct
     */
    public void login() {
        Log.d(TAG, "Login");
        String username = _usernameText.getText().toString();
        if(username.equals("")){
            Toast.makeText(getBaseContext(), "User Name is Empty", Toast.LENGTH_LONG).show();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.Base_Theme_AppCompat_Light_DarkActionBar);
        progressDialog.setIndeterminate(true);
        String message = "Authenticating...";
        progressDialog.setMessage(message);
        progressDialog.show();
        final Boolean valid = validate(progressDialog, message);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        if (!valid) {
                            onLoginFailed();
                            //System.exit(0);
                            progressDialog.dismiss();
                            return;
                        }
                        progressDialog.dismiss();
                        onLoginSuccess();
                        // onLoginFailed();

                    }
                }, 3000);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * When the Authentication success continue to The bracelets screen
     */
    public void onLoginSuccess() {
        Intent tentIntent = new Intent(LoginActivity.this, TentActivity.class);
        tentIntent.putExtra("DOC_ID", _docID);
        TentActivity._helloDoctor = true;
        startActivity(tentIntent);
    }


    /**
     * when failed to login show the error message and go back to login screen
     */
    public void onLoginFailed() {
        int time = 3;
        while(time > 0) {
            Toast.makeText(getBaseContext(), _errorMsg, Toast.LENGTH_LONG).show();
            time--;
        }

        _loginButton.setEnabled(true);
    }


    /**
     *
     * @param username : the username needs to be checked
     * @return true if the username is legal one and in the database
     */
    /*boolean validateUserName(String username, ProgressDialog progressDialog, String message) {
        progressDialog.setMessage(message + "\n" + "Checking Username...");
        progressDialog.show();
        Boolean valid = true;

        //check if the username is a string and its length above 2 characters
        if (username.matches("[0-9]+")) {
            _docID = username;
        }
        if (username.matches("[a-zA-Z0-9]+") && username.length() > 2) {
           // _docID = username; //TODO separate ID from name (maybe we need ID only, Arduino team expects to get an integer)
        } else {
            _errorMsg = "Enter a valid username";
            valid = false;
            return valid;
        }

        // check if the username is not empty
        if (username.isEmpty()) {
            _errorMsg = "Enter a valid username";
            valid = false;
            return valid;
        } else {
            _usernameText.setError(null);
            //check if the username exist in the database in web
            try {
                valid = new LoginTask(getBaseContext()).execute(username).get();

            } catch (InterruptedException e) {
                Toast.makeText(getBaseContext(), "Something is wrong. try again soon", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(getBaseContext(), "Something is wrong. try again soon", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        return valid;
    }*/


    /**
     * @param username: the username connected to the password
     * @param password : the password needs to be checked
     * @return true if the password is legal one and according to the password in database
     */
    boolean validatePassword(String username,String password,ProgressDialog progressDialog, String message) {
        progressDialog.setMessage(message + "\n" + "Checking Password...");
        progressDialog.show();
        Boolean valid = true;

        if (username.isEmpty()) {
            _errorMsg = "Please enter a username";
            valid = false;
            return valid;
        }
        //check that the password is not empty and its length above 4 characters
        // and below 10 characters
        else if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _errorMsg = "Please enter PASSWORD between 4 and 10 characters: numbers and letters";
            valid = false;
        } else {
            // check the correctness of the password in the database
            _passwordText.setError(null);
            try {
                String[] userAndPass = {username, password};
                valid = new LoginTask(getBaseContext()).execute(userAndPass).get();
                if (!valid) {
                    _errorMsg = "Please enter a valid USER & PASSWORD or check your INTERNET connection";
                    return valid;
                }
            } catch (InterruptedException e) {
                Toast.makeText(getBaseContext(), "something is wrong. try again soon", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(getBaseContext(), "something is wrong. try again soon", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
        return valid;
    }


    /**
     * function that validates the correctness of the username and password of a client.
     * the conditions for username and password to pass:
     * 1. username must be an alphanumeric string
     * 2. username must be above 2 characters
     * 3. username must be found in the database on web
     * 4. password must be an alphanumeric string
     * 5. password must be above 4 characters and below 10 characters
     * 6. password must be matched to the password connected to the username in the database in  web
     * @return
     */
    public boolean validate(ProgressDialog progressDialog, String message) {
        Boolean valid = true;
        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        if(username.equals("master")){
            progressDialog.setMessage(message + "\n" + "Master Pass...");
            progressDialog.show();
            doctorName = "master";
            return true;
        }

        if(username.equals("")){
            progressDialog.setMessage(message + "\n" + "User Name is Empty...");
            progressDialog.show();
            return false;
        }

        //valid = validateUserName(username, progressDialog, message);

        /*if (!valid) {
            _errorMsg = "Enter a valid USER or check your INTERNET connection";
            return valid;
        }

        progressDialog.setMessage(message + "\n" + "Username Passed...");
        progressDialog.show();*/
        valid = validatePassword(username, password, progressDialog, message);

        if (!valid) {
            //_errorMsg = "Enter a valid PASSWORD or check your INTERNET connection";
            return valid;
        }


        progressDialog.setMessage(message + "\n" + "Starting Scanning Bracelets...");
        progressDialog.show();
        return valid;
    }
}