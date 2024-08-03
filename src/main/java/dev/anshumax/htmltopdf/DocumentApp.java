package dev.anshumax.htmltopdf;

import dev.anshumax.htmltopdf.service.DocumentService;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@CommonsLog
@Component
public class DocumentApp {

    @Autowired
    DocumentService documentService;

    String outputPdfLoc = "/Users/anshuman/Downloads/";

    public void init(){
        log.info("Beginning execution");
    }

    public void execute(){
        try{
            for(int i = 0; i < 25; i++){
                String randomUserId = Integer.toString(RandomUtils.nextInt(10000, 100000));
                byte[] outputBytes = documentService.generateCombinedAccountStatementPdf(randomUserId);
                Files.write(Path.of(outputPdfLoc, "combined-pdf-"+ randomUserId + "-" + Instant.now().getEpochSecond() + ".pdf"), outputBytes);
            }


        }catch(Exception e){
            log.error("Error", e);
        }
    }

    public void exit(){
        System.exit(0);
    }
}
