package com.czl.demo.mvc.action;

import com.czl.demo.service.IDemoService;
import com.czl.spring.annotation.Autowired;
import com.czl.spring.annotation.Controller;
import com.czl.spring.annotation.RequestMapping;
import com.czl.spring.annotation.RequestParam;
import com.czl.spring.webmvc.ModelAndView;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/demo")
public class DemoAction {
	
	@Autowired
	private IDemoService demoService;
	
	@RequestMapping("/query.json")
	public ModelAndView query(HttpServletRequest req, HttpServletResponse resp,
							  @RequestParam("name") String name){
		String result = demoService.get(name);
		Map<String,String> map = new HashMap<String, String>();
		map.put("name",result);
		ModelAndView mv =new ModelAndView("first.html",map);
		return mv;
	}
	
	@RequestMapping("/edit.json")
	public void edit(HttpServletRequest req,HttpServletResponse resp,Integer id){

	}
	
}
