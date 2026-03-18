package Simulation;

import DataStructure.LinkedListOfPlayer;
import DataStructure.StackOfBatsman;
import DataStructure.StackOfBowler;
import DataStructure.StackOfTeam;
import Validetion.*;

import java.sql.SQLException;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.Stack;

import static DataBase.sqlQuery.*;
import static DataBase.sqlQuery.updateMatchStatus;


public class MatchSimulation {
    static LinkedListOfPlayer team1Players;
    static LinkedListOfPlayer team2Players;
    static Team team1;
    static Team team2;
    private static LinkedListOfPlayer.Player Batsman1;
    private static LinkedListOfPlayer.Player Batsman2;
    private static StackOfBatsman Bat1;
    private static StackOfBatsman Bat2;
    private static StackOfBowler Ball;
    private static StackOfTeam BatTeam;

    static int getValidCount(Scanner sc, int min) {
        while (true) {
            try {
                int count = sc.nextInt();
                if (count < min) {
                    throw new InvalidCountException("Minimum " + min + " required");
                }
                return count;
            } catch (InvalidCountException e) {
                System.out.println(e.getMessage());
                sc.nextLine();
            } catch (Exception e) {
                System.out.println("Invalid input");
                sc.nextLine();
            }
        }
    }

    public static Team whoBat(Scanner sc, Team team1, Team team2, Team tosswin) {
        int choice = getValidChoice(sc,
                "What would you decide [1 for BAT, 2 for BALL]? ", 1, 2);

        if (choice == 1) {
            System.out.println(tosswin.teamName + " decided to BAT first\n");
            return tosswin;
        } else {
            Team batting = (team1 == tosswin) ? team2 : team1;
            System.out.println(tosswin.teamName + " decided to BALL first\n");
            return batting;
        }
    }

    public static int getValidChoice(Scanner sc, String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int choice = sc.nextInt();
                if (choice < min || choice > max) {
                    throw new InvalidChoiceException("Please enter between " + min + " and " + max);
                }
                if(prompt.equalsIgnoreCase("even only:") && choice%2!=0){
                    throw new InvalidChoiceException("Please enter even number");
                }
                sc.nextLine();
                return choice;
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid number");
                sc.nextLine();
            } catch (InvalidChoiceException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static int getNoballChoice(Scanner sc) {
        while (true) {
            try {
                int run = sc.nextInt();
                if (run < 0 || run > 6 || run == 5) {
                    throw new InvalidRunException("Enter valid run [(0-4) or 6)]:");
                }
                sc.nextLine();
                return run;
            } catch (InvalidRunException e) {
                System.out.println(e.getMessage());
                sc.nextLine();
            }
        }
    }

    public static void displayScorecard(int overNumber, int ballNumberOfOver, Team BattingTeam,
                                        LinkedListOfPlayer.Player Batsman1,
                                        LinkedListOfPlayer.Player Batsman2,
                                        LinkedListOfPlayer.Player Bowler,
                                        int Target, int inning) {

        System.out.println("-----------------");
        String teamLine = BattingTeam.teamName + (BattingTeam.freeHitActive ? " [FREE HIT]" : "");
        System.out.println(teamLine + " ".repeat(Math.max(0, 25 - teamLine.length())));

        String scorePart = "(" + BattingTeam.totalRun + "/" + BattingTeam.wicketDown + ")";
        String overPart = "OVER: (" + overNumber + "." + ballNumberOfOver + ")";
        String targetPart = (inning == 2) ? "Target " + Target : "";

        System.out.println(scorePart + " ".repeat(Math.max(0, 15 - scorePart.length())) +
                        overPart + " ".repeat(Math.max(0, 15 - overPart.length())) +
                        targetPart);

        double crr = (overNumber == 0 && ballNumberOfOver == 0) ? 0.0 :
                (BattingTeam.totalRun * 6.0) / (overNumber * 6 + ballNumberOfOver);
        System.out.println("CRR: " + String.format("%.2f", crr));


        System.out.println();
        System.out.println("Batter               R     B     4s     6s");
        printBatsmanWithSpacing(Batsman1);
        printBatsmanWithSpacing(Batsman2);


        System.out.println();
        System.out.println("Bowler               O     R     W");
        String bowlerName = Bowler.getPlayerName();
        System.out.println(
                bowlerName + " ".repeat(Math.max(0, 21 - bowlerName.length())) +
                        (Bowler.getOversBowled()+"."+ballNumberOfOver) + " ".repeat(6 - (Bowler.getOversBowled()+"."+ballNumberOfOver).length()) +
                        Bowler.getRunsGiven() + " ".repeat(6 - String.valueOf(Bowler.getRunsGiven()).length()) +
                        Bowler.getWickets());

        System.out.println("-----------------\n");
    }

    private static void printBatsmanWithSpacing(LinkedListOfPlayer.Player batsman) {
        String name = batsman.getPlayerName() + (batsman.isOnStrike() ? "*" : "");
        System.out.println(
                name + " ".repeat(Math.max(0, 21 - name.length())) +
                        batsman.getRunsScored() + " ".repeat(6 - String.valueOf(batsman.getRunsScored()).length()) +
                        batsman.getBallsFaced() + " ".repeat(6 - String.valueOf(batsman.getBallsFaced()).length()) +
                        batsman.getFours() + " ".repeat(7 - String.valueOf(batsman.getFours()).length()) +
                        batsman.getSixes());
    }


    public static void StrickRotete(LinkedListOfPlayer.Player Batsman1, LinkedListOfPlayer.Player Batsman2) {
        boolean temp = Batsman1.isOnStrike();
        Batsman1.setOnStrike(Batsman2.isOnStrike());
        Batsman2.setOnStrike(temp);
    }

    private static void handleNormalBall(Team battingTeam, LinkedListOfPlayer.Player batsman,
                                         LinkedListOfPlayer.Bowler Bowler, Stack<String> overStack,
                                         int run, String msg) {
        System.out.println(msg);
        batsman.setRunsScored(batsman.getRunsScored() + run);
        batsman.setBallsFaced(batsman.getBallsFaced() + 1);
        Bowler.setRunsGiven(Bowler.getRunsGiven() + run);
        battingTeam.totalRun += run;
        overStack.push("" + run);
        if (run == 4)
            batsman.setFours(batsman.getFours() + 1);
        else if (run == 6)
            batsman.setSixes(batsman.getSixes() + 1);
        if(run%2!=0){
            StrickRotete(Batsman1,Batsman2);
        }
        battingTeam.freeHitActive = false;
    }

    private static void handleNoBall(Scanner sc, Team battingTeam, LinkedListOfPlayer.Player batsman,
                                     LinkedListOfPlayer.Bowler Bowler, Stack<String> overStack,int matchId,
                                     int inning,int over,int ball) throws SQLException {
        System.out.println("No ball! Free hit awarded for next ball!");
        battingTeam.freeHitActive = true;

        Bowler.setRunsGiven(Bowler.getRunsGiven() + 1);

        System.out.println("Enter e/E if Extra runs and Enter o/O if Runout in Noball else press [ENTER]:");
        String inNoball = sc.nextLine();
        if (inNoball.equalsIgnoreCase("E")) {
            int extra = Extras(sc, battingTeam, batsman, Bowler);
            updateBallByBallNoBall(matchId,inning,over,ball,0,(extra+1));
            overStack.push("No/" + extra);
        } else if (inNoball.equalsIgnoreCase("O")) {
            handleRunout(sc, battingTeam, Batsman1.isOnStrike() ? Batsman1 : Batsman2,
                    Batsman1.isOnStrike() ? Batsman2 : Batsman1, Bowler,matchId,inning,over,ball);
            battingTeam.freeHitActive = false;
        } else {
            System.out.println("Enter runs scored off the no ball (0-4)/6");
            int runs = getNoballChoice(sc);
            updateBallByBallNoBall(matchId,inning,over,ball,runs,1);
            handleNoBall(battingTeam, batsman, Bowler, runs);
            overStack.push("No/" + runs);
        }
    }

    private static int handleWide(Scanner sc, Team battingTeam, LinkedListOfPlayer.Player batsman,
                                   LinkedListOfPlayer.Bowler Bowler, Stack<String> overStack) {
        System.out.println("Wide!");
        System.out.println("Enter y/Y if Extra runs in wide else press [ENTER]:");
        int extra=0;
        if (sc.nextLine().equalsIgnoreCase("y")) {
            extra = Extras(sc, battingTeam, batsman, Bowler);
            overStack.push("W/" + extra);

        } else {
            overStack.push("W");
        }
        battingTeam.totalRun += 1;
        Bowler.setRunsGiven(Bowler.getRunsGiven() + 1);
        return extra+1;
    }

    private static int handleExtras(Scanner sc, Team battingTeam, LinkedListOfPlayer.Player batsman,
                                     LinkedListOfPlayer.Bowler Bowler, Stack<String> overStack) {
        int extra = Extras(sc, battingTeam, batsman, Bowler);
        overStack.push("E/" + extra);
        return extra;
    }
    private static boolean handleWicket(Scanner sc, Team battingTeam, LinkedListOfPlayer.Bowler Bowler,
                                        Stack<String> overStack, int totalPlayers, boolean isTeam1Batting,int matchID, int inning,int over,int ball) throws SQLException {

        System.out.println("Enter y/Y if this is a Runout else press [ENTER]:");
        boolean isRunout = sc.nextLine().equalsIgnoreCase("y");

        if (isRunout) {
            return handleRunoutWicket(sc, battingTeam, Bowler, overStack, totalPlayers,matchID,inning,over,ball);
        } else if (!battingTeam.freeHitActive) {
            return handleRegularWicket(sc, battingTeam, Bowler, overStack, totalPlayers,isTeam1Batting,matchID,inning,over,ball);
        } else {
            System.out.println("Free hit active - cannot be out except runout!");
            return false;
        }
    }

    private static boolean handleRunoutWicket(Scanner sc, Team battingTeam, LinkedListOfPlayer.Bowler Bowler,
                                              Stack<String> overStack, int totalPlayers,int matchID, int inning,int over,int ball) throws SQLException {
        System.out.println("Runout!");
        battingTeam.wicketDown++;
        LinkedListOfPlayer.Player onStrinck = Batsman1.isOnStrike() ? Batsman1 : Batsman2;

        int runs = handleRunout(sc, battingTeam, onStrinck,
                (onStrinck == Batsman1 ? Batsman2 : Batsman1), Bowler,matchID,inning,over,ball);
        overStack.push("RO/" + runs);

        return battingTeam.wicketDown == totalPlayers - 1;
    }

    private static boolean handleRegularWicket(Scanner sc, Team battingTeam, LinkedListOfPlayer.Bowler Bowler,
                                               Stack<String> overStack, int totalPlayers, boolean isTeam1Batting,int matchID,int inning,int over,int ball) throws SQLException {
        System.out.println("Wicket!");
        battingTeam.wicketDown++;
        LinkedListOfPlayer.Player outPlayer = Batsman1.isOnStrike() ? Batsman1 : Batsman2;
        updateBallByBallWicket(matchID,outPlayer.getPlayerId(),inning,over,ball,0);

        overStack.push("O");
        Bowler.setWickets(Bowler.getWickets() + 1);

        if (battingTeam.wicketDown == totalPlayers - 1)
            return true;

        return replaceBatsman(sc, battingTeam, outPlayer, isTeam1Batting,matchID);
    }

    private static boolean replaceBatsman(Scanner sc, Team battingTeam, LinkedListOfPlayer.Player outPlayer,
                                          boolean isTeam1Batting,int matchID) throws SQLException {
        LinkedListOfPlayer playerList = isTeam1Batting ? team1Players : team2Players;
        playerList.displayAvailablePlayersName();
        updateBattingStatus(matchID,outPlayer.getPlayerId(),"OUT");
        outPlayer.setPlaying(false);
        outPlayer.setOut(true);

        LinkedListOfPlayer.Player newBatsman = getValidPlayer(sc, playerList, "new batsman");
        newBatsman.setPlaying(true);
        updateBattingStatus(matchID,newBatsman.getPlayerId(),"PLAYING");


        if (outPlayer == Batsman1) {
            Batsman1 = newBatsman;
            Batsman1.setOnStrike(true);
        } else {
            Batsman2 = newBatsman;
            Batsman2.setOnStrike(false);
        }
        battingTeam.freeHitActive = false;

        return false;
    }

    private static int handleRunout(Scanner sc, Team team, LinkedListOfPlayer.Player striker,
                                    LinkedListOfPlayer.Player nonStriker, LinkedListOfPlayer.Bowler Bowler,int matchID,int inning,int over,int ball) throws SQLException {
        System.out.println("Enter runs scored before runout:");
        int runs = getValidChoice(sc, "Enter runs (0-2):", 0, 2);

        team.totalRun += runs;
        striker.setRunsScored(striker.getRunsScored() + runs);
        Bowler.setRunsGiven(Bowler.getRunsGiven() + runs);

        System.out.println("Who got run out? [1 for Striker / 2 for Non-Striker]");
        int whoRunOut = getValidChoice(sc, "Choose 1 or 2:", 1, 2);

        System.out.println("Which end was the runout? [1 for Keeper's End / 2 for Bowler's End]");
        int runoutEnd = getValidChoice(sc, "Choose 1 or 2:", 1, 2);

        LinkedListOfPlayer currentTeam = (team == team1) ? team1Players : team2Players;
        currentTeam.displayAvailablePlayersName();

        LinkedListOfPlayer.Player newBatsman = getValidPlayer(sc, currentTeam, "new batsman");
        newBatsman.setPlaying(true);
        updateBattingStatus(matchID,newBatsman.getPlayerId(),"PLAYING");


        LinkedListOfPlayer.Player outPlayer = (whoRunOut == 1) ? striker : nonStriker;
        updateBattingStatus(matchID,outPlayer.getPlayerId(),"OUT");
        outPlayer.setOut(true);
        outPlayer.setPlaying(false);
        updateBallByBallWicket(matchID,outPlayer.getPlayerId(),inning,over,ball,runs);


        if (outPlayer == Batsman1 && runoutEnd==1) {
            Batsman1 = newBatsman;
            Batsman1.setOnStrike(true);
            Batsman2.setOnStrike(false);
        } else if(outPlayer == Batsman1 && runoutEnd==2){
            Batsman1 = newBatsman;
            Batsman2.setOnStrike(true);
            Batsman1.setOnStrike(false);
        } else if (outPlayer == Batsman2 && runoutEnd==1) {
            Batsman2 = newBatsman;
            Batsman2.setOnStrike(true);
            Batsman1.setOnStrike(false);
        }
        else {
            Batsman2 = newBatsman;
            Batsman1.setOnStrike(true);
            Batsman2.setOnStrike(false);
        }

        return runs;
    }



    private static void playInnings(Scanner sc, Team BattingTeam, boolean isTeam1Batting,
                                    int totalOvers, int inning, int NumberOfPlayer, int target,int matchId) throws SQLException {
        int ballNumberOfOver = 0;
        int overNumber;
        final int dotBall = 0;
        final int SINGLE = 1;
        final int DOUBLE = 2;
        final int threeRuns = 3;
        final int FOUR = 4;
        final int SIX = 6;

        LinkedListOfPlayer.Bowler Bowler = getValidBowler(sc, isTeam1Batting ? team2Players : team1Players, null);
        Stack<String> ThisOver = new Stack<>();
        outerLoop: for (overNumber = 0; overNumber < totalOvers; overNumber++) {

            ThisOver.clear();

            for (ballNumberOfOver = 0; ballNumberOfOver < 6; ballNumberOfOver++) {


                displayScorecard(overNumber, ballNumberOfOver, BattingTeam, Batsman1, Batsman2, Bowler, target, inning);
                if (!ThisOver.isEmpty()) {
                    System.out.println("Over " + (overNumber + 1) + ":" + ThisOver);
                }

                LinkedListOfPlayer.Player CurrentBatsman = Batsman1.isOnStrike() ? Batsman1 : Batsman2;
                LinkedListOfPlayer.Player nonStricker= (!Batsman1.isOnStrike()) ? Batsman1 : Batsman2;
                String runInput = getValidRunInput(sc);

                BatTeam.push(new Team(BattingTeam));
                Ball.push(new LinkedListOfPlayer.Bowler(Bowler));
                Bat1.push(new LinkedListOfPlayer.Player(Batsman1));
                Bat2.push(new LinkedListOfPlayer.Player(Batsman2));

                switch (runInput) {
                    case "0":
                        handleNormalBall(BattingTeam, CurrentBatsman, Bowler, ThisOver, dotBall, "DotBall");
                            insertBallByBall(matchId,inning,overNumber,ballNumberOfOver, CurrentBatsman.getPlayerId(),nonStricker.getPlayerId(),
                                    Bowler.getPlayerId(),dotBall,0,null,"DotBall");
                        break;

                    case "1":
                        handleNormalBall(BattingTeam, CurrentBatsman, Bowler, ThisOver, SINGLE, "Single");
                        insertBallByBall(matchId,inning,overNumber,ballNumberOfOver, CurrentBatsman.getPlayerId(),nonStricker.getPlayerId(),
                                Bowler.getPlayerId(),SINGLE,0,null,"Single");

                        break;

                    case "2":
                        handleNormalBall(BattingTeam, CurrentBatsman, Bowler, ThisOver, DOUBLE, "Double");
                        insertBallByBall(matchId,inning,overNumber,ballNumberOfOver, CurrentBatsman.getPlayerId(),nonStricker.getPlayerId(),
                                Bowler.getPlayerId(),DOUBLE,0,null,"Double");

                        break;

                    case "3":
                        handleNormalBall(BattingTeam, CurrentBatsman, Bowler, ThisOver, threeRuns, "Three runs");
                        insertBallByBall(matchId,inning,overNumber,ballNumberOfOver, CurrentBatsman.getPlayerId(),nonStricker.getPlayerId(),
                                Bowler.getPlayerId(),threeRuns,0,null,"Three runs");

                        break;

                    case "4":
                        handleNormalBall(BattingTeam, CurrentBatsman, Bowler, ThisOver, FOUR , "Boundary");
                        insertBallByBall(matchId,inning,overNumber,ballNumberOfOver, CurrentBatsman.getPlayerId(),nonStricker.getPlayerId(),
                                Bowler.getPlayerId(),FOUR,0,null,"Boundary");

                        break;

                    case "6":
                        handleNormalBall(BattingTeam, CurrentBatsman, Bowler, ThisOver, SIX, "Six");
                        insertBallByBall(matchId,inning,overNumber,ballNumberOfOver, CurrentBatsman.getPlayerId(),nonStricker.getPlayerId(),
                                Bowler.getPlayerId(),SIX,0,null,"Six");

                        break;

                    case "N":
                        insertBallByBall(matchId,inning,overNumber,ballNumberOfOver, CurrentBatsman.getPlayerId(),nonStricker.getPlayerId(),
                                Bowler.getPlayerId(),dotBall,0,null,"NoBall");
                        handleNoBall(sc, BattingTeam, CurrentBatsman, Bowler, ThisOver,matchId,inning,overNumber,ballNumberOfOver);

                        ballNumberOfOver--;
                        break;

                    case "W":
                       int wideRuns = handleWide(sc, BattingTeam, CurrentBatsman, Bowler, ThisOver);
                        insertBallByBall(matchId,inning,overNumber,ballNumberOfOver, CurrentBatsman.getPlayerId(),nonStricker.getPlayerId(),
                                Bowler.getPlayerId(),0,wideRuns,null,"Wide");
                        ballNumberOfOver--;
                        break;

                    case "E":
                        int extras=handleExtras(sc, BattingTeam, CurrentBatsman, Bowler, ThisOver);
                        insertBallByBall(matchId,inning,overNumber,ballNumberOfOver, CurrentBatsman.getPlayerId(),nonStricker.getPlayerId(),
                                Bowler.getPlayerId(),0,extras,null,"Extras");

                        break;

                    case "O":
                        insertBallByBall(matchId,inning,overNumber,ballNumberOfOver, CurrentBatsman.getPlayerId(),nonStricker.getPlayerId(),
                                Bowler.getPlayerId(),0,0,null,"Wicket");
                        boolean end = handleWicket(sc, BattingTeam, Bowler, ThisOver, NumberOfPlayer, isTeam1Batting,matchId,inning,overNumber,ballNumberOfOver);
                        if (end)
                            break outerLoop;
                        break;

                    case "U":
                        if(ThisOver.isEmpty())
                            System.out.println("Enter overs first ball input");
                        else {
                            Bat1.pop();
                            Batsman1 = Bat1.pop();

                            Bat2.pop();
                            Batsman2 = Bat2.pop();

                            Ball.pop();
                            Bowler = Ball.pop();

                            BatTeam.pop();
                            BattingTeam = BatTeam.pop();

                            String checkUndo = ThisOver.pop();
                            undoLastBallOfMatchInning(matchId,inning);
                            if (!checkUndo.matches("[NW]")) {
                                    ballNumberOfOver -= 2;
                            }
                        }
                        break;



                    default:
                        System.out.println("Invalid input, ball will be repeated");
                        ballNumberOfOver--;
                        break;

                }

                double overPlayed = Double.parseDouble(overNumber + "." + (Math.max(ballNumberOfOver, 0)));
                BattingTeam.setOverPlayed(overPlayed);
                updateBattingStats(matchId,Batsman1.getPlayerId(),Batsman1.getRunsScored(),Batsman1.getBallsFaced(), Batsman1.getFours(), Batsman1.getSixes());
                updateBattingStats(matchId,Batsman2.getPlayerId(),Batsman2.getRunsScored(),Batsman2.getBallsFaced(), Batsman2.getFours(), Batsman2.getSixes());
                updateTeamBattingStats(matchId,BattingTeam.teamId,BattingTeam.totalRun,BattingTeam.wicketDown, overPlayed);
                updateBowlingStats(matchId,Bowler.getPlayerId(), Bowler.getWickets(),Double.parseDouble(Bowler.getOversBowled()+"."+(Math.max(ballNumberOfOver, 0))),Bowler.getRunsGiven());

                if(ballNumberOfOver==5){
                    System.out.println("Enter u/U for change last ball input else press [ENTER]");
                    if(sc.nextLine().equalsIgnoreCase("U")){
                        Batsman1=Bat1.pop();
                        Batsman2=Bat2.pop();
                        Bowler=Ball.pop();
                        BattingTeam=BatTeam.pop();
                        ThisOver.pop();
                        ballNumberOfOver--;
                    }
                }


                if (inning == 2 && BattingTeam.totalRun >= target) {
                    if(ballNumberOfOver!=5)
                        ballNumberOfOver++;
                    else {
                        overNumber++;
                        ballNumberOfOver=0;
                    }
                    BattingTeam.won = true;
                    break outerLoop;
                }
            }
            StrickRotete(Batsman1,Batsman2);

            System.out.println("Over " + (overNumber + 1) + " completed.");
            Bowler.setOversBowled(Bowler.getOversBowled() + 1);
            if (overNumber < totalOvers - 1) {
                Bowler = getValidBowler(sc, isTeam1Batting ? team2Players : team1Players, Bowler);
            }
        }
        updateBattingStats(matchId,Batsman1.getPlayerId(),Batsman1.getRunsScored(),Batsman1.getBallsFaced(), Batsman1.getFours(), Batsman1.getSixes());
        updateBattingStats(matchId,Batsman2.getPlayerId(),Batsman2.getRunsScored(),Batsman2.getBallsFaced(), Batsman2.getFours(), Batsman2.getSixes());
        updateTeamBattingStats(matchId,BattingTeam.teamId,BattingTeam.totalRun,BattingTeam.wicketDown,Double.parseDouble(overNumber+"."+((ballNumberOfOver==6)?0:ballNumberOfOver)));
        updateBowlingStats(matchId,Bowler.getPlayerId(),Bowler.getWickets(),Double.parseDouble(Bowler.getOversBowled()+"."+((   ballNumberOfOver==6)?0:ballNumberOfOver)), Bowler.getRunsGiven());
        if (inning == 1) {
            System.out.println("Inning 1 completed.");
            team1.Target = team1.totalRun + 1;
        } else {
            System.out.println("Match ended.");
        }

        if (overNumber == totalOvers) {
            displayScorecard(totalOvers, 0, BattingTeam, Batsman1, Batsman2, Bowler, target, inning);
        } else {
            displayScorecard(overNumber, ballNumberOfOver, BattingTeam, Batsman1, Batsman2, Bowler, target, inning);
        }
    }

    private static LinkedListOfPlayer.Player getValidPlayer(Scanner sc, LinkedListOfPlayer team, String role) {
        while (true) {
            System.out.print("Enter " + role + " name: ");
            String name = sc.nextLine().toUpperCase();
            LinkedListOfPlayer.Player player = team.getPlayer(name);

            if (player == null) {
                System.out.println("Player not found!");
                team.displayAvailablePlayersName();
                continue;
            }

            if (player.isOut()) {
                System.out.println("Player already out or playing!");
                continue;
            }

            return player;
        }
    }
    private static LinkedListOfPlayer.Bowler getValidBowler(Scanner sc, LinkedListOfPlayer team, LinkedListOfPlayer.Bowler previousBowler) {
        team.displayBowlersAndAllRoundersName(previousBowler);

        while (true) {
            System.out.print("Enter new bowler name: ");
            String name = sc.nextLine().toUpperCase();
            LinkedListOfPlayer.Player player = team.getPlayer(name);

            if (player == null) {
                System.out.println("Player not found!");
                team.displayBowlersAndAllRoundersName(previousBowler);
                continue;
            }

            if (player == previousBowler) {
                System.out.println("Same bowler cannot bowl consecutive overs!");
                continue;
            }

            if (!(player instanceof LinkedListOfPlayer.Bowler)) {
                System.out.println("Selected player cannot bowl!");
                continue;
            }

            return (LinkedListOfPlayer.Bowler) player;
        }
    }

    private static String getValidRunInput(Scanner sc) {
        while (true) {
            System.out.println("Enter run");
            System.out.print("0 1 2 3 4 6 \nN/n for <Noball> W/w for <wide> O/o for <Out> E/e for <Extras>\nU/u for <Undo>:");
            String input = sc.nextLine().toUpperCase();

            if (input.matches("[012346NWOEU]")) {
                return input;
            }
            System.out.println("Invalid input! Please try again.");
        }
    }

    private static void handleNoBall(Team team, LinkedListOfPlayer.Player batsman, LinkedListOfPlayer.Bowler Bowler, int runs) {
        team.totalRun += (1 + runs);
        batsman.setRunsScored(batsman.getRunsScored() + runs);
        Bowler.setRunsGiven(Bowler.getRunsGiven() +1 + runs);

        if (runs == 6) {
            batsman.setSixes(batsman.getSixes() + 1);
        } else if (runs == 4) {
            batsman.setFours(batsman.getFours() + 1);
        }

        if (runs % 2 == 1) {
            StrickRotete(batsman, (batsman == Batsman1 ? Batsman2 : Batsman1));
        }
    }

    private static int Extras(Scanner sc, Team team, LinkedListOfPlayer.Player batsman, LinkedListOfPlayer.Bowler Bowler) {
        System.out.println("Enter runs scored ball (0-4)");
        int runs = getValidChoice(sc, "Extras", 0, 4);
        team.totalRun += runs;
        Bowler.setRunsGiven(Bowler.getRunsGiven() + runs);

        if (runs % 2 == 1) {
            StrickRotete(batsman, (batsman == Batsman1 ? Batsman2 : Batsman1));
        }
        return runs;
    }

    private static void displayMatchResult(Team BattingTeam, int numberOfPlayer,Match match, int TournamentId) throws SQLException {
        boolean isTournament=match.matchType.equalsIgnoreCase("TOURNAMENT");
        Team WIN = null;
        Team LOSS = null;
        double nrr;
        System.out.println("\n=== MATCH RESULT ===");
        if (BattingTeam.won) {
            WIN=BattingTeam;
            LOSS = (BattingTeam == team1)?team2:team1;
            System.out.println(WIN.teamName + " won by " + (numberOfPlayer - BattingTeam.wicketDown - 1) + " wicket(s).");
            if (isTournament){
                nrr=calculateMatchNRR(WIN,LOSS);
                updatePointsTable(2,1,0,0,nrr,WIN.teamId,TournamentId);
                updatePointsTable(0,0,1,0,(-1*nrr),LOSS.teamId,TournamentId);
            }
        }else if(team1.totalRun==team2.totalRun){
            System.out.println("Match DRAW");
            if (isTournament){
                updatePointsTable(1,0,0,1,0,team1.teamId,TournamentId);
                updatePointsTable(1,0,0,1,0,team2.teamId,TournamentId);
            }
        }
        else {
            WIN = (BattingTeam == team1)?team2:team1;
            LOSS = (BattingTeam == team1)?team1:team2;
            System.out.println(WIN.teamName + " won by " + (WIN.totalRun - LOSS.totalRun) + " runs.");
            if (isTournament){
                nrr=calculateMatchNRR(WIN,LOSS);
                updatePointsTable(2,1,0,0,nrr,WIN.teamId,TournamentId);
                updatePointsTable(0,0,1,0,(-1*nrr),LOSS.teamId,TournamentId);
            }

        }
        if(WIN!=null && LOSS!=null) {
            updateWinner(match.MatchId, WIN.teamId);
            updateTeamMatchResult(match.MatchId, WIN.teamId, "WIN");
            updateTeamMatchResult(match.MatchId, LOSS.teamId, "LOSS");
        }
        else {
            updateTeamMatchResult(match.MatchId, team1.teamId, "DRAW");
            updateTeamMatchResult(match.MatchId, team2.teamId, "DRAW");
        }
        updateMatchStatus(match.MatchId, "COMPLETED");
        match.setMatchStatus("COMPLETED");
        if(match.matchType.equalsIgnoreCase("FINAL")){
            updateTournamentComplete(TournamentId, WIN != null ? WIN.teamId : 0);
        }

        if(match.matchType.equalsIgnoreCase("SERIES")){
            updateCompletedMatches(TournamentId);
        }
    }
    public static int convertToBalls(double overs) {
        int overPart = (int) overs;
        int ballPart = (int) Math.round((overs - overPart) * 10);
        return overPart * 6 + ballPart;
    }


    public static double calculateMatchNRR(Team team1, Team team2) {

        double team1RunRate = (double) team1.totalRun / convertToBalls(team1.overPlayed);
        double team2RunRate = (double) team2.totalRun / convertToBalls(team2.overPlayed);


        return team1RunRate - team2RunRate;
    }

    public static void Simulation(Scanner sc, Team Team1, Team Team2, LinkedListOfPlayer Team1Players, LinkedListOfPlayer Team2Players, int over,Match match,int tournamentId) throws InterruptedException, SQLException {
        updateMatchStatus(match.MatchId, "LIVE");
        Bat1=new StackOfBatsman();
        Bat2=new StackOfBatsman();
        Ball=new StackOfBowler();
        BatTeam=new StackOfTeam();

        team1 = Team1;
        String team1Name=team1.getTeamName();
        team2 = Team2;
        String team2Name =team2.getTeamName();

        team1Players = Team1Players;
        team2Players = Team2Players;

        System.out.println("\n" + team1Name + " Players:");
        int NumberOfPlayer=team1Players.displayPlayerName();

        System.out.println("\n" + team2Name + " Players:");
        team2Players.displayPlayerName();

        TossThread toss = new TossThread(sc, team1, team2);
        toss.start();
        toss.join();
        Team BattingTeam = whoBat(sc, team1, team2, toss.getWinner());
        boolean isTeam1Batting = BattingTeam == team1;
        System.out.println("\n=== FIRST INNINGS START ===");
        if (isTeam1Batting) {
            team1Players.displayAvailablePlayersName();
        } else {
            team2Players.displayAvailablePlayersName();
        }
        System.out.println();
        Batsman1 = getValidPlayer(sc, isTeam1Batting ? team1Players : team2Players, "opener 1");
        updateBattingStatus(match.MatchId, Batsman1.getPlayerId(),"PLAYING");
        Batsman1.setPlaying(true);

        Batsman2 = getValidPlayer(sc, isTeam1Batting ? team1Players : team2Players, "opener 2");
        updateBattingStatus(match.MatchId, Batsman2.getPlayerId(),"PLAYING");
        Batsman2.setPlaying(true);
        Batsman2.setOnStrike(false);
        System.out.println();

        playInnings(sc, BattingTeam, isTeam1Batting, over, 1, NumberOfPlayer, 0, match.MatchId);

        int Target = BattingTeam.totalRun +1;
        BattingTeam = (BattingTeam == team1) ? team2 : team1;
        isTeam1Batting = !isTeam1Batting;

        System.out.println("\n=== SECOND INNINGS START ===");
        if (isTeam1Batting) {
            team1Players.displayAvailablePlayersName();
        } else {
            team2Players.displayAvailablePlayersName();
        }
        Batsman1 = getValidPlayer(sc, isTeam1Batting ? team1Players : team2Players, "opener 1");
        Batsman1.setPlaying(true);

        Batsman2 = getValidPlayer(sc, isTeam1Batting ? team1Players : team2Players, "opener 2");
        Batsman2.setPlaying(true);
        Batsman2.setOnStrike(false);
        System.out.println();

        playInnings(sc, BattingTeam, isTeam1Batting, over, 2, NumberOfPlayer, Target, match.MatchId);

        displayMatchResult(BattingTeam, NumberOfPlayer,match,tournamentId);
    }
}

class TossThread extends Thread {
    private final Scanner sc;
    private final Team team1;
    private final Team team2;
    private Team winner;

    public TossThread(Scanner sc, Team team1, Team team2) {
        this.sc = sc;
        this.team1 = team1;
        this.team2 = team2;
    }

    public Team getWinner() {
        return winner;
    }

    @Override
    public void run() {
        System.out.println("------ " + team1.teamName + " Vs " + team2.teamName + " ------");
        System.out.println("Toss time");

        int tossChoice = MatchSimulation.getValidChoice(sc,
                team1.teamName + " Enter 0 for HEAD and 1 for TAIL: ", 0, 1);

        System.out.println(team1.teamName + " calls " + (tossChoice == 0 ? "HEAD" : "TAIL"));
        System.out.println("Coin in the air...\n");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        int coinResult = (int) (Math.random() * 2);
        winner = (tossChoice == coinResult) ? team1 : team2;

        System.out.println("It's " + (coinResult == 0 ? "HEAD" : "TAIL"));
        System.out.println(winner.teamName + " won the toss");
    }
}