package com.smart.service;

import java.util.Properties;

import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
	
public boolean sendEmail(String subject,String message,String to) {
		
		boolean f=false;
		//rest of the code
		
		String from="smartcontactmanagerscm@gmail.com";
		String password="bxzj bdjf jcct zmmr";
		
		//variable for gmail
		String host="smtp.gmail.com";
		
		//get the system properties
		Properties properties=System.getProperties();
		System.out.println("PROPERTIES "+properties);
		
		//setting important information to properties object
		
		//host set 
		properties.put("mail.smtp.auth",true);
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.port","587");
		properties.put("mail.smtp.host", host);
		
		
		
		//Step 1: to get the session object
		
		Session session=Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
		
				return new PasswordAuthentication(from, password);
			}
			
		
		});
		session.setDebug(true);
		//step 2: compose the messages
		try {
		Message m = new MimeMessage(session);
		
		//add recipient to message
		m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		
			//from email
			m.setFrom(new InternetAddress(from));
			
			
			//adding subject to message
			m.setSubject(subject);
			
			//adding text to message
			//m.setText(message);
			m.setContent(message,"text/html");
			
			//send
			//Step 3:send the message using transport class
			
			Transport.send(m);
			
			System.out.println("send msg successfully");
			f=true;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return f;
	}	
	

}
