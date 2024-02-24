package image_char_matching;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.*;

/**
 * The SubImgCharMatcher class provides functionality to match
 * characters with their corresponding
 * brightness values and normalize these values for efficient comparison.
 */
public class SubImgCharMatcher {
    /** The total number of pixels in a character's array. */
    private static final double PIXELS_IN_ARRAY = 16 * 16;

    /** TreeMap to map base characters to their brightness values. */
    private TreeMap<Character, Double> baseCharToBrightness;

    /** TreeMap to map normalized brightness values to corresponding characters. */
    private TreeMap<Double, TreeSet<Character>> normalizedCharToBrightness;

    /**
     * Constructs a SubImgCharMatcher object with a given character set.
     *
     * @param charset an array of characters representing the character set
     */
    public SubImgCharMatcher(char[] charset) {
        // Create a Comparator for sorting the TreeMap based on values
        Comparator<Character> comparator = new Comparator<Character>() {
            @Override
            public int compare(Character o1, Character o2) {
                return Double.compare(baseCharToBrightness.get(o1),
                        baseCharToBrightness.get(o2));
            }
        };

        // Create the TreeMap with the custom comparator
        this.baseCharToBrightness = new TreeMap<>(comparator);

        for (char c : charset) {
            baseCharToBrightness.put(c, calBaseBrightness(c));
        }
        this.createNormalizedBrightness();
    }

    /**
     * Returns the character with brightness closest to the specified brightness value.
     *
     * @param brightness the brightness value to match
     * @return the character with brightness closest to the specified value
     */
    public char getCharByImageBrightness(double brightness){
        // Find the entry with the least key greater than or equal to the targetValue
        Map.Entry<Double, TreeSet<Character>> ceilingEntry =
                normalizedCharToBrightness.ceilingEntry(brightness);

        // Find the entry with the greatest key less than or equal to the targetValue
        Map.Entry<Double, TreeSet<Character>> floorEntry =
                normalizedCharToBrightness.floorEntry(brightness);

        // Determine which entry is closer to the targetValue
        double ceilingDistance = ceilingEntry != null ?
                Math.abs(ceilingEntry.getKey() - brightness) : Double.MAX_VALUE;
        double floorDistance = floorEntry != null ?
                Math.abs(floorEntry.getKey() - brightness) : Double.MAX_VALUE;

        // Get the closest key
        double closestKey;
        if (ceilingDistance < floorDistance) {
            assert ceilingEntry != null;
            closestKey = ceilingEntry.getKey();
        } else {
            assert floorEntry != null;
            closestKey = floorEntry.getKey();
        }
        return normalizedCharToBrightness.get(closestKey).first();
    }

    /**
     * Checks if adding or removing a character changes the minimum
     * or maximum base brightness value.
     *
     * @param baseBrightnessC the base brightness value of the character
     * @return true if adding or removing the character changes the
     * minimum or maximum brightness, else false
     */
    private boolean checkChangeMinOrMax(double baseBrightnessC){
        return (baseBrightnessC > getMaxBaseBrightness() ||
                baseBrightnessC < getMinBaseBrightness());
    }

    /**
     * Adds a character to the matcher and updates the normalized brightness mapping.
     *
     * @param c the character to add
     */
    public void addChar(char c){
        double baseBrightnessC = calBaseBrightness(c);
        if(checkChangeMinOrMax(baseBrightnessC)){
            baseCharToBrightness.put(c, baseBrightnessC);
            createNormalizedBrightness();
        }
        else{
            baseCharToBrightness.put(c, baseBrightnessC);
            addToNormalizedTree(c);
        }
    }

    /**
     * Removes a character from the matcher and updates the normalized brightness mapping.
     *
     * @param c the character to remove
     */
    public void removeChar(char c) {
        double baseBrightnessC = calBaseBrightness(c);
        if(checkChangeMinOrMax(baseBrightnessC)){
            baseCharToBrightness.remove(c);
            createNormalizedBrightness();
        }
        else{
            baseCharToBrightness.remove(c);
            normalizedCharToBrightness.get(newCharBrightness(c)).remove(c);
        }
    }

    /**
     * Calculates the base brightness value for a given character.
     *
     * @param c the character
     * @return the base brightness value of the character
     */
    private double calBaseBrightness(char c){
        boolean[][] charConverted = CharConverter.convertToBoolArray(c);
        int white_counter = 0;
        for( int i=0; i < charConverted.length; i++){
            for(int j=0; j < charConverted[0].length; j ++){
                if(charConverted[i][j]){
                    white_counter++;
                }
            }
        }
        return white_counter / PIXELS_IN_ARRAY;
    }

    /**
     * Adds a character to the normalized brightness tree or updates its entry.
     *
     * @param c the character to add or update
     */
    private void addToNormalizedTree(char c){
        double newBrightnessC = newCharBrightness(c);
        if (!(this.normalizedCharToBrightness.containsKey(newBrightnessC))){
            TreeSet<Character> charTree = new TreeSet<Character>();
            charTree.add(c);
            normalizedCharToBrightness.put(newBrightnessC,charTree);
        }
        else{
            normalizedCharToBrightness.get(newBrightnessC).add(c);
        }
    }

    /**
     * Creates the normalized brightness mapping from the base characters.
     */
    private void createNormalizedBrightness(){
        this.normalizedCharToBrightness = new TreeMap<Double,TreeSet<Character>>();

        for (char c: baseCharToBrightness.keySet()){
            addToNormalizedTree(c);
        }
    }

    /**
     * Calculates the normalized brightness value for a given character.
     *
     * @param c the character
     * @return the normalized brightness value of the character
     */
    private double newCharBrightness(char c){
        double charBrightness = baseCharToBrightness.get(c);
        double minBrightnessValue = getMinBaseBrightness();
        double maxBrightnessValue = getMaxBaseBrightness();
        return (charBrightness - minBrightnessValue) / (maxBrightnessValue - minBrightnessValue);
    }

    /**
     * Retrieves the minimum base brightness value from the map.
     *
     * @return the minimum base brightness value
     */
    private double getMinBaseBrightness(){
        Character minBrightnessKey = baseCharToBrightness.firstKey();
        return baseCharToBrightness.get(minBrightnessKey);
    }

    /**
     * Retrieves the maximum base brightness value from the map.
     *
     * @return the maximum base brightness value
     */
    private double getMaxBaseBrightness(){
        Character maxBrightnessKey = baseCharToBrightness.lastKey();
        return baseCharToBrightness.get(maxBrightnessKey);
    }
}
