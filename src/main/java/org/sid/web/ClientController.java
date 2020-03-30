package org.sid.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.sid.dao.ClientRepository;
import org.sid.entities.Client;
import org.sid.entities.Formation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;


@Controller
public class ClientController {
	
	@Autowired
	private ClientRepository clientRepository;
	
	
	@RequestMapping(value="/home",method=RequestMethod.GET)
	public String logout(Model model,HttpSession session) {
		session.invalidate();
		return "home";
	}
	
	
	@RequestMapping(value="/about",method=RequestMethod.GET)
	public String about(Model model) {
		
		return "about-us";
	}
	
	@RequestMapping(value="/contact-us",method=RequestMethod.GET)
	public String contact(Model model,HttpSession session) {
		
		return "contact-us";
	}

	@RequestMapping(value="/Register",method=RequestMethod.GET)
	public String formProduit(Model model) {
		model.addAttribute("client",new Client());
		return "register";
	}
	@RequestMapping(value="/Registration",method=RequestMethod.POST)
	public String save(Model model ,Client client,HttpSession session,HttpServletRequest request) {
		clientRepository.save(client);
		session=request.getSession(true);
		
		session.setAttribute("user", client);
		//model.addAttribute("user",client);
	
		
		return "login";
	}
	@RequestMapping(value="/login",method=RequestMethod.GET)
	public String login(Model model) {
		
		model.addAttribute("client",new Client());
		return "login";
	}
	
	@RequestMapping(value="/Authentification",method=RequestMethod.POST)
	public String authentification(Model model  ,@RequestParam("email") String email, @RequestParam("password")  String password,HttpSession session,HttpServletRequest request) {
		List<Client> client = new ArrayList<Client>();
		client = clientRepository.findByEmail(email);
		if(client.size()==0) return "register";
		else if(!client.get(0).getPassword().equals(password)) return "loginError";
		else {
			session=request.getSession(true);
			session.setAttribute("user", client.get(0));
			
			return "index";
			}
	}
	@RequestMapping(value="/editTrainerProfil",method=RequestMethod.GET)
	public String editTrainerProfil(Model model,HttpServletRequest request,Long id) {
		HttpSession session=request.getSession(true);
		Client client=(Client) session.getAttribute("user");
		
		Client trainer=clientRepository.getOne(id);
		model.addAttribute("trainer", trainer);
		
		if(session.getAttribute("user")==null) return "trainer-profile-Visiteur";
		
		
		return "trainer-profile";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	

}

