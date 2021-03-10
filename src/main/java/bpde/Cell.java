package bpde;

import java.util.*;

import org.apache.http.*;
import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import java.time.Duration;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;

public class Cell {
  ArrayList<Character> oldSequence;
  ArrayList<Character> newSequence;
  ArrayList<Character> oxidizedSequence;
  ArrayList<Character> finalSequence;
  ArrayList<Adduct> adducts;
  ArrayList<Glycosylase> glycosylases;
  private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  public Cell() {
    oldSequence = new ArrayList<Character>();
    this.fillSequence(oldSequence);
    newSequence = new ArrayList<Character>();
    adducts = new ArrayList<Adduct>();
    glycosylases = new ArrayList<Glycosylase>();
    oxidizedSequence = new ArrayList<Character>();
    finalSequence = new ArrayList<Character>();
    try {
      this.fillAdducts(adducts);

    } catch (Exception e) {
      System.out.println("Adduct error: " + e);
    }
    try {
      this.fillGlycosylases(glycosylases);
    } catch (Exception e) {
      for (StackTraceElement ste : e.getStackTrace()) {
        System.out.println(ste);
    }
      // System.out.println("Glycosylases error: " + e.getStackTrace()[0].getLineNumber());
    }
    // try {
    // this.rankAndChangeAdducts(oldSequence, adducts, 0.6, false);
    // } catch (Exception e) {
    // System.out.println(e);
    // }
    // try {
    // this.createOxidizedBases(oldSequence, newSequence, glycosylases);
    // } catch (Exception e) {
    // System.out.println(e);
    // }
    // try {
    // this.getPercentSimilarity(finalSequence, oldSequence);
    // } catch (Exception e) {
    // System.out.println(e);
    // }
  }

  public void fillSequence(ArrayList<Character> sequence) {
    // Finding google chrome
    System.setProperty("webdriver.chrome.driver", "/Users/tevinwang/Downloads/chromedriver");
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    // Running without opening UI
    WebDriver driver = new ChromeDriver(options);
    WebDriverWait wait = new WebDriverWait(driver, 2);
    // Access the NCBI website
    driver.get("https://www.ncbi.nlm.nih.gov/nuccore/X54156.1?report=fasta");
    // Wait until the nucleotide bases show up
    WebElement firstResult = wait.until(presenceOfElementLocated(By.cssSelector("#viewercontent1 > pre")));
    // Input the html into a document
    Document doc = Jsoup.parse(driver.getPageSource());
    driver.close();
    // Find the bases, and input them into a character array, representing the
    // sequence
    for (char character : doc.select("#viewercontent1 > pre").text().split("\n", 2)[1].toCharArray()) {
      if (character != 32)
        sequence.add(character);
    }
  }

  public void fillAdducts(ArrayList<Adduct> adducts) throws IOException, GeneralSecurityException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    final String spreadsheetId = "1-FlHuCVLDYvtk_R7QxvegmDsjz9KLPVvmQ_1nVrHvjA";
    final String range = "Sheet1!A2:G";
    Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, SheetsQuickstart.getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME).build();
    ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
    List<List<Object>> values = response.getValues();
    if (values == null || values.isEmpty()) {
      System.out.println("No data found.");
    } else {
      System.out.println("Name, Target, Transversions");
      for (List row : values) {
        // Print columns A and E, which correspond to indices 0 and 4.
        System.out.println(Boolean.parseBoolean(row.get(6).toString()));
        adducts.add(new Adduct((String) row.get(0), (String) row.get(1), (String) row.get(2), (String) row.get(3),
            Integer.parseInt(row.get(4).toString()), Integer.parseInt(row.get(5).toString()),
            Boolean.parseBoolean(row.get(6).toString())));
        // System.out.println(adducts);
      }
      // System.out.println("hello");
    }
  }

  public ArrayList<Character> rankAndChangeAdducts(ArrayList<Character> origSequence, ArrayList<Adduct> adducts,
      double probability, boolean bapOnly) {
    ArrayList<Character> newSequence = new ArrayList<Character>();
    ArrayList<Character> sequence = new ArrayList<>(origSequence);
    Collections.shuffle(adducts);
    // Loop through each of the adducts
    for (int i = 0; i < adducts.size(); i++) {

      adducts.get(i).totalChanged = 0;
      newSequence = new ArrayList<Character>();
      if (!bapOnly || adducts.get(i).bap) {
        // Loop through each of the bases in the original sequence
        for (int j = 0; j < sequence.size(); j++) {
          // If the base matches with the first base in the target sequence
          if (j <= adducts.get(i).end && j >= adducts.get(i).beg && Math.random() < probability
              && j < sequence.size() - 1 && sequence.get(j) == adducts.get(i).target[0]) {
            boolean found = true;
            // Figure out if the rest of the bases match, if not set found = false
            for (int k = 1; k < adducts.get(i).target.length; k++) {
              if (sequence.get(j + k) != adducts.get(i).target[k]) {
                found = false;
              }
            }
            // If the sequence is found
            if (found) {
              // Increment the number of total bases changed for that adduct
              adducts.get(i).totalChanged += adducts.get(i).target.length;
              // Randomly select a transversion to change
              int randomNum = ThreadLocalRandom.current().nextInt(0, adducts.get(i).transversions.size());
              // For the new sequence, add bases related to that transversion
              for (int k = 0; k < adducts.get(i).target.length; k++) {
                newSequence.add(adducts.get(i).transversions.get(0)[k]);
              }
              // Skip to to next unknown base
              j += adducts.get(i).target.length - 1;
            } else {
              newSequence.add(sequence.get(j));
            }
            // If the base doesn't match, just add original bases
          } else {
            newSequence.add(sequence.get(j));
          }
        }
        sequence = new ArrayList<>(newSequence);
        System.out.println(adducts.get(i).name + ": " + adducts.get(i).totalChanged);
      }
    }
    this.newSequence = sequence;
    return sequence;

  }

  public void fillGlycosylases(ArrayList<Glycosylase> glycosylases) throws IOException, GeneralSecurityException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    final String spreadsheetId = "10T3ytTO8khJkuLQk0JdBLZyGYT_Snn3Vjs6LyvwcPSc";
    final String range = "Sheet1!A2:C";
    Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, SheetsQuickstart.getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME).build();
    ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
    List<List<Object>> values = response.getValues();
    ArrayList<Substrate> substrates = new ArrayList<Substrate>();
    if (values == null || values.isEmpty()) {
      System.out.println("No data found.");
    } else {
      // System.out.println("Name, Target, Transversions");
      System.out.println(values);
      for (int i = 0; i < values.size(); i++) {

        substrates.add(new Substrate((String) values.get(i).get(1), (String) values.get(i).get(2)));
        // Print columns A and E, which correspond to indices 0 and 4.
        System.out.println(values.get(i).get(0));
        System.out.println(i);
        if (i >= values.size() - 1 || !values.get(i + 1).get(0).equals(values.get(i).get(0))) {
          System.out.println(values.get(i).get(0));
          ArrayList<Substrate> copy = new ArrayList<Substrate>(substrates);
          glycosylases.add(new Glycosylase((String) values.get(i).get(0), copy));
          substrates = new ArrayList<Substrate>();
        }
      }
    }
  }

  public ArrayList<Character> createOxidizedBases(ArrayList<Character> origSequence,
      ArrayList<Character> adductSequence, ArrayList<Glycosylase> glycosylases) {
    ArrayList<Character> newSequence = new ArrayList<Character>();
    ArrayList<Character> sequence = new ArrayList<>(adductSequence);
    Collections.shuffle(glycosylases);
    System.out.println(glycosylases);
    // Loop through each of the glycosylases
    for (int i = 0; i < glycosylases.size(); i++) {

      for (int l = 0; l < glycosylases.get(i).substrates.size(); l++) {
        for (int m = 0; m < glycosylases.get(i).substrates.get(l).transversions.size(); m++) {
          glycosylases.get(i).correctionsAvail = 0;
          newSequence = new ArrayList<Character>();
          char[] target = glycosylases.get(i).substrates.get(l).transversions.keySet().toArray()[m].toString()
              .toCharArray();
          System.out.println(glycosylases.get(i).substrates.get(l).transversions.keySet().toArray()[m].toString());
          char[] correction = glycosylases.get(i).substrates.get(l).transversions
              .get(glycosylases.get(i).substrates.get(l).transversions.keySet().toArray()[m]).toCharArray();
          System.out.println(glycosylases.get(i).substrates.get(l).transversions
              .get(glycosylases.get(i).substrates.get(l).transversions.keySet().toArray()[m]));
          // Loop through each of the bases in the original sequence
          for (int j = 0; j < sequence.size(); j++) {
            // System.out.println(origSequence.get(j));
            // System.out.println(sequence.get(j));
            // If the base matches with the first base in the target sequence
            // if (j < sequence.size() - 1 && sequence.get(j).equals(target[0])) {
            if (Math.random() < glycosylases.get(i).probability && j < sequence.size() - 1
                && sequence.get(j).equals(target[0]) && origSequence.get(j).equals(correction[0])) {
              boolean found = true;
              // Figure out if the rest of the bases match, if not set found = false
              for (int k = 1; k < target.length; k++) {
                if (sequence.get(j + k) != target[k] || origSequence.get(j+k) != correction[k]) {
                  found = false;
                }
              }
              // If the sequence is found
              if (found) {
                // Increment the number of total bases changed for that adduct
                glycosylases.get(i).correctionsAvail += target.length;
                // Randomly select a transversion to change
                // int randomNum = ThreadLocalRandom.current().nextInt(0, 1);
                // For the new sequence, add bases related to that transversion
                for (int k = 0; k < correction.length; k++) {
                  newSequence.add(correction[k]);
                }
                // Skip to to next unknown base
                j += target.length - 1;
              } else {
                newSequence.add(sequence.get(j));
              }
              // If the base doesn't match, just add original bases
            } else {
              newSequence.add(sequence.get(j));
            }
          }
          sequence = new ArrayList<>(newSequence);
          System.out.println(glycosylases.get(i).name + ": " + glycosylases.get(i).correctionsAvail);
        }

      }

    }
    this.finalSequence = sequence;
    return sequence;
  }

  public double getPercentSimilarity(ArrayList<Character> sequence1, ArrayList<Character> sequence2) {
    double difference = 0;
    if (sequence1.size() != sequence2.size())
      return 0;
    for (int i = 0; i < sequence1.size(); i++) {
      if (!sequence1.get(i).equals(sequence2.get(i))) {
        difference++;
      }
    }
    return 1 - difference / sequence1.size();
  }

}