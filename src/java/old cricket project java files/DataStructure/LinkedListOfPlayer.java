package DataStructure;

import java.util.ArrayList;

public class LinkedListOfPlayer {


    public static   class Player{
        protected String playerName;
        protected int playerId;
        protected boolean onStrike;
        protected boolean isOut;
        protected boolean isPlaying;


        protected int runsScored;
        protected int ballsFaced;
        protected int sixes;
        protected int fours;

        protected int oversBowled;
        protected int wickets;
        protected int runsGiven;


        protected Player next;

        public  Player(String name,int playerId) {
            this.playerName = name;
            this.playerId=playerId;
            this.onStrike = true;
            this.isOut = false;
            this.isPlaying=false;
            this.next = null;
            this.runsScored = 0;
            this.ballsFaced = 0;
            this.sixes = 0;
            this.fours = 0;
        }
        public  Player(Player copy) {
            this.playerName = copy.playerName;
            this.playerId=copy.playerId;
            this.onStrike = copy.onStrike;
            this.isOut = copy.isOut;
            this.isPlaying=copy.isPlaying;
            this.next = copy.next;
            this.runsScored = copy.runsScored;
            this.ballsFaced = copy.ballsFaced;
            this.sixes = copy.sixes;
            this.fours = copy.fours;
        }

        public String getPlayerName() {return playerName;}

        public boolean isOnStrike() {
            return onStrike;
        }

        public void setOnStrike(boolean onStrike) {
            this.onStrike = onStrike;
        }

        public boolean isOut() {
            return isOut;
        }

        public void setOut(boolean out) {
            isOut = out;
        }

        public void setNext(Player next) {
            this.next = next;
        }

        public int getRunsScored() {
            return runsScored;
        }

        public void setRunsScored(int runsScored) {
            this.runsScored = runsScored;
        }

        public int getBallsFaced() {
            return ballsFaced;
        }

        public void setBallsFaced(int ballsFaced) {
            this.ballsFaced = ballsFaced;
        }

        public int getSixes() {
            return sixes;
        }

        public void setSixes(int sixes) {
            this.sixes = sixes;
        }

        public int getFours() {return fours;}

        public void setFours(int fours) {
            this.fours = fours;
        }

        public int getOversBowled() {return oversBowled;}

        public void setOversBowled(int oversBowled) {
            this.oversBowled = oversBowled;}

        public  int getWickets() {return wickets;}

        public void setWickets(int wickets) {
            this.wickets = wickets;}

        public  int getRunsGiven() {return runsGiven;}

        public void setRunsGiven(int runsGiven) {
            this.runsGiven = runsGiven;}

        public void setPlaying(boolean playing) {
            isPlaying = playing;
        }

        public int getPlayerId() {
            return playerId;
        }

        public void setPlayerId(int playerId) {
            this.playerId = playerId;
        }

    }

   public static class Batsman extends Player {
        public Batsman(String name,int playerId) {
            super(name,playerId);
        }
    }

    public static class Bowler extends Player {
        public Bowler(Bowler copy){
            super(copy);
        }
        public Bowler(String name,int playerId) {
            super(name,playerId);
        }

    }

    public static  class AllRounder extends Bowler {
        public AllRounder(String name,int playerId) {
            super(name,playerId);
        }

    }

    private Player head;

    public void addBatsman(String name,int playerId) {
        Batsman n = new Batsman(name,playerId);
        if (head == null) {
            head = n;
        } else {
            Player temp = head;
            while (temp.next != null) {
                temp = temp.next;
            }
            temp.next=n;
        }
    }

    public void addBowler(String name,int playerId) {
        Bowler n = new Bowler(name,playerId);
        if (head == null) {
            head = n;
        } else {
            Player temp = head;
            while (temp.next != null) {
                temp = temp.next;
            }
            temp.next=n;
        }

    }

    public void addAllrounder(String name,int playerId) {
        AllRounder n = new AllRounder(name,playerId);
        if (head == null) {
            head = n;
        } else {
            Player temp = head;
            while (temp.next != null) {
                temp = temp.next;
            }
            temp.next=n;
        }
    }

    public int displayPlayerName() {
        Player temp = head;
        int count=0;
        while (temp != null) {
            count++;
            System.out.println(temp.playerName);
            temp = temp.next;
        }
        return count;
    }
    public ArrayList<Player> getALlPlayer() {
        ArrayList<Player> players=new ArrayList<>();
        Player temp = head;
        while (temp != null) {
            players.add(temp);
            temp = temp.next;
        }
        return players;
    }

    public Player getPlayer(String name) {
        Player temp = head;
        while (temp != null && (!temp.playerName.equalsIgnoreCase(name))) {
            temp = temp.next;
        }
        return temp;
    }

    public void displayAvailablePlayersName() {
        Player temp = head;
        while (temp != null) {
            if(!temp.isOut() && !temp.isPlaying){
                System.out.print(temp.getPlayerName());
                if(temp instanceof Batsman)
                    System.out.println(" (Batsman)");
                else {
                    System.out.println( (temp instanceof AllRounder ? " (All-Rounder)" : " (Bowler)"));
                }
            }
            temp = temp.next;
        }
    }
    public void displayBowlersAndAllRoundersName(Bowler bowler) {
        System.out.println("Available bowlers/all-rounders:");
        Player current = head;
        while (current != null) {
            if (current instanceof Bowler && current!=bowler ) {
                System.out.println(current.getPlayerName() +
                        (current instanceof AllRounder ? " (All-Rounder)" : " (Bowler)"));
            }
            current = current.next;
        }
    }

    public void setDefault(){
        Player current = head;
        while (current != null) {
            current.onStrike = true;
            current.isOut = false;
            current.isPlaying=false;
            current.runsScored = 0;
            current.ballsFaced = 0;
            current.sixes = 0;
            current.fours = 0;
            current.oversBowled=0;
            current.wickets=0;
            current.runsGiven=0;
            current=current.next;
        }
    }

}