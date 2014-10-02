/*
 * KalmanLocationManager
 *
 * Copyright (c) 2014 Renato Villone
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.villoren.android.kalmanlocationmanager.lib;

import android.content.Context;
import android.location.LocationListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a means of requesting location updates.
 * <p>
 * Similar to Android's {@link android.location.LocationManager LocationManager}.
 */
public class KalmanLocationManager {

    /**
     * Specifies which of the native location providers to use, or a combination of them.
     */
    public enum UseProvider { GPS, NET, GPS_AND_NET }

    /**
     * Provider string assigned to predicted Location objects.
     */
    public static final String KALMAN_PROVIDER = "kalman";

    /**
     * Logger tag.
     */
    private static final String TAG = KalmanLocationManager.class.getSimpleName();

    /**
     * The Context the KalmanLocationManager is running in.
     */
    private final Context mContext;

    /**
     * Map that associates provided LocationListeners with created LooperThreads.
     */
    private final Map<LocationListener, LooperThread> mListener2Thread;

    /**
     * Constructor.
     *
     * @param context The Context for this KalmanLocationManager.
     */
    public KalmanLocationManager(Context context) {

        mContext = context;
        mListener2Thread = new HashMap<LocationListener, LooperThread>();
    }

    /**
     * Register for {@link android.location.Location Location} estimates using the given LocationListener callback.
     *
     *
     * @param useProvider Specifies which of the native location providers to use, or a combination of them.
     *
     * @param minTimeFilter Minimum time interval between location estimates, in milliseconds.
     *                      Indicates the frequency of predictions to be calculated by the filter,
     *                      thus the frequency of callbacks to be received by the given location listener.
     *
     * @param minTimeGpsProvider Minimum time interval between GPS readings, in milliseconds.
     *                           If {@link UseProvider#NET UseProvider.NET} was set, this value is ignored.
     *
     * @param minTimeNetProvider Minimum time interval between Network readings, in milliseconds.
     *                           If {@link UseProvider#GPS UseProvider.GPS} was set, this value is ignored.
     *
     * @param listener A {@link android.location.LocationListener LocationListener} whose
     *                 {@link android.location.LocationListener#onLocationChanged(android.location.Location) onLocationChanged(Location)}
     *                 method will be called for each location estimate produced by the filter. It will also receive
     *                 the status updates from the native providers.
     *
     * @param forwardProviderReadings Also forward location readings from the native providers to the given listener.
     *                                Note that <i>status</i> updates will always be forwarded.
     *
     */
    public void requestLocationUpdates(
            UseProvider useProvider,
            long minTimeFilter,
            long minTimeGpsProvider,
            long minTimeNetProvider,
            LocationListener listener,
            boolean forwardProviderReadings)
    {
        // Validate arguments
        if (useProvider == null)
            throw new IllegalArgumentException("useProvider can't be null");

        if (listener == null)
            throw new IllegalArgumentException("listener can't be null");

        if (minTimeFilter < 0) {

            Log.w(TAG, "minTimeFilter < 0. Setting to 0");
            minTimeFilter = 0;
        }

        if (minTimeGpsProvider < 0) {

            Log.w(TAG, "minTimeGpsProvider < 0. Setting to 0");
            minTimeGpsProvider = 0;
        }

        if (minTimeNetProvider < 0) {

            Log.w(TAG, "minTimeNetProvider < 0. Setting to 0");
            minTimeNetProvider = 0;
        }

        // Remove this listener if it is already in use
        if (mListener2Thread.containsKey(listener)) {

            Log.d(TAG, "Requested location updates with a listener that is already in use. Removing.");
            removeUpdates(listener);
        }

        LooperThread looperThread = new LooperThread(
                mContext, useProvider, minTimeFilter, minTimeGpsProvider, minTimeNetProvider,
                listener, forwardProviderReadings);

        mListener2Thread.put(listener, looperThread);
    }

    /**
     * Removes location estimates for the specified LocationListener.
     * <p>
     * Following this call, updates will no longer occur for this listener.
     *
     * @param listener Listener object that no longer needs location estimates.
     */
    public void removeUpdates(LocationListener listener) {

        LooperThread looperThread = mListener2Thread.remove(listener);

        if (looperThread == null) {

            Log.d(TAG, "Did not remove updates for given LocationListener. Wasn't registered in this instance.");
            return;
        }

        looperThread.close();
    }
}
