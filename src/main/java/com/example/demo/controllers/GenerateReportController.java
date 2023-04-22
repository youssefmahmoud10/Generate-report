package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.models.GenerateReportRequest;
import com.example.demo.services.GenerateReportService;

/**
 * @author YoussefMahmoud
 * @created Apr 22, 2023-3:21:40 AM
 */

@RestController
@RequestMapping("generateReportController") //http://localhost:8080/generateReportController/generateReport
public class GenerateReportController {

	@Autowired
	private GenerateReportService generateReportService;

	@CrossOrigin
	@PostMapping(path = "/generateReport")
	public String generateReport(@RequestBody GenerateReportRequest generateReportRequest) {
		return generateReportService.gnReport(generateReportRequest);
	}

}