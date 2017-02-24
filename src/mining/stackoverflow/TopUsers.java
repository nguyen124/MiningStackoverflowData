package mining.stackoverflow;

import java.util.List;

/**
 * Holder for answers
 */
public class TopUsers{
    private final List<UserData> byQuestions;
    private final List<UserData> byAnswers;

    public TopUsers(List<UserData> byQuestions, List<UserData> byAnswers) {
        this.byQuestions = byQuestions;
        this.byAnswers = byAnswers;
    }

    public List<UserData> getUsersByQuestions() {
        //return Collections.unmodifiableList(byQuestions);
        return byQuestions;
    }
    public List<UserData> getUsersByAnswers() {
        //return Collections.unmodifiableList(byAnswers);
        return byAnswers;
    }
}
