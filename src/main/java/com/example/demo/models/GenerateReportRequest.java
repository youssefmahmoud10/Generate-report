package com.example.demo.models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author YoussefMahmoud
 * @created Apr 22, 2023-3:23:23 AM
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateReportRequest {

	int reportId;
	String arReportName;
	String enReportName;
	String queryPath;

}