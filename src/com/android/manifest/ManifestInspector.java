package com.android.manifest;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class ManifestInspector {
				
	public static void main(String args[]){		
		
		//replace the file path with your AndroidManifest.xml file path
		String filepath = "G:/app.cobo.launcher/AndroidManifest.xml";
		File manifest = new File(filepath);
		if(!manifest.exists()){
			System.out.println("AndroidManifest.xml file does not exist at "+filepath+". Please replace the file path in ManifestInspector class with your manifest file path.");
		}else{
			String filename = "FeedData.txt";
			Document doc = getDocument(manifest);		 
			//check android manifest structure
			AMStructure ams = new AMStructure();
			ams.checkAMStructure(doc, filename);
			//check permission-based security issues
			AMSecurity amSecurity = new AMSecurity();
			amSecurity.checkSecurity(doc, filename);
		}					
	}
	
	private static Document getDocument(File manifest){
		Document doc = null;
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(manifest);
			doc.getDocumentElement().normalize();			
		}catch(Exception e){
			e.printStackTrace();
		}		
		return doc;
	}	

}
