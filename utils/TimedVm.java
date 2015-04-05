package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.power.PowerVm;

/**
 * Created by andrey on 02.04.15.
 */
public class TimedVm extends PowerVm {

    private static double launchTime;
    private static double terminationTime; //Time to live (pre-payed vm live time)

    public TimedVm(int id, int userId, double mips, int pesNumber, int ram, long bw, long size, int priority, String vmm, CloudletScheduler cloudletScheduler, double schedulingInterval) {
        super(id, userId, mips, pesNumber, ram, bw, size, priority, vmm, cloudletScheduler, schedulingInterval);
        launchTime = -1;
        terminationTime = -1;
    }
    public double getLaunchTime() {
        return launchTime;
    }

    public static void setLaunchTime(double launchTime) {
        TimedVm.launchTime = launchTime;
    }

    public static double getTerminationTime() {
        return terminationTime;
    }

    public static void setTerminationTime(double tt) {
        TimedVm.terminationTime = tt;
    }
}
