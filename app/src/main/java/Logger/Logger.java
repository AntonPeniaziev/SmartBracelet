package Logger;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by apeniazi on 07-Jun-17.
 */

public class Logger {
    BufferedWriter bw;
    private Context _context;
    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

    public Logger(Context ct) {
        if (!path.exists()) {
            path.mkdir();
        }
        _context = ct;
        path = new File(path, "101app" + System.currentTimeMillis() + ".log");
        MediaScannerConnection.scanFile(_context, new String[] {path.toString()}, null, null);
    }
    public void writeToLog(String mes) {
        MediaScannerConnection.scanFile(_context, new String[] {path.toString()}, null, null);
        try {
            bw = new BufferedWriter(new FileWriter(path.getAbsolutePath(), true));
            bw.write(mes);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
