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
            return false;
        }else if (dateToCheck.isAfter(deadlineDate)){
            return true; //on üle deadline
        }
        else{
            return false;
        }
    }

    public boolean isDeadlineApproaching() {
        // TODO: Mõelda, kas anda kasutajale valik mitu päeva varem ta hoiatust soovib
        if (DAYS.between(LocalDate.now(),deadlineDate) < 7) {
            return true;
        }
        return false;
    }

    public String dateToString(){
        //Teeb selle Date'i loetavaks inimeste jaoks.
        return deadlineDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

}