package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

import org.cloudbus.cloudsim.examples.SibSUTIS.power.PowerModelSpecPowerHuaweiRH2285V2Xeon2450;
import org.cloudbus.cloudsim.examples.SibSUTIS.power.PowerModelSpecPowerHuaweiRH2288HV2Xeon2609;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.power.models.PowerModel;

/**
 * Created by andrey on 11.02.15.
 */
public class ExtendedConstants extends Constants {
    public final static int CLOUDLET_LENGTH	= 50000 * (int) SIMULATION_LIMIT;
    public final static int HOST_TYPES	 = 2;
    public final static int[] HOST_MIPS	 = { 2400, 2100 };
    public final static int[] HOST_PES	 = { 8, 16};
    public final static int[] HOST_RAM	 = { 49152,16384 };
    public final static int HOST_BW		 = 1000000; // 1 Gbit/s
    public final static int HOST_STORAGE = 1000000; // 1 GB
    public final static int VM_TYPES	= 4;
    /*High-CPU, High-Memory, Medium, Small*/
    public final static int[] VM_MIPS	= { 2000, 1500, 1000, 500 };
    public final static int[] VM_RAM	= { 1024, 7500,  1740, 870};
    public final static int[] VM_PES	= { 1, 1, 1, 1 };
    public final static int VM_BW		= 10000; // 100 Mbit/s
    public final static int VM_SIZE		= 2500; // 2.5 GB
    public final static PowerModel[] HOST_POWER = {
            new PowerModelSpecPowerHuaweiRH2288HV2Xeon2609(),
            new PowerModelSpecPowerHuaweiRH2285V2Xeon2450()
    };
}

