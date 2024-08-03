package dev.anshumax.htmltopdf.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v127.page.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@CommonsLog
@Service
public class ParallelScreenshotTaker {
    GenericObjectPool<ChromeDriver> chromeDriverPool;
    @Autowired
    String chromeDriverLoc;

    Base64.Decoder base64Decoder = Base64.getDecoder();

    @PostConstruct
    public void init(){
        System.setProperty("webdriver.chrome.driver", chromeDriverLoc);
        this.chromeDriverPool = new GenericObjectPool<>(new ChromeDriverPoolFactory());
        this.chromeDriverPool.setMaxTotal(8);
        this.chromeDriverPool.setBlockWhenExhausted(true);
    }

    @Async("pdfGenerator")
    public CompletableFuture<Pair<Integer,byte[]>> generatePdfUsingSeleniumScreenshot(Pair<Integer,File> inputHtmlFileDescriptor) throws Exception{
            Integer pageNumber = inputHtmlFileDescriptor.getKey();
            File inputHtmlFile = inputHtmlFileDescriptor.getValue();
            String url = "file://" + inputHtmlFile.getAbsolutePath();

            ChromeDriver chromeDriver = chromeDriverPool.borrowObject();

            chromeDriver.get(url);

            log.info("Taking screenshot using " + chromeDriver);
//            Option 1
//            Map<String, Object> params = new HashMap<>();
//            params.put("landscape", false);
//            params.put("displayHeaderFooter", false);
//            params.put("printBackground", true);
//            params.put("preferCSSPageSize", true);
//
//            String command = "Page.printToPDF";
//            Map<String, Object> output = chromeDriver.executeCdpCommand(command, params);
//            byte[] outputPdfBytes = base64Decoder.decode((String) output.get("data"));

//            Option 2
            DevTools devTools = chromeDriver.getDevTools();
            devTools.createSession();
            Command<Page.PrintToPDFResponse> command =  Page.printToPDF(Optional.of(Boolean.FALSE),
                    Optional.of(Boolean.FALSE), Optional.of(Boolean.TRUE),
                    Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.of(Boolean.TRUE), Optional.of(Page.PrintToPDFTransferMode.RETURNASBASE64),
                    Optional.empty(), Optional.empty());
            String snap = devTools.send(command).getData();
            byte[] outputPdfBytes = base64Decoder.decode(snap);

//            Option 3
//            Map<String, Object> params = Map.ofEntries(
//                    Map.entry("landscape", false),
//                    Map.entry("displayHeaderFooter", false),
//                    Map.entry("printBackground", true),
//                    Map.entry("preferCSSPageSize", true)
//            );
//            String snap = devTools.send(new Command<>("Page.printToPDF", params, ConverterFunctions.map("data", String.class)));
//            byte[] outputPdfBytes = base64Decoder.decode(snap);

            chromeDriverPool.returnObject(chromeDriver);
            return CompletableFuture.supplyAsync(() -> Pair.of(pageNumber, outputPdfBytes));
    }

    @PreDestroy
    public void shutdown(){
        try{
            log.info("Evicting pool");
            chromeDriverPool.evict();
            log.info("closing pool");
            chromeDriverPool.close();
            log.info("Cleared");
        }catch(Exception e){
            log.info("Error closing pool", e);
        }
    }

}
