/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world.opentexts.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
            try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
                FileOutputStream fileOutputStream = new FileOutputStream(prefix + filename)) {
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
                System.out.println("CACHE MISS: " + prefix + filename);
            } catch (IOException e) {
                System.err.println("Unable to download IIIF manifest: " + url);
                e.printStackTrace();
            }
        } else {
            //System.out.println("CACHE HIT");
        }
    }
}
