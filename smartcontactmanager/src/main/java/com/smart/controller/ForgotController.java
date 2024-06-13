package com.smart.controller;

import java.util.Random;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	Random random=new Random(0001);
	
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	

	public ForgotController(EmailService emailService) {
		super();
		this.emailService = emailService;
	}
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session) {
		System.out.println("Email id: "+email);
		
		//generating otp of 4 digit
		
		
		//int otp = random.nextInt(9999);
		
		Supplier<String> s=()->{
			String otpp="";
			for(int i=0;i<6;i++) {
				otpp=otpp+(int)(Math.random()*10);
			}
			return otpp;
		};
		String otp=s.get();
		
		System.out.println("OTP is : "+otp);
		
		
		
		//write code for send otp to email..
		
		String subject="OTP From SCM";
		String message=""
				+ "<div style='border:1px solid #e2e2e2; padding:20px'>"
				+ "<h1>"
				+ "OTP is "
				+ "<b>" +otp
				+ "</b>"
				+ "</h1>"
				+ "</div>";
		
		
		String to=email;
		boolean flag = emailService.sendEmail(subject, message, to);
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email",email);
			return "verify_otp";
			
		}else {
			session.setAttribute("message", "Check your email ID!!");
			return "forgot_email_form";
		}
	}
	
	//verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") String otp,HttpSession session) {
		//int  myOtp=(int) session.getAttribute("myotp");
		String  myOtp= (String) session.getAttribute("myotp");
		String email= (String)session.getAttribute("email");
		
		if(myOtp.equals(otp)) {
			//password change form
			User user = userRepository.getUserByUserName(email);
			if(user==null)
			{
				//send error message
				
				session.setAttribute("message", "You have entered wrong email");
				return "forgot_email_form";
				
			}else {
				//send change password form
				return "password_change_form";
			
			}
			
		}else {
			session.setAttribute("message", "You have entered wrong Otp");
			return "verify_otp";
		}
	}
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newspassword,HttpSession session) {
		String email= (String)session.getAttribute("email");
		User user = userRepository.getUserByUserName(email);
		user.setPassword(this.bcrypt.encode(newspassword));
		this.userRepository.save(user);
		return "redirect:/signin?change=password changed successfully";
	}
}
