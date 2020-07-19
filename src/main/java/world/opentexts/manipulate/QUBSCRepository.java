/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world.opentexts.manipulate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import world.opentexts.util.MARC21LanguageCodeLookup;
import world.opentexts.validate.Validator;

/**
 * A manipulation tool to convert QUeen's University Belfast data feeds into the OpenTexts.World CSV format
 * 
 * @author Stuart Lewis
 */
public class QUBSCRepository {

    public static void main(String[] args) {
        // Take the filename in and out as the only parameters
        if (args.length < 2) {
            args = new String[2];
            args[0] = "c:\\otw\\qub\\qub.csv";
            args[1] = "c:\\otw\\qub\\qub-clean.csv";
        }

        try {
            // Open the input CSV
            String inFilename = args[0];
            System.out.println("Cleaning file: " + inFilename);
            //Reader in = new FileReader(inFilename, "UTF-8");
            Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFilename), "UTF-8"));

            // Open the output CSV
            String outFilename = args[1];
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFilename));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                                                    "organisation",
                                                    "idLocal",
                                                    "title",
                                                    "urlMain",
                                                    "year",
                                                    "publisher",
                                                    "creator",
                                                    "topic",
                                                    "description",
                                                    "urlPDF",
                                                    "urlOther",
                                                    "urlIIIF",
                                                    "placeOfPublication",
                                                    "licence",
                                                    "idOther",
                                                    "catLink",
                                                    "language"));
            
            // Setup some variables
            boolean header = false;
            int lineCounter = 1;
            String organisation = "", idLocal = "", title = "", urlMain = "", year = "",
                   publisher = "", creator = "", topic = "", description = "", urlPDF = "", 
                   urlOther = "", urlIIIF = "", placeOfPublication = "", licence = "", idOther = "",
                   catLink = "", language = "";
 
            CSVParser csvParser = new CSVParser(in, CSVFormat.DEFAULT
                    .withHeader("000", "100", "245", "260", "500", "546", "690", "691", "856")
                    .withIgnoreHeaderCase()
                    .withTrim());
            
            // Process each line
            for (CSVRecord record : csvParser) {
                if (!header) {
                    System.out.println(" - Processing header");
                    header = true;
                } else {
                    organisation = "Queen's University Belfast";
                    System.out.println(lineCounter++);
                    
                    // Check if this is an item (or a page we can skip)
                    if ("".equals(record.get("100"))) {
                        continue;
                    }
                    
                    idLocal = record.get("500");
                    if ("".equals(idLocal)) {
                        continue;
                    }
                    idLocal = idLocal.substring(4);
                    System.out.println("Item: " + idLocal);
                    
                    title = record.get("245");
                    if ("".equals(title)) {
                        System.err.println("Skipping item: " + idLocal + " as it doesn't have a title"); 
                        continue;
                    }
                    title = title.replaceAll("\\$", "d0llar");
                    title = title.substring(9);
                    title = title.replace("d0llara", " ");
                    title = title.replace("d0llarb", " ");
                    title = title.replace("d0llarc", " ");
                    title = title.replace("d0llarf", " ");
                    title = title.replace("d0llarn", " ");
                    title = title.replace("d0llarh", " ");
                    title = title.replace("d0llari", " ");
                    title = title.replace("d0llarl", " ");
                    title = title.replace("d0llarp", " ");
                    title = title.replace("d0llar1", " ");
                    title = title.replace("d0llar2", " ");
                    title = title.replace("d0llar3", " ");
                    title = title.replace("d0llar4", " ");
                    title = title.replace("d0llar5", " ");
                    title = title.replace("d0llar6", " ");
                    title = title.replace("d0llar7", " ");
                    title = title.replace("d0llar8", " ");
                    title = title.replace("d0llar9", " ");
                    title = title.replace("/", "");
                    title = title.replace("  ", " ");
                    if ((title.endsWith(" :")) || (title.endsWith(" /"))) {
                        title = title.substring(0, title.length() - 2).trim();
                    }
                    System.out.println(" Title: " + title);
                    if (title.endsWith("Index")) {
                        System.out.println("SKIPPING INDEX");
                        continue;
                    }
                    
                    // Select the last URL if there are multiple
                    urlMain = record.get("856");
                    urlMain = urlMain.replaceAll("\\$", "d0llar");
                    //System.out.println("URL = " + urlMain);
                    if (!urlMain.contains("d0llaru")) {
                        System.err.println("Skipping item: " + idLocal + " as it doesn't have a URL");
                        continue;
                    }
                    urlMain = urlMain.substring(urlMain.indexOf("d0llaru") + 7);
                    if (urlMain.contains("d0llar")) {
                        urlMain = urlMain.substring(0, urlMain.indexOf("d0llar"));
                    }
                    System.out.println(" URL: " + urlMain);
                    
                    // Select the first year if there are multiple
                    year = record.get("691");
                    System.out.println(" Year: " + year);
                    if (!"".equals(year)) {
                        year = year.substring(year.length() - 4);
                        System.out.println(" Year: " + year);
                    }
                
                    // Publisher 260 $b
                    publisher = ("Publisher not listed");
                    
                    creator = record.get("100");
                    creator = creator.replaceAll("\\$", "d0llar");
                    creator = creator.substring(creator.indexOf("d0llara") + 7);
                    if (creator.contains("d0llar")) {
                        creator = creator.substring(0, creator.indexOf("d0llar")).strip();
                    }
                    System.out.println(" Creator: " + creator);
                    
                    topic = record.get("690");
                    if (!"".equals(topic)) {
                        topic = topic.replaceAll("\\$", "d0llar");
                        topic = topic.substring(topic.indexOf("d0llara") + 7);
                        if (topic.contains("d0llar")) {
                            topic = topic.substring(0, topic.indexOf("d0llar")).strip();
                        }
                    }
                    topic = topic.replaceAll("; ", "|");
                    System.out.println(" Topic: " + topic);

                    description = "";
                    
                    placeOfPublication = "";    
                    
                    language = record.get("546");
                    if (!"".equals(language)) {
                        if (language.length() > 3) language = "1234mul";
                        language = MARC21LanguageCodeLookup.convert(language.substring(4));
                        System.out.println(" Language: " + language);
                    }
                    
                    licence = "";
                    
                    idOther = "";
                    
                    // Generate the catalogue link
                    catLink = "";
                    
                    //System.out.println(idLocal);
                    csvPrinter.printRecord(Arrays.asList(organisation, idLocal, title,
                                                         urlMain, year, publisher,
                                                         creator, topic, description,
                                                         urlPDF, urlOther, urlIIIF,
                                                         placeOfPublication, licence, idOther,
                                                         catLink, language));
                }
            }
            System.out.println("Writing file: " + outFilename);
            csvPrinter.flush();
            csvPrinter.close();
            
            // Run the validator
            Validator v = new Validator(outFilename);
        } catch (Exception e) {
            System.err.println("ERROR - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
