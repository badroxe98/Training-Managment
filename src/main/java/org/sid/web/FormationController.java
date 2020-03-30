package org.sid.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.Table;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;


import org.apache.commons.io.IOUtils;
import org.sid.dao.*;
import org.sid.entities.Client;
import org.sid.entities.Formation;
import org.sid.entities.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


@Controller
public class FormationController {
	
	private String TrainingPicture;
	@Autowired
	private FormationRepository formationRepository;
	@Autowired
	private LocalController localController;
	@Value("${dir.images}")
	private String imageDir;
	
	@RequestMapping(value="/TrainingManagement", method = RequestMethod.GET)
	public String home(Model model,HttpServletRequest request) {
		HttpSession session=request.getSession(true);
		if(session.getAttribute("user")==null) return "home";
		else return "index";		
	}
	@RequestMapping(value="/AddArticle", method = RequestMethod.GET)
	public String formulaireFormation(Model model,HttpServletRequest request) {
		HttpSession session=request.getSession(true);
		List<Local> local=localController.findAll();
	
		if(session.getAttribute("user")==null) {
			return "login";
		}
		model.addAttribute("formation",new Formation());
		model.addAttribute("locaux", local);
		return "Ad-listing";		
	}
	
	
	@RequestMapping(value="/save", method =RequestMethod.POST)
	public String save(Model model,HttpServletRequest request, Formation formation,@RequestParam("LocalId") Long localId,@RequestParam(name="picture") MultipartFile file, BindingResult bindingResult) throws IllegalStateException, IOException {
		HttpSession session =request.getSession(true);
		Client client=(Client) session.getAttribute("user");
		formation.setUser(client);
		Local local=localController.findById(localId);
		
		formation.setLocal(local);
		if(bindingResult.hasErrors()) {
			return "dashboard-my-ads";
		}
		
		if(!(file.isEmpty())) {
			formation.setSignificantPhoto(file.getOriginalFilename());
		
		}

		
		formationRepository.save(formation);
		if(!(file.isEmpty())) {
			formation.setSignificantPhoto(file.getOriginalFilename());
			file.transferTo(new File(imageDir+formation.getId()));
		}
		
		return "redirect:EditAds";	
	}
	
	@RequestMapping(value="/listFormation", method =RequestMethod.GET)
	public String listFormation(Model model,HttpServletRequest request) {
		HttpSession session=request.getSession(true);
		List<Formation> formation=formationRepository.findAll();
		model.addAttribute("formation",formation);
		if(session.getAttribute("user")==null) return "CategoryVisiteur";
		
		return "category";
	}
	
	
	
	@RequestMapping(value="/listFormationParCategory", method =RequestMethod.GET)
	public String listFormationParCategory(Model model,HttpServletRequest request,String cat) {
		HttpSession session=request.getSession(true);
		List<Formation> formation=formationRepository.findByArticleCat(cat);
		Long countFormation = formationRepository.countByArticleCat(cat);
		model.addAttribute("formation",formation);
		model.addAttribute("count",countFormation);
		
		if(session.getAttribute("user")==null) return "CategoryVisiteur";
		
		return "category";
	}
	
	@RequestMapping(value="/countByCategory")
	@ResponseBody
	public Long countByCategory(String cat) {
	
		return  formationRepository.countByArticleCat(cat);
	}
	
	
	@RequestMapping(value="/EditAds", method =RequestMethod.GET)
	public String EditMyAds(Model model,HttpServletRequest request) {
		HttpSession session=request.getSession(true);
		Client  client = (Client) session.getAttribute("user");
		if(session.getAttribute("user")==null) {
			return "login";
		}
		List<Formation> formation=formationRepository.findByUserId(client.getId());
		model.addAttribute("myformation",formation);
		List<Local> local=localController.ListeLocals(client.getId());
	
		model.addAttribute("local",local);
		return "dashboard-my-ads";
	}
	
	@RequestMapping(value="getPhoto",produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public byte[] getPhoto(Long id) throws FileNotFoundException, IOException {
		File f=new File(imageDir+id);
		return IOUtils.toByteArray(new FileInputStream(f));
	}
	
	
	@RequestMapping(value="/delete", method =RequestMethod.GET)
	public String delete(Long id) {
		
		formationRepository.deleteById(id);
		return "redirect:EditAds";
	}
	
	@RequestMapping(value="/viewArticle", method =RequestMethod.GET)
	public String viewArticle(Model model,HttpServletRequest request,Long id) {
		HttpSession session=request.getSession(true);
		Formation article= formationRepository.getOne(id);
		Client formateur=article.getUser();
		Long countFormation=formationRepository.countByIdFormation(id);
		model.addAttribute("countAvailablePlaces",article.getNbPlaces()-countFormation);
		model.addAttribute("article",article);
		model.addAttribute("formateur",formateur);
		Long Duree=article.getLastDay().getTime()-article.getFirstDay().getTime();
		Long Duration=(long) (Duree*(1.15741*Math.pow(10,-8)));
		model.addAttribute("Duration",Duration);
		if(session.getAttribute("user")==null) 
			return "singleVisiteur";
		else {
		
			return "single";
		}
	}
	
	@RequestMapping(value="/editArticle", method =RequestMethod.GET)
	public String editArticle(Model model,Long id,HttpServletRequest request) {
		HttpSession session=request.getSession(true);
		Client client=(Client) session.getAttribute("user");
		Formation a= formationRepository.getOne(id);
		model.addAttribute("article",a);
		List<Local> local=localController.findAll();
		model.addAttribute("locaux",local);
		TrainingPicture=a.getSignificantPhoto();
		return "UpdateArticle";
	}
	
	
	@RequestMapping(value="/UpdateArticle", method =RequestMethod.POST)
	
	public String update(Model model,HttpServletRequest request, Formation formation,@RequestParam("localId") Long localId,@RequestParam(name="picture") MultipartFile file, BindingResult bindingResult) throws IllegalStateException, IOException {
		HttpSession session =request.getSession(true);
		formation.setUser((Client) session.getAttribute("user"));
		
		Local local=localController.findById(localId);
		formation.setLocal(local);
		if(bindingResult.hasErrors()) {
			return "redirect:editArticle";
		}
		if(!(file.isEmpty())) {
			
			formation.setSignificantPhoto(file.getOriginalFilename());
			

		}
		if((file.isEmpty())) {
			
			formation.setSignificantPhoto(TrainingPicture);
			
		}
		
		if(!(file.isEmpty())) {
			formation.setSignificantPhoto(file.getOriginalFilename());
			File f=new File(imageDir+formation.getId());
			if(f.exists()) {
					byte[] bytes=file.getBytes();
					Path path=Paths.get(imageDir+formation.getId());
					Files.write(path, bytes);		
			}
			else 
				{
				
				file.transferTo(new File(imageDir+formation.getId()));
				}
			
		}
		
		formationRepository.save(formation);
		
		return "redirect:EditAds";	
	}
	
	
	//********  Pour la reservation des formations ***************
	
	@RequestMapping(value="/SendRequest", method =RequestMethod.GET)
	public String SendRequest(Model model,Long id,HttpServletRequest request) throws SQLException {
		HttpSession session=request.getSession(true);
		Client client=(Client) session.getAttribute("user");
		Formation formation=formationRepository.getOne(id);
		Long countFormation=formationRepository.countByIdFormation(id);
		List<Long> verifyIfExist=formationRepository.verifyIfAlreadyExist(client.getId());
		System.out.println(verifyIfExist.size());
		if(verifyIfExist.size()==0) {
			if(formation.equals(null)) {
				formationRepository.insertIntoReservation(client.getId(),id);
				return "redirect:listFormation";
			}
			else {
				if(formation.getNbPlaces()>countFormation) {
					formationRepository.insertIntoReservation(client.getId(),id);
					return "redirect:listFormation";
				}
				else return "redirect:PlacesPleines";
			}
		}
		else
			return "redirect:DejaPostuler";
		
	}
	
	@RequestMapping(value="/DejaPostuler", method =RequestMethod.GET)
	public String DejaPostuler(Model model,HttpServletRequest request) {
		HttpSession session=request.getSession(true);
		List<Formation> formation=formationRepository.findAll();
		model.addAttribute("formation",formation);
		if(session.getAttribute("user")==null) return "CategoryVisiteur";
		
		return "DejaPostuler";
	}
	
	@RequestMapping(value="/PlacesPleines", method =RequestMethod.GET)
	public String PlacesPleines(Model model,HttpServletRequest request) {
		HttpSession session=request.getSession(true);
		List<Formation> formation=formationRepository.findAll();
		model.addAttribute("formation",formation);
		if(session.getAttribute("user")==null) return "CategoryVisiteur";
		
		return "PlacesPleines";
	}
	@RequestMapping(value="/editUserProfile",method=RequestMethod.GET)
	public String editUserProfile(Model model,HttpServletRequest request) {
		HttpSession session=request.getSession(true);
		Client client=(Client) session.getAttribute("user");
		
		if(session.getAttribute("user")==null) {
			return "login";
		}
		List<Formation> formation=formationRepository.findReservedTraining(client.getId());
		model.addAttribute("myformation",formation);
		return "user-profile";
	}
	
	@RequestMapping(value="/deleteMyReservation", method =RequestMethod.GET)
	public String deleteMyReservation(Long tId,Long uId) {
		
		//System.out.println(tId);
		formationRepository.deleteMyReservation(tId,uId);
		return "redirect:editUserProfile";
	}
	
	//********  Pour la reservation des formations ***************
	

	
	
	
}
