package com.dua.finance;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

public class UpdateZelleData {

	public static final Logger logger = LogManager.getLogger(UpdateZelleData.class);

	public static void main(String[] args) {
		if (args.length == 0) {
			logger.error("please provide a valid source folder!");
			System.exit(0);
		}
		String sourceFolder = args[0];
		try {
			new UpdateZelleData().readDonorData(sourceFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readDonorData(String sourceFolder) throws Exception {
		List<Donor> donorList = new ArrayList<Donor>();
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
			if (file.getName().contains("Overall Donor List.csv")) {
				logger.info("skipping file/folder {}", file);
				logger.info("reading file {}", file);
				readFile(donorList, file);
			}
		}

		for (File file : files) {
			if (file.getName().contains("boa-zelle.csv")) {
				readUpdateZelleFile(donorList, file);
			}
		}

		logger.info("donorList size: {}", donorList.size());

	}

	public static void readUpdateZelleFile(List<Donor> donorList, File fileName) {

		String outputCsvPath = "c:/zelle/updated-zelle.csv";
		String nameRegex = "(?<=Zelle Transfer Conf# [^;]+; )(.+)|(?<=Zelle payment from )(.+?)(?=( for)| Conf#|$)";

		Pattern namePattern = Pattern.compile(nameRegex);

		try (CSVReader reader = new CSVReader(new FileReader(fileName));
				CSVWriter writer = new CSVWriter(new FileWriter(outputCsvPath))) {
			String[] nextLine;
			List<String[]> allElements = new ArrayList<>();

			// Read the header and write it unchanged
			String[] header = reader.readNext();
			allElements.add(header);

			// Indices based on the columns "Date", "Description", "Amount", "Name", "Email"
			int descriptionIndex = 1; // Assuming "Description" is the second column
			int nameIndex = 3; // Assuming "Name" is the fourth column
			int emailIndex = 4; // Assuming "Email" is the fifth column
			String matchedEmail = "";
			while ((nextLine = reader.readNext()) != null) {
				// Apply the regex to each description to find the name
				Matcher matcher = namePattern.matcher(nextLine[descriptionIndex]);
				if (matcher.find()) {

					String name = matcher.group().trim();
					nextLine[nameIndex] = name; // Update the Name column

					if (name.equals("TECHNIZANT LLC")) {
						logger.info("stop");
					}

					// for (Map.Entry<String, Donor> entry : donorList.entrySet()) {
					for (Donor donor : donorList) {

						String normalizedInputName = normalizeName(name);
						String normalizedDonorName = normalizeName(donor.getFullName());

						if (donor.getFullName().equals("TECHNIZANT LLC")) {
							logger.info("stop");
						}

						if (isPartialMatch(normalizedInputName, normalizedDonorName)) {
							matchedEmail = donor.getEmail();
							// Assuming you're breaking the loop after finding the first match
							break;
						}
					}

					nextLine[emailIndex] = matchedEmail; // Update the Email column accordingly
					matchedEmail = "";
				}
				allElements.add(nextLine); // Add the updated line to the list
			}

			// Write all elements to the new CSV file
			writer.writeAll(allElements);
		} catch (IOException | CsvValidationException e) {
			e.printStackTrace();
		}
	}

	// Helper method to check if any part of the name matches
	private static boolean isPartialMatch(String inputName, String donorName) {
		String[] inputNameParts = inputName.split("\\s+");
		String[] donorNameParts = donorName.split("\\s+");

		for (String inputPart : inputNameParts) {
			for (String donorPart : donorNameParts) {
				if (normalizeName(donorPart).contains(normalizeName(inputPart))
						|| normalizeName(inputPart).contains(normalizeName(donorPart))) {
					return true;
				}
			}
		}
		return false;
	}

	private static String normalizeName(String name) {
		return name.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
	}

	public static void readFile(List<Donor> donorList, File fileName) throws Exception {
		try (CSVReader reader = new CSVReader(new FileReader(fileName))) {

			List<String[]> recList = reader.readAll();
			int i = 0;

			for (String[] rec : recList) {
				i++;
				if (i == 1) {
					continue;
				}

				String fullName = null, firstName = null, lastName = null, email = null;

				fullName = rec[0];
				firstName = rec[1];
				lastName = rec[2];

				String[] names = Utility.parseName(fullName);
				if (names != null) {
					if (firstName == null) {
						firstName = names[0];
					}
					if (lastName == null) {
						lastName = names[1];
					}
				}

				email = rec[3];

				email = email.trim();

				Donor donor = new Donor(fullName, firstName, lastName, email, BigDecimal.ZERO, null, null, null, null, null, null, null);

				donorList.add(donor);
			}

		} catch (Exception e) {
			throw new Exception(e);
		}
	}

}
