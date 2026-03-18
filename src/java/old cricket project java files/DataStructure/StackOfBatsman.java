package DataStructure;


public class StackOfBatsman {


    private static class Node {
        LinkedListOfPlayer.Player data;
        Node next;

        Node(LinkedListOfPlayer.Player data) {
            this.data = data;
            this.next = null;
        }
    }


    private Node top;


    public StackOfBatsman() {
        top = null;
    }


    public void push(LinkedListOfPlayer.Player batsman) {
        Node newNode = new Node(batsman);
        newNode.next = top;
        top = newNode;
    }


    public LinkedListOfPlayer.Player pop() {
        if (isEmpty()) {
            System.out.println("Stack is empty!");
            return null;
        }
        LinkedListOfPlayer.Player popped = top.data;
        top = top.next;
        return popped;
    }

    public boolean isEmpty() {
        return top == null;
    }

   
}