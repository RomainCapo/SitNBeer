package com.example.demo.controllers;

import java.security.Principal;

import com.example.demo.models.Beer;
import com.example.demo.repositories.IBeerRepository;
import com.example.demo.repositories.IUserRepository;
import com.example.demo.validators.BeerAddValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.example.demo.services.BeerService;
import com.sipios.springsearch.anotation.SearchSpec;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class BeerController {

    @Autowired
    private BeerService beerService;

    @Autowired
	private IBeerRepository beerRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
	private BeerAddValidator beerAddValidator;

    @GetMapping("/beer/add")
	public String addBeerForm(Model model) {
        model.addAttribute("beer", new Beer());
        return "createBeer";
    }

    @RequestMapping("/beer")
    public String index(Model model, @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(8);

        Page<Beer> beerPage = beerService.findPaginated(PageRequest.of(currentPage - 1, pageSize));
        model.addAttribute("beerPage", beerPage);

        int totalPages = beerPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "beer";
    }

    @GetMapping("/beer/query")
    public String searchForCars(@SearchSpec Specification<Beer> specs, Model model,
            @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(8);

        Page<Beer> beerPage = beerService.findPaginatedWithSpecs(PageRequest.of(currentPage - 1, pageSize),
                Specification.where(specs));
        model.addAttribute("beerPage", beerPage);

        int totalPages = beerPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "beer";
    }

    @PostMapping("/beer/add")
    public String addBeer(@ModelAttribute Beer beer, Model model, BindingResult bindingResult, Principal principal){

        beerAddValidator.validate(beer, bindingResult);

		if(bindingResult.hasErrors()){
			return "createBeer";
        }
        //TODO : make method to find bar from user
        /*
        beer.setBar(bar);
        beerRepository.save(beer);*/

        return "redirect:/";
    }
}