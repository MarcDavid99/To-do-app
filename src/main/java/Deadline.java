public class Deadline extends Task{
    //peaks tegema ka deadline klassi, kus siis meetodid deadlinei settimiseks, gettimiseks jne
    //mingi by default deadline võiks ka olla iga ülesande lisamisel ja siis äkki mingi meetod mis ütleks
    //kas taski deadline on möödas


    public Deadline(User taskOwner, String taskDescription, int taskID) {
        super(taskOwner, taskDescription, taskID);
    }


}