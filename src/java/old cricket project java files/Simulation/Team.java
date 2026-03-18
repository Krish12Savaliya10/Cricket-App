package Simulation;
import java.util.ArrayList;

public class Team {
    int totalRun;
    double overPlayed;
    int wicketDown;
    int Target;
    String teamName;
    int teamId;
    boolean won;
    boolean freeHitActive;

    public Team(String teamName,int teamId) {
        this.teamName = teamName;
        this.teamId=teamId;
        totalRun = 0;
        overPlayed = 0;
        wicketDown = 0;
        won = false;
        freeHitActive = false;
    }
    public Team(Team forStack){
        this.teamId= forStack.teamId;
        this.teamName =forStack.teamName;
        this.totalRun = forStack.totalRun;
        this.overPlayed = forStack.overPlayed;
        this.wicketDown = forStack.wicketDown;
        this.won = forStack.won;
        this.freeHitActive = forStack.freeHitActive;
        this.Target=forStack.Target;
    }
    public void setDefault(){
        this.totalRun = 0;
        this.overPlayed = 0;
        this.wicketDown = 0;
        this.won = false;
        this.freeHitActive = false;
        this.Target=0;
    }

    @Override
    public String toString() {
        return "Team{" +
                "teamName='" + teamName + '\'' +
                '}';
    }

    public String getTeamName() {
        return teamName;
    }
    
    public int getTeamId() {return teamId;}

    public void setOverPlayed(double overPlayed) {
        this.overPlayed = overPlayed;
    }

    public static Team getTeamById(ArrayList<Team> teams, int teamId) {
        for (Team team : teams) {
            if (team.getTeamId() == teamId) {
                return team;
            }
        }
        return null;
    }
}