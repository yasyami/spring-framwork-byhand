package com.czl.demo.service.impl;


import com.czl.demo.service.IDemoService;
import com.czl.spring.annotation.Service;

@Service
public class DemoService implements IDemoService {

	public String get(String name) {
		return "My name is " + name;
	}

}
