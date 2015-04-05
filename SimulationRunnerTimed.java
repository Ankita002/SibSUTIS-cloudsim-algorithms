/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples.SibSUTIS;

import javafx.util.Pair;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.*;
import org.cloudbus.cloudsim.examples.SibSUTIS.vmAllocationPolicyes.VmAllocationPolicyFirstFit;
import org.cloudbus.cloudsim.examples.SibSUTIS.vmAllocationPolicyes.VmAllocationPolicyRoundRobin;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.examples.power.random.RandomHelper;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/*TODO
1.Random
2.First FIt
3.Round RObin
4. FFD Prod
5. FFD Sum
6. Dot-Product
7. Norm-based Greedy
*/
public class SimulationRunnerTimed {


    public static void main(String[] args) throws IOException {

        CloudSim.init(1, Calendar.getInstance(), false);
        TimedDatacenterBrocker brocker = TimedHelper.createTimedDatacenterBrocker();
        brocker.addMojosTaskList(MojosHelper.createTaskList(null));
        brocker.printMojosTaskSet();
        List<PowerHost> hostList = ExtendedHelper.createHostList(1000);
        ExtendedDatacenter datacenter = null;
        try {
            datacenter = (ExtendedDatacenter) ExtendedHelper.createDatacenter(
                    "Datacenter",
                    ExtendedDatacenter.class,
                    hostList,
                    new VmAllocationPolicyFirstFit(hostList)
            );
            datacenter.setDisableMigrations(true);


                CloudSim.terminateSimulation(Constants.SIMULATION_LIMIT);
                double lastClock = CloudSim.startSimulation();

                List<Cloudlet> newList = brocker.getCloudletReceivedList();
                Log.printLine("Received " + newList.size() + " cloudlets");

                CloudSim.stopSimulation();
        } catch (Exception e) {
            Log.printLine("FATAL ERROR: "+e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
        Log.printLine("Energy   " + datacenter.getPower() / (3600 * 1000)+" kWh\n");
    }

}
