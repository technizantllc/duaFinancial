package com.dua.finance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmailUtility {

	public static final Logger logger = LogManager.getLogger(EmailUtility.class);
	
public static void main(String args[])
{
	
	try {
		
		if(args.length == 0)
		{
			logger.error("please provide a valid source folder!");
			System.exit(0);
		}
		
		String sourceFolder = args[0];
		Properties appProps = Utility.getConfigProperties(sourceFolder + "/config/config.properties");
		List<Donor> donors = Utility.beanBuilder(Paths.get(sourceFolder + "/" + AppConstants.FINAL_REPORT));		
		sendMail(donors, sourceFolder, appProps);
		
	} catch (Exception e) {
		e.printStackTrace();
	}
		
}

public static void sendMail(List<Donor> donors, String sourceFolder, Properties appProps)
{
	Properties prop = new Properties();
    prop.put("mail.smtp.host", appProps.getProperty("mail.smtp.host"));
    prop.put("mail.smtp.port", appProps.getProperty("mail.smtp.port"));
    prop.put("mail.smtp.auth", "true");
    prop.put("mail.smtp.socketFactory.port", appProps.getProperty("mail.smtp.port"));
    prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

	Session session = Session.getInstance(prop,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(appProps.getProperty("email"), appProps.getProperty("password"));
                }
            });

    Transport transport = null;
    try {
    	// Reuse single transport connection to avoid rate limiting
    	transport = session.getTransport("smtp");
    	transport.connect();

    	for(Donor donor : donors)
    	{
    		String email = donor.getEmail();

    		// Skip if email is null, empty, or temp
    		if(email == null || email.trim().isEmpty() || email.contains("@temp.com"))
    		{
    			logger.warn("Skipping donor {} - invalid or temp email: {}", donor.getFullName(), email);
    			continue;
    		}
    		
	    	BodyPart messageBodyPart = new MimeBodyPart(); 
	    	messageBodyPart.setText("\n\n Assalamalekum, please find the attached donation receipt. "
	    			+ "\n\n If you have any questions please contact us at "+appProps.getProperty("email")+" \n\n Jazakallah Khair, \n\n "+ appProps.getProperty("org") +"\n "+ appProps.getProperty("website"));
	    	
	    	Message message = new MimeMessage(session); 
	    	message.setFrom(new InternetAddress(appProps.getProperty("email")));
	    	message.setSubject("Donation Receipt - "+appProps.getProperty("org")+" - Year "+appProps.getProperty("year")); 
	    	
	    	message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(donor.getEmail())); 
	    	
	    	MimeBodyPart attachmentPart = new MimeBodyPart();
	    	
	    	File receipt = new File(sourceFolder+"/Receipts/Receipt-"+donor.getFullName()+"-"+donor.getDonorId()+".pdf");
	    	File sentReceipt = new File(sourceFolder+"/Receipts/sent/Receipt-"+donor.getFullName()+"-"+donor.getDonorId()+".pdf");
	    	File receiptToDelete = new File(sourceFolder+"/Receipts/Receipt-"+donor.getFullName()+"-"+donor.getDonorId()+".rtf");
	    	attachmentPart.attachFile(receipt);
	    	
	    	Multipart multipart = new MimeMultipart();
	    	multipart.addBodyPart(messageBodyPart);
	    	multipart.addBodyPart(attachmentPart);
	
	    	message.setContent(multipart);
	        
	    	try {
	    		if(receipt.exists()) {
	    			// Use existing transport connection instead of Transport.send()
	    			transport.sendMessage(message, message.getAllRecipients());
	    			//move the receipt to sent folder
	    	    	Files.move(receipt.toPath(), sentReceipt.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    	    	logger.info("{} sent to {}", receipt.getName(), donor.getEmail());

	    	    	if(receiptToDelete.exists()) {
	    	    		receiptToDelete.deleteOnExit();
	    	    	}
	    	    	else {
	    	    		logger.info("{} doesn't exist",receiptToDelete.getName());
	    	    	}
	    		}
	    		else
	    		{
	    			logger.info("{} doesn't exist",receipt.getName());
	    		}
	    	}
	    	catch(Exception e)
	    	{
	    		logger.error("Failed to send email to {}: {}", donor.getEmail(), e.getMessage());
	    	}
	    	
    	}
        logger.info("Email sending complete");

    } catch (MessagingException e) {
        logger.error("Messaging error: {}", e.getMessage());
    } catch (IOException e) {
		logger.error("IO error: {}", e.getMessage());
	} finally {
    	if(transport != null) {
    		try {
    			transport.close();
    		} catch (MessagingException e) {
    			logger.error("Error closing transport: {}", e.getMessage());
    		}
    	}
    }
}

public static Set<String> listFilesUsingJavaIO(String dir) {
    return Stream.of(new File(dir).listFiles())
      .filter(file -> !file.isDirectory())
      .map(File::getName)
      .collect(Collectors.toSet());
}
	
}
