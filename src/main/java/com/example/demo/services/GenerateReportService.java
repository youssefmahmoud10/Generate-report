package com.example.demo.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.demo.models.GenerateReportRequest;

/**
 * @author YoussefMahmoud
 * @created Apr 22, 2023-3:25:40 AM
 */

@Service
public class GenerateReportService {

	private static final Logger logger = LoggerFactory.getLogger(GenerateReportService.class);

	public String readFileAsString(String fileName) {
		try {
			return new String(Files.readAllBytes(Paths.get(fileName)));
		} catch (Exception e) {
			logger.info(e.toString());
			return null;
		}
	}

	public void appendToFileFileWriter(File file, String content) {
		try {
			try (FileWriter fw = new FileWriter(file, true); BufferedWriter bw = new BufferedWriter(fw)) {
				bw.write(content);
				bw.newLine();
			}
		} catch (Exception e) {
			logger.info(e.toString());
		}
	}

	public List<String> extractParameters(String query) {
		List<String> parametersList = new ArrayList<>();
		Pattern pattern = Pattern.compile(":[a-zA-Z0-9]+_?[a-zA-Z0-9]+([^\\S\\r\\n]+)?");
		Matcher matcher = pattern.matcher(query);
		while (matcher.find()) {
			if (!parametersList.contains(matcher.group().substring(1).trim())
					&& !matcher.group().substring(1).trim().equals("LANG"))
				parametersList.add(matcher.group().substring(1).trim());
		}
		return parametersList;
	}

	public String editQuery(String query) {
		List<String> parametersList = extractParameters(query);
		for (int i = 0; i < parametersList.size(); i++) {
			if (parametersList.get(i).equalsIgnoreCase("FROM_DATE") || parametersList.get(i).equalsIgnoreCase("TO_DATE")
					|| parametersList.get(i).equalsIgnoreCase("DATE") || parametersList.get(i).equalsIgnoreCase("PDATE")
					|| parametersList.get(i).equalsIgnoreCase("BUSDATE")
					|| parametersList.get(i).equalsIgnoreCase("P_BUSDATE")
					|| parametersList.get(i).equalsIgnoreCase("P_BUSDATE2")
					|| parametersList.get(i).equalsIgnoreCase("P_DATE")
					|| parametersList.get(i).equalsIgnoreCase("BUS_DATE")
					|| parametersList.get(i).equalsIgnoreCase("PBUSDATE")
					|| parametersList.get(i).equalsIgnoreCase("FROM_DATE2")
					|| parametersList.get(i).equalsIgnoreCase("TO_DATE2"))
				query = query.replace(":" + parametersList.get(i),
						"TO_DATE('#" + parametersList.get(i) + "#', 'YYYY-MM-DD')");
			else
				query = query.replace(":" + parametersList.get(i), "#" + parametersList.get(i) + "#");
		}
		query = query.replace(":LANG", "1");
		query = query.replace("--", "  ");
		query = query.replace("'", "''");
		query = query.replace("/\\*", "  ");
		query = query.replace("\\*/", "  ");
		return query;
	}

	public List<String> extractHeaders(String query) {
		List<String> headersList = new ArrayList<>();
		try {
			Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@10.3.1.101:1531:eplus", "Bedayti2",
					"Bedayti2");
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
				headersList.add(resultSet.getMetaData().getColumnName(i + 1));
			}
			connection.close();
			statement.close();
		} catch (Exception e) {
			logger.info(e.toString());
		}
		return headersList;
	}

	public String getHeadersArabicNames(String key) {
		try {
			ResourceBundle headersArabicNames = new PropertyResourceBundle(
					new FileInputStream("headersArabicNames.properties"));
			return headersArabicNames.getString(key);
		} catch (Exception e) {
			return null;
		}
	}

	public void insertIntoRepHeaders(File file, int reportId, String query) {
		List<String> headersList = extractHeaders(query);
		for (int i = 0; i < headersList.size(); i++)
			appendToFileFileWriter(file,
					"Insert into rep_headers (REPORT_ID, COLUMN_ID, AR_NAME, EN_NAME) values ("
							+ Integer.toString(reportId) + ", " + Integer.toString(i + 1) + ", '"
							+ getHeadersArabicNames(headersList.get(i)) + "', '" + headersList.get(i) + "');");
		appendToFileFileWriter(file, "");
	}

	public String getParameterTypes(String key) {
		try {
			ResourceBundle parameterTypes = new PropertyResourceBundle(
					new FileInputStream("parameterTypes.properties"));
			return parameterTypes.getString(key);
		} catch (Exception e) {
			return null;
		}
	}

	public String getParameterQueries(String key) {
		try {
			ResourceBundle parameterQueries = new PropertyResourceBundle(
					new FileInputStream("parameterQueries.properties"));
			return parameterQueries.getString(key);
		} catch (Exception e) {
			return null;
		}
	}

	public String getParameterArabicNames(String key) {
		try {
			ResourceBundle parameterArabicNames = new PropertyResourceBundle(
					new FileInputStream("parameterArabicNames.properties"));
			return parameterArabicNames.getString(key);
		} catch (Exception e) {
			return null;
		}
	}

	public void insertIntoRepUiParameters(File file, int reportId, String query) {
		List<String> parametersList = extractParameters(query);
		for (int i = 0; i < parametersList.size(); i++)
			appendToFileFileWriter(file,
					"Insert into rep_ui_params (PARAM_ID, REPORT_ID, PARAM_NAME, DATA_TYPE, PARAM_QUERY, "
							+ "PARAM_AR, PARAM_EN, DEPEND_PARAMS) values (" + Integer.toString(i + 1) + ", "
							+ Integer.toString(reportId) + ", '" + parametersList.get(i) + "', "
							+ getParameterTypes(parametersList.get(i).toUpperCase()) + ", '"
							+ getParameterQueries(parametersList.get(i).toUpperCase()) + "', '"
							+ getParameterArabicNames(parametersList.get(i).toUpperCase()) + "', '"
							+ parametersList.get(i) + "', null);");
		appendToFileFileWriter(file, "");
	}

	public void insertIntoRepParameters(File file, int reportId, String query) {
		List<String> parametersList = extractParameters(query);
		for (int i = 0; i < parametersList.size(); i++)
			appendToFileFileWriter(file,
					"Insert into rep_parameters (REPORT_ID, PARAM_ID, PARAM_NAME, PARAM_UI, PARAM_TYPE) values ("
							+ Integer.toString(reportId) + ", " + Integer.toString(i + 1) + ", '"
							+ parametersList.get(i) + "', '" + parametersList.get(i) + "', 1);");
		appendToFileFileWriter(file, "");
	}

	public void generateReport(int reportId, String reportArName, String reportEnName, String query) {
		File file = new File(
				"C:/Users/YoussefMahmoud/Desktop/Automated reports/" + Integer.toString(reportId) + " Queries.txt");

		appendToFileFileWriter(file,
				"Insert into Rep_reports (REPORT_ID, REPORT_NAME, REPORT_NAME_AR, QUERY,VISIBLE, TYPE, HEADER, REPORT_NAME_EN) values ("
						+ Integer.toString(reportId) + ", '" + reportEnName + "', '" + reportArName + "', '"
						+ editQuery(query) + "', 1, 3,'" + reportArName + "', '" + reportEnName + "');\n");
		insertIntoRepHeaders(file, reportId, query);
		insertIntoRepUiParameters(file, reportId, query);
		insertIntoRepParameters(file, reportId, query);
		appendToFileFileWriter(file, "Insert into bp_reports_roles (REPORT_ID, ROLE_ID) values ("
				+ Integer.toString(reportId) + ", 200);\n");
		appendToFileFileWriter(file, "Insert into report_redirect (REPORT_ID, URL, FLAG) values ("
				+ Integer.toString(reportId) + ", 'http://localhost:9191/report', 1);\n");
		appendToFileFileWriter(file, "Insert into bp_reports_names (REPORT_ID, LANG_CODE, DESCR) values ("
				+ Integer.toString(reportId) + ", 'AR', '" + reportArName + "');");
		appendToFileFileWriter(file, "Insert into bp_reports_names (REPORT_ID, LANG_CODE, DESCR) values ("
				+ Integer.toString(reportId) + ", 'EN', '" + reportEnName + "');\n");
	}

	public String gnReport(GenerateReportRequest generateReportRequest) {
		generateReport(generateReportRequest.getReportId(), generateReportRequest.getArReportName(),
				generateReportRequest.getEnReportName(), readFileAsString(generateReportRequest.getQueryPath()));
		return "File created successfully.";
	}

}