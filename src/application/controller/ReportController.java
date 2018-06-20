package application.controller;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import application.SessionManager;
import application.model.Publisher;
import application.model.ReportObject;
import application.model.gateway.PublisherTableGateway;
import application.model.gateway.ReportObjectTableGateway;
import application.util.exception.AppException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class ReportController extends ViewController {
	
	private static Logger logger = LogManager.getLogger(ReportController.class);

    @FXML private ComboBox<Publisher> publisherReport;
    @FXML private TextField reportFilePath;
    @FXML private Button generateReport;
    @FXML private Button browse;
    
    private ObservableList<Publisher> publishers;
    private PublisherTableGateway pubGateway;
    private ReportObjectTableGateway reportObjectGateway;
    private Publisher publisher;
    private List<ReportObject> reportObjects;
    
    
    public ReportController() {
    		this.pubGateway = new PublisherTableGateway();
    		this.reportObjectGateway = new ReportObjectTableGateway();
    		try {
				publishers = FXCollections.observableArrayList(pubGateway.getPublishers());
			} catch (AppException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }

    @FXML
    void onBrowseClicked(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("JavaFX Projects");
		//File defaultDirectory = new File("c:/dev/javafx");
		//chooser.setInitialDirectory(defaultDirectory);
		File selectedDirectory = chooser.showDialog(null);
		reportFilePath.setText(selectedDirectory.getAbsolutePath() + "/report.xls");
    }
    
    @FXML
    void onGenerateClicked(ActionEvent event) {
    		Publisher pub = publisherReport.valueProperty().get();
    		String fp = reportFilePath.getText();
    		logger.info(pub);
    		logger.info(fp);
		try {
			reportObjects = reportObjectGateway.getSets(pub.getId());
		} catch (AppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    		logger.info("books for publisher: " + pub.getPublisherName() + " - " + reportObjects);
    		generateReport(pub, fp, reportObjects);
    }
    
    void generateReport(Publisher pub, String fp, List<ReportObject> reportObjects) {
    		HSSFWorkbook workbook = new HSSFWorkbook();
    		HSSFSheet sheet = workbook.createSheet("Publisher");
    		sheet.setColumnWidth(0, 8000);
    		sheet.setColumnWidth(1, 5000);
    		sheet.setColumnWidth(2, 3500);
    		sheet.setColumnWidth(3, 2500);
    		DecimalFormat df = new DecimalFormat("#.##");
    	    df.setRoundingMode(RoundingMode.CEILING);
    	    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    		LocalDateTime now = LocalDateTime.now();
    		
    		// title font
    		HSSFFont titleFont = workbook.createFont();
    		titleFont.setFontName("Courier New");
    		titleFont.setFontHeightInPoints((short) 24);
    		titleFont.setBold(true);
    		HSSFCellStyle titleStyle = workbook.createCellStyle();
    		titleStyle.setFont(titleFont);
    		
    		// bold header font size 12
    		HSSFFont headerFont = workbook.createFont();
    		headerFont.setFontName("Courier New");
    		headerFont.setFontHeightInPoints((short) 12);
    		headerFont.setBold(true);
    		HSSFCellStyle headerStyle = workbook.createCellStyle();
    		headerStyle.setFont(headerFont);
    		
    		// bold column style size 12
    		HSSFFont boldFont = workbook.createFont();
    		boldFont.setBold(true);
    		HSSFCellStyle boldStyle = workbook.createCellStyle();
    		boldStyle.setFont(boldFont);
    		
    		// non bold size 12 font
    		HSSFFont font = workbook.createFont();
    		font.setBold(false);
    		HSSFCellStyle normalStyle = workbook.createCellStyle();
    		normalStyle.setFont(font);
    		
    		// title
    		Row row = sheet.createRow(0);
    		Cell cell = row.createCell(0);
    		cell.setCellValue("Royalty Report");
    		cell.setCellStyle(titleStyle);
    		
    		// header
    		row = sheet.createRow(1);
    		cell = row.createCell(0);
    		cell.setCellValue("Publisher: " + pub.getPublisherName());
    		cell.setCellStyle(headerStyle);
    		
    		row = sheet.createRow(2);
    		cell = row.createCell(0);
    		cell.setCellValue("Report generated on " + dtf.format(now));
    		cell.setCellStyle(headerStyle);
    		
    		
    		// Column Titles
    		row = sheet.createRow(4);
    		// cell 0 - book title
    		cell = row.createCell(0);
    		cell.setCellValue("Book Title");
    		cell.setCellStyle(boldStyle);
    		// cell 1 - isbn
    		cell = row.createCell(1);
    		cell.setCellValue("ISBN");
    		cell.setCellStyle(boldStyle);
    		// cell 2 - author
    		cell = row.createCell(2);
    		cell.setCellValue("Author");
    		cell.setCellStyle(boldStyle);
    		// cell 3 - royalty
    		cell = row.createCell(3);
    		cell.setCellValue("Royalty");
    		cell.setCellStyle(boldStyle);
    		
    		int rownum = 5;
    		double royalty = 0;
    		
    		String currentSetTitle = null;
    		for (int i = 0; i < reportObjects.size(); ++i) {
    			String currentTitle = reportObjects.get(i).getTitle();
    			row = sheet.createRow(rownum);
    			if(!currentTitle.equals(currentSetTitle)) {
    				currentSetTitle = currentTitle;
	    			cell = row.createCell(0);
	    			cell.setCellValue(reportObjects.get(i).getTitle());
	    			cell.setCellStyle(normalStyle);
	    			cell = row.createCell(1);
	    			cell.setCellValue(reportObjects.get(i).getISBN());
	    			cell.setCellStyle(normalStyle);
    			}
    			cell = row.createCell(2);
    			cell.setCellValue(reportObjects.get(i).getAuthor());
    			cell.setCellStyle(normalStyle);
    			cell = row.createCell(3);
    			cell.setCellValue(df.format(reportObjects.get(i).getRoyalty()*100) + "%");
    			cell.setCellStyle(normalStyle);
    			royalty += reportObjects.get(i).getRoyalty();
    			rownum++;
    			if(i+1 < reportObjects.size() && !reportObjects.get(i+1).getTitle().equals(currentTitle)) {
    				row = sheet.createRow(rownum);
    				cell = row.createCell(2);
        			cell.setCellValue("Total Royalty");
        			cell.setCellStyle(boldStyle);
        			cell = row.createCell(3);
        			cell.setCellValue(df.format(royalty*100) + "%");
        			cell.setCellStyle(boldStyle);
        			royalty = 0;
        			rownum += 2;
    			}
    		}
    		row = sheet.createRow(rownum);
		cell = row.createCell(2);
		cell.setCellValue("Total Royalty");
		cell.setCellStyle(boldStyle);
		cell = row.createCell(3);
		cell.setCellValue(df.format(royalty*100) + "%");
		cell.setCellStyle(boldStyle);
    		
    		
    		
		try (FileOutputStream f = new FileOutputStream(new File(fp))) {
    			workbook.write(f);
    		} catch (IOException e) {
    			e.printStackTrace();
    		} finally {
    			try {
    				workbook.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    		
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		logger.info("calling initializable");
		
		
		publisherReport.setItems(publishers);
		//publisherReport.valueProperty().bindBidirectional(publisher.getPublisherNameProperty());
	}

}