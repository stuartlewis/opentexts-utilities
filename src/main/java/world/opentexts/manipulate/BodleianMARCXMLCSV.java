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
 * A manipulation tool to convert Bodleian data feeds into the OpenTexts.World CSV format
 * 
 * @author Stuart Lewis
 */
public class BodleianMARCXMLCSV {

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
                    .withHeader("907", "245", "856", "260", "264", "100", "610", "500", "008", "001")
                    .withIgnoreHeaderCase()
                    .withTrim());
            
            // Process each line
            for (CSVRecord record : csvParser) {
                if (!header) {
                    System.out.println(" - Processing header");
                    header = true;
                } else {
                    organisation = "Bodleian Libraries";

                    idLocal = record.get("001");
                    //System.out.println("Item: " + idLocal);
                    
                    title = record.get("245").substring(2);
                    //System.out.println("TITLE = " + title);
                    /**if (idLocal.equals("012768846")) title = "O národních písních a pověstech plemen slovanských.";
                    if (idLocal.equals("012943621")) title = "Poesias.";
                    if (idLocal.equals("013253779")) title = "Sochinenīi͡a."; 
                    if (idLocal.equals("013356385")) title = "Russkīe ugolovnye prot͡sessy.";
                    if (idLocal.equals("013366578")) title = "Opisanīe dokumentov i bumag, khrani͡ashchikhsi͡a v b. Moskovskom arkhivi͡e Ministerstva i͡ustit͡sīi."; 
                    if (idLocal.equals("014558923")) title = "Nuovo dizionario italiano-armeno-turco."; 
                    if (idLocal.equals("014857596")) title = "The sarcophagus of Ānchnesrāneferȧb [ed.] by E.A.W. Budge."; 
                    if (idLocal.equals("015329247")) title = "Sefinet uş-şuara."; 
                    if (idLocal.equals("015564513")) title = "Aushadhisaṅgraha kalpavallī."; 
                    if (idLocal.equals("015683598")) title = "Padmāvatī khaṇḍa, arthāt, Dillī ke rājā Pr̥thvī Rāja ne atyanta yuddha kara rānī Padmāvatī ke sātha vivāha kiyā, aura, Ālhakhaṇḍa, jisameṃ Ālhā Ūdala kā saṅgrāma Pr̥thvī Rājase, ati uttama chanda meṃ racita hai."; 
                    if (idLocal.equals("015760729")) title = "Padārthavidyāsāra."; 
                    if (idLocal.equals("015764886")) title = "Śaṅkara carita sudhā"; 
                    if (idLocal.equals("015782924")) title = "Satyanāma Bihāra Bindrāvana."; 
                    if (idLocal.equals("015782928")) title = "Samīra Vihāra Vindrāvana."; 
                    if (idLocal.equals("016103195")) title = "Nithān thīap suphāsit."; 
                    if (idLocal.equals("016152713")) title = "T̤ot̤a kahānī."; 
                    if (idLocal.equals("016238458")) title = "Kulliyāt."; 
                    if (idLocal.equals("016527175")) title = "Kulliyāt-i Naẓīr Akbarābādī."; 
                    if (idLocal.equals("016653343")) title = "Guldastah-yi karāmat."; 
                    if (idLocal.equals("016663082")) title = "Armag̲ẖān-i dost.";
                    if (idLocal.equals("016688985")) title = "Qavāʿid-i Urdū."; 
                    if (idLocal.equals("016703257")) title = "Mavāʿiẓ-i Ḥaidariyah."; 
                    if (idLocal.equals("016706369")) title = "Bārān māh mālī o bulbul."; 
                    if (idLocal.equals("016732319")) title = "Mas̲navī-yi sirr-i ḥaqq."; 
                    if (idLocal.equals("016822873")) title = "Qiṣṣah-yi māhīgīr."; 
                    if (idLocal.equals("016851752")) title = "Sair-i Maqbūl."; 
                    if (idLocal.equals("016919300")) title = "Siyar al-aqt̤āb."; 
                    if (idLocal.equals("016932401")) title = "Bahāristān-i tārīḵẖ, maʿrūf bah, Gulzār-i shāhī."; 
                    if (idLocal.equals("016935377")) title = "Bikaṭ kahānī."; 
                    if (idLocal.equals("016943958")) title = "Bārah māsah, mausūm bah, Birah bārish."; 
                    if (idLocal.equals("016954452")) title = "Farhang-i ʿishq."; 
                    if (idLocal.equals("016959840")) title = "Gulshan-i beḵẖār."; 
                    if (idLocal.equals("016959923")) title = "Tarjumah-yi Ḥadāʼiq al-balāg̲ẖat."; 
                    if (idLocal.equals("016995799")) title = "Dīvān-i Vāst̤ī."; 
                    if (idLocal.equals("017009334")) title = "Gulshan-i Sarvarī."; 
                    if (idLocal.equals("017009404")) title = "Jangnāmah-yi Karbalā."; 
                    if (idLocal.equals("017018625")) title = "Gulistān-i beḵẖizāṉ, maʿrūf bah Nag̲ẖmah-yi ʿandalīb."; 
                    if (idLocal.equals("017019006")) title = "Naʿt-i Sarvarī."; 
                    if (idLocal.equals("017038664")) title = "Surmah-yi bīnish."; 
                    if (idLocal.equals("017080229")) title = "Tuḥfah-yi Sarvarī."; 
                    if (idLocal.equals("017229434")) title = "Banjārah nāmah."; 
                    if (idLocal.equals("017230023")) title = "Farhang-i Dastūr al-ṣibyān."; 
                    if (idLocal.equals("017253838")) title = "Manhaj al-najāh."; 
                    if (idLocal.equals("017253842")) title = "Marj al-Baḥrain fī faz̤āʼil al-Ḥaramain."; */
                    if ("".equals(title)) {
                        System.err.println("Skipping item: " + idLocal + " as it doesn't have a title"); 
                        continue;
                    }
                    title = title.replaceAll("\\$", "d0llar");
                    //title = title.substring(7);
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
                    if ((title.endsWith(" :")) || (title.endsWith(" /"))) {
                        title = title.substring(0, title.length() - 2);
                    }
                    title = title.trim();
                    //System.out.println("Title: " + title);
                    
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
                    if (urlMain.contains(";")) { urlMain = urlMain.substring(0, urlMain.indexOf(";")); }
                    
                    // Select the first year if there are multiple
                    year = record.get("008");
                    if (("".equals(year)) || (year.length() < 12)) {
                        year = "";
                    } else {
                        year = year.substring(7, 11);     
                        if (year.contains("\\\\")) {
                                year = "";
                        }
                    }
                    date = year;
                    date = date.replaceAll("[|]", "0");
                    if (year.contains("uuu")) { year = ""; }
                    year = year.replaceAll("u", "0");
                    year = year.replaceAll("[|]", "0");
                    try {
                        int y = Integer.parseInt(year);
                        if ((y < 1000) || (y > 2025)){
                            year = "";
                        } 
                    } catch (NumberFormatException e) {
                        year = "";
                    }
                    //System.out.println(" - Year: " + date + " / " + year);
                                                           
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

                    // Select the last URL if there are multiple
                    urlPDF = "";

                    // Select the last URL if there are multiple
                    urlIIIF = "";
                    
                    // Select the last URL if there are multiple
                    urlPlainText = "";

                    // Select the last URL if there are multiple
                    urlALTOXML = "";
                    
                    // Select the last URL if there are multiple
                    urlOther = "";

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
                    
                    licence = "";

                    idOther = "";
                    
                    // Generate the catalogue link
                    catLink = "http://solo.bodleian.ox.ac.uk/permalink/f/89vilt/oxfaleph" + idLocal ;
                    
                    //System.out.println(idLocal);
                    csvPrinter.printRecord(Arrays.asList(organisation, idLocal, title,
                                                         urlMain, year, date, publisher,
                                                         creator, topic, description,
                                                         urlPDF, urlIIIF, urlPlainText, urlALTOXML, urlOther,
                                                         placeOfPublication, licence, idOther,
                                                         catLink, language));
                    
                    System.out.println(lineCounter++);
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
