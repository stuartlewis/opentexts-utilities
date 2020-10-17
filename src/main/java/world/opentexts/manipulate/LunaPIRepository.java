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
 * A manipulation tool to convert The Lunar and Planetary Institute OAI-PMH 
 * data feeds into the OpenTexts.World CSV format
 * 
 * @author Stuart Lewis
 */
public class LunaPIRepository {

    public static void main(String[] args) {
        // Take the filename in and out as the only parameters
        if (args.length < 2) {
            args = new String[2];
            args[0] = "c:\\otw\\lapi\\lapi-908.xml";
            args[1] = "c:\\otw\\lapi-908.csv";
        }

        try {
            // Open the input text file
            String inFilename = args[0];
            System.out.println("Reading file: " + inFilename);
            //Reader in = new FileReader(inFilename, "UTF-8");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFilename), "UTF-8"));

            // Open the output CSV
            String outFilename = args[1];
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
 
            // Process each line
            String line;
            try {
                while ((line = in.readLine()) != null) {
                    if ((line.trim().startsWith("<?xml")) ||
                        (line.trim().startsWith("<OAI-PMH")) ||
                        (line.trim().startsWith("<responseDate")) ||
                        (line.trim().startsWith("<request")) ||
                        (line.trim().startsWith("<ListRecords"))) {
                        // Skip
                        System.out.println(" Skipping - " + line);
                    } else {
                        organisation = "Lunar and Planetary Institute";
                        System.out.println(lineCounter++);

                        // Skip lines
                        while (!line.equals("<metadata>")) {
                            // Do nothing
                            System.out.println(" Skipping -- " + line);
                            line = in.readLine().trim();
                        }

                        // Skip oai_dc
                        line = in.readLine().trim();
                        System.out.println(" Skipping --- " + line);

                        // Process metadata
                        while (!line.equals("</oai_dc:dc>")) {
                            line = in.readLine().trim();

                            if ((line.startsWith("<dc:title>"))  && ("".equals(title))) {
                                title = line.substring(10, line.length() - 11);
                                System.out.println(" - Title: " + title);
                            } 

                            if ((line.startsWith("<dc:identifier>")) && ("".equals(idLocal)) && (line.length() < 60)) {
                                idLocal = line.substring(15, line.length() - 16);
                                System.out.println(" - IDLocal: " + idLocal);
                            } 

                            if ((line.startsWith("<dc:identifier>")) && (line.contains("(OCoLC)"))) {
                                idOther = line.substring(15, line.length() - 16);
                                System.out.println(" - IDOther: " + idOther);
                            }

                            if ((line.startsWith("<dc:identifier>")) && (line.contains("https://hdl.handle.net/20.500.11753/"))) {
                                urlMain = line.substring(15, line.length() - 16);
                                System.out.println(" - URL Main: " + urlMain);
                            }

                            if (line.startsWith("<dc:contributor>")) {
                                creator += "||" + line.substring(16, line.length() - 17);
                                if (creator.startsWith("||")) { creator = creator.substring(2); }
                                System.out.println(" - Creator: " + creator);
                            } 

                            if (line.startsWith("<dc:relation>")) {
                                publisher += "||" + line.substring(13, line.length() - 14);
                                if (publisher.startsWith("||")) { publisher = publisher.substring(2); }
                                System.out.println(" - Publisher: " + publisher);
                            } 

                            if (line.startsWith("<dc:relation>")) {
                                description += "||" + line.substring(16, line.length() - 17);
                                if (description.startsWith("||")) { description = description.substring(2); }
                                System.out.println(" - Description: " + description);
                            } 

                            if (line.startsWith("<dc:date>")) {
                                date = line.substring(9, 13);
                                year = date;
                                System.out.println(" - Date: " + date);
                            } 

                            if (line.equals("<dc:language>en</dc:language>")) {
                                language = "English";
                                System.out.println(" - Language: " + language);
                            }          
                        }

                        //System.out.println(idLocal);
                        csvPrinter.printRecord(Arrays.asList(organisation, idLocal, title, 
                                                             urlMain, year, date, publisher,
                                                             creator, topic, description,
                                                             urlPDF, urlIIIF, urlPlainText, urlALTOXML, urlOther, 
                                                             placeOfPublication, licence, idOther,
                                                             catLink, language));
                        idLocal = "";
                        title = "";
                        date = "";
                        idOther = "";
                        creator = "";
                        urlMain = "";
                        description = "";
                        publisher = "";

                        System.out.println(" - END OF RECORD");
                    }
                }
            } catch (Exception e) {
                System.out.println("Writing file: " + outFilename);
                csvPrinter.flush();
                csvPrinter.close();

                // Run the validator
                Validator v = new Validator(outFilename);
            }
        } catch (Exception e) {
            System.err.println("ERROR - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
