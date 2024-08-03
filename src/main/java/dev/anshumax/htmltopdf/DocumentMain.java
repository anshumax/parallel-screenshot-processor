package dev.anshumax.htmltopdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocumentMain implements CommandLineRunner {

    public static void main(String[] args){
        SpringApplication.run(DocumentMain.class);
    }


    @Autowired
    DocumentApp app;

    @Override
    public void run(String... args) {
        app.init();
        app.execute();
        app.exit();
    }




}
