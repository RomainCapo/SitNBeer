package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class HomeController{

    @GetMapping({"/", "/home"})
	public String index() {
		return "home";
	}

}