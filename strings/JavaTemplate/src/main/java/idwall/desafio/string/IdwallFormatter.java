package idwall.desafio.string;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rodrigo Catão Araujo on 06/02/2018.
 */
public class IdwallFormatter extends StringFormatter {
    private int lineLengthLimit   = 40;         // Max length a line should have
    private boolean shouldJustify = true;       // If formatting should justify line

    /**
     * Should format as described in the challenge
     *
     * @param text
     * @return
     */
    @Override
    public String format(String text) {
        // Splits text into paragraphs
        String[] paragraphs = text.split("\n", -1);

        StringBuilder builder = new StringBuilder();

        // Splits paragraphs into words
        for (String paragraph : paragraphs) {
            String[] words = paragraph.split(" ");

            // Index of the word I'm at
            int wordIndex = 0;

            // For handling empty lines
            if (words.length == 1 && words[0].length() == 0)
                builder.append('\n');

            while (wordIndex < words.length - 1) {
                // Number of characters in current line
                int firstWord = wordIndex;
                int charCount = words[wordIndex++].length();

                do {
                    int currentWordLength = words[wordIndex++].length();
                    
                    // Exit if adding the word plus a space would surpass character limit
                    if (charCount + currentWordLength + 1 > lineLengthLimit) {
                        wordIndex--;
                        break;
                    }
                    charCount += currentWordLength + 1;
                    // or if reached last word in paragraph
                } while (wordIndex < words.length);

                // Here we build the string
                int extraSpaces = lineLengthLimit - charCount;
                int baseSpaceAmount = 1;
                int remainderSpaces = 0;

                int wordCount =  wordIndex - firstWord;

                if (shouldJustify && wordCount != 1) {
                    baseSpaceAmount = extraSpaces / (wordCount - 1) + 1;
                    remainderSpaces = extraSpaces % (wordCount - 1);
                }

                // It seems from the output files that justification is done every other space
                int evenSpacesQty = (wordCount - 1) / 2 + (wordCount - 1) % 2;
                // Fill even spaces first, then odd ones
                int even = Math.min(remainderSpaces, evenSpacesQty); // Number of even spaces to receive an extra space
                int odd  = remainderSpaces - even; // Number of odd spaces to receive an extra space

                for (int wordToAdd = firstWord; wordToAdd < wordIndex; wordToAdd++) {
                    builder.append(words[wordToAdd]);

                    if (wordToAdd == wordIndex - 1)
                        break;

                    // Base spaces (minimum spaces between words)
                    for (int space = 0; space < baseSpaceAmount; space++)
                        builder.append(' ');

                    // Insert extra spaces
                    if ((wordToAdd - firstWord) / 2 < ((wordToAdd - firstWord) % 2 == 0 ? even : odd))
                        builder.append(' ');
                }
                builder.append('\n');
            }
        }
        return builder.toString();
    }
}
