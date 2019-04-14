import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;


public class Deadline{

    //Kasutatud lingid: https://stackoverflow.com/questions/27005861/calculate-days-between-two-dates-in-java-8
    //http://tutorials.jenkov.com/java-date-time/localdate.html
    //https://www.javatpoint.com/java-localdate

    private LocalDate deadlineDate;


    public void setDeadline(int deadlineAmount){
        //deadlineAmount = deadline päevade arv
        if(deadlineDate == null) {
            LocalDate dateOnCreation = LocalDate.now();
            deadlineDate = dateOnCreation.plusDays(deadlineAmount);
        }
        else {
            deadlineDate = deadlineDate.plusDays(deadlineAmount);
        }
    }

    public LocalDate getDeadlineDate() {
        return deadlineDate;
    }

    public boolean isPastDeadline(){
        LocalDate dateToCheck = LocalDate.now();
        if (dateToCheck.isEqual(deadlineDate)){
            System.out.println("Same day as deadline!");
            return false;
        }else if (dateToCheck.isAfter(deadlineDate)){
            System.out.println("Past deadline");
            return true; //on üle deadline
        }else{
            System.out.println("You have " + DAYS.between(LocalDate.now(),deadlineDate) + " Until deadline");
            return false;
        }
    }

    // TODO: teha meetod, mis kontrollib deadline kaugust
    public boolean isDeadlineApproaching() {
        return true;
    }

    public String dateToString(){
        //Teeb selle Date'i loetavaks inimeste jaoks.
        return deadlineDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

}