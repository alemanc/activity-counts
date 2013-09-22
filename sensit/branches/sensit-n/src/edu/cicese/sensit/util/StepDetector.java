package edu.cicese.sensit.util;

/**
 * Created with IntelliJ IDEA.
 * User: netzahdzc
 * Date: 21/09/13
 * Time: 15:51
 * To change this template use File | Settings | File Templates.
 */
/*
 *  Pedometer - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//import com.example.pericomudo.DatabaseHandler;
//import com.example.pericomudo.zGlobal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;
import edu.cicese.sensit.db.DBAdapter;
import edu.cicese.sensit.SensitActivity;


/**
 * Detects steps and notifies all listeners (that implement StepListener).
 * @author Levente Bagi
 * @todo REFACTOR: SensorListener is deprecated
 */

@SuppressLint("NewApi")
public class StepDetector implements SensorEventListener
{
    //SENSOR_ID_INTODB => stepCount
    //private DatabaseHandler dbh;
    public static int steps=0;
    private DBAdapter dbAdapter;
    private final static int SENSOR_ID_INTODB=5;
    private static final int REQUEST_ENABLE_BT = 1;
    //private zGlobal global = new zGlobal();
    private Context context = null;
    //SENSOR_ID_INTODB => stepCount

    private final static String TAG = "StepDetector";
    private float   mLimit = 10;
    private float   mLastValues[] = new float[3*2];
    private float   mScale[] = new float[2];
    private float   mYOffset;

    private float   mLastDirections[] = new float[3*2];
    private float   mLastExtremes[][] = { new float[3*2], new float[3*2] };
    private float   mLastDiff[] = new float[3*2];
    private int     mLastMatch = -1;

    public StepDetector() {
        int h = 480; // TODO: remove this constant
        mYOffset = h * 0.5f;
        mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
    }

    public void setSensitivity(float sensitivity) {
        //Log.i(TAG, "qqq sensitivity");
        //dbh = new DatabaseHandler();
        dbAdapter = new DBAdapter();
        mLimit = sensitivity; // 1.97  2.96  4.44  6.66  10.00  15.00  22.50  33.75  50.62
    }

    @SuppressWarnings("deprecation")
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
            }
            else {
                int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
                if (j == 1) {
                    float vSum = 0;
                    for (int i=0 ; i<3 ; i++) {
                        final float v = mYOffset + event.values[i] * mScale[j];
                        vSum += v;
                    }

                    //Log.i(TAG, "qqq X: " + event.values[1] + ", Y: " + event.values[0] + ", Z: " + event.values[2]);
                    int k = 0;
                    float v = vSum / 3;

                    //I send accelerometer data: X Y Z
                    //dbh.saveDataSteps(global.getUSERID(), event.values[1]+"", event.values[0]+"", event.values[2]+"", v+"");

                    float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
                    if (direction == - mLastDirections[k]) {
                        // Direction changed
                        int extType = (direction > 0 ? 0 : 1); // minumum or maximum?
                        mLastExtremes[extType][k] = mLastValues[k];
                        float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);
                        //dbh.saveDataSteps(global.getUSERID(), event.values[1]+"", event.values[0]+"", event.values[2]+"", diff+"");

                        //Log.i(TAG, "qqq diff OUT: " + diff);
                        if (diff > mLimit) {
                            //Log.i(TAG, "qqq diff IN: " + diff);
                            boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k]*2/3);
                            boolean isPreviousLargeEnough = mLastDiff[k] > (diff/3);
                            boolean isNotContra = (mLastMatch != 1 - extType);

                            if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                                incrementSteps();
                                mLastMatch = extType;
                            }
                            else {
                                mLastMatch = -1;
                            }
                        }
                        mLastDiff[k] = diff;
                    }
                    mLastDirections[k] = direction;
                    mLastValues[k] = v;
                }
            }
        }
    }

    public void incrementSteps() {
        steps += 1;
        Log.i(TAG, "[Pedometer] Step detected: " + steps);
        /*Toast.makeText(SensitActivity.getContext(),
                "[Pedometer] STEP: " + steps,
                Toast.LENGTH_LONG).show();*/

    }

    public static void resetStepCounter(){
        Log.i(TAG, "[Pedometer] Resetting steps' counter");
        /*Toast.makeText(SensitActivity.getContext(),
                "[Pedometer] RESTARTING",
                Toast.LENGTH_LONG).show();*/
        steps = 0;
    }

    public static int getCounts() {
        return steps;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }
}