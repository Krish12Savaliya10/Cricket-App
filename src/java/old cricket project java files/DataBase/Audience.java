package DataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import static DataBase.sqlQuery.getCon;
import static Simulation.MatchSimulation.getValidChoice;

public class Audience {
    int choice;
    Connection con;
    public Audience() throws SQLException {
        this.con=getCon();
    }

    public void AudienceMenu(Scanner sc) throws SQLException {
        do {
            System.out.println("\nEnter 1 for single matches");
            System.out.println("Enter 2 for series matches");
            System.out.println("Enter 3 for tournaments matches");
            System.out.println("Enter 4 for exit");
            choice = getValidChoice(sc,"",1,4);

            switch (choice){
                case 1:
                    singleMatchMenu(sc);
                    break;

                case 2:
                    seriesMatchMenu(sc);
                    break;

                case 3:
                    tournamentMatchMenu(sc);
                    break;

                case 4:
                    System.out.println("Thank You");
                    break;
            }
        }while (choice!=4);

    }

    public void singleMatchMenu(Scanner sc) throws SQLException {
        do {
            System.out.println("Enter 1 for live matches stats");
            System.out.println("Enter 2 for completed matches stats");
            System.out.println("Enter 3 for back");
            choice = getValidChoice(sc,"",1,3);

            switch (choice){
                case 1:
                    liveMatches(sc,"SINGLE");
                    break;
                case 2:
                    completedMatches(sc,"SINGLE");
                    break;

                case 3:
                    System.out.println("Single matches menu close");
                    break;
            }
        }while (choice!=3);
    }

    public void seriesMatchMenu(Scanner sc) throws SQLException {
        do {
            System.out.println("Enter 1 for live series stats");
            System.out.println("Enter 2 for upcoming series stats");
            System.out.println("Enter 3 for upcoming series stats");
            System.out.println("Enter 4 for back");
            choice = getValidChoice(sc,"",1,3);

            switch (choice){
                case 1:
                    displaySeriesIds("LIVE");
                    break;

                case 2:
                    displaySeriesIds("UPCOMING");
                    break;

                case 3:
                    completedMatches(sc,"COMPLETED");
                    break;

                case 4:
                    System.out.println("Series matches menu close");
                    break;
            }
        }while (choice!=4);
    }

    public void tournamentMatchMenu(Scanner sc) throws SQLException {
        do {
            System.out.println("Enter 1 for live tournament stats");
            System.out.println("Enter 2 for upcoming tournament stats");
            System.out.println("Enter 3 for upcoming tournament stats");
            System.out.println("Enter 4 for back");
            choice = getValidChoice(sc,"",1,3);

            switch (choice){
                case 1:
                    displayTournamentIds("LIVE");
                    break;

                case 2:
                    displayTournamentIds("UPCOMING");
                    break;

                case 3:
                    completedMatches(sc,"COMPLETED");
                    break;

                case 4:
                    System.out.println("Tournament matches menu close");
                    break;
            }
        }while (choice!=4);
    }

    public void liveMatches(Scanner sc,String type) throws SQLException {
        displayMatchIds("LIVE",type);

    }

    public void completedMatches(Scanner sc,String type) throws SQLException {
        displayMatchIds("COMPLETED",type);
    }

    public void upcomingMatches(Scanner sc,String type) throws SQLException {
        displayMatchIds("UPCOMING",type);
    }


    public void displayMatchIds(String status, String type) throws SQLException {
        String sql = "SELECT m.match_id, t1.team_name AS team1_name, t2.team_name AS team2_name " +
                "FROM matches m " +
                "JOIN teams t1 ON m.team1_id = t1.team_id " +
                "JOIN teams t2 ON m.team2_id = t2.team_id " +
                "WHERE m.match_status=? AND m.match_type=?";
        try(PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1,status);
            ps.setString(2,type);
            ResultSet rs=ps.executeQuery();
            String team1,team2;
            int MatchId;
            System.out.println("Match Id  Team1          Team2");
            while (rs.next()){
                MatchId=rs.getInt("match_id");
                team1=rs.getString("team1_name");
                team2=rs.getString("team2_name");
                System.out.println(MatchId+" ".repeat(10-String.valueOf(MatchId).length())+
                        team1+" ".repeat(15-team1.length())+team2);
            }
        }
    }

    public void displaySeriesIds(String status) throws SQLException {
        String sql = "SELECT series_id, series_name, total_matches, completed_matches, year" +
                "FROM series WHERE series_status = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();

            int seriesId, totalMatches, completedMatches,year;
            String seriesName;

            System.out.println("Series menu");
            System.out.println("Id        Year  Total  Completed  Name");

            while (rs.next()) {
                seriesId = rs.getInt("series_id");
                seriesName = rs.getString("series_name");
                totalMatches = rs.getInt("total_matches");
                completedMatches = rs.getInt("completed_matches");
                year=rs.getInt("year");

                System.out.println(seriesId+"  "+year+"    "+totalMatches+"        "+completedMatches+"      "+seriesName);
            }
        }
    }

    public void displayTournamentIds(String status) throws SQLException {
        String sql="SELECT tournament_id,tournament_name,year FROM tournaments WHERE tournament_status=?";
        try(PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            int TournamentId,TournamentYear;
            String TournamentName ;
            System.out.println("Tournament menu");
            System.out.println("Id        Year  Name");
            while (rs.next()){
                TournamentId=rs.getInt("tournament_id");
                TournamentYear=rs.getInt("year");
                TournamentName=rs.getString("tournament_name");
                System.out.println(TournamentId+"   "+ TournamentYear+"   "+TournamentName);
            }
        }
    }
}
