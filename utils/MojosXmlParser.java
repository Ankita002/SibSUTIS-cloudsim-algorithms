package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

import org.cloudbus.cloudsim.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrey on 04.03.15.
 */
public class MojosXmlParser {
    public static class MojosTask {
        List<MojosRequest> requstList;
        String guid;
        String arrivalTime;
        String contRequests;
        MojosTask() {
            requstList = new ArrayList<MojosRequest>();
        }
    }

    public static class MojosRequest {
        int vmType;
        int nodes;
        String time;
        int priority;

    }

    public static List<MojosTask> parse(String filePath) {
        List<MojosTask> result = new ArrayList<MojosTask>();


        File fXmlFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        Document doc = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (doc == null || dBuilder == null )
            return  result;

        doc.getDocumentElement().normalize();

        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

        NodeList taskList = doc.getElementsByTagName("JOB");
        for (int i = 0; i < taskList.getLength(); i++) {
            Node nNode = taskList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                MojosTask task = new MojosTask();
                Log.printLine("GUID: " + eElement.getAttribute("GUID"));
                task.guid = eElement.getAttribute("GUID");
                NodeList requestList = eElement.getElementsByTagName("REQUEST");

                for (int j = 0; j < requestList.getLength(); j++) {
                    Node rNode = requestList.item(j);
                    if(rNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element rElement =  (Element) rNode;
                        MojosRequest request = new MojosRequest();
                        request.priority = Integer.parseInt(rElement.getAttribute("PRIORITY"));
                        request.nodes = Integer.parseInt(rElement.getAttribute("NODES"));
                        request.time = rElement.getAttribute("TIME");
                        request.vmType = Integer.parseInt(rElement.getAttribute("VM_TYPE"));
                        task.requstList.add(request);
                    }
                }
                result.add(task);
            }
        }
        return result;
    }
}
