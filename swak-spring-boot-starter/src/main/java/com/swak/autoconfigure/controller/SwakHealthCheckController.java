package com.swak.autoconfigure.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Slf4j
@RestController
public class SwakHealthCheckController {

	@GetMapping("healthCheck")
	public String ping(@RequestParam(defaultValue = "false") Boolean debug, HttpServletRequest request) {
		if (debug) {
			Enumeration<String> e = request.getHeaderNames();
			while (e.hasMoreElements()) {
				String name = e.nextElement();
				log.info("{} = {}", name, request.getHeader(name));
			}
		}
		return "pong";
	}
}
