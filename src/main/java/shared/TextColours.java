package shared;

public class TextColours {
    // https://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println

    // Algse värvi taastamine
    public static final String ANSI_RESET = "\u001B[0m";

    // Tavalised värvid
    public static final String ANSI_RED = "\033[0;31m";     // RED          <--- Vahepealkirjad
    public static final String ANSI_YELLOW = "\u001B[33m";  // YELLOW       <--- Hoiatused ja vigased sisendid
    public static final String ANSI_PURPLE = "\033[0;35m";  // PURPLE       <---- Lisamärkused

    // Bold ja ere värv
    public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN        <--- Programmi alguspealkiri
}
