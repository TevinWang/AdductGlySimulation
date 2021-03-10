package bpde;

import java.util.*;

public class Glycosylase {
  public String name;
  
  public ArrayList<Substrate> substrates;
  public int correctionsAvail;
  public double probability = 0.1;
  public double expression = Integer.MAX_VALUE;

  public Glycosylase(String name, ArrayList<Substrate> substrates) {
    this.name = name;
    this.substrates = substrates;
    
    
  }
  public void setExpression(double expression) {
    this.expression = expression;
    probability = 2 / (1 + Math.exp((2/expression)));
    // probability = 1;
    System.out.println(probability);
  }

}
