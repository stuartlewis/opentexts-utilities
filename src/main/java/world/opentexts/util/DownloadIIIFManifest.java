/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world.opentexts.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Download (and cache) a IIIF manifest
 * 
 * @author Stuart Lewis
 */
public class DownloadIIIFManifest {
    
    public static boolean get(String url, String prefix, boolean complete, int sleep) {
        String filename = url.replaceAll("/", "_").replaceAll(":", "-");
        //System.out.println(filename);
        File manifest = new File(prefix + filename);
        
        // If it isn't in the cache...
        if (!manifest.exists()) {
            try {
                // Sleep for the number of seconds requested
                try {
                    if (sleep > 0) Thread.sleep(sleep * 1000);
                } catch (InterruptedException ie) {
                    // Meh?!
                }
                
                BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8));
                PrintWriter pw = new PrintWriter (new FileWriter(prefix + filename, StandardCharsets.UTF_8));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    pw.write(inputLine + "\n");
                    
                    if ((!complete) && (inputLine.contains("\"sequences\": ["))) {
                        break;
                    }
                }
                System.out.println("CACHE MISS: " + prefix + filename);
                in.close();
                pw.close();
            } catch (IOException e) {
                System.err.println("Unable to download IIIF manifest: " + url);
                e.printStackTrace();
                return false;
            }
        } else {
            return true;
            //System.out.println("CACHE HIT");
        }
        return true;
    }
}
