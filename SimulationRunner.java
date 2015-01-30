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
import java.util.Calendar;
import java.util.List;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.ExtendedHelper;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.examples.power.random.RandomHelper;
import org.cloudbus.cloudsim.power.*;


/**
 * A simple example showing how to create
 * a datacenter with two hosts and run two
 * cloudlets on it. The cloudlets run in
 * VMs with different MIPS requirements.
 * The cloudlets will take different time
 * to complete the execution depending on
 * the requested VM performance.
 */
public class SimulationRunner {

    public static void main(String[] args) throws IOException {
        int NUMBER_OF_VMS = 20;
        int NUMBER_OF_HOSTS = 10;
        String experimentName = "random_npa";
        String outputFolder = "output";

        Log.setDisabled(!Constants.ENABLE_OUTPUT);
        Log.printLine("Starting " + experimentName);

        try {
            CloudSim.init(1, Calendar.getInstance(), false);

            DatacenterBroker broker =  ExtendedHelper.createBroker();
            int brokerId = broker.getId();

            List<Cloudlet> cloudletList = RandomHelper.createCloudletList(
                    brokerId,
                    NUMBER_OF_VMS);
            List<Vm> vmList = ExtendedHelper.createVmList(brokerId, cloudletList.size());
            List<PowerHost> hostList = ExtendedHelper.createHostList(NUMBER_OF_HOSTS);
            PowerDatacenter datacenter = (PowerDatacenter) ExtendedHelper.createDatacenter(
                    "Datacenter",
                    PowerDatacenter.class,
                    hostList,
//                    new VmAllocationPolicyRandom(hostList)
//                    new VmAllocationPolicyRoundRobin(hostList)
//                    new VmAllocationPolicyFirstFit(hostList)
                    new VmAllocationPolicyNBG(hostList)
            );

            datacenter.setDisableMigrations(false);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            CloudSim.terminateSimulation(Constants.SIMULATION_LIMIT);
            double lastClock = CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();
            Log.printLine("Received " + newList.size() + " cloudlets");

            CloudSim.stopSimulation();

            ExtendedHelper.printResults(
                    datacenter,
                    vmList,
                    lastClock,
                    experimentName,
                    Constants.OUTPUT_CSV,
                    outputFolder);

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            System.exit(0);
        }

        Log.printLine("Finished " + experimentName);
    }

}
