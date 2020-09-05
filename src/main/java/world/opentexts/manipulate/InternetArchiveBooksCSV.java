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
import java.util.HashSet;
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
 * 
 * Command to grab the main results:
 *  wget "https://archive.org/advancedsearch.php?q=collection%3A%28americana%29&fl%5B%5D=creator&fl%5B%5D=description&fl%5B%5D=external-identifier&fl%5B%5D=genre&fl%5B%5D=identifier&fl%5B%5D=language&fl%5B%5D=licenseurl&fl%5B%5D=publisher&fl%5B%5D=subject&fl%5B%5D=title&fl%5B%5D=year&sort%5B%5D=&sort%5B%5D=&sort%5B%5D=&rows=5000000&page=1&callback=callback&save=yes&output=csv"
 * 
 * Command to grab the exclude list:
 *  wget "https://archive.org/advancedsearch.php?q=collection%3A%28inlibrary%29&fl%5B%5D=creator&fl%5B%5D=description&fl%5B%5D=external-identifier&fl%5B%5D=genre&fl%5B%5D=identifier&fl%5B%5D=language&fl%5B%5D=licenseurl&fl%5B%5D=publisher&fl%5B%5D=subject&fl%5B%5D=title&fl%5B%5D=year&sort%5B%5D=&sort%5B%5D=&sort%5B%5D=&rows=5000000&and%5B%5D%3Dlending___status%3A%22is_readable%22&page=1&callback=callback&save=yes&output=csv"
 * 
 * Parameters to run this script:
 *  c:\otw\IA\ia-americana-filtered.csv c:\otw\IA\ia-americana-exclude.csv c:\otw\ia-clean-filtered.csv
 */
public class InternetArchiveBooksCSV {
    
    public static void main(String[] args) {
        // Take the filename in and out as the only parameters
        if (args.length < 3) {
            System.err.println("Please supply input, exclude, and output filenames");
            System.exit(0);
        }

        boolean debug = false;
        
        try {
            // Open the input CSV
            String inExFilename = args[1];
            System.out.println("Processing file: " + inExFilename);
            Reader inEx = new BufferedReader(new InputStreamReader(new FileInputStream(inExFilename), "UTF-8"));

            CSVParser csvParserEx = new CSVParser(inEx, CSVFormat.DEFAULT
                    .withHeader("creator","description","external-identifier","genre","identifier","language","licenseurl","publisher","subject","title","year")
                    .withIgnoreHeaderCase()
                    .withTrim());
 
            // Setup some variables
            boolean header = false;
            int lineCounter = 1;
            String id = "";
            HashSet<String> excludedID = new HashSet<String>();
            
            // Process each line
            for (CSVRecord record : csvParserEx) {
                if (!header) {
                    System.out.println(" - Processing header");
                    header = true;
                } else {
                    lineCounter++;
                    if (lineCounter % 10000 == 0) System.out.println("Line: " + lineCounter);
                
                    id = record.get("identifier");
                    //if (debug) System.out.println(id);
                    excludedID.add(id);
                }
            }
            inEx.close();
            
            // Open the input CSV
            String inFilename = args[0];
            System.out.println("Processing file: " + inFilename);
            Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFilename), "UTF-8"));

            // Open the output CSV
            String outFilename = args[2];
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFilename));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                                                    "organisation",
                                                    "idLocal",
                                                    "title",
                                                    "urlMain",
                                                    "year",
                                                    "date",
                                                    "publisher",
                                                    "creator",
                                                    "topic",
                                                    "description",
                                                    "urlPDF",
                                                    "urlIIIF",
                                                    "urlPlainText",
                                                    "urlALTOXML",
                                                    "urlOther",
                                                    "placeOfPublication",
                                                    "licence",
                                                    "idOther",
                                                    "catLink",
                                                    "language"));
            
            // Setup some variables
            header = false;
            lineCounter = 1;
            String organisation = "", idLocal = "", title = "", urlMain = "", year = "", date = "",
                   publisher = "", creator = "", topic = "", description = "", urlPDF = "", 
                   urlIIIF = "", urlPlainText = "", urlALTOXML = "", urlOther = "", 
                   placeOfPublication = "", licence = "", idOther = "",
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
                    if (lineCounter % 10000 == 0) System.out.println("Line: " + lineCounter);
                    //if (lineCounter > 100000) break;
                    
                    organisation = "Internet Archive Books";

                    idLocal = record.get("identifier");
                    //if (debug) System.out.println(" - ID: " + idLocal);
                    if (excludedID.contains(idLocal)) {
                        if (debug) System.out.println("  - SKIPPING (excluded) " + idLocal);
                        continue;
                    }
                    
                    try {
                        year = record.get("year");
                    } catch (Exception e) {
                        // Thrown if the line is too short - so just just skip this istem
                        if (debug) System.err.println("ERROR - " + idLocal + " is too short");
                        continue;
                    }
                    date = year;
                    try {
                        int y = Integer.parseInt(year);
                        if ((y < 1000) || (y > 2025)){
                            year = "";
                        } 
                    } catch (NumberFormatException e) {
                        year = "";
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

                    urlPlainText = "https://archive.org/download/" + idLocal + "/" + idLocal + "_djvu.txt";

                    urlIIIF = "https://iiif.archivelab.org/iiif/" + idLocal + "/manifest.json";

                    placeOfPublication = "";

                    licence = record.get("licenseurl");
                    
                    idOther = record.get("external-identifier");

                    catLink = urlMain;
                    
                    language = MARC21LanguageCodeLookup.convert(record.get("language"));
                    
                    csvPrinter.printRecord(Arrays.asList(organisation, idLocal, title,
                                                         urlMain, year, date, publisher,
                                                         creator, topic, description,
                                                         urlPDF, urlIIIF, urlPlainText, urlALTOXML, urlOther,
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
