package com.android.manifest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AMSecurity {
	
	ArrayList<String> usePermission = new ArrayList<String>();
	ArrayList<String> systemPermission = new ArrayList<String>();
	ArrayList<String> receiverAction = new ArrayList<String>();
	int targetSdk;
	
	public void checkSecurity(Document doc, String filename){
		populateSystemPermission();
		populateData(filename);
		getUsesSdk(doc);
		
		NodeList usesPermission = doc.getElementsByTagName("uses-permission");
		checkUsesPermission(usesPermission, "<uses-permission>");
		NodeList usesPermission23 = doc.getElementsByTagName("uses-permission-sdk-23");
		checkUsesPermission(usesPermission23, "<uses-permission-sdk-23>");
								
		checkPermission(doc);
		checkUsesPermissionForReceiverAction(doc);
		checkUsesPermissionForActivityAction(doc);
		checkExposedServices(doc);
		checkExposedReceivers(doc);
		checkExposedProviders(doc);
		checkActivity(doc);		
		
	}
	
	private void populateSystemPermission(){		
		try {
			Scanner scan = new Scanner(new File("Permissions.txt"));
			while(scan.hasNext()){
				systemPermission.add(scan.next());
			}
			scan.close();			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private void populateData(String filename){
		try {
			Scanner data = new Scanner(new File(filename));
			while(data.hasNext()){
				String next = data.next();
				if(next.startsWith("receiverAction+")){
					receiverAction.add(next.substring(next.indexOf("+")+1));
				}
			}
			data.close();			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getUsesSdk(Document doc){
		Element elem = (Element) doc.getElementsByTagName("uses-sdk").item(0);
		String target = elem.getAttribute("android:targetSdkVersion");
		if(!target.isEmpty()){
			targetSdk = Integer.parseInt(target);
		}				
	}
	
	private void checkUsesPermission(NodeList list, String tag){		
		for(int i=0; i<list.getLength(); i++){
			Element elem = (Element) list.item(i);
			NamedNodeMap attr = elem.getAttributes();
			for(int j=0; j<attr.getLength(); j++){
				if(attr.item(j).getNodeName().equals("android:name")){
					String permission = attr.item(j).getNodeValue();
					if(permission.startsWith("android.permission.") || permission.startsWith("com.android.launcher.permission.")
							|| permission.startsWith("com.android.browser.permission.") || permission.startsWith("com.android.alarm.permission.")
							|| permission.startsWith("com.android.voicemail.permission.")){
						if(!systemPermission.contains(permission)){
							//check for custom permission with pattern android.permission.string.PERMISSION_NAME
							boolean custom = checkCustom(permission);
							if(!custom){
								System.out.println("Warning: "+tag+" contains incorrect permission "+permission+" in the application.");								
							}							
						}
					}
					if(!usePermission.contains(permission)){
						usePermission.add(permission);
					}else{
						System.out.println("Warning: duplicate "+permission+" "+tag+" in the application.");						
					}
				}
			}
		}
	}
	
	private boolean checkCustom(String permission){
		boolean custom = false;
		if(permission.startsWith("android.permission.")){
			int index = 0;
			int count = 0;
			for(int i=0; i<permission.length(); i++){
				if(Character.isUpperCase(permission.charAt(i))){
					index = i;
					break;
				}
			}
			if(index>0){
				String testString = permission.substring(0, index);
				for(int j=0; j<testString.length(); j++){
					if(testString.charAt(j) == '.'){
						count++;
					}
				}
			}
			if(count>2){
				custom = true;
			}
		}
		return custom;
	}
	
	private void checkPermission(Document doc){
		NodeList permission = doc.getElementsByTagName("permission");
		for(int i=0; i<permission.getLength(); i++){
			Element elem = (Element) permission.item(i);
			String name = elem.getAttribute("android:name");
			if(systemPermission.contains(name)){
				System.out.println("Warning: system permission "+name+" does not need to be defined in <permission>.");				
			}
		}
	}
	
	private void checkUsesPermissionForReceiverAction(Document doc){
		NodeList receiver = doc.getElementsByTagName("receiver");
		for(int i=0; i<receiver.getLength(); i++){
			NodeList child = receiver.item(i).getChildNodes();
			for(int j=0; j<child.getLength(); j++){
				if(child.item(j).getNodeName().equals("intent-filter")){
					NodeList grandChild = child.item(j).getChildNodes();
					for(int k=0; k<grandChild.getLength(); k++){
						if(grandChild.item(k).getNodeName().equals("action")){
							Element elem = (Element) grandChild.item(k);
							String name = elem.getAttribute("android:name");
							if(receiverAction.contains(name)){
								if(!usePermission.contains("android.permission.BLUETOOTH")){
									System.out.println("Warning: "+name+" action requires android.permission.BLUETOOTH in <uses-permission>.");									
								}
							}else if(name.equals("android.app.action.DEVICE_ADMIN_ENABLED")){
								if(!usePermission.contains("android.permission.BIND_DEVICE_ADMIN")){
									System.out.println("Warning: "+name+" action requires android.permission.BIND_DEVICE_ADMIN in <uses-permission>.");									
								}
							}else if(name.equals("android.bluetooth.device.action.FOUND")){
								if(!(usePermission.contains("android.permission.BLUETOOTH") && usePermission.contains("android.permission.ACCESS_COARSE_LOCATION"))){
									System.out.println("Warning: "+name+" action requires android.permission.BLUETOOTH and android.permission.ACCESS_COARSE_LOCATION in <uses-permission>.");									
								}
							}else if(name.equals("android.bluetooth.device.action.PAIRING_REQUEST")){
								if(!usePermission.contains("android.permission.BLUETOOTH_ADMIN")){
									System.out.println("Warning: "+name+" action requires android.permission.BLUETOOTH_ADMIN in <uses-permission>.");
								}
							}else if(name.equals("android.intent.action.BOOT_COMPLETED")){
								if(!usePermission.contains("android.permission.RECEIVE_BOOT_COMPLETED")){
									System.out.println("Warning: "+name+" action requires android.permission.RECEIVE_BOOT_COMPLETED in <uses-permission>.");									
								}
							}else if(name.equals("android.intent.action.NEW_OUTGOING_CALL")){
								if(!usePermission.contains("android.permission.PROCESS_OUTGOING_CALLS")){
									System.out.println("Warning: "+name+" action requires android.permission.PROCESS_OUTGOING_CALLS in <uses-permission>.");									
								}
							}else if(name.equals("android.intent.action.PHONE_STATE")){
								if(!usePermission.contains("android.permission.READ_PHONE_STATE")){
									System.out.println("Warning: "+name+" action requires android.permission.READ_PHONE_STATE in <uses-permission>.");									
								}
							}else if(name.equals("android.provider.Telephony.SMS_DELIVER")){
								String declaredPermission = getReceiverPermission(receiver.item(i));
								if(declaredPermission == null || (!declaredPermission.equals("android.permission.BROADCAST_SMS"))){
									System.out.println("Warning: "+name+" action requires declaration of android.permission.BROADCAST_SMS in <receiver>.");									
								}
							}else if(name.equals("android.provider.Telephony.WAP_PUSH_DELIVER")){
								String declaredPermission = getReceiverPermission(receiver.item(i));
								if(declaredPermission == null || (!declaredPermission.equals("android.permission.BROADCAST_WAP_PUSH"))){
									System.out.println("Warning: "+name+" action requires declaration of android.permission.BROADCAST_WAP_PUSH in <receiver>.");
								}
							}
						}
					}
				}
			}
		}
	}
	
	private String getReceiverPermission(Node receiver){
		String name = null;
		NamedNodeMap attr = receiver.getAttributes();
		for(int i=0; i<attr.getLength(); i++){
			if(attr.item(i).getNodeName().equals("android:permission")){
				name = attr.item(i).getNodeValue();
			}
		}
		return name;
	}
	
	private void checkUsesPermissionForActivityAction(Document doc){
		NodeList activity = doc.getElementsByTagName("activity");
		for(int i=0; i<activity.getLength(); i++){
			NodeList child = activity.item(i).getChildNodes();
			for(int j=0; j<child.getLength(); j++){
				if(child.item(j).getNodeName().equals("intent-filter")){
					NodeList grandChild = child.item(j).getChildNodes();
					for(int k=0; k<grandChild.getLength(); k++){
						if(grandChild.item(k).getNodeName().equals("action")){
							Element elem = (Element) grandChild.item(k);
							String name = elem.getAttribute("android:name");
							if(name.equals("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE") || name.equals("android.bluetooth.adapter.action.REQUEST_ENABLE")){
								if(!usePermission.contains("android.permission.BLUETOOTH")){
									System.out.println("Warning: "+name+" action requires android.permission.BLUETOOTH in <uses-permission>.");
								}
							}else if(name.equals("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS")){
								if(!usePermission.contains("android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS")){
									System.out.println("Warning: "+name+" action requires android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS in <uses-permission>.");									
								}
							}else if(name.equals("android.intent.action.SET_ALARM") || name.equals("android.intent.action.SET_TIMER")){
								if(!usePermission.contains("com.android.alarm.permission.SET_ALARM")){
									System.out.println("Warning: "+name+" action requires com.android.alarm.permission.SET_ALARM in <uses-permission>.");									
								}
							}else if(name.equals("android.intent.action.INSTALL_PACKAGE")){								
								if(targetSdk > 22 && (!usePermission.contains("android.permission.REQUEST_INSTALL_PACKAGES"))){									
									System.out.println("Warning: "+name+" action requires android.permission.REQUEST_INSTALL_PACKAGES in <uses-permission>.");
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void checkExposed(NodeList list, String tag){
		for(int i=0; i<list.getLength(); i++){
			String exported = null;
			boolean hasFilter = false;
			boolean hasPermission = false;
			
			Element elem = (Element) list.item(i);
			if(elem.hasAttribute("android:exported")){
				exported = elem.getAttribute("android:exported");
			}
			if(elem.hasAttribute("android:permission")){
				hasPermission = true;
			}			
			NodeList child = elem.getChildNodes();
			for(int j=0; j<child.getLength(); j++){
				if(child.item(j).getNodeName().equals("intent-filter")){
					hasFilter = true;
				}
			}
			
			if(exported == null){
				if(hasFilter && (!hasPermission)){
					System.out.println("Warning: "+elem.getAttribute("android:name")+" "+tag+" component is exported without protection.");					
				}
				if((!hasFilter) && hasPermission){
					System.out.println("Warning: private ("+elem.getAttribute("android:name")+" "+tag+") component does not require permission-based protection.");					
				}
			}else if(exported.equals("true")){
				if(!hasPermission){
					System.out.println("Warning: "+elem.getAttribute("android:name")+" "+tag+" component is exported without protection.");					
				}
				if(!hasFilter){
					System.out.println("Warning: "+elem.getAttribute("android:name")+" "+tag+" component is exported without intent-filter.");					
				}
			}else if(exported.equals("false")){
				//not related to security
				if(hasFilter){
					System.out.println("Warning: private ("+elem.getAttribute("android:name")+" "+tag+") component does not require intent-filter unless it is supposed to receive intent from system.");					
				}
				if(hasPermission){
					System.out.println("Warning: private ("+elem.getAttribute("android:name")+" "+tag+") component does not require permission-based protection.");					
				}
			}
		}
	}
	
	private void checkExposedServices(Document doc){
		NodeList services = doc.getElementsByTagName("service");
		checkExposed(services, "service");
	}
	
	private void checkExposedReceivers(Document doc){
		NodeList receivers = doc.getElementsByTagName("receiver");
		checkExposed(receivers, "receiver");
	}
	
	private void checkExposedProviders(Document doc){
		NodeList sdk = doc.getElementsByTagName("uses-sdk");
		Element elem = (Element) sdk.item(0);
		
		boolean allExported = false;
		int minSdk = 0;
		if(elem.getAttribute("android:minSdkVersion").equals("")){
			minSdk = 1;
		}else{
			minSdk = Integer.parseInt(elem.getAttribute("android:minSdkVersion"));
		}
		if(minSdk<17){
			allExported = true;
		}
		
		NodeList provider = doc.getElementsByTagName("provider");
		for(int i=0; i<provider.getLength(); i++){
			Element proElem = (Element) provider.item(i);
			boolean hasPermission = false;
			boolean exported = false;
			boolean hasGrantUriP = false;
			boolean hasChild = false;
			if(proElem.hasAttribute("android:permission") || proElem.hasAttribute("android:readPermission") || proElem.hasAttribute("android:writePermission")){
				hasPermission = true;
			}
			if(!proElem.hasAttribute("android:exported")){
				exported = allExported;
			}else if(proElem.getAttribute("android:exported").equals("true")){
				exported = true;
			}
			
			if(exported && (!hasPermission)){
				System.out.println("Warning: "+proElem.getAttribute("android:name")+" provider component is exported without protection.");				
			}
			if((!exported) && hasPermission){
				System.out.println("Warning: private "+proElem.getAttribute("android:name")+" provider component does not require permission-based protection.");				
			}
			//check for grantUriPermissions
			if(proElem.hasAttribute("android:grantUriPermissions")){
				if(proElem.getAttribute("android:grantUriPermissions").equals("true")){
					hasGrantUriP = true;
				}
			}
			NodeList child = provider.item(i).getChildNodes();
			for(int j=0; j<child.getLength(); j++){
				if(child.item(j).getNodeName().equals("grant-uri-permission")){
					hasChild = true;
				}
			}
			if(hasGrantUriP && hasChild){
				System.out.println("Warning: either make android:grantUriPermissions=false or remove <grant-uri-permission> in "+proElem.getAttribute("android:name")+" provider component.");				
			}
		}
	}
	
	private void checkActivity(Document doc){
		NodeList activity = doc.getElementsByTagName("activity");
		for(int i=0; i<activity.getLength(); i++){
			Element elem = (Element) activity.item(i);
			String exported = null;
			boolean hasFilter = false;
			if(elem.hasAttribute("android:exported")){
				exported = elem.getAttribute("android:exported");
			}
			NodeList child = elem.getChildNodes();
			for(int j=0; j<child.getLength(); j++){
				if(child.item(j).getNodeName().equals("intent-filter")){
					hasFilter = true;
				}
			}
			if(exported != null){
				if(exported.equals("false") && hasFilter){
					System.out.println("Warning: private "+elem.getAttribute("android:name")+" activity component does not require intent-filter unless it is supposed to receive intents from system.");					
				}
				if(exported.equals("true") && (!hasFilter)){
					System.out.println("Warning: exported "+elem.getAttribute("android:name")+" activity component without intent-filter.");					
				}
			}
		}
	}
}
