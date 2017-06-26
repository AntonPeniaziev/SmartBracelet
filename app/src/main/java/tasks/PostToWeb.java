package tasks;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user on 26/06/2017.
 */

public class PostToWeb {

    private static final String webUrl = "https://firstaidbracelet.herokuapp.com/soldiersChange";
    private static final String msg = "dbUpdate";

    /**
     * doing http POST to refresh the website for our changes
     */
    public static void postToWeb() {
        HttpURLConnection client = null;
        try {
            // Defined URL  where to send data
            URL url = new URL(webUrl);
            client = (HttpURLConnection) url.openConnection();

            client.setRequestProperty("Accept","text/html;charset=utf-8");
            client.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            client.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
            client.setRequestMethod("POST");
            client.setDoOutput(true);

            DataOutputStream printout = new DataOutputStream(client.getOutputStream());
            printout.writeBytes(msg);
            printout.flush();
            printout.close();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if(client != null)
                    client.disconnect();
            }
            catch(Exception ex) { ex.printStackTrace(); }
        }
    }
}
