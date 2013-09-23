package edu.cicese.sensit;

/**
 * Created with IntelliJ IDEA.
 * User: netzahdzc
 * Date: 21/09/13
 * Time: 16:00
 * To change this template use File | Settings | File Templates.
 */

/**
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

import edu.cicese.sensit.util.StepDetector;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;

/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application.
 *
 * <p>Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */

public class StepService extends Service {
    private Sensor mSensor;
    private StepDetector mStepDetector;
    private SensorManager mSensorManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // Start detecting
        mStepDetector = new StepDetector();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerDetector();

        loadSettings();
    }

    @SuppressLint("InlinedApi")
    private void registerDetector() {
        mSensor = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER /*|
            Sensor.TYPE_MAGNETIC_FIELD |
            Sensor.TYPE_ORIENTATION*/);
        mSensorManager.registerListener(mStepDetector,
                mSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void loadSettings() {
        if (mStepDetector != null) {
            mStepDetector.setSensitivity(
                    Float.valueOf("22.5")
            );
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        StepDetector.resetStepCounter();
        super.onDestroy();
        mSensorManager.unregisterListener(mStepDetector);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}