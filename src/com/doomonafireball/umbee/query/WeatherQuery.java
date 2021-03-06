package com.doomonafireball.umbee.query;

import com.doomonafireball.umbee.MainApp;
import com.doomonafireball.umbee.R;
import com.doomonafireball.umbee.model.NoaaByDay;
import com.doomonafireball.umbee.util.RestClient;
import com.doomonafireball.umbee.util.SharedPrefsManager;
import com.doomonafireball.umbee.util.UmbeeNotifUtils;
import com.doomonafireball.umbee.util.UmbeeTimeUtils;
import com.doomonafireball.umbee.util.XmlParser;

import org.json.JSONException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.GregorianCalendar;

/**
 * User: derek Date: 5/26/12 Time: 6:33 PM
 */
public class WeatherQuery extends AsyncTask<Void, Void, Void> {

    private static final String NOAA_URL
            = "http://graphical.weather.gov/xml/sample_products/browser_interface/ndfdBrowserClientByDay.php";

    private boolean mCreateNotif;
    private Context mContext;
    private NoaaByDay mNbd = new NoaaByDay();
    private SharedPrefsManager mSPM;
    private Handler mHandler;
    private String response;
    private String errorMessage;
    private int responseCode;

    public WeatherQuery(Context ctx, boolean displayProgress, boolean createNotif, Handler handler) {
        SharedPrefsManager.initialize(ctx);
        mSPM = SharedPrefsManager.getInstance();
        this.mContext = ctx;
        this.mCreateNotif = createNotif;
        this.mHandler = handler;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Get NOAA results
        RestClient noaaClient = new RestClient(NOAA_URL);
        noaaClient.addParam("format", "24 hourly");
        noaaClient.addParam("numDays", "6");
        noaaClient.addParam("zipCodeList", mSPM.getLocation());
        noaaClient.addParam("startDate",
                UmbeeTimeUtils.formatNoaaForCalendar(new GregorianCalendar()));
        try {
            noaaClient.execute(RestClient.RequestMethod.GET);
        } catch (Exception e) {
            e.printStackTrace();
        }
        response = noaaClient.getResponse();
        errorMessage = noaaClient.getErrorMessage();
        responseCode = noaaClient.getResponseCode();
        if (response != null) {
            Log.d(MainApp.TAG, "NOAA response: " + response);
            // Parse the response
            mNbd = XmlParser.parseNoaaByDay(response);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        // Persist data to SharedPrefs
        try {
            mSPM.setNoaaByDayString(mNbd.toJsonString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mCreateNotif) {
            // Create notification
            UmbeeNotifUtils.createNotification(mContext, mNbd);
        }
        mHandler.sendMessage(new Message());
    }
}
