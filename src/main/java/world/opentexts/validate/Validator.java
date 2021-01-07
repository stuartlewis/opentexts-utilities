/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world.opentexts.validate;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 * Validate an import file against the OpenTexts.World specification
 * 
 * @author Stuart Lewis
 * 
 * @see <a href="https://opentexts.world/contribute">https://opentexts.world/contribute</a>>
 */
public class Validator {
    
    public Validator(String filename) throws Exception {
        // Open the CSV
        System.out.println("Validating file: " + filename + "\n");
        Reader in = new FileReader(filename);
        
        // Setup some variables
        boolean header = true;
        ArrayList<String> headings = new ArrayList<String>();
        String org = null;
        int lineCounter = 0;
        
        // Process each line
        for (CSVRecord record : CSVFormat.DEFAULT.parse(in)) {
            lineCounter++;            
            
            // First, skip the header if needed
            if (header) {
                header = false;
                // Store the headings
                for (String field : record) {
                    headings.add(field);
                    System.out.println(" - Adding heading (" + headings.size() + "): " + field);
                
                    // Check the headings
                    String expected = "";
                    switch(headings.size()) {
                        case 1: expected = "organisation";
                                break;
                        case 2: expected = "idLocal";
                                break;
                        case 3: expected = "title";
                                break;
                        case 4: expected = "urlMain";
                                break;
                        case 5: expected = "year";
                                break;
                        case 6: expected = "date";
                                break;
                        case 7: expected = "publisher";
                                break;
                        case 8: expected = "creator";
                                break;
                        case 9: expected = "topic";
                                break;
                        case 10: expected = "description";
                                break;
                        case 11: expected = "urlPDF";
                                break;
                        case 12: expected = "urlIIIF";
                                break;
                        case 13: expected = "urlPlainText";
                                break;
                        case 14: expected = "urlALTOXML";
                                break;
                        case 15: expected = "urlTEI";
                                break;
                        case 16: expected = "urlOther";
                                break;
                        case 17: expected = "placeOfPublication";
                                break;
                        case 18: expected = "licence";
                                break;
                        case 19: expected = "idOther";
                                break;
                        case 20: expected = "catLink";
                                break;
                        case 21: expected = "language";
                                break;
                        default: expected = "$.$";
                                System.err.println("  - ERROR: Incorrect headings - too many fields");
                                System.err.println("  - There is likely data in this column, with no heading");
                                System.exit(0);
                                break;
                    }
                    if (!expected.equals(field)) {
                        System.err.println("  - ERROR: Expect heading '" + expected + "' in heading position " + headings.size());
                        System.exit(0);
                    }        
                }
            } else {
                // Now check each field meets the requirements
                int position = 0;
                //System.out.println(record);
                for (String value : record) {
                    // Set the organisation if required
                    if (org == null) {
                        org = value;
                        System.out.println(" - Setting organisation: " + org);
                    }
                    
                    switch(++position) {
                        case 1: //organisation
                            if (!org.equals(value)) {
                                System.err.println(" - ERROR on line " + lineCounter);
                                System.err.println("  - Organsiation ('" + value + "') should be '" + org + "'");
                                System.exit(0);
                            }
                            this.checkNonRepeatable(lineCounter, "organisation", value, record);
                            break;
                        case 2: // idLocal
                            this.checkMandatory(lineCounter, "idLocal", value, record);
                            this.checkNonRepeatable(lineCounter, "idLocal", value, record);
                            break;
                        case 3: // title
                            this.checkMandatory(lineCounter, "title", value, record);
                            this.checkNonRepeatable(lineCounter, "title", value, record);
                            break;
                        case 4: // urlMain
                            this.checkMandatory(lineCounter, "urlMain", value, record);
                            this.checkNonRepeatable(lineCounter, "urlMain", value, record);
                            this.checkIsHttp(lineCounter, "urlMain", value, record);
                            break;
                        case 5: // year
                            this.checkNonRepeatable(lineCounter, "year", value, record);
                            this.checkYear(lineCounter, "year", value, record);
                            break;
                        case 6: // date
                            this.checkNonRepeatable(lineCounter, "date", value, record);
                            break;
                        case 7: // publisher
                            break;
                        case 8: // creator
                            break;
                        case 9: // topic
                            break;
                        case 10: // description
                            break;
                        case 11: // urlPDF
                            this.checkNonRepeatable(lineCounter, "urlPDF", value, record);
                            this.checkIsHttp(lineCounter, "urlPDF", value, record);
                            break;
                        case 12: // urlIIIF
                            this.checkNonRepeatable(lineCounter, "urlIIIF", value, record);
                            this.checkIsHttp(lineCounter, "urlIIIF", value, record);
                            break;
                        case 13: // urlPlainText
                            this.checkNonRepeatable(lineCounter, "urlPlainText", value, record);
                            this.checkIsHttp(lineCounter, "urlPlainText", value, record);
                            break;
                        case 14: // urlALTOXML
                            this.checkNonRepeatable(lineCounter, "urlALTOXML", value, record);
                            this.checkIsHttp(lineCounter, "urlALTOXML", value, record);
                            break;
                        case 15: // urlTEI
                            this.checkNonRepeatable(lineCounter, "urlTEI", value, record);
                            this.checkIsHttp(lineCounter, "urlTEI", value, record);
                            break;
                        case 16: // urlOther
                            break;
                        case 17: // placeOfPublication
                            break;
                        case 18: // licence
                            this.checkNonRepeatable(lineCounter, "licence", value, record);
                            break;
                        case 19: // idOther
                            break;
                        case 20: // catLink
                            this.checkNonRepeatable(lineCounter, "catLink", value, record);
                            this.checkIsHttp(lineCounter, "catLink", value, record);
                            break;
                        case 21: // language
                            this.checkNonRepeatable(lineCounter, "language", value, record);
                            break;
                        default: // Something has gone wrong
                            System.err.println(" - ERROR on line " + lineCounter);
                            System.err.println("  - Unexpected value (" + value + ") - too many fields");
                            System.exit(0);
                            break;
                    }
                }
            }
        }
        
        // Report success!
        System.out.println("\n - SUCCESS!");
        System.out.println("   - Processed " + lineCounter + " records\n");
    }
    
    private void checkMandatory(int lineCounter, String field, String value, CSVRecord record) {
        if (value.equals("")) {
            System.err.println(" - ERROR on line " + lineCounter);
            System.err.println("  - " + field + " cannot be empty, it is a MANDATORY field");
            System.err.println(record);
            System.exit(0);
        }
    }
    
    private void checkNonRepeatable(int lineCounter, String field, String value, CSVRecord record) {
        if (value.contains("|")) {
            System.err.println(" - ERROR on line " + lineCounter);
            System.err.println("  - " + field + " is not a repeatable field:");
            System.err.println("  - " + value);
            System.err.println(record);
            System.exit(0);
        }
    }
    
    private void checkIsHttp(int lineCounter, String field, String value, CSVRecord record) {
        if ((!value.startsWith("http")) && (!"".equals(value))) {
            System.err.println(" - ERROR on line " + lineCounter);
            System.err.println("  - " + field + " does not look like a http/s link:");
            System.err.println("  - " + value);
            System.err.println(record);
            System.exit(0);
        }
    }
    
    private void checkYear(int lineCounter, String field, String value, CSVRecord record) {
        if (value.equals("")) return;
        try {
            int y = Integer.parseInt(value);
            if (y < -550) {
                System.err.println(" - ERROR on line " + lineCounter);
                System.err.println("  - " + field + " is < 250:");
                System.err.println("  - " + value);
                System.err.println(record);
                System.exit(0);
            }
            if (y > 2025) {
                System.err.println(" - ERROR on line " + lineCounter);
                System.err.println("  - " + field + " is > 2025:");
                System.err.println("  - " + value);
                System.err.println(record);
                System.exit(0);
            }    
        } catch (NumberFormatException e) {
            System.err.println(" - ERROR on line " + lineCounter);
            System.err.println("  - " + field + " is not a number:");
            System.err.println("  - '" + value + "'");
            System.err.println(record);
            System.exit(0);
        }
    }
    
    public static void main(String[] args) {
        // Take the filename as the only parameter
        /**if (args.length < 1) {
            System.err.println("Please supply a filename to validate");
            System.exit(0);
        }*/
        
        try {
            Validator v = new Validator("c:\\otw\\cul.csv");   
        } catch (Exception e) {
            System.err.println("Error encountered:");
            System.err.println(" - " + e.getMessage() + "\n");
            e.printStackTrace();
            System.exit(0);
        }
    }
}