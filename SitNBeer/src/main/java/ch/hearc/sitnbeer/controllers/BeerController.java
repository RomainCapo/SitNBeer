/**
 * SitNBeer
 * Romain Capocasale, Vincent Moulin and Jonas Freiburghaus
 * He-Arc, INF3dlm-a
 * Spring Course
 * 2019-2020
 */

package ch.hearc.sitnbeer.controllers;

import java.security.Principal;

import ch.hearc.sitnbeer.models.Bar;
import ch.hearc.sitnbeer.models.Beer;
import ch.hearc.sitnbeer.models.User;
import ch.hearc.sitnbeer.repositories.IBarRepository;
import ch.hearc.sitnbeer.repositories.IBeerRepository;
import ch.hearc.sitnbeer.repositories.IUserRepository;
import ch.hearc.sitnbeer.validators.BeerAddValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.Valid;

import ch.hearc.sitnbeer.services.BeerService;
import com.sipios.springsearch.anotation.SearchSpec;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
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
    private IBarRepository barRepository;

    @Autowired
    private BeerAddValidator beerAddValidator;

    // Constantes
    private static final String BEERS = "beers";
    private static final String CREATE_BEER = "createBeer";
    private static final String UPDATE_BEER = "updateBeer";
    private static final String HOME = "home";

    @GetMapping("/beer/add")
    public String addBeerForm(Model model) {
        model.addAttribute("beer", new Beer());
        return CREATE_BEER;
    }

    @GetMapping("/beers")
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

        return BEERS;
    }

    /**
     * Allows you to make a detailed search for beer
     * 
     * @param specs Search Criterion
     * @param model Model Spring Object
     * @param page  page number
     * @param size  number of item display on a page
     * @return template
     */
    @GetMapping("/beer/query")
    public String searchForBeers(@SearchSpec Specification<Beer> specs, Model model,
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

        return BEERS;
    }

    @PostMapping("/beer/add")
    public String addBeer(@ModelAttribute Beer beer, Model model, BindingResult bindingResult, Principal principal) {
        beerAddValidator.validate(beer, bindingResult);

        if (bindingResult.hasErrors()) {
            return CREATE_BEER;
        }
        User loggedUser = userRepository.findByUsername(principal.getName());
        Bar bar = loggedUser.getOwnedBar();
        beer.setBar(bar);

        beerRepository.save(beer);

        return HOME;
    }

    @GetMapping("/beer/update/{beerId}")
    public String updateBeerForm(@PathVariable long beerId, Model model) {

        Optional<Beer> optionalBeer = beerRepository.findById(beerId);
        if (optionalBeer.isPresent()) {
            model.addAttribute("beer", optionalBeer.get());
            return UPDATE_BEER;
        }
        return HOME;
    }

    /**
     * Allow to update a beer from an id
     * 
     * @param id            Id of a beer
     * @param beer          Beer Object to update
     * @param model         Model Spring Object
     * @param bindingResult
     * @return template
     */
    @PostMapping("/beer/update/{id}")
    public String updateBeer(@PathVariable Long id, @Valid Beer beer, Model model, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            beer.setId(id);
            return UPDATE_BEER;
        }

        beerRepository.save(beer);
        return HOME;
    }

    /**
     * Display all updatble beer for a bar
     * 
     * @param model Model Spring Object
     * @param barId Id of a bar
     * @return template
     */
    @GetMapping("/beer/edit/{barId}")
    public String updateBeerOfBar(Model model, @PathVariable long barId) {
        Optional<Bar> optionalBar = barRepository.findById(barId);
        Bar bar = null;

        if (optionalBar.isPresent()) {
            bar = optionalBar.get();
            Iterable<Beer> beers = beerRepository.findByBar(bar);
            model.addAttribute(BEERS, beers);
            return "beersOfBar";
        }

        return HOME;
    }
}