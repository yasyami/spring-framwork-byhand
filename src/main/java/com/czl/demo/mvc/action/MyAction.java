package com.czl.demo.mvc.action;

import com.czl.demo.service.IDemoService;
import com.czl.spring.annotation.Autowired;
import com.czl.spring.annotation.Controller;
import com.czl.spring.annotation.RequestMapping;

@Controller
public class MyAction {

		@Autowired
		IDemoService demoService;
	
		@RequestMapping("/index.html")
		public void query(){

		}
	
}
