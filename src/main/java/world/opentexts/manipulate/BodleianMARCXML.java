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
 * A manipulation tool to convert Bodleian data feeds into the OpenTexts.World CSV format
 * 
 * @author Stuart Lewis
 */
public class BodleianMARCXML {

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
                    //.withHeader("RecordID", "DateAdded", "DateChanged", "Author", "Title", "CopyrightDate", "Barcode", "Classification", "MainEntry", "Custom1", "Custom2", "Custom3", "Custom4", "Custom5", "ImportErrors", "ValidationErrors", "TagNumber", "Ind1", "Ind2", "ControlData", "Sort", "LDR", "001", "003", "005", "006", "008", "007", "010", "016", "028", "033", "034", "035", "015", "017", "020", "024", "029", "037", "039", "040", "041", "080", "092", "111", "090", "100", "025", "038", "043", "055", "070", "245", "250", "042", "045", "047", "048", "049", "050", "051", "052", "060", "066", "099", "110", "210", "240", "242", "260", "072", "082", "086", "093", "095", "130", "246", "300", "243", "255", "264", "490", "590", "856", "504", "581", "800", "084", "336", "502", "518", "535", "550", "630", "700", "247", "256", "310", "337", "539", "541", "655", "730", "263", "338", "362", "501", "505", "534", "545", "610", "650", "740", "772", "506", "520", "536", "540", "585", "588", "751", "773", "961", "530", "562", "611", "321", "340", "500", "516", "522", "525", "533", "753", "009", "011", "583", "600", "652", "690", "710", "938", "059", "651", "752", "770", "515", "538", "776", "810", "830", "880", "900", "935", "546", "561", "711", "775", "956", "212", "508", "510", "720", "902", "959", "960", "774", "777", "787", "947", "765", "907", "079", "648", "945", "653", "940", "096", "580", "780", "785")
                    .withHeader("RecordID", "DateAdded", "DateChanged", "Author", "Title", "CopyrightDate", "Barcode", "Classification", "MainEntry", "Custom1", "Custom2", "Custom3", "Custom4", "Custom5", "ImportErrors", "ValidationErrors", "TagNumber", "Ind1", "Ind2", "ControlData", "Sort", "LDR", "001", "003", "005", "006", "007", "008", "010", "015", "016", "018", "019", "024", "012", "017", "020", "028", "029", "035", "022", "025", "030", "066", "032", "038", "044", "240", "250", "059", "099", "256", "037", "047", "049", "080", "084", "086", "092", "111", "045", "048", "055", "089", "090", "093", "096", "247", "255", "264", "365", "538", "590", "041", "042", "060", "243", "490", "655", "043", "051", "070", "087", "100", "679", "800", "039", "069", "074", "336", "350", "505", "530", "690", "040", "072", "337", "340", "440", "506", "514", "130", "245", "338", "410", "501", "516", "563", "752", "772", "776", "830", "909", "961", "082", "246", "347", "503", "504", "561", "770", "110", "260", "321", "534", "581", "600", "852", "950", "050", "300", "509", "720", "853", "919", "990", "310", "547", "362", "730", "751", "760", "969", "500", "956", "810", "955", "515", "546", "991", "998", "212", "510", "960", "995", "996", "520", "588", "540", "939", "522", "533", "539", "611", "777", "787", "994", "210", "525", "550", "780", "890", "938", "222", "630", "935", "941", "580", "762", "555", "610", "740", "775", "011", "650", "936", "981", "651", "711", "929", "700", "710", "774", "583", "785", "886", "940", "856", "912", "978", "850", "999", "880")
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
                    
                    //"$aThe works of Mr. Thomas Brown, in prose and verse :$bserious, moral, and comical /$cTo which is prefix'd, A character of Mr. Tho. Brown and his writings, by James Drake"
                    title = record.get("245");
                    //System.out.println("TITLE = " + title);
                    if (idLocal.equals("012768846")) title = "O národních písních a pověstech plemen slovanských.";
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
                    if (idLocal.equals("017253842")) title = "Marj al-Baḥrain fī faz̤āʼil al-Ḥaramain."; 
                    if ("".equals(title)) {
                        System.err.println("Skipping item: " + idLocal + " as it doesn't have a title"); 
                        continue;
                    }
                    title = title.replaceAll("\\$", "d0llar");
                    title = title.substring(7);
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
                        title = title.substring(0, title.length() - 2).trim();
                    }
                    
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
                    year = record.get("CopyrightDate");

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
                    publisher = publisher.replace("d0llarb", " ");
   
                    creator = record.get("100");
                    if (!"".equals(creator)) {
                        creator = creator.replaceAll("\\$", "d0llar");
                        creator = creator.substring(creator.indexOf("d0llara") + 7);
                        //System.out.println(creator);
                        if (creator.contains("d0llar")) {
                            creator = creator.substring(0, creator.indexOf("d0llar")).strip();
                        }
                    }
                    
                    topic = record.get("610");
                    if (!"".equals(topic)) {
                        topic = topic.replaceAll("\\$", "d0llar");
                        topic = topic.substring(topic.indexOf("d0llara") + 7);
                        if (topic.contains("d0llar")) {
                            topic = topic.substring(0, topic.indexOf("d0llar")).strip();
                        }
                    }

                    description = record.get("500");
                    if (!"".equals(description)) {
                        description = description.replaceAll("\\$a", "|").substring(1);
                    }

                    // Select the last URL if there are multiple
                    urlPDF = "";

                    // Select the last URL if there are multiple
                    urlOther = "";

                    // Select the last URL if there are multiple
                    urlIIIF = "";

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
                    
                    // Select the first licence if there are multiple
                    licence = "";

                    idOther = "";
                    
                    // Generate the catalogue link
                    catLink = "http://solo.bodleian.ox.ac.uk/permalink/f/89vilt/oxfaleph" + idLocal ;

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
