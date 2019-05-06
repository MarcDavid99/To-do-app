package shared;

public enum Commands {
    //https://stackoverflow.com/questions/3990319/storing-integer-values-as-constants-in-enum-manner-in-java
    DO_CLOSE_TODO_LIST_1(3),
    DO_DISPLAY_TASK_CERTAIN(10),
    DO_ADD_TASK(11),
    DO_DISPLAY_TASK(12),
    DO_ADD_COMMENT(13),
    DO_PUSH_DEADLINE(14),
    DO_COMPLETE_TASK(15),
    DO_ADD_TASK_TO_OTHER_USER(16),
    DO_SEARCH_TASKS(17),
    DO_FOLLOW_TASK(18),
    DO_CLOSE_TODO_LIST_2(19),
    DO_LOG_OUT(20),
    ERROR_OCCURED(21),
    DO_SEARCH_TASKS_BY_DESCRIPTION(22),
    DO_SEARCH_TASKS_BY_USERNAME(23),
    DO_SEARCH_TASKS_BY_DEADLINE(24),
    DO_DISPLAY_TASK_BY_TOPIC(32),
    DO_SAVE_NEW_USER(91),
    DO_VERIFY_CLIENT(92),
    DO_CONFIRM_LOGIN(93),
    DO_NOT_CONFIRM_LOGIN(94),
    DO_CHECK_FOR_USERNAME(95),
    DO_DELETE_USER(5482); // "salajane" value

    private final int value;

    Commands(int newValue) {
        value = newValue;
    }
    public int getValue(){return value;}
}
