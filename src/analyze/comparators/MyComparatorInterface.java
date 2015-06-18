/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analyze.comparators;

/**
 *
 * @author User
 */
interface MyComparatorInterface {
    public boolean compare(float x, float y);

    public boolean compare(int x, int y);

    public boolean compare(String x, String y);

    public boolean compare(byte x, byte y);

    public boolean compare(boolean x, boolean y);
    
    public String getOutputString();
}
