
package DataBase;

import DataStructure.LinkedListOfPlayer;
import Simulation.Match;
import Simulation.Team;

import java.sql.*;
import java.util.ArrayList;


import static Simulation.Team.getTeamById;
public class sqlQuery {
    public static Connection getCon() throws SQLException {
        String url = "jdbc:mysql://localhost:8889/cricket Simulation";
        String user = "root";
        String password = "root";
        return DriverManager.getConnection(url, user, password);
    }

    public static void insertTeam(Connection con,int teamId, String name, String email) throws SQLException {
        String sql = "INSERT INTO Teams (team_id,team_name,Host_id) VALUES (?,?,(select user_id from Users where email=?))";

        try(PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1,teamId);
            ps.setString(2,name);
            ps.setString(3,email);
            ps.executeUpdate();
        }
    }

    public static void insertPlayer(Connection con,int playerId,String name, String role, String teamName) throws SQLException {
        String sql = "INSERT INTO Players (player_id, player_name, role, team_id) VALUES" +
                " (?,?, ?, (SELECT team_id FROM Teams WHERE team_name = ?))";

        try(PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setString(2, name);
            ps.setString(3, role);
            ps.setString(4, teamName);
            ps.executeUpdate();
        }
    }
    public static void insertSchedule(Connection con,int match_id,int team1Id,int team2Id,String emailId,int matchNumber,int over, String matchType, int typeId) throws SQLException {
        String sql = "INSERT INTO Matches(match_id ,Host_Id,team1_id,team2_id,Match_Number,inning_overs, match_type, match_type_id) VALUES" +
                " (?,(select user_id from Users where email=?),?,?,?,?,?,?)";

        try(PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, match_id);
            ps.setString(2, emailId);
            ps.setInt(3, team1Id);
            ps.setInt(4, team2Id);
            ps.setInt(5, matchNumber);
            ps.setInt(6, over);
            ps.setString(7, matchType);
            ps.setInt(8, typeId);
            ps.executeUpdate();
        }
    }

    public static void insertTournament(Connection con,int tournamentId,String tournament_name,int year,String email) throws SQLException {
        String sql = "INSERT INTO Tournaments(tournament_name ,year,host_id, tournament_id) VALUES " +
                "(?,?,(select user_id from Users where email=?),?)";

        try(PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tournament_name);
            ps.setInt(2, year);
            ps.setString(3, email);
            ps.setInt(4, tournamentId);
            ps.executeUpdate();
        }
    }
    public static void insertIntoPointsTable(Connection con,int tournamentId,int team_id) throws SQLException {
        String sql = "INSERT INTO PointsTable(tournament_id ,team_id) VALUES (?,?)";

        try(PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, team_id);
            ps.executeUpdate();
        }
    }

    public static void insertSeries(Connection con,int seriesId,String series_name,int team1Id,int team2Id,int total_matches,String email) throws SQLException {
        String sql = "INSERT INTO series(series_id, series_name, host_id, team1_id, team2_id,total_matches) VALUES " +
                "(?,?,(select user_id from Users where email=?),?,?,?)";

        try(PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, seriesId);
            ps.setString(2, series_name);
            ps.setString(3, email);
            ps.setInt(4, team1Id);
            ps.setInt(5, team2Id);
            ps.setInt(6, total_matches);
            ps.executeUpdate();
        }
    }

    public static void insertTeamMatchStats(int match_id, int  team_id,int tournament_id) throws SQLException {
        String sql = "INSERT INTO TeamMatchStats(tournament_id, match_id, team_id) " +
                "VALUES (?, ?, ? ) ";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tournament_id);
            ps.setInt(2, match_id);
            ps.setInt(3, team_id);
            ps.executeUpdate();
        }
    }

    public static void insertPlayerMatchStats(int match_id,int player_id) throws SQLException {
        String sql = "INSERT INTO PlayerMatchStats(match_id,player_id) " +
                "VALUES (?, ?)";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, match_id);
            ps.setInt(2, player_id);
            ps.executeUpdate();
        }
    }


    public static void insertBallByBall(int match_id, int innings, int over_number, int ball_number,
                                        int striker_id, int non_striker_id, int bowler_id,
                                        int runs_batsman, int extras_runs,
                                        Integer out_player_id, String ball_summary) throws SQLException {

        String sql = "CALL insertBallByBall(?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, match_id);
            ps.setInt(2, innings);
            ps.setInt(3, over_number);
            ps.setInt(4, ball_number);
            ps.setInt(5, striker_id);
            ps.setInt(6, non_striker_id);
            ps.setInt(7, bowler_id);
            ps.setInt(8, runs_batsman);
            ps.setInt(9, extras_runs);

            if (out_player_id != null) {
                ps.setInt(10, out_player_id);
            } else {
                ps.setNull(10, java.sql.Types.INTEGER);
            }

            ps.setString(11, ball_summary);

            ps.executeUpdate();
        }
    }


    public static void updateBattingStats(int matchId, int playerId, int runs, int ball, int four, int six) throws SQLException{
        String sql = "UPDATE PlayerMatchStats SET "
                + "runs_scored = ?,balls_faced = ?, "
                + "fours =  ?, sixes = ? "
                + "WHERE match_id = ? AND player_id = ?";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, runs);
            ps.setInt(2, ball);
            ps.setInt(3, four);
            ps.setInt(4, six);
            ps.setInt(5, matchId);
            ps.setInt(6, playerId);
            ps.executeUpdate();
        }
    }


    public static void updateBowlingStats(int matchId, int playerId, int wickets, double over, int runs_conceded) throws SQLException{
        String sql = "UPDATE PlayerMatchStats SET "
                + "wickets = ?,overs_bowled = ?, "
                + "runs_conceded = ? "
                + "WHERE match_id = ? AND player_id = ?";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, wickets);
            ps.setDouble(2, over);
            ps.setInt(3, runs_conceded);
            ps.setInt(4, matchId);
            ps.setInt(5, playerId);

            ps.executeUpdate();
        }
    }


    public static void updateTeamBattingStats(int matchId, int teamId, int runs, int wicket, double overPlayed) throws SQLException{
        String sql = "UPDATE TeamMatchStats SET runs_scored = ?,wickets_lost = ?, "
                + "overs_played =  ? WHERE match_id = ? AND team_id = ?";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, runs);
            ps.setInt(2, wicket);
            ps.setDouble(3, overPlayed);
            ps.setInt(4, matchId);
            ps.setInt(5, teamId);
            ps.executeUpdate();
        }
    }

    public static void updateBattingStatus(int match_id, int player_id, String status) throws SQLException {
        String sql = "UPDATE PlayerMatchStats SET BattingStats = ? WHERE match_id = ? AND player_id = ?";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, match_id);
            ps.setInt(3, player_id);
            ps.executeUpdate();
        }
    }

    public static void updateMatchStatus(int match_id, String status) throws SQLException {
        String sql = "UPDATE Matches SET match_status = ? WHERE match_id = ?";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, match_id);
            ps.executeUpdate();
        }
    }


    public static void updateWinner(int match_id, int winningTeamId) throws SQLException {
        String sql = "UPDATE Matches SET winner_team_id = ? WHERE match_id = ?";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, winningTeamId);
            ps.setInt(2, match_id);
            ps.executeUpdate();
        }
    }

    public static void updateTeamMatchResult(int match_id, int teamId, String result) throws SQLException {
        String sql = "UPDATE TeamMatchStats SET result = ? WHERE match_id = ? and team_id=?";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, result);
            ps.setInt(2, match_id);
            ps.setInt(3, teamId);
            ps.executeUpdate();
        }
    }


    public static void updatePointsTable(int points,int matches_won,int matches_lost,int matches_drawn,double nrr,int teamId, int tournamentId) throws SQLException {
        String sql = "CALL updatePointsTable(?, ?, ?, ?, ?, ?, ?)";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setInt(2, matches_won);
            ps.setInt(3, matches_lost);
            ps.setInt(4, matches_drawn);
            ps.setDouble(5, nrr);
            ps.setInt(6, teamId);
            ps.setInt(7, tournamentId);
            ps.executeUpdate();
        }
    }

    public static void updateBallByBallNoBall(int match_id,int inning,int over,int ball,int scoredRun,int extras) throws SQLException {
        String sql = "CALL updateBallByBallNoBall(?, ?, ?, ?, ?, ?)";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, match_id);
            ps.setInt(2, inning);
            ps.setInt(3, over);
            ps.setInt(4, ball);
            ps.setInt(5, scoredRun);
            ps.setInt(6, extras);
            ps.executeUpdate();
        }
    }

    public static void updateBallByBallWicket(int match_id,int outPlayerId,int inning,int over,int ball,int scoredRun) throws SQLException {
        String sql = "CALL updateBallByBallWicket(?, ?, ?, ?, ?, ?);";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, match_id);
            ps.setInt(2, outPlayerId);
            ps.setInt(3, inning);
            ps.setInt(4, over);
            ps.setInt(5, ball);
            ps.setInt(6, scoredRun);
            ps.executeUpdate();
        }
    }

    public static void undoLastBallOfMatchInning(int matchId, int innings) throws SQLException {
        String sql = "CALL undoLastBallOfMatchInning(?, ?)";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, matchId);
            ps.setInt(2, innings);
            ps.executeUpdate();
        }
    }

    public static ArrayList<Team> getTeamData(String email) throws SQLException {
        ArrayList<Team> teams = new ArrayList<>();

        String sql = "CALL getTeamDataByEmail(?)";

        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Team teamFromDb = new Team(rs.getString("team_name"), rs.getInt("team_id"));
                teams.add(teamFromDb);
            }
        }
        return teams;
    }

    public static ArrayList<Match> getSchedule(String email, ArrayList<Team> Teams, String matchType) throws SQLException {
        ArrayList<Match> schedule = new ArrayList<>();

        String sql = "CALL getScheduleByEmail(?,?)";

        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, matchType);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Match match = new Match(
                        getTeamById(Teams, rs.getInt("team1_id")),
                        getTeamById(Teams, rs.getInt("team2_id")),
                        rs.getInt("match_id"),matchType);

                match.setInningOvers(rs.getInt("inning_overs"));
                match.setMatchStatus(rs.getString("match_status"));
                schedule.add(match);
            }
        }
        return schedule;
    }



    public static LinkedListOfPlayer getPlayersForTeamsInRoleOrder(Team team) throws SQLException {
        LinkedListOfPlayer allPlayers = new LinkedListOfPlayer();

        String sql = "CALL getPlayersForTeam(?)";

        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, team.getTeamId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String name = rs.getString("player_name");
                int id = rs.getInt("player_id");
                String role = rs.getString("role");

                if (role.equalsIgnoreCase("BATSMAN"))
                    allPlayers.addBatsman(name, id);
                else if (role.equalsIgnoreCase("ALLROUNDER"))
                    allPlayers.addAllrounder(name, id);
                else
                    allPlayers.addBowler(name, id);
            }
        }
        return allPlayers;
    }

    public static ArrayList<Team> getTopTeams(int tournamentId) throws SQLException {
        ArrayList<Team> topTeams = new ArrayList<>();
        String sql = "SELECT t.team_id, t.team_name " +
                "FROM PointsTable pt " +
                "JOIN Teams t ON pt.team_id = t.team_id " +
                "WHERE pt.tournament_id = ? " +
                "ORDER BY pt.points DESC, pt.net_run_rate DESC " +
                "LIMIT 4";

        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                topTeams.add(new Team(rs.getString("team_name"), rs.getInt("team_id")));
            }
        }
        return topTeams;
    }

    public static boolean isTournament(int tId) throws SQLException {
        String sql = "SELECT is_tournament(?) as result";
        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getBoolean("result");
        }
    }

    public static boolean isTournamentDone(int tId) throws SQLException {
        String sql = "SELECT all_matches_done(?) as result";
        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getBoolean("result");
        }
    }


    public static int getTournamentId(String email) throws SQLException {
        String sql = "SELECT tournament_id FROM Tournaments WHERE host_id=(select user_id from Users where email=?)" +
                " and tournament_status <> 'COMPLETED'";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("tournament_id");
            } else {
                return -1;
            }
        }
    }
    public static int getSeriesId(String email) throws SQLException {
        String sql = "SELECT series_id FROM series WHERE host_id=(select user_id from Users where email=?) and series_status <> 'COMPLETED'";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("series_id");
            } else {
                return -1;
            }
        }
    }

    public static boolean arePlayoffsScheduled(int tournamentId) throws SQLException {
        String sql = "SELECT COUNT(*) AS match_count FROM Matches " +
                "WHERE match_type_id = ? AND match_type <> 'TOURNAMENT'";

        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, tournamentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("match_count") > 0;
            }
        }
        return false;
    }


    public static int getTournamentOver(int tournamentId) throws SQLException {
        String sql = "SELECT DISTINCT inning_overs FROM Matches WHERE match_type_id = ?";

        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, tournamentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("inning_overs");
            }
        }
        return 2;
    }

    public static Team getMatchWinner(String matchType, int tournamentId) throws SQLException {
        String sql = "SELECT team_id, team_name FROM teams WHERE " +
                "team_id=(SELECT winner_team_id FROM matches WHERE match_type=? and match_type_id=?) ";
        try (Connection con = getCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matchType);
            ps.setInt(2, tournamentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return (new Team(rs.getString("team_name"),rs.getInt("team_id")) ) ;
                }
            }
        }
        return null;
    }

    public static boolean isFinalAdded(int tournamentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Matches WHERE match_type_id = ? AND match_type = 'FINAL'";
        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static void updateTournamentComplete(int tournamentId,int winnerId) throws SQLException {
        String sql = "UPDATE tournaments SET tournament_status='COMPLETED',winner_team_id=? where tournament_id=?";
        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1,winnerId);
            ps.setInt(2,tournamentId);
            ps.executeUpdate();
        }

    }

    public static void updateCompletedMatches(int seriesId) throws SQLException {
        String sql = "UPDATE series SET completed_matche = completed_matche + 1 WHERE series_id = ?";

        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, seriesId);
            ps.executeUpdate();
        }
    }
    public static boolean isSingleMatchAlreadySet(String email) throws SQLException {
        String sql = "SELECT COUNT(match_id) AS match_count FROM matches " +
                "WHERE Host_Id=(select user_id from Users where email=?) AND match_type='SINGLE' AND match_status <>'COMPLETED'";
        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1,email);
            ResultSet rs=ps.executeQuery();
            rs.next();
            return (rs.getInt("match_count")>0);
        }
    }

    public static boolean isTournamentAlreadySet(String email) throws SQLException {
        String sql = "SELECT COUNT(tournament_id) as tournament_count FROM tournaments WHERE " +
                "Host_Id=(select user_id from Users where email=?) AND tournament_status <> 'COMPLETED'";
        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1,email);
            ResultSet rs=ps.executeQuery();
            rs.next();
            return (rs.getInt("tournament_count")==1);
        }
    }

    public static boolean isSeriesAlreadySet(String email) throws SQLException {
        String sql = "SELECT COUNT(series_id) AS seriesCount FROM series WHERE host_id=(select user_id from Users where email=?) and series_status <> 'COMPLETED'";

        try(Connection con = getCon();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt("seriesCount")>0;
        }
    }
    public static void displayPointsTable(int tournamentId){
        String query = "SELECT t.team_name, pt.matches_played, pt.matches_won, " +
                "pt.matches_lost, pt.matches_drawn, pt.points, pt.net_run_rate " +
                "FROM pointstable pt " +
                "JOIN teams t ON pt.team_id = t.team_id " +
                "WHERE pt.tournament_id = ? " +
                "ORDER BY pt.points DESC, pt.net_run_rate DESC";

        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, tournamentId);
            ResultSet rs = ps.executeQuery();

            System.out.println("Team Name" + " ".repeat(26 - "Team Name".length())
                    + "P" + " ".repeat(5)
                    + "W" + " ".repeat(5)
                    + "L" + " ".repeat(5)
                    + "D" + " ".repeat(5)
                    + "Points" + " ".repeat(8 - "Points".length())
                    + "NRR");

            System.out.println("-".repeat(65));

            while (rs.next()) {
                String teamName = rs.getString("team_name");
                int played = rs.getInt("matches_played");
                int won = rs.getInt("matches_won");
                int lost = rs.getInt("matches_lost");
                int drawn = rs.getInt("matches_drawn");
                int points = rs.getInt("points");
                double nrr = rs.getDouble("net_run_rate");

                System.out.println(teamName + " ".repeat(26 - teamName.length())
                        + played + " ".repeat(6 - String.valueOf(played).length())
                        + won + " ".repeat(6 - String.valueOf(won).length())
                        + lost + " ".repeat(6 - String.valueOf(lost).length())
                        + drawn + " ".repeat(6 - String.valueOf(drawn).length())
                        + points + " ".repeat(8 - String.valueOf(points).length())
                        + String.format("%.3f", nrr));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}