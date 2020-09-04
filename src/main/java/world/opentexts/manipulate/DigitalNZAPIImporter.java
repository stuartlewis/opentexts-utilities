/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world.opentexts.manipulate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import world.opentexts.util.MARC21LanguageCodeLookup;
import world.opentexts.validate.Validator;

/**
 * A manipulation tool to import DigitalNZ data feeds into the OpenTexts.World CSV format
 * 
 * @author Stuart Lewis
 */
public class DigitalNZAPIImporter {

    public static Boolean debug = false;
    
    public static void main(String[] args) {
        // Take the filename in and out as the only parameters
        if (args.length < 2) {
            System.err.println("Please supply API key and output filenames");
            System.exit(0);
        }

        try {
            // The API key
            String apiKey = args[0];

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
 
            // Organisation
            organisation = "DigitalNZ";
            
            // Open the first page, then keep looping until all pages of results are processed
            int page = 1;
            while (page < 2445) {
                String url = "https://api.digitalnz.org/v3/records.json?api_key=" + apiKey + "&text=*&and[category][]=Books&per_page=100&page=" + page++;
                URL apiURL = new URL(url);
                BufferedReader br = new BufferedReader(new InputStreamReader(apiURL.openStream(), "UTF-8"));
                System.out.println(url);
                
                JSONParser jp = new JSONParser();
                JSONObject topJson = (JSONObject)jp.parse(br);
                JSONObject search = (JSONObject)topJson.get("search");
                JSONArray results = (JSONArray)search.get("results");
                Iterator<JSONObject> resultsIterator = results.iterator();
                while (resultsIterator.hasNext()) {
                
                    JSONObject result = resultsIterator.next();
                    
                    // LocalID
                    idLocal = ((Long)result.get("id")).toString();
                    if (debug) System.out.println(idLocal);
                    
                    // Title
                    title = (String)result.get("title");
                    title.replaceAll("[|]", "-");
                    if (debug) System.out.println(" - " + title);
                    
                    // URLMain
                    urlMain = (String)result.get("landing_url");
                    if (debug) System.out.println(" - " + urlMain);
                    
                    // Year
                    date = "Unknown";
                    JSONArray dates = (JSONArray)result.get("date");
                    if (dates.size() > 0) {
                        date = (String)dates.get(0);
                        date = date.substring(0, 4);
                        if (debug) System.out.println(" - " + year);
                    }
                    try {
                        int y = Integer.parseInt(date);
                        if ((y < 1000) || (y > 2025)){
                            year = "";
                        } 
                    } catch (NumberFormatException e) {
                        year = "";
                    }
                    
                    // Publisher
                    publisher = "";
                    JSONArray publishers = (JSONArray)result.get("publisher");
                    if (publishers.size() > 0) {
                        publisher = (String)publishers.get(0);
                        if (debug) System.out.println(" - " + publisher);
                    }
                    
                    // Creator
                    creator = "";
                    JSONArray creators = (JSONArray)result.get("creator");
                    if (creators.size() > 0) {
                        creator = (String)creators.get(0);
                        if (debug) System.out.println(" - " + creator);
                    }

                    // Topic
                    topic = "";
                    
                    // Description
                    description = (String)result.get("description");
                    if (description == null) description = "";
                    if (debug) System.out.println(" - " + description);
                    
                    // urlPDF
                    urlPDF = "";
                    
                    // urlOther
                    urlOther = "";
                    
                    // urlIIIF
                    urlIIIF = "";
                    
                    // placeOfPublication
                    placeOfPublication = "";
                    
                    // licence
                    licence = "";
                    JSONArray rights = (JSONArray)result.get("rights_url");
                    if (rights.size() > 0) {
                        licence = (String)rights.get(0);
                        if (debug) System.out.println(" - " + licence);
                    }
                    
                    // idOther
                    idOther = "";
                    
                    // catLink
                    catLink = "https://digitalnz.org/records/" + idLocal;
                    if (debug) System.out.println(" - " + catLink);
                    
                    // language
                    language = "";
                    JSONArray languages = (JSONArray)result.get("language");
                    if (languages.size() > 0) {
                        language = (String)languages.get(0);
                        language = MARC21LanguageCodeLookup.convert(language);
                        if (debug) System.out.println(" - " + language);
                    }
                    
                    // Write the CSV entry
                    csvPrinter.printRecord(Arrays.asList(organisation, idLocal, title,
                                                         urlMain, year, date, publisher,
                                                         creator, topic, description,
                                                         urlPDF, urlIIIF, urlPlainText, urlALTOXML, urlOther,
                                                         placeOfPublication, licence, idOther,
                                                         catLink, language));
                    if (debug) System.out.println();
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
