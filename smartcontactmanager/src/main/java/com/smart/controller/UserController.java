package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName=principal.getName();
		System.out.println("USERNAME " +userName);
		
		
		//get the user using username(email)
		User user = userRepository.getUserByUserName(userName);
		
		System.out.println("USER "+user);
		model.addAttribute("user",user);

	}
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		model.addAttribute("title","user dashboard");
		return "normal/user_dashboard";
	}
	
	//open add form handler
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title","add contacts");
		model.addAttribute("contact",new Contact());
		
		
		return "normal/add_contact_form";
	}
	//processing add contact form
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,
			HttpSession session) {
		try {

			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			//processing and uploading file
			
			if(file.isEmpty()) {
				//if the file is empty the try our message
				System.out.println("File is Empty");
				contact.setImage("contact.png");
				
			}else {
				contact.setImage(file.getOriginalFilename());
				
				File savefile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path ,StandardCopyOption.REPLACE_EXISTING );
				
				System.out.println("Image is uploaded");
				
			}
			
			
			
			contact.setUser(user);
			user.getContacts().add(contact);
			
			
			
			this.userRepository.save(user);
			
			System.out.println("DATA "+contact);
			System.out.println("Added to database");
			
			//message success...
			session.setAttribute("message", new Message("Your contact is added!!", "success"));
			
		} catch (Exception e) {
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			
			//message error......
			session.setAttribute("message", new Message("Something went wrong!! Try Again","danger"));
		}
		return "normal/add_contact_form";
	}
	
	
		//show  contacts handler
		//per page=5[n]
		//current page=0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
		m.addAttribute("title","show user Contacts");
		
		//we have send contact list from here
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		//currentPage-page
		//Contact Per page - 5
		Pageable pageable = PageRequest.of(page,5);
		
		Page<Contact> contacts= this.contactRepository.findContactsByUser(user.getId(),pageable);
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model m,Principal principal)
	{
		
		System.out.println("CID "+cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId())
		{
			m.addAttribute("contact",contact);
			m.addAttribute("title",contact.getName());
		}
		
		
		
		return "normal/contact_detail";
	}
	
	//delete contact handler
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,Principal principal,HttpSession session) {
		
		Contact contact = this.contactRepository.findById(cId).get();
		
		
		String userName = principal.getName();
		
		User user = this.userRepository.getUserByUserName(userName);
		
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
			
		System.out.println("DELETED");
			
		session.setAttribute("message", new Message("Your contact is deleted!!", "success"));
		
		
		
		
		return "redirect:/user/show-contacts/0";
	}
	
	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cId,Model m)
	{
		
		m.addAttribute("title", "Update Contact");
		
		Contact contact = this.contactRepository.findById(cId).get();
		m.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
	//update contact handler
	
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file,
			Model m,HttpSession session,Principal principal) {
		
		
		try {
			//old contact detail
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			//image
			if(!file.isEmpty()) {
				
				//delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldContactDetail.getImage());
				file1.delete();
				
				
				//update new photo
				
				File savefile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path ,StandardCopyOption.REPLACE_EXISTING );
				
				contact.setImage(file.getOriginalFilename());
				
			}else {
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Contact is updated","success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		System.out.println("CONTACT NAME "+contact.getName());
		System.out.println("CONTACT NAME "+contact.getcId());
		return "redirect:/user/"+contact.getcId()+"/contact";
		
	}
	
	//Your Profile setting
	
	@GetMapping("/profile")
	public String yourProfile(Model model) 
	{
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	
	//open setting handler
	@GetMapping("/settings")
	public String openSettings()
	{
		return "normal/settings";
	}
	
	//change password
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword,
			Principal principal,HttpSession session)
	{
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		
		if(this.passwordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			//change the password
			currentUser.setPassword(this.passwordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is successfully changed...","success"));
			
		}else {
			//error..
			session.setAttribute("message", new Message("Wrong!!! new password can't be your old password...Please Try Again","danger"));
			return "redirect:/user/settings";
		}
		
		
		System.out.println("OLD PASSWORD: "+oldPassword);
		System.out.println("NEW PASSWORD: "+newPassword);
		
		return "redirect:/user/index";
	}
	
	
}
