/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world.opentexts.manipulate;

import java.io.BufferedWriter;
import java.io.FileReader;
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
 * A manipulation tool to convert Internet Archive Books CSV into the OpenTexts.World CSV format
 * 
 * @author Stuart Lewis
 */
public class InternetArchiveBooksCSV {
    
    public static void main(String[] args) {
        // Take the filename in and out as the only parameters
        if (args.length < 2) {
            System.err.println("Please supply input and output filenames");
            System.exit(0);
        }

        boolean debug = true;
        
        try {
            // Open the input CSV
            String inFilename = args[0];
            System.out.println("Processing file: " + inFilename);
            Reader in = new FileReader(inFilename);

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
                    .withHeader("creator","description","external-identifier","genre","identifier","language","licenseurl","publisher","subject","title","year")
                    .withIgnoreHeaderCase()
                    .withTrim());
 
            // Process each line
            for (CSVRecord record : csvParser) {
                if (!header) {
                    System.out.println(" - Processing header");
                    header = true;
                } else {
                    lineCounter++;
                    if (lineCounter % 100 == 0) System.out.println("Line: " + lineCounter);
                    if (lineCounter > 100000) break;
                    
                    organisation = "Internet Archive Books";

                    idLocal = record.get("identifier");
                    //if (debug) System.out.println(" - ID: " + idLocal);
                    
                    try {
                        year = record.get("year");
                    } catch (Exception e) {
                        // Thrown if the line is too short - so just just skip this istem
                        if (debug) System.err.println("ERROR - " + idLocal + " is too short");
                        continue;
                    }
                    
                    title = record.get("title");
                    if (idLocal.equals("hitoryofmediaevalphil0000wulf")) {
                        title = "History of Mediaeval Philosophy";
                    } else if (title.equals("")) {
                        System.err.println("ERROR: " + idLocal + " has no title");
                        continue;
                        //System.exit(0);
                    }
                    title = title.replaceAll("\\\\|b", "");
                    title = title.replaceAll("\\|", "");
                    
                    urlMain = "https://archive.org/details/" + idLocal;
                    
                    publisher = record.get("publisher");    

                    creator = record.get("creator");

                    topic = record.get("subject");

                    description = record.get("description");

                    urlPDF = "https://archive.org/download/" + idLocal + "/" + idLocal + ".pdf";

                    urlOther = "https://archive.org/download/" + idLocal + "/" + idLocal + "_djvu.txt";

                    urlIIIF = "https://iiif.archivelab.org/iiif/" + idLocal + "/manifest.json";

                    placeOfPublication = "";

                    licence = record.get("licenseurl");
                    
                    idOther = record.get("external-identifier");

                    catLink = urlMain;
                    
                    language = MARC21LanguageCodeLookup.convert(record.get("language"));
                    
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
        }
    }
}
