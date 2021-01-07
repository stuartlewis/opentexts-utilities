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
 * A manipulation tool to convert HathiFiles from Hathi Trust into the OpenTexts.World CSV format
 * 
 * @author Stuart Lewis
 */
public class HathiTrustImporter {
    
    public static void main(String[] args) {
        // Take the filename in and out as the only parameters
        /**if (args.length < 2) {
            System.err.println("Please supply input and output filenames");
            System.exit(0);
        }*/
        
        boolean debug = false;
        
        try {
            // Open the input CSV
            String inFilename = "C://otw//HT//hathi_full_20210101.txt";
            System.out.println("Processing file: " + inFilename);
            Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFilename), "UTF-8"));

            // Open the output CSV
            String outFilename = "C://otw//HT//ht.csv";
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
                                                    "urlTEI",
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
                   urlIIIF = "", urlPlainText = "", urlALTOXML = "", urlTEI = "", urlOther = "", placeOfPublication = "", 
                   licence = "", idOther = "", catLink = "", language = "";
            
            CSVParser csvParser = new CSVParser(in, CSVFormat.newFormat('\t')
                    .withHeader("htid", "access", "rights", "ht_bib_key", "description", "source", "source_bib_num", "oclc_num", 
                                "isbn", "issn", "lccn", "title", "imprint", "rights_reason_code", "rights_timestamp", "us_gov_doc_flag",
                                "rights_date_used", "pub_place", "lang", "bib_fmt", "collection_code", "content_provider_code",
                                "responsible_entity_code", "digitization_agent_code", "access_profile_code", "author")
                    .withIgnoreHeaderCase()
                    .withTrim());
 
            // Process each line
            for (CSVRecord record : csvParser) {
                if (!header) {
                    System.out.println(" - Processing header");
                    header = true;
                } else {
                    lineCounter++;
                    if (lineCounter % 1000000 == 0) System.out.println("Line: " + lineCounter);
                    //if (lineCounter > 100000) break;
                    
                    // Check the rights
                    String code = record.get("rights");
                    licence = "";
                    if ((code.equals("pd")) ||
                        (code.startsWith("cc-")) ||
                        (code.equals("ic-world")) ||
                        (code.equals("und-world"))) {
                        // We're open!
                        if (code.startsWith("cc-")) {
                            licence = code;
                        }
                    } else {
                        continue;
                    }
                    
                    organisation = "HathiTrust";

                    idLocal = record.get("htid");
                    if (debug) System.out.println(" - ID: " + idLocal);
                    
                    try {
                        date = record.get("rights_date_used");
                    } catch (Exception e) {
                        // Thrown if the line is too short - so just just skip this istem
                        if (debug) System.err.println("ERROR - " + idLocal + " is too short");
                        continue;
                    }
                    if ("9999".equals(date)) date = "Unknown";
                    if (debug) System.out.println("  - Date: " + date); 
                    year = date;
                    try {
                        int y = Integer.parseInt(year);
                        if ((y < 1000) || (y > 2025)){
                            year = "";
                        } 
                    } catch (NumberFormatException e) {
                        year = "";
                    }
                    if (debug) System.out.println("  - Year: " + year);
                    
                    title = record.get("title");
                    if (title.contains("|")) title = title.replace('|', 'i');
                    if (debug) System.out.println("  - Title: " + title);
                    
                    urlMain = "https://hdl.handle.net/2027/" + idLocal;
                    if (debug) System.out.println("  - URL: " + urlMain);
                    
                    publisher = record.get("imprint");
                    if (debug) System.out.println("  - Publisher: " + publisher);
                    
                    creator = record.get("author");
                    if (debug) System.out.println("  - Creator: " + creator);
                    
                    topic = "";

                    description = record.get("description");
                    if (debug) System.out.println("  - Description: " + description);

                    String accessProfile = record.get("access_profile_code");
                    urlPDF = "";
                    urlPlainText = "";
                    if (accessProfile.equals("open")) {
                        urlPDF = "https://babel.hathitrust.org/cgi/imgsrv/download/pdf?id=" + idLocal;
                        urlPlainText = "https://babel.hathitrust.org/cgi/imgsrv/download/plaintext?id=" + idLocal;
                        if (debug) System.out.println("  - URL Plaintext: " + urlPlainText);
                        if (debug) System.out.println("  - URL PDF: " + urlPDF);
                    }
                    
                    urlIIIF = "";
                    
                    urlALTOXML = "";
                    urlOther = "https://babel.hathitrust.org/cgi/ssd?id=" + idLocal;

                    placeOfPublication = record.get("pub_place");
                    if (debug) System.out.println("  - Place of Publication: " + placeOfPublication);

                    // Include ISBN / ISSN / OCLC / LCCN
                    idOther = "";
                    if (!record.get("isbn").equals("")) {
                        idOther = record.get("isbn").replaceAll(",", "|");
                    }
                    if (!record.get("issn").equals("")) {
                        if (!idOther.equals("")) idOther += "|";
                        idOther = record.get("issn").replaceAll(",", "|");
                    }
                    if (!record.get("lccn").equals("")) {
                        if (!idOther.equals("")) idOther += "|";
                        idOther = record.get("lccn").replaceAll(",", "|");
                    }
                    if (debug) System.out.println("  - Other IDs: " + idOther);


                    catLink = urlMain;
                    
                    language = MARC21LanguageCodeLookup.convert(record.get("lang"));
                    if (debug) System.out.println("  - Language: " + language);

                    csvPrinter.printRecord(Arrays.asList(organisation, idLocal, title,
                                                         urlMain, year, date, publisher,
                                                         creator, topic, description,
                                                         urlPDF, urlIIIF, urlPlainText, 
                                                         urlALTOXML, urlTEI, urlOther,
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
