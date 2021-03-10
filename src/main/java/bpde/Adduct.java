package bpde;

import java.util.*;

public class Adduct implements Comparable<Adduct>{
  String carcinogen;
  String name;
  char[] target;
  ArrayList<Character[]> transversions;
  int totalChanged;
  int beg;
  int end;
  boolean bap;
  // Set<String> codons;

  public Adduct(String carcinogen, String name, String target, String transversion, int beg, int end, boolean bap) {
    this.carcinogen = carcinogen;
    System.out.println(transversion);
    this.name = name;
    this.target = new char[target.length()];
    this.transversions = new ArrayList<Character[]>();
    this.beg = beg;
    this.end = end;
    this.bap = bap;
    // codons = new Set<String>();
    for (int i = 0; i < this.target.length; i++) {
      this.target[i] = target.charAt(i);
    }
    for (int i = 0; i < transversion.split(",").length; i++) {
      System.out.println(transversion.split(",")[i].length());
      this.transversions.add(new Character[transversion.split(",")[i].length()]);
      for (int j = 0; j < transversion.split(",")[i].length(); j++) {
        this.transversions.get(i)[j] = transversion.split(",")[i].charAt(j);
      }
    }

  }

  public String toString() {
    return name + "\n" + target.toString() + "\n" + transversions.toString();
  }
  public int compareTo(Adduct other) {
    return -Integer.compare(totalChanged, other.totalChanged);
  }
}
