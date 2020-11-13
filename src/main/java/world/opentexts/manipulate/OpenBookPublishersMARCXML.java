/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world.opentexts.manipulate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import world.opentexts.util.DownloadIIIFManifest;
import world.opentexts.util.MARC21LanguageCodeLookup;
import world.opentexts.validate.Validator;

/**
 * A manipulation tool to convert Open Book Publishers' data feeds into the OpenTexts.World CSV format
 * 
 * @author Stuart Lewis
 */
public class OpenBookPublishersMARCXML {

    public static void run(String[] args) {
        boolean debug = true;

        // Take the filename in and out as the only parameters
        /**if (args.length < 2) {
            System.err.println("Please supply input and output filenames");
            System.exit(0);
        }*/

        try {
            // Open the input CSV
            String inFilename = "c:\\otw\\OBP\\october2020.csv";
            System.out.println("Cleaning file: " + inFilename);
            //Reader in = new FileReader(inFilename, "UTF-8");
            Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFilename), "UTF-8"));

            // Open the output CSV
            String outFilename = "c:\\otw\\obp.csv";
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
            boolean header = false;
            int lineCounter = 1;
            String organisation = "", idLocal = "", title = "", urlMain = "", year = "", date = "",
                   publisher = "", creator = "", topic = "", description = "", urlPDF = "", 
                   urlIIIF = "", urlPlainText = "", urlALTOXML = "", urlOther = "", 
                   placeOfPublication = "", licence = "", idOther = "", catLink = "", language = "";
 
            CSVParser csvParser = new CSVParser(in, CSVFormat.DEFAULT
                    .withHeader("907", "245", "856", "260", "264", "100", "610", "500", "008", "001", "540")
                    .withIgnoreHeaderCase()
                    .withTrim());
            
            // Process each line
            for (CSVRecord record : csvParser) {
                if (!header) {
                    System.out.println(" - Processing header");
                    header = true;
                } else {
                    organisation = "Open Book Publishers";

                    idLocal = record.get("001").substring(2, 10);
                    if (debug) System.out.println("ID: " + title);
                    
                    title = record.get("245");
                    if ("".equals(title)) {
                        System.err.println("Skipping item: " + idLocal + " as it doesn't have a title"); 
                        continue;
                    }
                    title = title.substring(4);
                    title = title.substring(0, title.indexOf("$"));
                    if (debug) System.out.println(" - Title: " + title);
                                        
                    // $3(t.13(1865))$uhttp://purl.ox.ac.uk/uuid/7a8e629e39b5417aa410cb5687d9f69a$3(t.14(1865))$uhttp://purl.ox.ac.uk/uuid/1c6220f6e8254ced8f51ab2a2ae4fe28
                    // Select the last URL if there are multiple
                    urlMain = record.get("856");
                    urlMain = urlMain.substring(4);
                    urlMain = urlMain.substring(0, urlMain.indexOf("$"));
                    if (debug) System.out.println(" - urlMain: " + urlMain);
                    
                    // Select the first year if there are multiple
                    date = record.get("008");
                    if (("".equals(date)) || (date.length() < 12)) {
                        date = "";
                    } else {
                        date = date.substring(7, 11);     
                        if (date.contains("\\\\")) {
                                date = "";
                        }
                    }
                    year = date;
                    
                    try {
                        int y = Integer.parseInt(year);
                        if ((y < 1000) || (y > 2025)){
                            year = "";
                        } 
                    } catch (NumberFormatException e) {
                        year = "";
                    }
                    if (debug) System.out.println(" - Year: " + year);
                    
                    // Publisher 260 $b
                    publisher = "Open Book Publishers";
                       
                    creator = record.get("100");
                    if (!"".equals(creator)) {
                        creator = creator.substring(4);
                        creator = creator.substring(0, creator.indexOf("$"));
                        if (creator.endsWith(",")) {
                            creator = creator.substring(0, creator.length() - 1);
                        }
                        if (debug) System.out.println(" - creator: " + creator);
                    }
                                        
                    topic = "";

                    description = "";
                    
                    urlPDF = "";

                    urlPlainText = "";

                    urlIIIF = "";

                    placeOfPublication = "Cambridge";    
                    
                    language = record.get("008");
                    if (("".equals(language)) || (language.length() < 38)) {
                        language = "Not specified";
                        //System.out.println(language);
                    } else {
                        language = MARC21LanguageCodeLookup.convert(language.substring(35, 38));     
                    }
                    if (debug) System.out.println(" - language: " + language);
                    
                    licence = record.get("540");
                    if (licence.contains("(")) {
                        licence = licence.substring(licence.indexOf("(") + 1, licence.indexOf(")"));
                    } else if (licence.contains("Creative")) {
                        if (debug) System.out.println(" - licence: " + licence);
                        licence = licence.substring(licence.indexOf("Creative"));
                        if (licence.contains(". ")) {
                            licence = licence.substring(0, licence.indexOf(". "));
                        } else if (licence.contains(".For")) {
                            licence = licence.substring(0, licence.indexOf(".For"));
                        }
                    } else {
                        System.out.println("Unknown licence: " + licence);
                        System.exit(0);
                    }
                    if (debug) System.out.println(" - licence: " + licence);
                    
                    idOther = "";
                    
                    // Generate the catalogue link
                    catLink = urlMain;

                    //System.out.println(idLocal);
                    csvPrinter.printRecord(Arrays.asList(organisation, idLocal, title,
                                                         urlMain, year, date, publisher,
                                                         creator, topic, description,
                                                         urlPDF, urlIIIF, urlPlainText, urlALTOXML, urlOther,
                                                         placeOfPublication, licence, idOther,
                                                         catLink, language));
                    
                    //System.out.println(lineCounter++);
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
    
    public static void main(String[] args) throws Exception {
        File dir = new File("c:\\otw\\wellcome\\");
        File [] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".csv");
            }
        });

        for (File file : files) {
            String[] params = new String[2];
            params[0] = file.getCanonicalPath();
            params[1] = "c:\\otw\\wt-" + file.getName();
            System.out.println("Processing " + params[0] + " to " + params[1]);
            OpenBookPublishersMARCXML.run(params);
        }
    }
}
