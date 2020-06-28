/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world.opentexts.manipulate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
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
 * A manipulation tool to convert Wellcome data feeds into the OpenTexts.World CSV format
 * 
 * @author Stuart Lewis
 */
public class WellcomeMARCXML {

    public static void main(String[] args) {
        // Take the filename in and out as the only parameters
        if (args.length < 2) {
            System.err.println("Please supply input and output filenames");
            System.exit(0);
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
                    .withHeader("907", "245", "856", "260", "264", "100", "610", "500", "008")
                    .withIgnoreHeaderCase()
                    .withTrim());
            
            // Process each line
            for (CSVRecord record : csvParser) {
                if (!header) {
                    System.out.println(" - Processing header");
                    header = true;
                } else {
                    organisation = "Wellcome Collection";

                    idLocal = record.get("907").substring(6, 14);
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
                    //System.out.println("Title: " + title);
                    
                    // $3(t.13(1865))$uhttp://purl.ox.ac.uk/uuid/7a8e629e39b5417aa410cb5687d9f69a$3(t.14(1865))$uhttp://purl.ox.ac.uk/uuid/1c6220f6e8254ced8f51ab2a2ae4fe28
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
                    
                    // Select the first year if there are multiple
                    year = record.get("008");
                    if (("".equals(year)) || (year.length() < 12)) {
                        year = "Unknown";
                    } else {
                        year = year.substring(7, 11);     
                        if (year.contains("\\\\")) {
                                year = "Unknown";
                        }
                    }
                    //System.out.println(year);
                    
                    // Publisher 260 $b
                    publisher = record.get("260");
                    if (!"".equals(publisher)) {
                        publisher = publisher.replaceAll("\\$", "d0llar");
                        if (publisher.contains("d0llarb")) {
                            publisher = publisher.substring(publisher.indexOf("d0llarb") + 7);
                            if (publisher.contains("d0llarc")){ 
                                publisher = publisher.substring(0, publisher.indexOf("d0llarc"));
                            }
                        } else {
                            publisher = ("Publisher not listed");
                        }
                    }
                    String twoSixFour = record.get("264");
                    if (!"".equals(twoSixFour)) {
                        twoSixFour = twoSixFour.replaceAll("\\$", "d0llar");
                        twoSixFour = twoSixFour.substring(twoSixFour.indexOf("d0llarb") + 7);
                        if (twoSixFour.contains("d0llarc")) {
                            twoSixFour = twoSixFour.substring(0, twoSixFour.indexOf("d0llarc"));
                        }
                        publisher = publisher + twoSixFour.replaceAll("d0llarc", "").strip();   
                    }
                    publisher = publisher.replace("d0llara", " ");
                    publisher = publisher.replace("d0llarb", " ").trim();
                    if (publisher.endsWith(",")) {
                        publisher = publisher.substring(0, publisher.length() - 1);
                    }
   
                    creator = record.get("100");
                    if (!"".equals(creator)) {
                        creator = creator.replaceAll("\\$", "d0llar");
                        creator = creator.substring(creator.indexOf("d0llara") + 7);
                        //System.out.println(creator);
                        if (creator.contains("d0llar")) {
                            creator = creator.substring(0, creator.indexOf("d0llar")).strip();
                        }
                    }
                    if (creator.endsWith(",")) {
                        creator = creator.substring(0, creator.length() - 1);
                    }
                    
                    topic = record.get("610");
                    if (!"".equals(topic)) {
                        topic = topic.replaceAll("\\$", "d0llar");
                        topic = topic.substring(topic.indexOf("d0llara") + 7);
                        if (topic.contains("d0llar")) {
                            topic = topic.substring(0, topic.indexOf("d0llar")).strip();
                        }
                    }

                    String tempDescription = record.get("500");
                    description = "";
                    if (!"".equals(tempDescription)) {
                        String[] descriptions = tempDescription.split("\\\\\\$a");
                        for (String d : descriptions) {
                            //System.out.println(d);
                            description = description + d + "|";
                        }
                        description = description.replace("\\", "");
                        description = description.substring(1, description.length() - 1);
                    }

                    urlPDF = "https://dlcs.io/pdf/wellcome/pdf-item/b" + idLocal + "/0";

                    urlOther = "https://wellcomelibrary.org/service/fulltext/b" + idLocal + "/0?raw=true";

                    urlIIIF = "https://wellcomelibrary.org/iiif/b" + idLocal + "/manifest";

                    placeOfPublication = record.get("260");    
                    if (!"".equals(placeOfPublication)) {
                        placeOfPublication = placeOfPublication.replaceAll("\\$", "d0llar");
                        placeOfPublication = placeOfPublication.substring(placeOfPublication.indexOf("d0llara") + 7);
                        //System.out.println(placeOfPublication);
                        if (placeOfPublication.contains("d0llar")) {
                            placeOfPublication = placeOfPublication.substring(0, placeOfPublication.indexOf("d0llar")).strip();
                        }
                    }
                    twoSixFour = record.get("264");
                    if (!"".equals(twoSixFour)) {
                        twoSixFour = twoSixFour.replaceAll("\\$", "d0llar");
                        twoSixFour = twoSixFour.substring(twoSixFour.indexOf("d0llara") + 7);
                        if (twoSixFour.contains("d0llarb")) {
                            twoSixFour = twoSixFour.substring(0, twoSixFour.indexOf("d0llarb"));
                        }
                        placeOfPublication = placeOfPublication + twoSixFour;
                    }
                    placeOfPublication = placeOfPublication.replaceAll("d0llara", " ").strip();
                    placeOfPublication = placeOfPublication.replaceAll("d0llarc", " ").strip();
                    placeOfPublication = placeOfPublication.replaceAll("d0llard", " ").strip();
                    placeOfPublication = placeOfPublication.replaceAll("d0llare", " ").strip();
                    placeOfPublication = placeOfPublication.replaceAll("d0llarf", " ").strip();
                    placeOfPublication = placeOfPublication.replaceAll("d0llarg", " ").strip();
                    if ((placeOfPublication.endsWith(" :")) || (placeOfPublication.endsWith(" /"))) {
                        placeOfPublication = placeOfPublication.substring(0, placeOfPublication.length() - 2).trim();
                    }
                    
                    language = record.get("008");
                    if (("".equals(language)) || (language.length() < 38)) {
                        language = "Not specified";
                        //System.out.println(language);
                    } else {
                        language = MARC21LanguageCodeLookup.convert(language.substring(35, 38));     
                    }
                    //System.out.println(language);
                    
                    // Download the manifest
                    licence = "";
                    String prefix = "c:/otw/manifests/wellcome/";
                    DownloadIIIFManifest.get(urlIIIF, prefix);
                    try {
			List<String> allLines = Files.readAllLines(Paths.get(prefix + urlIIIF.replaceAll("/", "_").replaceAll(":", "-")));
			for (String line : allLines) {
                            line = line.trim();
                            if (line.startsWith("\"license\"")) {
                                licence = line.split(" ")[1].trim();
                                licence = licence.substring(1, licence.length() - 2);
                                //System.out.println("Licence: " + licence);
                                break;
                            }
			}
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }

                    idOther = "";
                    
                    // Generate the catalogue link
                    catLink = "https://search.wellcomelibrary.org/iii/encore/record/C__Rb" + idLocal.substring(0, 7) + "?lang=eng";

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
