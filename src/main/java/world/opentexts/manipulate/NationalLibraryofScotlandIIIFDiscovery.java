/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world.opentexts.manipulate;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import world.opentexts.util.DownloadIIIFManifest;
import world.opentexts.validate.Validator;

/**
 * Harvest NLS content via IIIF manifest traversal and discovery
 * 
 * @author Stuart Lewis
 */
public class NationalLibraryofScotlandIIIFDiscovery {
    
    public static void main(String[] args) throws Exception {
        // Whether to show logging or not
        boolean debug = false;

        // Open the output CSV
        String outFilename = "c:\\otw\\nls-iiif-clean.csv";
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

        // 1) Iterate through collections
        String top = "https://view.nls.uk/collections/top.json";
        String prefix = "c:/otw/manifests/nls/";
        DownloadIIIFManifest.get(top, prefix, true);
        
        JSONParser discoveryParser = new JSONParser();
        JSONObject discoveryJson = (JSONObject)discoveryParser.parse(new FileReader(prefix + top.replaceAll("/", "_").replaceAll(":", "-")));
        JSONArray collections = (JSONArray)discoveryJson.get("collections");
        Iterator<JSONObject> collectionsIterator = collections.iterator();
        while (collectionsIterator.hasNext()) {
            // 2) Iterate through items in each collection
            JSONObject jsonCollection = collectionsIterator.next();
            String collectionURL = (String)jsonCollection.get("@id");
            String collectionLabel = (String)jsonCollection.get("label");
            if (debug) System.out.println("Parsing collection: " + collectionURL + " (" + collectionLabel + ")");
            DownloadIIIFManifest.get(collectionURL, prefix, true);
            
            JSONParser collectionsParser = new JSONParser();
            JSONObject collectionJson = (JSONObject)collectionsParser.parse(new FileReader(prefix + collectionURL.replaceAll("/", "_").replaceAll(":", "-")));
            JSONArray manifests = (JSONArray)collectionJson.get("manifests");
            if (manifests != null) {
                Iterator<JSONObject> manifestsIterator = manifests.iterator();
                while (manifestsIterator.hasNext()) {
                    // 3) Process item
                    JSONObject jsonManifest = manifestsIterator.next();
                    String manifestURL = (String)jsonManifest.get("@id");
                    String manifestTitle = (String)jsonManifest.get("label");
                    if (debug) System.out.println("Parsing item: " + manifestURL + " (" + manifestTitle + ")");
                    DownloadIIIFManifest.get(manifestURL, prefix, true);
                    
                    JSONParser itemParser = new JSONParser();
                    JSONObject itemJson = (JSONObject)itemParser.parse(new FileReader(prefix + manifestURL.replaceAll("/", "_").replaceAll(":", "-")));
                                        
                    String organisation = "", idLocal = "", title = "", urlMain = "", year = "",
                           publisher = "", creator = "", topic = "", description = "", urlPDF = "", 
                           urlOther = "", urlIIIF = "", placeOfPublication = "", licence = "", idOther = "",
                           catLink = "", language = "";

                    organisation = "National Library of Scotland";
                    
                    idLocal = manifestURL.substring(0, manifestURL.lastIndexOf('/'));
                    idLocal = idLocal.substring(idLocal.lastIndexOf('/') + 1);
                    if (debug) System.out.println(" - idLocal = " + idLocal);
                    
                    title = manifestTitle.replaceAll("\\|", "");
                    if (debug) System.out.println(" - title = " + title);
                    
                    urlMain = "https://digital.nls.uk/" + idLocal;
                    if (debug) System.out.println(" - urlMain = " + urlMain);
                    
                    urlIIIF = manifestURL;
                    
                    licence = (String)itemJson.get("attribution");
                    if ((licence != null) && (licence.contains("http"))) {
                        licence = licence.substring(licence.indexOf("http"));
                        licence = licence.substring(0, licence.indexOf("\""));
                        if (debug) System.out.println(" - licence = " + licence);
                    } else {
                        licence = "";
                    }
                    
                    language = "Not specified";
                     
                    JSONArray metadata = (JSONArray)itemJson.get("metadata");
                    Iterator<JSONObject> metadataIterator = metadata.iterator();
                    while (metadataIterator.hasNext()) {
                        JSONObject metadataElement = metadataIterator.next();
                        
                        String label = (String)metadataElement.get("label");
                        String value = (String)metadataElement.get("value");
                        
                        if (("Date printed".equals(label)) || ("Date published".equals(label))) {
                            year = value;
                            if (debug) System.out.println(" - year = " + value);
                        } else if ("Author".equals(label)) {
                            creator = value;
                            if (debug) System.out.println(" creator = " + creator);
                        } else if ("Publisher".equals(label)) {
                            publisher = value;
                            if (debug) System.out.println(" publisher = " + publisher);
                        } else if ("Subject/content".equals(label)) {
                            if (!"".equals(topic)) topic = topic + "|";
                            topic = topic + value;
                            if (debug) System.out.println(" - topic = " + topic);
                        } else if ("Description".equals(label)) {
                            if (!"".equals(description)) description = description + "|";
                            description = description + value;
                            if (debug) System.out.println(" - description = " + topic);
                        } 
                    }
                    
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
                    
                    // Write the item to the CSV file
                    csvPrinter.printRecord(Arrays.asList(organisation, idLocal, title,
                                                         urlMain, year, publisher,
                                                         creator, topic, description,
                                                         urlPDF, urlOther, urlIIIF,
                                                         placeOfPublication, licence, idOther,
                                                         catLink, language));
                }
            }
        }
        
        System.out.println("Writing file: " + outFilename);
        csvPrinter.flush();
        csvPrinter.close();

        // Run the validator
        Validator v = new Validator(outFilename);
    }
}
