/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import model.entity.Person;

/**
 *
 * @author henar
 */
public class PersonExporter {
    public static void exportCSV(File file, ArrayList<Person> people) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        
        bw.write("nif,name,dateOfBirth,hasPhoto");
        bw.newLine();
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");    
        
        for (Person person : people) {
            String nif = escapeSpecialCharacters(person.getNif());
            String name = escapeSpecialCharacters(person.getName());
            
            String dateOfBirth = "";
            if (person.getDateOfBirth() != null) {
                dateOfBirth = dateFormat.format(person.getDateOfBirth());
            }
            
            String hasPhoto = "NO";
            if (person.getPhoto() != null) {
                hasPhoto = "YES";
            }
            
            bw.write(nif + "," + name + "," + dateOfBirth + "," + hasPhoto);
            bw.newLine();
        }
        
        bw.flush();
        bw.close();
    } 
    
    /* Method from https://www.baeldung.com/java-csv */
    private static String escapeSpecialCharacters(String data) {
        if (data == null) {
            return "";
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (escapedData.contains(",") || escapedData.contains("\"") || escapedData.contains("'")) {
            escapedData = escapedData.replace("\"", "\"\"");
            escapedData = "\"" + escapedData + "\"";
        }
        return escapedData;
    }
}
