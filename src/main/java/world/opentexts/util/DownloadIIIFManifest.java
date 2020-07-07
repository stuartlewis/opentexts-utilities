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

/**
 * Download (and cache) a IIIF manifest
 * 
 * @author Stuart Lewis
 */
public class DownloadIIIFManifest {
    
    public static void get(String url, String prefix) {
        String filename = url.replaceAll("/", "_").replaceAll(":", "-");
        //System.out.println(filename);
        File manifest = new File(prefix + filename);
        
        // If it isn't in the cache...
        if (!manifest.exists()) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                PrintWriter pw = new PrintWriter (new FileWriter(prefix + filename));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    pw.write(inputLine + "\n");
                    
                    if (inputLine.contains("\"sequences\": [")) {
                        break;
                    }
                }
                System.out.println("CACHE MISS: " + prefix + filename);
                in.close();
                pw.close();
            } catch (IOException e) {
                System.err.println("Unable to download IIIF manifest: " + url);
                e.printStackTrace();
            }
        } else {
            //System.out.println("CACHE HIT");
        }
    }
}
