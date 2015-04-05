/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples.SibSUTIS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javafx.util.Pair;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.ExtendedDatacenter;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.ExtendedDatacenterBrocker;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.ExtendedHelper;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.MojosHelper;
import org.cloudbus.cloudsim.examples.SibSUTIS.vmAllocationPolicyes.VmAllocationPolicyFirstFit;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.examples.power.random.RandomHelper;
import org.cloudbus.cloudsim.power.*;


/*TODO
1.Random
2.First FIt
3.Round RObin
4. FFD Prod
5. FFD Sum
6. Dot-Product
7. Norm-based Greedy
*/
public class SimulationRunner {


    public static void main(String[] args) throws IOException {
        int NUMBER_OF_HOSTS = 100;
        int MIN_VMS = 100;
        int SIMULATION_STEP = 100;
        int MAX_VMS = 1000;

        List<Pair<Double, Integer>> resultList = new ArrayList<Pair<Double, Integer>>();
        for (int i = MIN_VMS; i <= MAX_VMS; i += SIMULATION_STEP) {
            String experimentName = "random_npa";
            String outputFolder = "output";

            int NUMBER_OF_VMS = i;
            Log.setDisabled(!Constants.ENABLE_OUTPUT);
            Log.printLine("Starting " + experimentName);


            try {
                CloudSim.init(1, Calendar.getInstance(), false);

//            ExtendedDatacenterBrocker broker =  ExtendedHelper.createExtendedBrocker(ExtendedDatacenterBrocker.VM_ALLOCATION_MODE_LIST);
                ExtendedDatacenterBrocker broker = ExtendedHelper.createExtendedBrocker(ExtendedDatacenterBrocker.VM_ALLOCATION_MODE_STANDART);
                int brokerId = broker.getId();
                Log.printLine("brocker id: " + brokerId);

                List<Cloudlet> cloudletList = RandomHelper.createCloudletList(
                        brokerId,
                        NUMBER_OF_VMS);

//                List<Vm> vmList = ExtendedHelper.createVmList(brokerId, cloudletList.size());
                List<Vm> vmList = MojosHelper.createVmList(brokerId, cloudletList.size());
                List<PowerHost> hostList = ExtendedHelper.createHostList(NUMBER_OF_HOSTS);
                ExtendedDatacenter datacenter = (ExtendedDatacenter) ExtendedHelper.createDatacenter(
                        "Datacenter",
                        ExtendedDatacenter.class,
                        hostList,
//                    new VmAllocationPolicyRandom(hostList)
//                    new VmAllocationPolicyRoundRobin(hostList)
                    new VmAllocationPolicyFirstFit(hostList)
//                    new VmAllocationPolicyNBG(hostList)
//                    new VmAllocationPolicyFFDProd(hostList)
//                    new VmAllocationPolicyFFDSum(hostList)
//                        new VmAllocationPolicyDotProduct(hostList)
                );

                datacenter.setDisableMigrations(true);

                broker.submitVmList(vmList);
                broker.submitCloudletList(cloudletList);

                CloudSim.terminateSimulation(Constants.SIMULATION_LIMIT);
                double lastClock = CloudSim.startSimulation();

                List<Cloudlet> newList = broker.getCloudletReceivedList();
                Log.printLine("Received " + newList.size() + " cloudlets");

                CloudSim.stopSimulation();

//                ExtendedHelper.printResults(
//                        datacenter,
//                        vmList,
//                        lastClock,
//                        experimentName,
//                        Constants.OUTPUT_CSV,
//                        outputFolder);

//                Log.printLine("Maximum used hosts: " + datacenter.getMaximumUsedHostsCount());
                double energy = datacenter.getPower() / (3600 * 1000);
                resultList.add(new Pair<Double, Integer>(energy,datacenter.getMaximumUsedHostsCount()));

            } catch (Exception e) {
                e.printStackTrace();
                Log.printLine("The simulation has been terminated due to an unexpected error");
                System.exit(0);
            }
        }
        for (int i = MIN_VMS, j = 0; i <= MAX_VMS && j < resultList.size(); i += SIMULATION_STEP, j++) {
            Log.printLine("Res for vms["+i+"]: \n"+
            "Energy consumption:  "+resultList.get(j).getKey()+" kWh\n"+
            "Used hosts: "+resultList.get(j).getValue()+"\n");
        }
    }

}
