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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVReader;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

public class DonationsWithOutEmail {

	public static final Logger logger = LogManager.getLogger(DonationsWithOutEmail.class);

	public static void main(String[] args) {

		if (args.length == 0) {
			logger.error("please provide a valid source folder!");
			System.exit(0);
		}
		String sourceFolder = args[0];
		try {
			new DonationsWithOutEmail().readDonationData(sourceFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readDonationData(String sourceFolder) throws Exception {
		Map<String, Donor> donorMap = new HashMap<String, Donor>();
		logger.info("scanning {}", sourceFolder);
		File directory = new File(sourceFolder);

		if (!directory.exists()) {
			logger.error("source folder doesn't exist!");
			return;
		}

		File files[] = directory.listFiles();
		if (files.length == 0) {
			logger.error("files don't exist!");
			return;
		}

		for (File file : files) {
			logger.info("reading file {}", file);
			readFile(donorMap, file);
		}

		// Write to a final consolidated report
		logger.info("total number of unique donors: " + donorMap.size());
		try {

			Writer writer = Files.newBufferedWriter(Paths.get(sourceFolder + "/" + AppConstants.FINAL_REPORT));
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void readFile(Map<String, Donor> donorMap, File fileName) throws Exception {
		try (CSVReader reader = new CSVReader(new FileReader(fileName))) {

			List<String[]> recList = reader.readAll();
			int i = 0;

			for (String[] rec : recList) {
				i++;
				if (i == 1) {
					continue;
				}

				String fullName = null, firstName = null, lastName = null, email = null, phone = null, address1 = null, mode = null, project = null,
						city = null, state = null, zip = null;
				java.math.BigDecimal donationAmount;

				fullName = rec[1];
				firstName = rec[2];
				lastName = rec[3];
				logger.info("Name: {}",fullName);

				String[] names = Utility.parseName(fullName);
				if (names != null) {
					if (firstName == null) {
						firstName = names[0];
					}
					if (lastName == null) {
						lastName = names[1];
					}
				}

				email = rec[4];
				if (!StringUtils.isEmpty(email)) {
					email = email.trim();
				}
				phone = rec[5];
				address1 = rec[6];
				city = rec[7];
				state = rec[8];
				zip = rec[9];
				mode = rec[10];
				project = rec[11];

				donationAmount = Utility.getBigDecimal(rec[10]);

				Donor donor = donorMap.get(fullName);
				Donor temp = new Donor(fullName, firstName, lastName, email, donationAmount, phone, address1, city,
						state, zip, mode, project);

				if (donor == null) {
					donor = temp;
				} else {
					donor = Utility.syncObject(donor, temp);
					donor.setDonationAmount(donor.getDonationAmount().add(donationAmount));
				}

				donorMap.put(fullName, donor);
			}

		} catch (Exception e) {
			throw new Exception(e);
		}
	}

}
