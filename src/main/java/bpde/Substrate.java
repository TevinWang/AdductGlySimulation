package bpde;
import java.util.*;

public class Substrate {
  String name;
  HashMap<String, String> transversions;

  public Substrate(String name, String transversions) {
    this.name = name;
    this.transversions = new HashMap<String, String>();
    String[] transversionArray = transversions.split(",");
    for (String transversion : transversionArray) {
      this.transversions.put(transversion.split(":")[0], transversion.split(":")[1]);
    }
    
  }
  public String toString() {
    return this.name;
  }
}
