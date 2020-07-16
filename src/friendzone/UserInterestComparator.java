package friendzone;

import friendzone.Coordination;
import friendzone.UserAcc;

/**
 *
 * @author TING WEI JING
 */
public class UserInterestComparator implements Comparable<UserInterestComparator> {

    public UserAcc partnerUserAcc;
    public UserAcc sourceUserAcc;
    public int score = 0;
    private static int distanceLimit = 500;
    public static int penalty = -1000;

    public UserInterestComparator(UserAcc sourceUserAcc, UserAcc partnerUserAcc) {
        this.sourceUserAcc = sourceUserAcc;
        this.partnerUserAcc = partnerUserAcc;
        this.score = calScore();
    }

    private int calScore() {
        if(sourceUserAcc.equals(partnerUserAcc)) {
            return -100000;
        }
        int score = 0;
        int distance = (int) Coordination.calDist(sourceUserAcc, partnerUserAcc);
        if(distance > UserInterestComparator.distanceLimit) {
            score += penalty;
            score -= distance;
        }
        else {
            score -= distance;
        }
        for(String x : sourceUserAcc.interest) {
            for(String y : partnerUserAcc.interest) {
                if(x.equals(y) && !x.equals("")) {
                    score += 50;
                }
            }
        }
        return score;
    }

    @Override
    public int compareTo(UserInterestComparator o) {
        if(this.score > o.score) {
            return 1;
        }
        else if(this.score == o.score) {
            return 0;
        }
        else {
            return -1;
        }
    }

}
