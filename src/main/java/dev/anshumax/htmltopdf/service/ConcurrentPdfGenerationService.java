package dev.anshumax.htmltopdf.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@CommonsLog
@Service
public class ConcurrentPdfGenerationService {

    final PDFMergerUtility mergerUtility = new PDFMergerUtility();

    @Autowired
    ParallelScreenshotTaker parallelScreenshotTaker;

    @PostConstruct
    public void init(){
        mergerUtility.setIgnoreAcroFormErrors(true);
    }

    public byte[] generateCombinedPdfFromHtmlFiles( List<Pair<Integer,File>> inputHtmlFilesDescriptors) throws Exception {

        ByteArrayOutputStream outputCombinedPdfByteArrayOutputStream = new ByteArrayOutputStream();

        Map<Integer,byte[]> outputPdfBytesWithPageNumbers = new HashMap<>();
        List<CompletableFuture<Pair<Integer,byte[]>>> results = new ArrayList<>();
        for(Pair<Integer,File> inputHtmlFileDescriptor:inputHtmlFilesDescriptors) {
            results.add(parallelScreenshotTaker.generatePdfUsingSeleniumScreenshot(inputHtmlFileDescriptor));
        }

        for(CompletableFuture<Pair<Integer,byte[]>> result: results){
                Pair<Integer,byte[]> outputFileDescriptor = Optional.of(result.get())
                        .orElseThrow(() -> new Exception("Unable to convert HTML to PDF"));
                outputPdfBytesWithPageNumbers.put(outputFileDescriptor.getKey(), outputFileDescriptor.getValue());

        }

        List<byte[]> outputPdfBytesList = outputPdfBytesWithPageNumbers.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();

        try(PDDocument outputDocument = new PDDocument()){

            for(byte[] outputPdfBytes: outputPdfBytesList){
                PDDocument documentFromBytes = Loader.loadPDF(outputPdfBytes);
                mergerUtility.appendDocument(outputDocument, documentFromBytes);
            }
            outputDocument.save(outputCombinedPdfByteArrayOutputStream);
        }catch (Exception e){
            log.error("Exception!", e);
        }

        return outputCombinedPdfByteArrayOutputStream.toByteArray();
    }





}
