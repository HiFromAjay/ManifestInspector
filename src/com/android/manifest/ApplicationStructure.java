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

public class ApplicationStructure {
	
	ArrayList<String> applicationAttributes = new ArrayList<String>();
	ArrayList<String> appChild = new ArrayList<String>();
	ArrayList<String> activityAttributes = new ArrayList<String>();
	ArrayList<String> activityName = new ArrayList<String>();
	ArrayList<String> activityAliasAttributes = new ArrayList<String>();
	ArrayList<String> serviceAttributes = new ArrayList<String>();
	ArrayList<String> receiverAttributes = new ArrayList<String>();
	ArrayList<String> providerAttributes = new ArrayList<String>();
	ArrayList<String> dataAttributes = new ArrayList<String>();
	ArrayList<String> grantUriPermissionAttributes = new ArrayList<String>();
	ArrayList<String> pathPermissionAttributes = new ArrayList<String>();
	ArrayList<String> activityTFAttributes = new ArrayList<String>();
	ArrayList<String> tempAttributes = new ArrayList<String>();
	ArrayList<String> serviceName = new ArrayList<String>();
	ArrayList<String> receiverName = new ArrayList<String>();
	ArrayList<String> providerName = new ArrayList<String>();
	
	String packageName;
	boolean hasBanner = false;
	
	public void checkAppStructure(String filename, Document doc, String pack){		
		packageName = pack;
		populateAttributes(filename);
		checkApplicationTag(doc, filename);
		checkActivityTag(doc, filename);
		checkActivityAliasTag(doc);
		checkServiceTag(doc);
		checkReceiverTag(doc);
		checkProviderTag(doc);
		checkUsesLibraryTag(doc);
		checkMetaDataTag(doc);
		checkIntentFilterTag(doc);
		checkActionTag(doc);
		checkCategoryTag(doc);
		checkDataTag(doc);
		checkGrantUriPermissionTag(doc);
		checkPathPermission(doc);
		checkTVCategory(doc);
	}
	
	private void populateAttributes(String filename){
		try {
			Scanner data = new Scanner(new File(filename));
			while(data.hasNext()){
				String next = data.next();
				if(next.startsWith("application+")){
					applicationAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("appChild+")){
					appChild.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("activity+")){
					activityAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("activity-alias+")){
					activityAliasAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("service+")){
					serviceAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("receiver+")){
					receiverAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("provider+")){
					providerAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("data+")){
					dataAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("grant-uri-permission+")){
					grantUriPermissionAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("path-permission+")){
					pathPermissionAttributes.add(next.substring(next.indexOf("+")+1));					
				}
				if(next.startsWith("actTF+")){
					activityTFAttributes.add(next.substring(next.indexOf("+")+1));					
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
				//android:taskAffinity attribute of <activity> can have empty value
				if(!(tag.equals("<activity>") && attr.item(j).getNodeName().equals("android:taskAffinity"))){
					System.out.println("Warning: "+attr.item(j).getNodeName()+ " attribute of "+tag+" has empty value.");
				}				
			}
		}
	}
	
	private void checkApplicationTag(Document doc, String filename){
		NodeList app = doc.getElementsByTagName("application");
		Element elem = (Element) app.item(0);
		NamedNodeMap attr = elem.getAttributes();
		checkAttributes(applicationAttributes, "<application>", attr);
		for(int j=0; j<attr.getLength(); j++){
			//for android:banner
			if(attr.item(j).getNodeName().equals("android:banner")){
				hasBanner = true;
			}
			//for android:manageSpaceActivity
			if(attr.item(j).getNodeName().equals("android:manageSpaceActivity")){				
				String mActivityName = attr.item(j).getNodeValue().substring(attr.item(j).getNodeValue().lastIndexOf(".")+1);
				boolean hasActivity = false;
				NodeList activity = doc.getElementsByTagName("activity");
				for(int k=0; k<activity.getLength(); k++){
					Element elem1 = (Element) activity.item(k);
					NamedNodeMap actAttr = elem1.getAttributes();
					for(int l=0; l<actAttr.getLength(); l++){
						if(actAttr.item(l).getNodeName().equals("android:name")){
							String name = actAttr.item(l).getNodeValue().substring(actAttr.item(l).getNodeValue().lastIndexOf(".")+1);
							if(name.equals(mActivityName)){
								hasActivity = true;
							}
						}
					}
				}
				if(!hasActivity){
					System.out.println("Warning: android:manageSpaceActivity in <application> requires "+mActivityName+" activity to be declared.");					
				}
			}
			//for <android:supportsRtl>
			if(attr.item(j).getNodeName().equals("android:supportsRtl")){
				if(attr.item(j).getNodeValue().equals("true")){
					boolean hasTargetVersion = false;
					NodeList usesSdk = doc.getElementsByTagName("uses-sdk");
					for(int n=0; n<usesSdk.getLength(); n++){
						Element elem2 = (Element) usesSdk.item(n);
						NamedNodeMap sdkAttr = elem2.getAttributes();
						for(int m=0; m<sdkAttr.getLength(); m++){
							if(sdkAttr.item(m).getNodeName().equals("android:targetSdkVersion")){
								hasTargetVersion = true;
								if(Integer.parseInt(sdkAttr.item(m).getNodeValue()) < 17){
									System.out.println("Warning: value of android:targetSdkVersion must be 17 or higher to use RTL layouts.");									
								}
							}
						}
					}
					if(!hasTargetVersion){
						System.out.println("Warning: declare android:targetSdkVersion 17 or higher in <uses-sdk> to use RTL layouts.");
					}
				}
			}
			//for android:uiOptions
			if(attr.item(j).getNodeName().equals("android:uiOptions")){
				if(!(attr.item(j).getNodeValue().equals("none") || attr.item(j).getNodeValue().equals("splitActionBarWhenNarrow"))){
					System.out.println("Warning: value of android:uiOptions in <application> must be either none or splitActionBarWhenNarrow.");
				}
			}
		}
		//check child of <application>
		NodeList applicationChild = app.item(0).getChildNodes();
		for(int i=0; i<applicationChild.getLength(); i++){			
			if(applicationChild.item(i).getNodeType() == Node.ELEMENT_NODE){
				if(!appChild.contains(applicationChild.item(i).getNodeName())){
					System.out.println("Warning: "+applicationChild.item(i).getNodeName()+" is unrecognized child of <application>.");					
				}
			}
		}
	}
	
	private void checkActivityTag(Document doc, String filename){
		NodeList activity = doc.getElementsByTagName("activity");
		addActivityName(activity);		
		for(int i=0; i<activity.getLength(); i++){
			Element elem = (Element) activity.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(activityAttributes, "<activity>", attr);
			//for android:name
			String name = null;
			String launchMode = "standard";
			boolean hasAndroidName = false;
			for(int m=0; m<attr.getLength(); m++){
				if(attr.item(m).getNodeName().equals("android:name")){
					hasAndroidName = true;
					name = attr.item(m).getNodeValue().substring(attr.item(m).getNodeValue().lastIndexOf(".")+1);					
				}
				if(attr.item(m).getNodeName().equals("android:launchMode")){
					launchMode = attr.item(m).getNodeValue();
				}
			}
			if(!hasAndroidName){
				System.out.println("Warning: <activity> must have android:name attribute.");
			}
						
			for(int k=0; k<attr.getLength(); k++){
				//for android:banner
				if(attr.item(k).getNodeName().equals("android:banner")){
					hasBanner = true;
				}
				//for android:allowTaskReparenting
				if(attr.item(k).getNodeName().equals("android:allowTaskReparenting")){
					if(attr.item(k).getNodeValue().equals("true")){						
						if(launchMode.equals("singleTask") || launchMode.equals("singleInstance")){
							System.out.println("Warning: reparenting is not allowed in singleTask or singleInstance android:launchMode in "+name+" activity.");							
						}
					}
				}
				//for android:parentActivityName
				if(attr.item(k).getNodeName().equals("android:parentActivityName")){
					String parentName = null;
					if(attr.item(k).getNodeValue().startsWith(".")){
						parentName = packageName+attr.item(k).getNodeValue();
					}else{
						parentName = attr.item(k).getNodeValue();
					}
					if(!activityName.contains(parentName)){
						System.out.println("Warning: android:parentActivityName in "+name+" activity requires "+parentName+" activity to be declared.");						
					}
				}
				//for android:uiOptions
				if(attr.item(k).getNodeName().equals("android:uiOptions")){
					if(!(attr.item(k).getNodeValue().equals("none") || attr.item(k).getNodeValue().equals("splitActionBarWhenNarrow"))){
						System.out.println("Warning: value of android:uiOptions attribute in <activity> is incorrect.");
					}
				}
				//for android:configChanges
				if(attr.item(k).getNodeName().equals("android:configChanges")){
					populateTempAttributes(filename, "android:configChanges");
					String value = attr.item(k).getNodeValue();
					Scanner scan = new Scanner(value);
					scan.useDelimiter("\\|");
					ArrayList<String> multipleValues = new ArrayList<String>();
					while(scan.hasNext()){
						multipleValues.add(scan.next());
					}
					scan.close();
					boolean wrongValue = false;
					for(int n=0; n<multipleValues.size(); n++){
						if(!tempAttributes.contains(multipleValues.get(n))){
							wrongValue = true;
						}
					}
					if(wrongValue){
						System.out.println("Warning: value of android:configChanges attribute in <activity> is incorrect.");
					}
				}
				//for android:documentLaunchMode
				if(attr.item(k).getNodeName().equals("android:documentLaunchMode")){
					populateTempAttributes(filename, "android:documentLaunchMode");
					if(!tempAttributes.contains(attr.item(k).getNodeValue())){
						System.out.println("Warning: value of android:documentLaunchMode attribute in <activity> is incorrect.");
					}
					if(attr.item(k).getNodeValue().equals("intoExisting") || attr.item(k).getNodeValue().equals("always")){
						if(!launchMode.equals("standard")){
							System.out.println("Warning: for android:documentLaunchMode values other than none and never the activity must be defined with android:launchMode=\"standard\".");
						}
					}
				}
				//for android:launchMode
				if(attr.item(k).getNodeName().equals("android:launchMode")){
					populateTempAttributes(filename, "android:launchMode");
					if(!tempAttributes.contains(attr.item(k).getNodeValue())){
						System.out.println("Warning: value of android:launchMode attribute in <activity> is incorrect.");
					}
				}
				//for android:screenOrientation
				if(attr.item(k).getNodeName().equals("android:screenOrientation")){
					populateTempAttributes(filename, "android:screenOrientation");
					if(!tempAttributes.contains(attr.item(k).getNodeValue())){
						System.out.println("Warning: value of android:screenOrientation attribute in <activity> is incorrect.");						
					}
				}
				//for android:windowSoftInputMode
				if(attr.item(k).getNodeName().equals("android:windowSoftInputMode")){
					populateTempAttributes(filename, "android:windowSoftInputMode");
					String value = attr.item(k).getNodeValue();
					ArrayList<String> valueList = new ArrayList<String>();
					Scanner scan = new Scanner(value);
					scan.useDelimiter("\\|");
					while(scan.hasNext()){
						valueList.add(scan.next());
					}
					scan.close();
					boolean wrong = false;
					int state = 0, adjust = 0;
					for(int l=0; l<valueList.size(); l++){						
						if(!tempAttributes.contains(valueList.get(l))){
							wrong = true;
						}
						if(valueList.get(l).startsWith("state")){
							state++;
						}
						if(valueList.get(l).startsWith("adjust")){
							adjust++;
						}
					}
					if(wrong){
						System.out.println("Warning: value of android:windowSoftInputMode attribute in <activity> is incorrect.");						
					}
					if(state>1 || adjust>1){
						System.out.println("Warning: setting multiple state... or adjust... values in android:windowSoftInputMode attribute of <activity> has undefined results.");						
					}
				}				
			}			
			//check child of activity
			NodeList activityChild = activity.item(i).getChildNodes();
			checkChild(activityChild, "<activity>");
									
		}		 
	}
	
	private void checkChild(NodeList list, String tag){
		boolean filterHasAction = false;
		for(int j=0; j<list.getLength(); j++){
			if(list.item(j).getNodeType() == Node.ELEMENT_NODE){
				if(tag.equals("<provider>")){
					if(!(list.item(j).getNodeName().equals("grant-uri-permission") || list.item(j).getNodeName().equals("meta-data")
							|| list.item(j).getNodeName().equals("path-permission"))){
						System.out.println("Warning: "+list.item(j).getNodeName()+" is unrecognized child of "+tag+".");						
					}
				}else if(tag.equals("<intent-filter>")){
					if(!(list.item(j).getNodeName().equals("action") || list.item(j).getNodeName().equals("category")
							|| list.item(j).getNodeName().equals("data"))){
						System.out.println("Warning: "+list.item(j).getNodeName()+" is unrecognized child of "+tag+".");						
					}
					if(list.item(j).getNodeName().equals("action")){
						filterHasAction = true;
					}
				}else{
					if(!(list.item(j).getNodeName().equals("intent-filter") || list.item(j).getNodeName().equals("meta-data"))){
						System.out.println("Warning: "+list.item(j).getNodeName()+" is unrecognized child of "+tag+".");						
					}
				}				
			}
		}
		if(tag.equals("<intent-filter>") && !filterHasAction){
			System.out.println("Warning: <intent-filter> must contain at least one <action>.");			
		}
	}
	
	private void addActivityName(NodeList activity){
		for(int i=0; i<activity.getLength(); i++){
			Element elem = (Element) activity.item(i);
			NamedNodeMap attr = elem.getAttributes();
			for(int j=0; j<attr.getLength(); j++){
				if(attr.item(j).getNodeName().equals("android:name")){
					String name = null;
					if(attr.item(j).getNodeValue().startsWith(".")){
						name = packageName+attr.item(j).getNodeValue();
					}else{
						name = attr.item(j).getNodeValue();
					}
					if(activityName.contains(name)){
						System.out.println("Warning: "+name+" <activity> already exists in the application.");						
					}else{
						activityName.add(name);
					}										
				}
			}
		}
	}
	
	private void checkActivityAliasTag(Document doc){
		NodeList activityAlias = doc.getElementsByTagName("activity-alias");
		for(int i=0; i<activityAlias.getLength(); i++){
			Element elem = (Element) activityAlias.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(activityAliasAttributes, "<activity-alias>", attr);
			//check child of activity-alias
			NodeList aliasChild = activityAlias.item(i).getChildNodes();
			checkChild(aliasChild, "<activity-alias>");
			//get alias name
			String aliasName = "unnamed";
			for(int k=0; k<attr.getLength(); k++){
				if(attr.item(k).getNodeName().equals("android:name")){
					aliasName = attr.item(k).getNodeValue().substring(attr.item(k).getNodeValue().lastIndexOf(".")+1);
				}				
			}
			boolean targetActivity = false;
			boolean matchTarget = false;
			String target = null;
			for(int j=0; j<attr.getLength(); j++){
				//for android:targetActivity
				if(attr.item(j).getNodeName().equals("android:targetActivity")){
					targetActivity = true;
					if(attr.item(j).getNodeValue().startsWith(".")){
						target = packageName+attr.item(j).getNodeValue();
					}else{
						target = attr.item(j).getNodeValue();
					}
					
					if(!activityName.contains(target)){						
						System.out.println("Warning: declare target activity for "+aliasName+" <activity-alias>.");						
					}else{
						matchTarget = true;
					}
				}
			}
			if(!targetActivity){
				System.out.println("Warning: declare target activity for "+aliasName+" <activity-alias>.");
			}
			if(matchTarget){
				Node targetAct = null;
				NodeList activities = doc.getElementsByTagName("activity");
				for(int m=0; m<activities.getLength(); m++){
					Element actElem = (Element) activities.item(m);
					NamedNodeMap actMap = actElem.getAttributes();
					for(int n=0; n<actMap.getLength(); n++){
						if(actMap.item(n).getNodeName().equals("android:name")){
							String nameValue = null;
							if(actMap.item(n).getNodeValue().startsWith(".")){
								nameValue = packageName+actMap.item(n).getNodeValue();
							}else{
								nameValue = actMap.item(n).getNodeValue();
							}
							if(nameValue.equals(target)){
								targetAct = activities.item(m);								
							}
						}
					}
				}
				if(activityAlias.item(i).compareDocumentPosition(targetAct) != 2){
					System.out.println("Warning: "+target+" <activity> must prcede "+aliasName+" <activity-alias>.");
				}
			}												
		}
	}
	
	private void checkServiceTag(Document doc){
		NodeList service = doc.getElementsByTagName("service");
		for(int i=0; i<service.getLength(); i++){
			Element elem = (Element) service.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(serviceAttributes, "<service>", attr);
			//check child of service
			NodeList serviceChild = service.item(i).getChildNodes();
			checkChild(serviceChild, "<service>");
			//for android:name
			checkName(attr, "<service>");
		}
	}
	
	private void checkName(NamedNodeMap attr, String tag){
		boolean hasName = false;
		for(int j=0; j<attr.getLength(); j++){
			if(attr.item(j).getNodeName().equals("android:name")){
				hasName = true;
				String name = null;				
				if(attr.item(j).getNodeValue().startsWith(".")){
					name = packageName+attr.item(j).getNodeValue();
				}else{
					name = attr.item(j).getNodeValue();
				}
				if(tag.equals("<service>")){
					if(!serviceName.contains(name)){
						serviceName.add(name);
					}else{
						System.out.println("Warning: "+name+" <service> already exists in the application.");						
					}
				}else if(tag.equals("<receiver>")){
					if(!receiverName.contains(name)){
						receiverName.add(name);
					}else{
						System.out.println("Warning: "+name+" <receiver> already exists in the application.");						
					}
				}else if(tag.equals("<provider>")){
					if(!providerName.contains(name)){
						providerName.add(name);
					}else{
						System.out.println("Warning: "+name+" <provider> already exists in the application.");						
					}
				}				
			}
		}
		if(!hasName){
			System.out.println("Warning: "+tag+" must have android:name attribute.");
		}
	}
	
	private void checkReceiverTag(Document doc){
		NodeList receiver = doc.getElementsByTagName("receiver");
		for(int i=0; i<receiver.getLength(); i++){
			Element elem = (Element) receiver.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(receiverAttributes, "<receiver>", attr);
			//check child of receiver
			NodeList receiverChild = receiver.item(i).getChildNodes();
			checkChild(receiverChild, "<receiver>");
			//for android:name
			checkName(attr, "<receiver>");
			checkReceiverAction(receiver.item(i));
		}
	}
	
	private void checkReceiverAction(Node receiver){
		NodeList child = receiver.getChildNodes();
		for(int i=0; i<child.getLength(); i++){
			if(child.item(i).getNodeName().equals("intent-filter")){
				NodeList grandChild = child.item(i).getChildNodes();
				for(int j=0; j<grandChild.getLength(); j++){
					if(grandChild.item(j).getNodeName().equals("action")){
						Element elem = (Element) grandChild.item(j);
						String actionName = elem.getAttribute("android:name");
							if(actionName.equals("android.intent.action.BATTERY_CHANGED") || actionName.equals("android.intent.action.CONFIGURATION_CHANGED") || 
									actionName.equals("android.intent.action.SCREEN_OFF") || actionName.equals("android.intent.action.SCREEN_ON") || 
									actionName.equals("android.intent.action.TIME_TICK") || actionName.equals("android.media.action.HDMI_AUDIO_PLUG") ||
									actionName.equals("android.intent.action.USER_BACKGROUND") || actionName.equals("android.intent.action.USER_FOREGROUND")){
								System.out.println("Warning: cannot receive "+actionName+" through components declared in manifests. Register explicitly with Context.registerReceiver().");																
							}						
					}
				}
			}
		}
	}
	
	private void checkProviderTag(Document doc){
		NodeList provider = doc.getElementsByTagName("provider");
		for(int i=0; i<provider.getLength(); i++){
			Element elem = (Element) provider.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(providerAttributes, "<provider>", attr);
			//check child of provider
			NodeList providerChild = provider.item(i).getChildNodes();
			checkChild(providerChild, "<provider>");
			//for android:authorities
			boolean hasAuthorities = false;
			for(int j=0; j<attr.getLength(); j++){
				if(attr.item(j).getNodeName().equals("android:authorities")){
					hasAuthorities = true;
				}
			}
			if(!hasAuthorities){
				System.out.println("Warning: <provider> must have android:authorities attribute.");
			}
			//for android:name
			checkName(attr, "<provider>");
		}
	}
	
	private void checkUsesLibraryTag(Document doc){
		NodeList usesLibrary = doc.getElementsByTagName("uses-library");
		for(int i=0; i<usesLibrary.getLength(); i++){
			Element elem = (Element) usesLibrary.item(i);
			NamedNodeMap attr = elem.getAttributes();
			for(int j=0; j<attr.getLength(); j++){
				if(!(attr.item(j).getNodeName().equals("android:name") || attr.item(j).getNodeName().equals("android:required"))){
					System.out.println("Warning: "+attr.item(j).getNodeName()+ " is not an attribute of <uses-library>.");					
				}
				if(attr.item(j).getNodeValue().isEmpty()){
					System.out.println("Warning: "+attr.item(j).getNodeName()+ " attribute of <uses-library> has empty value.");
				}
				if(attr.item(j).getNodeName().equals("android:required")){
					if(!(attr.item(j).getNodeValue().equals("true") || attr.item(j).getNodeValue().equals("false"))){
						System.out.println("Warning: value of android:required attribute in <uses-library> is incorrect.");
					}
				}
			}
		}
	}
	
	private void checkMetaDataTag(Document doc){
		NodeList metaData = doc.getElementsByTagName("meta-data");
		for(int i=0; i<metaData.getLength(); i++){
			Element elem = (Element) metaData.item(i);
			NamedNodeMap attr = elem.getAttributes();
			for(int j=0; j<attr.getLength(); j++){
				if(!(attr.item(j).getNodeName().equals("android:name") || attr.item(j).getNodeName().equals("android:resource")
						|| attr.item(j).getNodeName().equals("android:value"))){
					System.out.println("Warning: "+attr.item(j).getNodeName()+ " is not an attribute of <meta-data>.");					
				}
				if(attr.item(j).getNodeValue().isEmpty()){
					System.out.println("Warning: "+attr.item(j).getNodeName()+ " attribute of <meta-data> has empty value.");					
				}
			}
		}
	}
	
	private void checkIntentFilterTag(Document doc){
		NodeList intentFilter = doc.getElementsByTagName("intent-filter");
		for(int i=0; i<intentFilter.getLength(); i++){
			Element elem = (Element) intentFilter.item(i);
			NamedNodeMap attr = elem.getAttributes();
			for(int j=0; j<attr.getLength(); j++){
				if(!(attr.item(j).getNodeName().equals("android:icon") || attr.item(j).getNodeName().equals("android:label")
						|| attr.item(j).getNodeName().equals("android:priority"))){
					System.out.println("Warning: "+attr.item(j).getNodeName()+ " is not an attribute of <intent-filter>.");					
				}
				if(attr.item(j).getNodeValue().isEmpty()){
					System.out.println("Warning: "+attr.item(j).getNodeName()+ " attribute of <intent-filter> has empty value.");					
				}
			}
			//check for child
			NodeList intentFilterChild = intentFilter.item(i).getChildNodes();
			checkChild(intentFilterChild, "<intent-filter>");
			//check <data> of an intent-filter
			boolean scheme = false, host = false, port = false, path = false, pathPattern = false, pathPrefix = false, mime = false, hasData = false;
			for(int k=0; k<intentFilterChild.getLength(); k++){
				if(intentFilterChild.item(k).getNodeName().equals("data")){
					hasData = true;
					Element elem1 = (Element) intentFilterChild.item(k);
					NamedNodeMap attrData = elem1.getAttributes();
					for(int l=0; l<attrData.getLength(); l++){
						if(attrData.item(l).getNodeName().equals("android:scheme")){
							scheme = true;
						}else if(attrData.item(l).getNodeName().equals("android:host")){
							host = true;
						}else if(attrData.item(l).getNodeName().equals("android:port")){
							port = true;
						}else if(attrData.item(l).getNodeName().equals("android:path")){
							path = true;
						}else if(attrData.item(l).getNodeName().equals("android:pathPattern")){
							pathPattern = true;
						}else if(attrData.item(l).getNodeName().equals("android:pathPrefix")){
							pathPrefix = true;
						}else if(attrData.item(l).getNodeName().equals("android:mimeType")){
							mime = true;
						}
					}
				}
			}
			if(hasData){
				if(!(scheme || mime)){
					System.out.println("Warning: <data> must have either android:scheme or android:mimeType attribute.");					
				}
				if(port || path || pathPattern || pathPrefix){
					if(!host){
						System.out.println("Warning: declare android:host attribute in <data>.");						
					}
				}
			}
		}
	}
	
	private void checkActionTag(Document doc){
		NodeList action = doc.getElementsByTagName("action");
		for(int i=0; i<action.getLength(); i++){
			Element elem = (Element) action.item(i);
			NamedNodeMap attr = elem.getAttributes();
			for(int j=0; j<attr.getLength(); j++){
				if(!attr.item(j).getNodeName().equals("android:name")){
					System.out.println("Warning: "+attr.item(j).getNodeName()+ " is not an attribute of <action>.");					
				}
				if(attr.item(j).getNodeValue().isEmpty()){
					System.out.println("Warning: "+attr.item(j).getNodeName()+ " attribute of <action> has empty value.");					
				}
			}
		}
	}
	
	private void checkCategoryTag(Document doc){
		NodeList category = doc.getElementsByTagName("category");
		for(int i=0; i<category.getLength(); i++){
			Element elem = (Element) category.item(i);
			NamedNodeMap attr = elem.getAttributes();
			for(int j=0; j<attr.getLength(); j++){
				if(!attr.item(j).getNodeName().equals("android:name")){
					System.out.println("Warning: "+attr.item(j).getNodeName()+ " is not an attribute of <category>.");					
				}
				if(attr.item(j).getNodeValue().isEmpty()){
					System.out.println("Warning: "+attr.item(j).getNodeName()+ " attribute of <category> has empty value.");
				}
			}
		}
	}
	
	private void checkDataTag(Document doc){
		NodeList data = doc.getElementsByTagName("data");
		for(int i=0; i<data.getLength(); i++){
			Element elem = (Element) data.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(dataAttributes, "<data>", attr);
		}
	}
	
	private void checkGrantUriPermissionTag(Document doc){
		NodeList grantUri = doc.getElementsByTagName("grant-uri-permission");
		for(int i=0; i<grantUri.getLength(); i++){
			Element elem = (Element) grantUri.item(i);
			NamedNodeMap attr = elem.getAttributes();
			if(attr.getLength()>1){
				System.out.println("Warning: <grant-uri-permission> can have only one path-related attribute.");
			}
			checkAttributes(grantUriPermissionAttributes, "<grant-uri-permission>", attr);
		}
	}
	
	private void checkPathPermission(Document doc){
		NodeList pathPermission = doc.getElementsByTagName("path-permission");
		for(int i=0; i<pathPermission.getLength(); i++){
			Element elem = (Element) pathPermission.item(i);
			NamedNodeMap attr = elem.getAttributes();
			checkAttributes(pathPermissionAttributes, "<path-permission>", attr);
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
	
	private void checkTVCategory(Document doc){
		boolean hasCategory = false;
		NodeList activity = doc.getElementsByTagName("activity");
		for(int i=0; i<activity.getLength(); i++){
			NodeList child = activity.item(i).getChildNodes();
			for(int j=0; j<child.getLength(); j++){				
				if(child.item(j).getNodeName().equals("intent-filter")){
					NodeList grandChild = child.item(j).getChildNodes();
					for(int k=0; k<grandChild.getLength(); k++){
						if(grandChild.item(k).getNodeType() == Node.ELEMENT_NODE){
							Element elem = (Element) grandChild.item(k);
							if(elem.getNodeName().equals("category")){
								if(elem.getAttribute("android:name").equals("android.intent.category.LEANBACK_LAUNCHER")){
									hasCategory = true;
								}
							}
						}						
					}
				}
			}
		}
		if(hasBanner && (!hasCategory)){
			System.out.println("Warning: android:banner attribute requires activity with android.intent.category.LEANBACK_LAUNCHER category.");			
		}
		if((!hasBanner) && hasCategory){
			System.out.println("Warning: activity with android.intent.category.LEANBACK_LAUNCHER category must require android:banner attribute in <application> or <activity>.");			
		}
	}
}
