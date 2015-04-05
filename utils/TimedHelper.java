package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

/**
 * Created by andrey on 02.04.15.
 */
public class TimedHelper {
    public static TimedDatacenterBrocker createTimedDatacenterBrocker() {
        TimedDatacenterBrocker brocker = null;
        try {
            brocker = new TimedDatacenterBrocker("TimedDatacenterBrocker");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return brocker;
    }
}
