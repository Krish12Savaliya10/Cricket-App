package DataStructure;

public class StackOfBowler {
    private static class Node {
        LinkedListOfPlayer.Bowler data;
        Node next;

        Node(LinkedListOfPlayer.Bowler data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node top;

    public StackOfBowler() {
        top = null;
    }

    public void push(LinkedListOfPlayer.Bowler bowler) {
        Node newNode = new Node(bowler);
        newNode.next = top;
        top = newNode;
    }

    public LinkedListOfPlayer.Bowler pop() {
        if (isEmpty()) {
            System.out.println("Stack is empty!");
            return null;
        }
        LinkedListOfPlayer.Bowler popped = top.data;
        top = top.next;
        return popped;
    }

    public boolean isEmpty() {
        return top == null;
    }
}
