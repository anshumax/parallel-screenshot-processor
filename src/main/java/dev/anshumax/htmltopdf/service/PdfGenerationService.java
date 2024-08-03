package dev.anshumax.htmltopdf.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommonsLog
@Service
public class PdfGenerationService {

    ChromeDriver chromeDriver;


    @Autowired
    String chromeDriverLoc;

    @PostConstruct
    public void init(){
        System.setProperty("webdriver.chrome.driver", chromeDriverLoc);
        ChromeOptions options = new ChromeOptions();
        String[] args = {
                "--headless",
                "--disable-gpu",
                "--disable-dev-shm-usage",
                "--no-sandbox"
        };
        options.addArguments(args);

        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.images", 2);
        options.setExperimentalOption("prefs", chromePrefs);

        this.chromeDriver  = new ChromeDriver(options);
    }

    public byte[] generateCombinedPdfFromHtmlFiles(List<File> inputHtmlFiles) throws IOException {
        ByteArrayOutputStream outputCombinedPdfByteArrayOutputStream = new ByteArrayOutputStream();

        List<byte[]> outputPdfBytesList = new ArrayList<>();
        for(File inputHtmlFile:inputHtmlFiles){
            outputPdfBytesList.add(generatePdfUsingSeleniumScreenshot(inputHtmlFile));
        }

        PDFMergerUtility mergerUtility = new PDFMergerUtility();
        try(PDDocument outputDocument = new PDDocument()){

            for(byte[] outputPdfBytes: outputPdfBytesList){
                PDDocument documentFromBytes = Loader.loadPDF(outputPdfBytes);
                mergerUtility.appendDocument(outputDocument, documentFromBytes);
            }
            outputDocument.save(outputCombinedPdfByteArrayOutputStream);
        }

        return outputCombinedPdfByteArrayOutputStream.toByteArray();
    }

    public byte[] generatePdfUsingSeleniumScreenshot(File inputHtmlFile) {
        String url = "file://" + inputHtmlFile.getAbsolutePath();
        log.info("Opening " + url);
        chromeDriver.get(url);

        Map<String, Object> params = new HashMap<>();
        params.put("landscape", false);
        params.put("displayHeaderFooter", false);
        params.put("printBackground", true);
        params.put("preferCSSPageSize", true);

        String command = "Page.printToPDF";
        Map<String, Object> output = chromeDriver.executeCdpCommand(command, params);

        return java.util.Base64.getDecoder().decode((String) output.get("data"));
    }

    @PreDestroy
    public void shutdown(){
        chromeDriver.quit();
    }

}
