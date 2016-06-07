package com.android.manifest;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AMStructure {
	
	ArrayList<String> manifestAttributes = new ArrayList<String>();
	ArrayList<String> childOfManifestTag = new ArrayList<String>();
	ArrayList<String> instrumentationAttributes = new ArrayList<String>();
	ArrayList<String> supportsScreensAttributes = new ArrayList<String>();
	ArrayList<String> usesConfigurationAttributes = new ArrayList<String>();
	ArrayList<String> usesFeatureAttributes = new ArrayList<String>();
	ArrayList<String> usesFeatureValues = new ArrayList<String>();
	ArrayList<String> usesSdkAttributes = new ArrayList<String>();
	ArrayList<String> pTreeAttributes = new ArrayList<String>();
	ArrayList<String> pGroupAttributes = new ArrayList<String>();
	ArrayList<String> permissionAttributes = new ArrayList<String>();
	ArrayList<String> usesPermissionAttributes = new ArrayList<String>();
	ArrayList<String> usesPermission23Attributes = new ArrayList<String>();
	ArrayList<String> pRequireFeature = new ArrayList<String>();
	ArrayList<String> featureList = new ArrayList<String>();
	ArrayList<String> currentFeatureList = new ArrayList<String>();
	ArrayList<String> pGroup = new ArrayList<String>();
	ArrayList<String> tempAttributes = new ArrayList<String>();
	
	String packageName;
	
	public void checkAMStructure(Document doc, String filename){
		
		populateAttributes(filename);
				
		Node root = doc.getDocumentElement();		
		checkManifestTag(root);		
		checkChildOfManifestTag(root);
		checkCompatibleScreensTag(doc, filename);
		checkInstrumentationTag(doc);
		checkSupportsGlTextureTag(doc);
		checkSupportsScreensTag(doc);
		checkUsesConfigurationTag(doc, filename);
		checkUsesFeatureTag(doc);
		checkUsesSdkTag(doc);
		checkPermissionTreeTag(doc);
		checkPermissionGroupTag(doc);
		checkPermissionTag(doc);
		checkUsesPermissionTag(doc);
		checkUsesPermission23Tag(doc);
		checkApplicationTag(root);
		
		ApplicationStructure as = new ApplicationStructure();
		as.checkAppStructure(filename, doc, packageName);
	}
	
	private void populateAttributes(String filename){
		try {
			Scanner data = new Scanner(new File(filename));			
			while(data.hasNext()){
				String next = data.next();
				if(next.startsWith("manifest+")){
					manifestAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("rootChild+")){
					childOfManifestTag.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("instrumentation+")){
					instrumentationAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("supports-screens+")){
					supportsScreensAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("uses-configuration+")){
					usesConfigurationAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("uses-feature+")){
					usesFeatureAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("featureValue+")){
					usesFeatureValues.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("uses-sdk+")){
					usesSdkAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("permission-tree+")){
					pTreeAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("permission-group+")){
					pGroupAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("permission+")){
					permissionAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("uses-permission+")){
					usesPermissionAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("uses-permission-sdk-23+")){
					usesPermission23Attributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("pRequireFeature+")){
					pRequireFeature.add(next.substring(next.indexOf("+")+1));					
				}
			}
			data.close();			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private void checkAttributes(ArrayList<String> list, String tag, NamedNodeMap attr){
		for(int j=0; j<attr.getLength(); j++){
			if(!list.contains(attr.item(j).getNodeName())){
				System.out.println("Warning: "+attr.item(j).getNodeName()+ " is not an attribute of "+tag+".");				
			}
			if(attr.item(j).getNodeValue().isEmpty()){
				System.out.println("Warning: "+attr.item(j).getNodeName()+ " attribute of "+tag+" has empty value.");				
			}
		}
	}
	
	private void checkManifestTag(Node root){
		//check for <manifest> as root element
		if(!root.getNodeName().equals("manifest")){
			System.out.println("Warning: <manifest> must be the root element in AndroidManifest.xml");
		}
		
		//<manifest> must have xmlns:android and package attributes.		
		if(root.getNodeType() == Node.ELEMENT_NODE){
			Element elem = (Element) root;
			if(!elem.hasAttribute("xmlns:android")){
				System.out.println("Warning: <manifest> must have xmlns:android attribute.");
			}
			if(!elem.getAttribute("xmlns:android").equals("http://schemas.android.com/apk/res/android")){
				System.out.println("Warning: value of xmlns:android attribute in <manifest> must be http://schemas.android.com/apk/res/android.");
			}			
			if(!elem.hasAttribute("package")){
				System.out.println("Warning: <manifest> must have package attribute.");
			}					
			packageName = elem.getAttribute("package");			
		}
		
		//check for unspecified attributes and empty value in <manifest>		
		if(root.getNodeType() == Node.ELEMENT_NODE){
			Element elem = (Element) root;
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(manifestAttributes, "<manifest>", attr);
			//check value of android:installLocation
			for(int i=0; i<attr.getLength(); i++){
				if(attr.item(i).getNodeName().equals("android:installLocation")){
					if(!(attr.item(i).getNodeValue().equals("internalOnly") || attr.item(i).getNodeValue().equals("auto")
							|| attr.item(i).getNodeValue().equals("preferExternal"))){
						System.out.println("Warning: android:installLocation of <manifest> can have only following values: auto, internalOnly, or preferExternal.");
					}
				}
			}
		}
	}
	
	private void checkChildOfManifestTag(Node root){
		//check for unrecognized child of <manifest> tag
		NodeList nlist = root.getChildNodes();		
		for(int i=0; i<nlist.getLength(); i++){
			if(nlist.item(i).getNodeType() == Node.ELEMENT_NODE){
				if(!childOfManifestTag.contains(nlist.item(i).getNodeName())){
					System.out.println("Warning: "+nlist.item(i).getNodeName()+" is unrecognized child of <manifest>.");					
				}
			}			
		}
	}
	
	private void checkCompatibleScreensTag(Document doc, String filename){
		boolean childNode = false;		
		NodeList compatibleScreen = doc.getElementsByTagName("compatible-screens");
		if(compatibleScreen.getLength()>1){
			System.out.println("Warning: only one instance of <compatible-screens> is allowed.");
		}else if(compatibleScreen.getLength()==1){
			NodeList screen = compatibleScreen.item(0).getChildNodes();
			for(int i=0; i<screen.getLength(); i++){
				if(screen.item(i).getNodeType() == Node.ELEMENT_NODE){
					childNode = true;
					if(screen.item(i).getNodeName().equals("screen")){
						Element elem = (Element) screen.item(i);
						NamedNodeMap screenAttr = elem.getAttributes();
						for(int k=0; k<screenAttr.getLength(); k++){
							if(!(screenAttr.item(k).getNodeName().equals("android:screenSize") || screenAttr.item(k).getNodeName().equals("android:screenDensity"))){
								System.out.println("Warning: "+screenAttr.item(k).getNodeName()+ " is not an attribute of <screen>.");								
							}
						}
						boolean hasBothAttributes = false;
						if(elem.hasAttribute("android:screenSize") && elem.hasAttribute("android:screenDensity")){
							hasBothAttributes = true;
							populateTempAttributes(filename, "android:screenSize");
							if(elem.getAttribute("android:screenSize").isEmpty()){
								System.out.println("Warning: android:screenSize attribute of <screen> is empty.");
							}else if(!tempAttributes.contains(elem.getAttribute("android:screenSize"))){
								System.out.println("Warning: value of android:screenSize attribute of <screen> is incorrect.");
							}
							populateTempAttributes(filename, "android:screenDensity");
							if(elem.getAttribute("android:screenDensity").isEmpty()){
								System.out.println("Warning: android:screenDensity attribute of <screen> is empty.");
							}else if(!tempAttributes.contains(elem.getAttribute("android:screenDensity"))){
								System.out.println("Warning: value of android:screenDensity attribute of <screen> is incorrect.");																
							}
						}
						if(!hasBothAttributes){
							System.out.println("Warning: <screen> must include both android:screenSize and android:screenDensity attributes.");							
						}						
					}else{
						System.out.println("Warning: "+screen.item(i).getNodeName()+" is unrecognized child of <compatible-screens>.");
					}
				}
			}
			if(!childNode){
				System.out.println("Warning: At least one instance of <screen> must be placed inside <compatible-screens>.");				
			}
		}		
	}
	
	private void checkInstrumentationTag(Document doc){
		NodeList instrumentation = doc.getElementsByTagName("instrumentation");
		for(int i=0; i<instrumentation.getLength(); i++){
			Element elem = (Element) instrumentation.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(instrumentationAttributes, "<instrumentation>", attr);
			for(int j=0; j<attr.getLength(); j++){
				if(attr.item(j).getNodeName().equals("android:functionalTest") || attr.item(j).getNodeName().equals("android:handleProfiling")){
					if(!(attr.item(j).getNodeValue().equals("true") || attr.item(j).getNodeValue().equals("false"))){
						System.out.println("Warning: value of "+attr.item(j).getNodeName()+" attribute of <instrumentation> is incorrect.");
					}
				}
			}
		}
	}
	
	private void checkSupportsGlTextureTag(Document doc){
		NodeList supportsgl = doc.getElementsByTagName("supports-gl-texture");
		for(int i=0; i<supportsgl.getLength(); i++){
			Element elem = (Element) supportsgl.item(i);
			NamedNodeMap attr = elem.getAttributes();			
			for(int j=0; j<attr.getLength(); j++){
				if(!attr.item(j).getNodeName().equals("android:name")){
					System.out.println("Warning: "+attr.item(j).getNodeName()+ " is not an attribute of <suppots-gl-texture>.");					
				}
				if(attr.item(j).getNodeValue().isEmpty()){
					System.out.println("Warning: "+attr.item(j).getNodeName()+ " attribute of <suppots-gl-texture> has empty value.");
				}
			}
		}
	}
	
	private void checkSupportsScreensTag(Document doc){
		NodeList supports = doc.getElementsByTagName("supports-screens");
		for(int i=0; i<supports.getLength(); i++){
			Element elem = (Element) supports.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(supportsScreensAttributes, "<supports-screens>", attr);
			for(int j=0; j<attr.getLength(); j++){				
				if(attr.item(j).getNodeName().equals("android:resizeable")){
					System.out.println("Warning: android:resizeable attribute of <supports-screens> is deprecated.");					
				}				
			}
		}
	}
	
	private void checkUsesConfigurationTag(Document doc, String filename){
		NodeList usesconfig = doc.getElementsByTagName("uses-configuration");
		for(int i=0; i<usesconfig.getLength(); i++){
			Element elem = (Element) usesconfig.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(usesConfigurationAttributes, "<uses-configuration>", attr);
			for(int j=0; j<attr.getLength(); j++){
				if(attr.item(j).getNodeName().equals("android:reqFiveWayNav") || attr.item(j).getNodeName().equals("android:reqHardKeyboard")){
					if(!(attr.item(j).getNodeValue().equals("true") || attr.item(j).getNodeValue().equals("false"))){
						System.out.println("Warning: value of "+attr.item(j).getNodeName()+" attribute of <uses-configuration> is incorrect.");
					}
				}
				if(attr.item(j).getNodeName().equals("android:reqKeyboardType")){
					populateTempAttributes(filename, "android:reqKeyboardType");
					if(!tempAttributes.contains(attr.item(j).getNodeValue())){
						System.out.println("Warning: value of android:reqKeyboardType attribute of <uses-configuration> is incorrect.");
					}
				}
				if(attr.item(j).getNodeName().equals("android:reqNavigation")){
					populateTempAttributes(filename, "android:reqNavigation");
					if(!tempAttributes.contains(attr.item(j).getNodeValue())){
						System.out.println("Warning: value of android:reqNavigation attribute of <uses-configuration> is incorrect.");
					}
				}
				if(attr.item(j).getNodeName().equals("android:reqTouchScreen")){
					populateTempAttributes(filename, "android:reqTouchScreen");
					if(!tempAttributes.contains(attr.item(j).getNodeValue())){
						System.out.println("Warning: value of android:reqTouchScreen attribute of <uses-configuration> is incorrect.");
					}
				}
			}
		}
	}
	
	private void checkUsesFeatureTag(Document doc){
		NodeList features = doc.getElementsByTagName("uses-feature");
		for(int i=0; i<features.getLength(); i++){
			Element elem = (Element) features.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(usesFeatureAttributes, "<uses-feature>", attr);
			for(int j=0; j<attr.getLength(); j++){				
				if(attr.item(j).getNodeName().equals("android:name")){
					featureList.add(attr.item(j).getNodeValue());
					if(!usesFeatureValues.contains(attr.item(j).getNodeValue())){
						System.out.println("Warning: "+attr.item(j).getNodeValue()+ " is unrecognized value for android:name attribute of <uses-feature>.");						
					}
				}
				if(attr.item(j).getNodeName().equals("android:required")){
					if(!(attr.item(j).getNodeValue().equals("true") || attr.item(j).getNodeValue().equals("false"))){
						System.out.println("Warning: value of android:required attribute of <uses-feature> is incorrect.");
					}
				}
			}
		}
	}
	
	private void checkUsesSdkTag(Document doc){
		NodeList sdk = doc.getElementsByTagName("uses-sdk");
		if(sdk.getLength() == 0){
			System.out.println("Warning: declare <uses-sdk>.");
		}else if(sdk.getLength()>1){
			System.out.println("Warning: only one instance of <uses-sdk> is allowed.");
		}
		for(int i=0; i<sdk.getLength(); i++){
			Element elem = (Element) sdk.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(usesSdkAttributes, "<uses-sdk>", attr);
			if(!elem.hasAttribute("android:minSdkVersion")){
				System.out.println("Warning: missing android:minSdkVersion attribute in <uses-sdk>.");
			}
			if(!elem.hasAttribute("android:targetSdkVersion")){
				System.out.println("Warning: missing android:targetSdkVersion attribute in <uses-sdk>.");
			}
		}
	}
	
	private void checkPermissionTreeTag(Document doc){
		NodeList ptree = doc.getElementsByTagName("permission-tree");
		for(int i=0; i<ptree.getLength(); i++){
			Element elem = (Element) ptree.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(pTreeAttributes, "<permission-tree>", attr);			
		}
	}
	
	private void checkPermissionGroupTag(Document doc){
		NodeList pgroup = doc.getElementsByTagName("permission-group");
		for(int i=0; i<pgroup.getLength(); i++){
			Element elem = (Element) pgroup.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(pGroupAttributes, "<permission-group>", attr);			
		}
	}
	
	private void checkPermissionTag(Document doc){
		NodeList permission = doc.getElementsByTagName("permission");
		for(int i=0; i<permission.getLength(); i++){
			Element elem = (Element) permission.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(permissionAttributes, "<permission>", attr);
			for(int j=0; j<attr.getLength(); j++){
				if(attr.item(j).getNodeName().equals("android:protectionLevel")){					
					if(!(attr.item(j).getNodeValue().equals("normal") || attr.item(j).getNodeValue().equals("dangerous")
							|| attr.item(j).getNodeValue().equals("signature") || attr.item(j).getNodeValue().equals("signatureOrSystem"))){
						System.out.println("Warning: value of android:protectionLevel in <permission> is incorrect.");						
					}
				}
			}
		}
	}
	
	private void checkUsesPermissionTag(Document doc){
		NodeList usesp = doc.getElementsByTagName("uses-permission");
		for(int i=0; i<usesp.getLength(); i++){
			Element elem = (Element) usesp.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(usesPermissionAttributes, "<uses-permission>", attr);
			for(int j=0; j<attr.getLength(); j++){				
				if(attr.item(j).getNodeName().equals("android:name")){
					if(pRequireFeature.contains(attr.item(j).getNodeValue())){
						checkFeatureList(attr.item(j).getNodeValue());						
					}
				}				
			}
		}
	}
	
	private void checkUsesPermission23Tag(Document doc){
		NodeList usesp23 = doc.getElementsByTagName("uses-permission-sdk-23");
		for(int i=0; i<usesp23.getLength(); i++){
			Element elem = (Element) usesp23.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(usesPermission23Attributes, "<uses-permission-sdk-23>", attr);
			for(int j=0; j<attr.getLength(); j++){				
				if(attr.item(j).getNodeName().equals("android:name")){
					if(pRequireFeature.contains(attr.item(j).getNodeValue())){
						checkFeatureList(attr.item(j).getNodeValue());						
					}
				}				
			}
		}
	}
		
	private void checkFeatureList(String pName){
		if(pName.equals("android.permission.BLUETOOTH") || pName.equals("android.permission.BLUETOOTH_ADMIN")){
			if((!featureList.contains("android.hardware.bluetooth")) && (!currentFeatureList.contains("android.hardware.bluetooth"))){
				System.out.println("Warning: explicit declaration of android.hardware.bluetooth in <uses-feature> is recommended.");
				currentFeatureList.add("android.hardware.bluetooth");				
			}
		}else if(pName.equals("android.permission.CAMERA")){
			if((!(featureList.contains("android.hardware.camera") && featureList.contains("android.hardware.camera.autofocus"))) &&
					(!(currentFeatureList.contains("android.hardware.camera") && currentFeatureList.contains("android.hardware.camera.autofocus")))){
				System.out.println("Warning: explicit declaration of android.hardware.camera and android.hardware.camera.autofocus in <uses-feature> is recommended.");
				currentFeatureList.add("android.hardware.camera");
				currentFeatureList.add("android.hardware.camera.autofocus");				
			}
		}else if(pName.equals("android.permission.ACCESS_MOCK_LOCATION") || pName.equals("android.permission.ACCESS_LOCATION_EXTRA_COMMANDS")
				|| pName.equals("android.permission.INSTALL_LOCATION_PROVIDER")){
			if((!featureList.contains("android.hardware.location")) && (!currentFeatureList.contains("android.hardware.location"))){
				System.out.println("Warning: explicit declaration of android.hardware.location in <uses-feature> is recommended.");
				currentFeatureList.add("android.hardware.location");				
			}
		}else if(pName.equals("android.permission.ACCESS_COARSE_LOCATION")){
			if((!(featureList.contains("android.hardware.location.network") && featureList.contains("android.hardware.location"))) &&
					(!(currentFeatureList.contains("android.hardware.location.network") && currentFeatureList.contains("android.hardware.location")))){
				System.out.println("Warning: explicit declaration of android.hardware.location.network and android.hardware.location in <uses-feature> is recommended.");
				currentFeatureList.add("android.hardware.location.network");
				currentFeatureList.add("android.hardware.location");				
			}
		}else if(pName.equals("android.permission.ACCESS_FINE_LOCATION")){
			if((!(featureList.contains("android.hardware.location.gps") && featureList.contains("android.hardware.location"))) &&
					(!(currentFeatureList.contains("android.hardware.location.gps") && currentFeatureList.contains("android.hardware.location")))){
				System.out.println("Warning: explicit declaration of android.hardware.location.gps and android.hardware.location in <uses-feature> is recommended.");
				currentFeatureList.add("android.hardware.location.gps");
				currentFeatureList.add("android.hardware.location");				
			}
		}else if(pName.equals("android.permission.RECORD_AUDIO")){
			if((!featureList.contains("android.hardware.microphone")) && (!currentFeatureList.contains("android.hardware.microphone"))){
				System.out.println("Warning: explicit declaration of android.hardware.microphone in <uses-feature> is recommended.");
				currentFeatureList.add("android.hardware.microphone");				
			}
		}else if(pName.equals("android.permission.CALL_PHONE") || pName.equals("android.permission.CALL_PRIVILEGED") || pName.equals("android.permission.MODIFY_PHONE_STATE")
				|| pName.equals("android.permission.READ_SMS") || pName.equals("android.permission.RECEIVE_SMS") || pName.equals("android.permission.RECEIVE_MMS")
				|| pName.equals("android.permission.PROCESS_OUTGOING_CALLS") || pName.equals("android.permission.RECEIVE_WAP_PUSH")
				|| pName.equals("android.permission.SEND_SMS") || pName.equals("android.permission.WRITE_APN_SETTINGS") || pName.equals("android.permission.WRITE_SMS")){
			if((!featureList.contains("android.hardware.telephony")) && (!currentFeatureList.contains("android.hardware.telephony"))){
				System.out.println("Warning: explicit declaration of android.hardware.telephony in <uses-feature> is recommended.");
				currentFeatureList.add("android.hardware.telephony");				
			}
		}else if(pName.equals("android.permission.ACCESS_WIFI_STATE") || pName.equals("android.permission.CHANGE_WIFI_STATE")
				|| pName.equals("android.permission.CHANGE_WIFI_MULTICAST_STATE")){
			if((!featureList.contains("android.hardware.wifi")) && (!currentFeatureList.contains("android.hardware.wifi"))){
				System.out.println("Warning: explicit declaration of android.hardware.wifi in <uses-feature> is recommended.");
				currentFeatureList.add("android.hardware.wifi");			
			}
		}
	}
	
	private void checkApplicationTag(Node root){
		//check for <application>
		NodeList nl = root.getChildNodes();
		boolean hasApplication = false;
		for(int i=0; i<nl.getLength(); i++){
			if(nl.item(i).getNodeName().equals("application")){
				hasApplication = true;
			}
		}
		if(!hasApplication){
			System.out.println("Warning: AndroidManifest.xml must have <application>.");
		}
	}
	
	private void populateTempAttributes(String filename, String name){
		tempAttributes.clear();			
		try {
			Scanner data = new Scanner(new File(filename));
			while(data.hasNext()){
				String next = data.next();
				if(next.startsWith(name+"+")){
					tempAttributes.add(next.substring(next.indexOf("+")+1));					
				}
			}
			data.close();			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}	
}
