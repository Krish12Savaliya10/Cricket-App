package Simulation;
import DataStructure.LinkedListOfPlayer;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.ArrayList;

import static DataBase.sqlQuery.*;
import static Simulation.MatchSimulation.*;

public class Tournament {

    static ArrayList<Match> Schedule = new ArrayList<>();
    static ArrayList<Team> Teams = new ArrayList<>();
    static ArrayList<Team> Group1 = new ArrayList<>();
    static ArrayList<Team> Group2 = new ArrayList<>();
    static HashMap<Team, LinkedListOfPlayer> TeamSet=new HashMap<>();
    static int choice;
    static int year = 0;
    static String TournamentName = "";
    static int over ;
    static int tournamentId=0;

    //for new match generation
    public static boolean organizeMatch(String email) throws  SQLException {
        Connection con=getCon();
        con.setAutoCommit(false);
        Scanner sc = new Scanner(System.in);


        System.out.println("Enter 1 for Single Match");
        System.out.println("Enter 2 for Series");
        System.out.println("Enter 3 for Tournament");
        choice = getValidChoice(sc,"",1,3);
        int playerCount;

        switch (choice) {
            case 1 :
                if(!isSingleMatchAlreadySet(email)) {
                    System.out.println("Enter number of player in match per team (6 to 11)");
                    playerCount = getValidChoice(sc, "", 6, 11);
                    TwoTeamsMatches(con, sc, 1, (playerCount), email);
                    printSchedule("Single Match Schedule");
                    return true;
                }
                else{
                    System.out.println("Single match is already set first finish that match");
                    return false;
                }

            case 2 :
                if(!isSeriesAlreadySet(email)) {
                    System.out.print("Enter series name:");
                    TournamentName = sc.nextLine().toUpperCase();
                    year = LocalDate.now().getYear();
                    tournamentId = (int) ((Math.random() * 89999) + 100000);
                    System.out.print("Enter number of matches (2 to 5):");
                    int n = getValidChoice(sc, "", 2, 5);
                    System.out.println("Enter number of player in match per team (6 to 11)");
                    playerCount = getValidChoice(sc, "", 6, 11);
                    TwoTeamsMatches(con, sc, n, playerCount, email);

                    while (true) {
                        try {
                            insertSeries(con, tournamentId, TournamentName, Schedule.getFirst().team1.teamId, Schedule.getFirst().team2.teamId, n, email);
                            break;
                        } catch (SQLException e) {
                            tournamentId = (int) ((Math.random() * 89999) + 100000);
                            System.out.println(e.getMessage());
                        }
                    }
                    printSchedule("Series Schedule");
                    return true;
                }else {
                    System.out.println("Series is already live please complete first");
                    return false;
                }

            case 3 :
                if(!isTournamentAlreadySet(email)){
                    System.out.print("Enter Tournament name:");
                    TournamentName = sc.nextLine().toUpperCase();
                    year = LocalDate.now().getYear();
                    tournamentId = (int) ((Math.random() * 89999) + 100000);
                    while (true) {
                        try {
                            insertTournament(con, tournamentId, TournamentName, year, email);
                            break;
                        } catch (SQLException e) {
                            tournamentId = (int) ((Math.random() * 89999) + 100000);
                        }
                    }
                    System.out.println("Enter number of teams between 6 to 12 ");
                    int num = getValidChoice(sc, "even only:", 6, 12);

                    System.out.println("Enter number of player in match per team (6 to 11)");
                    playerCount = getValidChoice(sc, "", 6, 11);

                    String teamName;
                    Team addTeam;
                    for (int i = 1; i <= num; i++) {
                        System.out.println("\n==================");
                        System.out.print("Enter team " + (i) + " name: ");
                        teamName = sc.nextLine().toUpperCase();
                        addTeam = new Team(teamName, (int) ((Math.random() * 89999) + 100000));
                        while (true) {
                            try {
                                insertTeam(con, addTeam.teamId, addTeam.teamName, email);
                                insertIntoPointsTable(con, tournamentId, addTeam.teamId);

                                break;
                            } catch (SQLException e) {
                                if (e.getErrorCode() == 1062 || e.getMessage().toLowerCase().contains("duplicate")) {
                                    System.out.print("Duplicate Team name detected: " + addTeam.teamName + ".\nPlease enter a different name: ");
                                    teamName = sc.nextLine().toUpperCase();
                                    addTeam.teamName = teamName;
                                } else {
                                    break;
                                }
                            }
                        }
                        Teams.add(addTeam);
                        TeamSet.put(addTeam, new LinkedListOfPlayer());
                        inputPlayers(con, sc, TeamSet.get(addTeam), teamName, playerCount);

                    }

                    int TournamentType = getValidChoice(sc, "\nEnter 1 for non group tournament " + num * (num - 1) / 2 + " matches \nEnter 2 for group tournament", 1, 2);
                    System.out.println("Enter choice:");
                    generateGroups(num);

                    if (TournamentType == 1) {
                        crossGroupMatches(Group1, Group2);
                        sameGroupMatches(Group2, Group1);
                    } else if (TournamentType == 2) {

                        System.out.println("Group 1: " + Group1);
                        System.out.println("Group 2: " + Group2);
                        System.out.println("Choose Schedule Type:");
                        System.out.println("1. Same Group Matches Only " + (Group1.size() * (Group1.size() - 1)) + " Matches");
                        System.out.println("2. Other Group Matches Only " + (Group1.size() * Group1.size()) + " Matches");
                        System.out.println("3. 1 match with group and 2 match with other group " + (Group1.size() * (Group1.size() - 1) + 2 * (Group1.size() * Group1.size())) + " Matches");
                        System.out.println("4. 2 match with group and 1 match with other group " + (2 * (Group1.size() * (Group1.size() - 1)) + (Group1.size() * Group1.size())) + " Matches");
                        System.out.println("5. Play with all other teams twice " + (2 * ((Group1.size() * (Group1.size() - 1)) + (Group1.size() * Group1.size()))) + " Matches");
                        System.out.println("Enter choice:");
                        int opt = getValidChoice(sc, "", 1, 5);

                        switch (opt) {
                            case 1:
                                sameGroupMatches(Group1, Group2);
                                break;
                            case 2:
                                crossGroupMatches(Group1, Group2);
                                break;
                            case 3:
                                crossGroupMatches(Group1, Group2);
                                Shuffle();
                                sameGroupMatches(Group1, Group2);
                                Shuffle();
                                crossGroupMatches(Group2, Group1);
                                break;
                            case 4:
                                sameGroupMatches(Group1, Group2);
                                Shuffle();
                                crossGroupMatches(Group1, Group2);
                                Shuffle();
                                sameGroupMatches(Group2, Group1);
                                break;
                            case 5:
                                sameGroupMatches(Group1, Group2);
                                Shuffle();
                                crossGroupMatches(Group2, Group1);
                                Shuffle();
                                sameGroupMatches(Group2, Group1);
                                Shuffle();
                                crossGroupMatches(Group1, Group2);
                                break;
                        }
                        printSchedule("Final Tournament Schedule");
                    }
                    return true;
                }
                else {
                    System.out.println("Tournament already set please complete");
                    return false;
                }
        }
        System.out.println("Enter number of overs (2 to 50)");
        over = getValidChoice(sc, "", 2, 50);
        int matchNumber=1;
        for(Match match:Schedule){
            match.inningOvers=over;
            while(true) {
                try {
                    insertSchedule(con,match.MatchId, match.team1.teamId, match.team2.teamId, email, matchNumber, match.inningOvers, match.matchType,tournamentId);
                    break;
                } catch (SQLException e) {
                    match.MatchId=(int)((Math.random()*89999)+100000);
                }
            }
            match.setMatchStatus("UPCOMING");
            matchNumber++;
        }
        con.commit();
        return true;
    }

    //get match data from match
    public static void startMatch(Scanner sc) throws SQLException, InterruptedException {
        int matchStart;
        do {
            Match match = Schedule.removeFirst();
            Team team1 = match.team1;
            Team team2 = match.team2;
            System.out.println("\n");
            if(match.matchType.equalsIgnoreCase("TOURNAMENT")||
                    match.matchType.contains("SEMI")||match.matchType.equalsIgnoreCase("FINAL")){
                displayPointsTable(tournamentId);
            }
            System.out.println("\nPress 1 to start "+team1.teamName+" vs "+team2.teamName);
            System.out.println("Press 2 to exit");


            matchStart=getValidChoice(sc,"",1,2);
            if(matchStart==1) {
                insertTeamMatchStats(match.MatchId, team1.teamId, tournamentId);
                insertTeamMatchStats(match.MatchId, team2.teamId, tournamentId);
                match.setMatchStatus("LIVE");

                LinkedListOfPlayer team1Player = TeamSet.get(team1);
                LinkedListOfPlayer team2Player = TeamSet.get(team2);
                for (LinkedListOfPlayer.Player player : team1Player.getALlPlayer()) {
                    insertPlayerMatchStats(match.MatchId, player.getPlayerId());
                }
                for (LinkedListOfPlayer.Player player : team2Player.getALlPlayer()) {
                    insertPlayerMatchStats(match.MatchId, player.getPlayerId());
                }

                team1.setDefault();
                team1Player.setDefault();
                team2.setDefault();
                team2Player.setDefault();

                System.out.println("\n--------"+team1.teamName+" vs "+team2.teamName+"---------");

                Simulation(sc, team1, team2, team1Player, team2Player, match.inningOvers, match, tournamentId);
            }
        } while (!Schedule.isEmpty() && matchStart==1) ;

    }

    //get schedule data from data base
    public static void setScheduleFromDB(String email,Scanner sc){
        Teams.clear();
        Schedule.clear();
        TeamSet.clear();
        try {
            int choice=getValidChoice(sc,"\nEnter 1 for tournaments\nEnter 2 for series\nenter 3 for single match\n",1,3);
            if(choice==1) {
                tournamentId = getTournamentId(email);
                if(isTournament(tournamentId) && isTournamentDone(tournamentId)){
                    Teams.addAll(getTopTeams(tournamentId));
                    for (Team team : Teams) {
                        TeamSet.put(team, getPlayersForTeamsInRoleOrder(team));
                    }
                    if(!arePlayoffsScheduled(tournamentId)){
                        System.out.println("Play Off");
                        genratePlayOff(email);
                    }
                    if((!isFinalAdded(tournamentId)) ){
                        checkAndGenerateFinal(email);
                    }
                    printSchedule("Knockout Schedule");
                    Schedule.addAll(getSchedule(email, Teams,"platOff"));
                }
                else {
                    Teams.addAll(getTeamData(email));
                    Schedule.addAll(getSchedule(email, Teams,"TOURNAMENT"));
                    for (Team team : Teams) {
                        TeamSet.put(team, getPlayersForTeamsInRoleOrder(team));
                    }
                    printSchedule("Remaining Tournament Schedule");

                }

            }
           else if(choice==2) {
                tournamentId = getSeriesId(email);
                Teams.addAll(getTeamData(email));
                Schedule.addAll(getSchedule(email, Teams,"SERIES"));
                printSchedule("Remaining Series Schedule");
           }
           else{
                Teams.addAll(getTeamData(email));
                Schedule.addAll(getSchedule(email, Teams,"SINGLE"));
           }
            for (Team team : Teams) {
                TeamSet.put(team, getPlayersForTeamsInRoleOrder(team));
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println("no data for live tournaments");
        }
    }

    static void TwoTeamsMatches(Connection con,Scanner sc, int matches,int numberOfPlayer,String email){
        System.out.print("Enter Team 1 name: ");
        String team1name = sc.nextLine().toUpperCase();
        Team team1 = new Team(team1name, (int)((Math.random() * 89999) + 100000));

        while (true) {
            try {
                insertTeam(con,team1.teamId, team1.teamName, email);
                break;
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062 || e.getMessage().toLowerCase().contains("duplicate")) {
                    System.out.print("Duplicate Team name detected: " + team1.teamName + ".\nPlease enter a different name: ");
                    team1name = sc.nextLine().toUpperCase();
                    team1.teamName = team1name;
                } else {
                    break;
                }
            }
        }

        System.out.print("Enter Team 2 name: ");
        String team2name = sc.nextLine().toUpperCase();
        Team team2 = new Team(team2name, (int)((Math.random() * 89999) + 100000));

        while (true) {
            try {
                insertTeam(con,team2.teamId, team2.teamName, email);
                break;
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062 || e.getMessage().toLowerCase().contains("duplicate")) {
                    System.out.print("Duplicate Team name detected: " + team2.teamName + ".Please enter a different name: ");
                    team2name = sc.nextLine().toUpperCase();
                    team2.teamName = team2name;
                } else {
                    break;
                }
            }
        }
        TeamSet.put(team1,new LinkedListOfPlayer());
        inputPlayers(con,sc,TeamSet.get(team1),team1name,numberOfPlayer);
        TeamSet.put(team2,new LinkedListOfPlayer());
        inputPlayers(con,sc,TeamSet.get(team2),team2name,numberOfPlayer);

        for (int i = 1; i <=matches; i++) {
            Schedule.add(new Match(team1, team2,(int)((Math.random()*89999)+100000),(matches!=1)?"SERIES":"SINGLE"));
        }
        for (Match m:Schedule){
            if(matches!=1)
                m.setMatchType("SERIES");
            else
                m.setMatchType("SINGLE");
        }
    }
    static void Shuffle(){
        Collections.shuffle(Group1);
        Collections.shuffle(Group2);
    }

    static void generateGroups(int totalTeams) {
        Collections.shuffle(Teams);
        int mid = totalTeams / 2;
        Group1.addAll(Teams.subList(0, mid));
        Group2.addAll(Teams.subList(mid, totalTeams));
    }

    static void sameGroupMatches(ArrayList<Team> group1,ArrayList<Team> group2) {
        ArrayList<Match> Team1Schedule=new ArrayList<>();
        ArrayList<Match> Team2Schedule=new ArrayList<>();
        int size = group1.size();
        for (int i = 0; i <size; i++) {
            for (int j = i + 1; j <size; j++) {
                if((int)(Math.random()*2+1)%2==1) {
                    Team1Schedule.add(new Match(group1.get(i), group1.get(j),(int)((Math.random()*89999)+100000),"TOURNAMENT"));
                    Team2Schedule.add(new Match(group2.get(i), group2.get(j),(int)((Math.random()*89999)+100000),"TOURNAMENT"));
                }
                else{
                    Team1Schedule.add(new Match(group1.get(j), group1.get(i),(int)((Math.random()*89999)+100000),"TOURNAMENT"));
                    Team2Schedule.add(new Match(group2.get(j), group2.get(i),(int)((Math.random()*89999)+100000),"TOURNAMENT"));
                }
            }
        }
        Collections.shuffle(Team1Schedule);
        Collections.reverse(Team1Schedule);
        Collections.shuffle(Team2Schedule);
        Collections.reverse(Team2Schedule);
        while(!Team1Schedule.isEmpty()){
            Schedule.add(Team1Schedule.removeFirst());
            Schedule.add(Team2Schedule.removeFirst());
        }

    }


    static void crossGroupMatches(ArrayList<Team> group1,ArrayList<Team> group2) {
        for(int i=0;i<group1.size();i++){
            if(i!=0) {
                group2.addLast(group2.removeFirst());
            }
            for(int j=0;j<group2.size();j++){
                if((int)(Math.random()*2+1)%2==1)
                    Schedule.add(new Match(group1.get(j), group2.get(j),(int)((Math.random()*89999)+100000),"TOURNAMENT"));
                else
                    Schedule.add(new Match(group2.get(j), group1.get(j),(int)((Math.random()*89999)+100000),"TOURNAMENT"));

            }
        }

    }

    static void printSchedule(String title) {
        System.out.println("\n--- "+title+" ---");
        int matchNo = 1;
        for (Match m : Schedule) {
            System.out.println("Match " + matchNo++ + ": " + m.team1.getTeamName() + " vs " + m.team2.getTeamName());
        }
    }

    static void genratePlayOff(String email) throws SQLException {
        Team team1=Teams.get(0);
        Team team2=Teams.get(1);
        Team team3=Teams.get(2);
        Team team4=Teams.get(3);
        Match semi_1=new Match(team1,team4,(int)((Math.random()*89999)+100000),"SEMI1");
        Match semi_2=new Match(team2,team3,(int)((Math.random()*89999)+100000),"SEMI2");
        int over=getTournamentOver(tournamentId);
        Schedule.addAll(Arrays.asList(semi_1,semi_2));
        int matchNumber=0;
        for(Match match:Schedule){
            match.inningOvers=over;
            matchNumber++;
            insertSchedule(getCon(),match.MatchId, match.team1.teamId, match.team2.teamId, email, matchNumber, match.inningOvers, match.matchType,tournamentId);
        }
    }
    static void checkAndGenerateFinal(String email) throws SQLException {
        Team semi1Winner = getMatchWinner("SEMI1", tournamentId);
        Team semi2Winner = getMatchWinner("SEMI2", tournamentId);

        if (semi1Winner != null && semi2Winner != null) {
            System.out.println("Generating FINAL match: " + semi1Winner.getTeamName() + " vs " + semi2Winner.getTeamName());

            TeamSet.put(semi1Winner,getPlayersForTeamsInRoleOrder(semi1Winner));
            TeamSet.put(semi2Winner,getPlayersForTeamsInRoleOrder(semi2Winner));

            Match finalMatch = new Match(semi1Winner, semi2Winner, (int) ((Math.random() * 89999) + 100000), "FINAL");


            finalMatch.inningOvers =  getTournamentOver(tournamentId);

            insertSchedule(getCon(),finalMatch.MatchId, finalMatch.team1.teamId, finalMatch.team2.teamId,
                    email, 0, finalMatch.inningOvers, finalMatch.matchType, tournamentId);

            finalMatch.setMatchStatus("UPCOMING");
            Schedule.add(finalMatch);
        }
    }
    static void inputPlayers(Connection con,Scanner sc, LinkedListOfPlayer teamPlayers, String teamName, int NumberOfPlayer)  {
        int batsmenCount, allRoundersCount, bowlersCount;

        do {
            System.out.println("Enter number of batsmen for " + teamName + " (must be at least 2):");
            batsmenCount = getValidCount(sc, 2);

            System.out.println("Enter number of all-rounders for " + teamName + " (must be at least 0):");
            allRoundersCount = getValidCount(sc, 0);

            System.out.println("Enter number of bowlers for " + teamName + " (must be at least 2):");
            bowlersCount = getValidCount(sc, 2);

            if (batsmenCount + allRoundersCount + bowlersCount != NumberOfPlayer) {
                System.out.println("Total players must be exactly " + NumberOfPlayer + "! Current sum: " +
                        (batsmenCount + allRoundersCount + bowlersCount));
            }
        } while ((batsmenCount + allRoundersCount + bowlersCount) != NumberOfPlayer);

        sc.nextLine();
        System.out.println("\nEnter batsmen names:");
        for (int i = 1; i <= batsmenCount; i++) {
            System.out.print("Batsman " + i + ": ");
            String name = sc.nextLine().toUpperCase();
            int id=(int)((Math.random()*89999)+100000);
            while(true) {
                try {insertPlayer(con,id,name,"BATSMAN",teamName);
                    break;
                }
                catch (SQLException e) {
                    if (e.getErrorCode() == 1062 || e.getMessage().contains("Duplicate") || e.getMessage().contains("already exists")) {
                        System.out.print("Duplicate name detected: " + name + "\nPlease enter a different name:");
                        name = sc.nextLine().toUpperCase();
                        id = (int)((Math.random()*89999)+100000);
                    } else {
                        id = (int)((Math.random()*89999)+100000);
                    }
                }
            }
            teamPlayers.addBatsman(name,id);
            teamPlayers.getPlayer(name).setPlayerId(id);
        }

        if (allRoundersCount != 0) {
            System.out.println("\nEnter all-rounders names:");
            for (int i = 1; i <= allRoundersCount; i++) {
                System.out.print("All-rounder " + i + ": ");
                String name = sc.nextLine().toUpperCase();
                int id=(int)((Math.random()*89999)+100000);
                while(true) {
                    try {
                        insertPlayer(con,id,name,"ALLROUNDER",teamName);
                        break;
                    }
                    catch (SQLException e) {
                        if (e.getErrorCode() == 1062 || e.getMessage().contains("Duplicate") || e.getMessage().contains("already exists")) {
                            System.out.print("Duplicate name detected: " + name + ".\nPlease enter a different name:");
                            name = sc.nextLine().toUpperCase();
                            id = (int)((Math.random()*89999)+100000);
                        } else {
                            id = (int)((Math.random()*89999)+100000);
                        }
                    }
                }
                teamPlayers.addAllrounder(name,id);
                teamPlayers.getPlayer(name).setPlayerId(id);
            }
        }

        System.out.println("\nEnter bowlers names:");
        for (int i = 1; i <= bowlersCount; i++) {
            System.out.print("Bowler " + i + ": ");
            String name = sc.nextLine().toUpperCase();
            int id=(int)((Math.random()*89999)+100000);
            while(true) {
                try {
                    insertPlayer(con,id,name,"BOWLER",teamName);
                    break;
                }
                catch (SQLException e) {
                    if (e.getErrorCode() == 1062 || e.getMessage().contains("Duplicate") || e.getMessage().contains("already exists")) {
                        System.out.print("Duplicate name detected: " + name + ".\nPlease enter a different name:");
                        name = sc.nextLine().toUpperCase();
                        id = (int)((Math.random()*89999)+100000);
                    } else {
                        id = (int)((Math.random()*89999)+100000);
                    }
                }
            }
            teamPlayers.addBowler(name,id);
            teamPlayers.getPlayer(name).setPlayerId(id);
        }
    }
}