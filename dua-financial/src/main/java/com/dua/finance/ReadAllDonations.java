package com.dua.finance;

import java.io.File;
import java.io.FileReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVReader;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

public class ReadAllDonations {
	
	public static final Logger logger = LogManager.getLogger(ReadAllDonations.class);
	public static int tmpEmailCounter = 0;
	public static String tmpEmail = "temp-counter@temp.com";
	
	public static void main(String[] args) {
		
				if(args.length == 0)
				{
					logger.error("please provide a valid source folder!");
					System.exit(0);
				}
				String sourceFolder = args[0];
				try {
					new ReadAllDonations().readDonationData(sourceFolder);
				} catch (Exception e) {
					e.printStackTrace();
				}
	}
	
	public void readDonationData(String sourceFolder) throws Exception
	{		
		Map<String, Donor> donorMap = new HashMap<String, Donor>();
		logger.info("scanning {}", sourceFolder);
		File directory = new File(sourceFolder);
		
		if(!directory.exists())
		{
			logger.error("source folder doesn't exist!");
			return;
		}
		
		File files[] = directory.listFiles();
		if(files.length == 0)
		{
			logger.error("files don't exist!");
			return;
		}
		
		for(File file : files)
		{			
			if(file.getName().contains(AppConstants.FINAL_REPORT) || file.isDirectory())
			{
				logger.info("skipping file/folder {}",file);
				continue;
			}
			logger.info("reading file {}",file);
			readFile(donorMap, file);
		}
		
		// Write to a final consolidated report
		logger.info("total number of unique donors: " + donorMap.size());
		try {
			
			Writer writer = Files.newBufferedWriter(Paths.get(sourceFolder+"/"+AppConstants.FINAL_REPORT));
			writer.append(Donor.getHeader());
			StatefulBeanToCsvBuilder<Donor> builder = new StatefulBeanToCsvBuilder<>(writer);
	        StatefulBeanToCsv<Donor> beanWriter = builder.build();
				        
	        List<Donor> finalList = new ArrayList<Donor>();
	        int counter = 0;
			for (Map.Entry<String, Donor> entry : donorMap.entrySet()) {
				counter++;
				Donor donor = entry.getValue();
				donor.setDonorId(counter);
		        finalList.add(donor);
		    }
			
			beanWriter.write(finalList);
			writer.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static void readFile(Map<String, Donor> donorMap, File fileName) throws Exception 
	{	
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {

            List<String[]> recList = reader.readAll();
            if(recList.isEmpty()) return;

            // Parse header to find column indices
            String[] header = recList.get(0);
            int colCount = header.length;
            int modeIdx = -1, projectIdx = -1, amountIdx = -1;
            for(int h=0; h<colCount; h++) {
            	String col = header[h].trim().toLowerCase();
            	if(col.equals("mode")) modeIdx = h;
            	else if(col.equals("project")) projectIdx = h;
            	else if(col.equals("amount")) amountIdx = h;
            }
            // Default amount index if not found by header (fallback to index 9)
            if(amountIdx == -1) amountIdx = 9;

            for(int i=1; i<recList.size(); i++)
            {
            	String[] rec = recList.get(i);

            	String fullName=null, firstName=null, lastName=null, email=null, phone=null, address1=null, city=null, state=null, zip=null, mode=null, project=null;
            	double donationAmount;

            	fullName = rec[0];
            	firstName = rec[1];
            	lastName = rec[2];

            	String[] names = Utility.parseName(fullName);
            	if(names!=null) {
            		if(firstName == null) {
            			firstName = names[0];
            		}
            		if(lastName == null) {
            			lastName = names[1];
            		}
            	}

            	email = rec[3];

            	// Skip record if all name fields are empty and no real email
            	boolean hasName = (fullName != null && !fullName.trim().isEmpty())
            			|| (firstName != null && !firstName.trim().isEmpty())
            			|| (lastName != null && !lastName.trim().isEmpty());
            	boolean hasRealEmail = email != null && !email.trim().isEmpty() && !email.trim().toLowerCase().startsWith("temp");

            	if(!hasName && !hasRealEmail) {
            		logger.info("skipping record with no name and no real email at line {}", (i+1));
            		continue;
            	}

            	if(email == null || "".equals(email.trim())) {
            		tmpEmailCounter++;
            		email = tmpEmail.replaceAll("counter", String.valueOf(tmpEmailCounter));
            	}

            	email = email.trim();
            	phone = rec[4];
            	address1 = rec[5];
            	city = rec[6];
            	state = rec[7];
            	zip = rec[8];
            	if(modeIdx >= 0 && modeIdx < rec.length) mode = rec[modeIdx];
            	if(projectIdx >= 0 && projectIdx < rec.length) project = rec[projectIdx];

            	donationAmount = Utility.getDouble(rec[amountIdx]);
            	
            	Donor donor = donorMap.get(email);            	
            	Donor temp = new Donor(fullName, firstName, lastName, email, donationAmount, phone, address1, city, state, zip, mode, project);

            	if(donor==null) {
            		donor = temp;
            	}
            	else
            	{
            		donor = Utility.syncObject(donor, temp);
            		donor.setDonationAmount(donor.getDonationAmount()+donationAmount);
            	}
            	
            	donorMap.put(email, donor);            	
            }
            
        }
        catch(Exception e)
        {
        	throw new Exception(e);
        }
	}
	
}