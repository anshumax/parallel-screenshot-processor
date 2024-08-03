package dev.anshumax.htmltopdf.service;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@CommonsLog
@Service
public class DocumentService {

    @Autowired
    ConcurrentPdfGenerationService concurrentPdfGenerationService;

    StopWatch stopWatch = new StopWatch();


    public byte[] generateCombinedAccountStatementPdf(String userId) throws Exception {
        log.info("Generating for " + userId);
        stopWatch.start();
        List<Pair<Integer,File>> inputHtmlFileDescriptors = new ArrayList<>();

        List<File> inputFiles = Arrays.stream(Objects.requireNonNull(new File("/Users/anshuman/Downloads/HTML Archive").listFiles()))
                .sorted().toList();
        for(int i = 0 ; i < inputFiles.size(); i++){
            inputHtmlFileDescriptors.add(Pair.of(i, inputFiles.get(i)));
        }

        byte[] outputBytes = concurrentPdfGenerationService.generateCombinedPdfFromHtmlFiles(inputHtmlFileDescriptors);
        stopWatch.stop();
        log.info("Time taken: " + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms");
        stopWatch.reset();
        return outputBytes;
    }
}
