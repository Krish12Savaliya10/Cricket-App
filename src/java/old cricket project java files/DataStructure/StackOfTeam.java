package DataStructure;

import Simulation.Team;

public class StackOfTeam {
    private static class Node {
        Team data;
        Node next;

        Node(Team data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node top;


    public StackOfTeam() {
        top = null;
    }


    public void push(Team team) {
        Node newNode = new Node(team);
        newNode.next = top;
        top = newNode;
    }


    public Team pop() {
        if (isEmpty()) {
            System.out.println("Team stack is empty!");
            return null;
        }
        Team popped = top.data;
        top = top.next;
        return popped;
    }

    public boolean isEmpty() {
        return top == null;
    }

}
