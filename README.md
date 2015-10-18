# KalmanLocationManager

Use KalmanLocationManager in Android to receive filtered Location estimates.

It takes advantage of a Kalman filter algorithm to predict fixes (ordinary Android `Location` objects).

Measurement updates are gathered from either the GPS or Network provider (or the combination of both), taking into account the accuracy of those updates to calculate their contribution to the predicted fix.

What is that good for?

* <b>"Interpolate" between real fixes</b> by setting a filter update interval faster than the real provider update interval. Usually, the fastest GPS update interval in most Android devices is one second. By setting the filter to 1/24 second you can animate a fluid transition from one fix to the next.

*  <b>Gain accuracy</b> by setting a filter update interval slower than the real provider update interval. Each fix measurement improves the accuracy obtained for a filter estimate.

See example in `MainActivity` for usage.

---

Changes:

* Updated for Studio 1.4, Api level 23 (see "To do")
* Forcing prediction step between consecutive correction steps (reduce overshoot)
---

To do:

* Add some missing javadoc
* Make example MainActivity nice
* Make this readme nice
* Handle new Security exceptions when requesting/removing location updates.
---

Copyright (c) 2014 Renato Villone.

See LICENSE.txt for license rights and limitations (MIT).
