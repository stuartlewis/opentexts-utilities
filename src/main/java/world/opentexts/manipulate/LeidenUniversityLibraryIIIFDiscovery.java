/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world.opentexts.manipulate;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import world.opentexts.util.DownloadIIIFManifest;
import world.opentexts.validate.Validator;

/**
 * Harvest Leiden content via IIIF manifest traversal and discovery
 * 
 * @author Stuart Lewis
 */
public class LeidenUniversityLibraryIIIFDiscovery {
    
    public static void main(String[] args) throws Exception {
        // Whether to show logging or not
        boolean debug = true;

        // Open the output CSV
        String outFilename = "c:\\otw\\leiden-iiif.csv";
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

        // 1) Iterate through collections
        String top = "https://view.nls.uk/collections/top.json";
        String prefix = "c:/otw/manifests/leiden/";
        
        int counter = 0;
        
//DownloadIIIFManifest.get(top, prefix, true);
        
        //JSONParser discoveryParser = new JSONParser();
        //JSONObject discoveryJson = (JSONObject)discoveryParser.parse(new FileReader(prefix + top.replaceAll("/", "_").replaceAll(":", "-")));
        //JSONArray collections = (JSONArray)discoveryJson.get("collections");
        //Iterator<JSONObject> collectionsIterator = collections.iterator();
        
        //while (collectionsIterator.hasNext()) {
            // 2) Iterate through items in each collection
            //JSONObject jsonCollection = collectionsIterator.next();
            //String collectionURL = (String)jsonCollection.get("@id");
            String collectionURL = "https://digitalcollections.universiteitleiden.nl/rarebooks/datastream/MANIFEST/view";
            String collectionLabel = "Rare Books";
            if (debug) System.out.println("Parsing collection: " + collectionURL + " (" + collectionLabel + ")");
            DownloadIIIFManifest.get(collectionURL, prefix, true, 10);
            
            JSONParser collectionsParser = new JSONParser();
            JSONObject collectionJson = (JSONObject)collectionsParser.parse(new FileReader(prefix + collectionURL.replaceAll("/", "_").replaceAll(":", "-"), StandardCharsets.UTF_8));
            JSONArray manifests = (JSONArray)collectionJson.get("members");
            if (manifests != null) {
                Iterator<JSONObject> manifestsIterator = manifests.iterator();
                while (manifestsIterator.hasNext()) {
                    // 3) Process item
                    JSONObject jsonManifest = manifestsIterator.next();
                    String manifestURL = (String)jsonManifest.get("@id");
                    String manifestTitle = (String)jsonManifest.get("label");
                    
                    if (debug) System.out.println("Parsing item (" + counter++ + "): " + manifestURL + " (" + manifestTitle + ")");
                    boolean got = DownloadIIIFManifest.get(manifestURL, prefix, true, 10);
                    if (!got){    
                        System.err.println("Skipping: " + manifestURL);
                        continue;
                    }
                    
                    JSONParser itemParser = new JSONParser();
                    JSONObject itemJson = (JSONObject)itemParser.parse(new FileReader(prefix + manifestURL.replaceAll("/", "_").replaceAll(":", "-")));
                                        
                    String organisation = "", idLocal = "", title = "", urlMain = "", year = "", date = "",
                           publisher = "", creator = "", topic = "", description = "", urlPDF = "", 
                           urlIIIF = "", urlPlainText = "", urlALTOXML = "", urlTEI = "", urlOther = "", 
                           placeOfPublication = "", licence = "", idOther = "", catLink = "", language = "";

                    organisation = "Leiden University Libraries";
                    
                    idLocal = manifestURL.substring(0, manifestURL.lastIndexOf('/'));
                    idLocal = idLocal.substring(idLocal.lastIndexOf(':') + 1);
                    if (debug) System.out.println(" - idLocal = " + idLocal);
                    
                    title = manifestTitle.replaceAll("\\|", "");
                    if (debug) System.out.println(" - title = " + title);
                    
                    urlMain = "http://hdl.handle.net/1887.1/item:" + idLocal;
                    if (debug) System.out.println(" - urlMain = " + urlMain);
                    
                    urlIIIF = manifestURL;
                    if (debug) System.out.println(" - urlIIIF = " + urlIIIF);
                    
                    licence = (String)itemJson.get("license");
                    if ((licence != null) && (licence.contains("http"))) {
                        licence = licence.substring(licence.indexOf("http"));
                        licence = licence.substring(0, licence.indexOf(">") - 1);
                        if (debug) System.out.println(" - licence = " + licence);
                    } else {
                        licence = "";
                    }
                    
                    if (itemJson.get("metadata") instanceof JSONObject) {
                        continue;
                    }
                    JSONArray metadata = (JSONArray)itemJson.get("metadata");
                    Iterator<JSONObject> metadataIterator = metadata.iterator();
                    while (metadataIterator.hasNext()) {
                        JSONObject metadataElement = metadataIterator.next();
                        //System.out.println("XX " + metadataElement.toJSONString());
                        
                        String label = (String)metadataElement.get("label");
                        String value = "";
                        try {
                            value = (String)metadataElement.get("value");
                        } catch (ClassCastException cce) {
                            Object inner = (Object)metadataElement.get("value");
                            if (inner instanceof JSONArray) {
                                Iterator<JSONObject> innerIterator = ((JSONArray)inner).iterator();
                                while (innerIterator.hasNext()) {
                                    value = value + "|" + innerIterator.next();
                                }
                                // Strip the leading pipe
                                value = value.substring(1);
                            } else if (inner instanceof JSONObject) {
                                value = (String)((JSONObject)inner).get("@value");
                            }
                        }
                        
                        if (("Language".equals(label))) {
                            language = value;
                            if (debug) System.out.println(" - language = " + language);
                        } else if (("Date printed".equals(label)) || ("Date published".equals(label))) {
                            date = value;
                            if (debug) System.out.println(" - date = " + date);
                            year = date;
                            try {
                                int y = Integer.parseInt(date);
                                if ((y < 1000) || (y > 2025)){
                                    year = "";
                                } 
                            } catch (NumberFormatException e) {
                                year = "";
                            }
                            if (debug) System.out.println(" - year = " + year);
                        } else if ("Author/creator".equals(label)) {
                            creator = value;
                            if (debug) System.out.println(" - creator = " + creator);
                        } else if ("Publisher".equals(label)) {
                            publisher = value;
                            if (debug) System.out.println(" - publisher = " + publisher);
                        } else if ("Subject/content".equals(label)) {
                            if (!"".equals(topic)) topic = topic + "|";
                            topic = topic + value;
                            if (debug) System.out.println(" - topic = " + topic);
                        } else if ("Description".equals(label)) {
                            if (!"".equals(description)) description = description + "|";
                            description = description + value;
                            if (debug) System.out.println(" - description = " + topic);
                        } else if ("Published".equals(label)) {
                            placeOfPublication = value;
                            if (debug) System.out.println(" - placeOfPublication = " + placeOfPublication);
                            
                            // Try and find a year
                            Pattern pattern = Pattern.compile("(\\d{4})");
                            Matcher matcher = pattern.matcher(placeOfPublication);
                            if (matcher.find()) {
                                year = matcher.group(1);
                                date = year;
                                if (debug) System.out.println(" - year = " + year);
                            }
                        } 
                    }
                    
                    // Remove year from the published field if we can
                    if (placeOfPublication.endsWith(", " + year)) {
                        placeOfPublication = placeOfPublication.substring(0, placeOfPublication.length() - 6);
                        if (debug) System.out.println(" - NEW placeOfPublication = " + placeOfPublication);
                    } else if (placeOfPublication.endsWith(", " + year + ".")) {
                        placeOfPublication = placeOfPublication.substring(0, placeOfPublication.length() - 7);
                        if (debug) System.out.println(" - NEW placeOfPublication = " + placeOfPublication);
                    }
                    
                    if (description.endsWith("|")) description = description.substring(0, description.length() - 1);
                    
                    JSONArray sequences = (JSONArray)itemJson.get("sequences");
                    Iterator<JSONObject> sequencesIterator = sequences.iterator();
                    while (sequencesIterator.hasNext()) {
                        JSONObject thing = sequencesIterator.next();
                        JSONArray renderingArray = (JSONArray)thing.get("rendering");
                        if (renderingArray != null) {
                            JSONObject rendering = (JSONObject)renderingArray.get(0);
                            urlPDF = (String)rendering.get("@id");
                            if (debug) System.out.println(" - urlPDF = " + urlPDF); 
                        }
                    } 
                    
                    urlPlainText = "https://digitalcollections.universiteitleiden.nl/view/item/" + idLocal + "/datastream/OCR/download";
                    if (!checkURL(urlPlainText)) urlPlainText = "";
                    if (debug) System.out.println(" - urlPlainText = " + urlPlainText);
                    
                    urlPDF = "https://digitalcollections.universiteitleiden.nl/view/item/" + idLocal + "/datastream/PDF/download";
                    if (!checkURL(urlPDF)) urlPDF = "";
                    if (debug) System.out.println(" - urlPDF = " + urlPDF);
                    
                    // Write the item to the CSV file
                    csvPrinter.printRecord(Arrays.asList(organisation, idLocal, title,
                                                         urlMain, year, date, publisher,
                                                         creator, topic, description,
                                                         urlPDF, urlIIIF, urlPlainText, urlALTOXML, 
                                                         urlTEI, urlOther, 
                                                         placeOfPublication, licence, idOther,
                                                         catLink, language));
                }
            }
        //}
        
        System.out.println("Writing file: " + outFilename);
        csvPrinter.flush();
        csvPrinter.close();

        // Run the validator
        Validator v = new Validator(outFilename);
    }
    
    private static boolean checkURL(String u) {
        try {
            URL url = new URL(u);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int code = connection.getResponseCode();
            if ((code >= 200) && (code < 300)) return true;
        } catch (IOException ioe) {
                return false;
        }
        return false;
    }
}
