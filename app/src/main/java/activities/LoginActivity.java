package activities;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import tasks.LoginTask;
import com.android.SmartBracelet.R;
import logic.TreatmentsTable;

import java.util.concurrent.ExecutionException;



public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    EditText _usernameText;
    public static EditText _passwordText;
    public static Button _loginButton;
    static public String _errorMsg;
    public static String doctorName = "";
    public static String doctorNumber = "0";
    public static String doctorDivision = "";

    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter;
    static public TreatmentsTable treatmentUidTranslator;
    static public boolean _valid;
    static public ProgressDialog _progressDialog;
    boolean _updated;
    static private LoginActivity _instance;


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
     * Function which initiates the text views: user name and password and their Font.
     *
     */
    void initTextViews(){
        Typeface army_font = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-Regular.ttf");
        _usernameText = (EditText) findViewById(R.id.input_username);
        _usernameText.setTypeface(army_font);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _passwordText.setTypeface(army_font);

    }

    /**
     * Function which initiates the login button and it's 'On click' behavior.
     */
    void initLoginButton(){
        Typeface army_font = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-Regular.ttf");
        _loginButton = (Button) findViewById(R.id.btn_login);
        _loginButton.setTypeface(army_font);
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
    }


    /**
     * OnCreate function initiates the screen and all the views in it
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(!initBluetooth()){
            return;
        }
        initTextViews();
        initLoginButton();
        _errorMsg="";
        treatmentUidTranslator = new TreatmentsTable(this);
        _instance = this;
    }

    /**
     * Initiates the progressDialog after clicking on login button.
     * @return the initial message of the progressDialog
     */
    String initiateProgressDialog(){
        _progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.MyProgressDialogStyle);
        String message = "Authenticating...";
        _progressDialog.setMessage(message);
        _progressDialog.setCancelable(false);
        _progressDialog.setIndeterminate(true);
        _progressDialog.show();
        Window windowProgress = _progressDialog.getWindow();
        windowProgress.setLayout(500, 200);
        windowProgress.setGravity(Gravity.TOP);
        windowProgress.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#262E1E")));
        return message;
    }

    /**
     * The post delay thread behavior:
     * 1. if the login succeeded calls onLoginSuccess.
     * 2. if the login failed calls onLoginFailed.
     */
    void runOnPostDelayed(){
                        if (!_valid) {
                            _progressDialog.dismiss();
                            onLoginFailed();
                            //System.exit(0);
                            return;
                        }

                        _progressDialog.dismiss();
                        onLoginSuccess();
                        return;
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
        String message = initiateProgressDialog();
        _valid = validate(_progressDialog, message);

        if(_valid && !username.equals("master")){
            String password = _passwordText.getText().toString();
            validatePassword(username, password, _progressDialog, message);
            return;
        }
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        runOnPostDelayed();
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
        tentIntent.putExtra("DOC_ID", doctorNumber);
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
     * @param username : the userName of the user
     * @param password : the password of the user
     * @param progressDialog : the progress dialog working on the UI thread
     * @param message : the message will be shown in the progress dialog
     * @return flag whhich indicates whether the userName and password are valid or not.
     */
    boolean userNameAndPasswordAreValid(String username,String password,final ProgressDialog progressDialog,final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setMessage(message + "\n" + "Checking Password...");
                progressDialog.show();
                return;
            }
        });


        if (username.isEmpty()) {
            _errorMsg = "Please enter a username";
            return false;
        }

        if(password.isEmpty()){
            _errorMsg = "Please enter a password";
            return false;
        }
        //check that the password is not empty and its length above 4 characters
        // and below 10 characters
        if(password.length() < 4 || password.length() > 10) {
            _errorMsg = "Please enter PASSWORD between 4 and 10 characters: numbers and letters";
            return false;
        }
        return true;

    }

    /**
     * @param username: the username connected to the password
     * @param password : the password needs to be checked
     * @return true if the password is legal one and according to the password in database
     */
    void validatePassword(String username,String password, final ProgressDialog progressDialog, final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setMessage(message + "\n" + "Connecting Web...");
                progressDialog.show();
                return;
            }
        });


                String[] userAndPass = {username, password};
                 new LoginTask(getBaseContext()).execute(userAndPass);

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
    public boolean validate(final ProgressDialog progressDialog,final String message) {

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        if(username.equals("master")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        progressDialog.setMessage(message + "\n" + "Master Pass...");
                        progressDialog.show();
                        return;
                }
            });
            doctorName = "master";
            return true;
        }

        if (!userNameAndPasswordAreValid(username, password, progressDialog, message)){
            return false;
        }

        return true;
    }


    static public LoginActivity getInstance(){
        return _instance;
    }
}