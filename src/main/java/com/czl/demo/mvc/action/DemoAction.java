package com.czl.demo.mvc.action;

import com.czl.demo.service.IDemoService;
import com.czl.spring.annotation.Autowired;
import com.czl.spring.annotation.Controller;
import com.czl.spring.annotation.RequestMapping;
import com.czl.spring.annotation.RequestParam;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/demo")
public class DemoAction {
	
	@Autowired
	private IDemoService demoService;
	
	@RequestMapping("/query.json")
	public void query(HttpServletRequest req,HttpServletResponse resp,
		   @RequestParam("name") String name){
		String result = demoService.get(name);
		System.out.println(result);
//		try {
//			resp.getWriter().write(result);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	@RequestMapping("/edit.json")
	public void edit(HttpServletRequest req,HttpServletResponse resp,Integer id){

	}
	
}
