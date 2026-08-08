package DS;
import DS.*;


public class Stack {
    class StackNode {

        String    data;   // the event message stored in this node
        StackNode next;   // pointer to the node below in the stack

        public StackNode(String data) {
            this.data = data;
            this.next = null; // no next node yet
        }
    }

    private StackNode top;  // pointer to top of the stack
    private int size;       // current number of elements

    public Stack() {
        this.top  = null;
        this.size = 0;
    }

    public void push(String data) {
        // Create a new node
        StackNode newNode = new StackNode(data);

        // If stack is not empty, link new node to current top
        if (top != null) {
            newNode.next = top;
        }

        // New node becomes the top
        top = newNode;
        size++;
    }

    public String pop() {
        if (isEmpty()) {
            return null;
        }

        // Save the data from top node
        String data = top.data;

        // Move top pointer to next node
        top = top.next;
        size--;

        return data;
    }

    public String peek() {
        if (isEmpty()) {
            return null;
        }
        return top.data;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public int getSize() {
        return size;
    }

    public ArrayList<String> displayAll() {
        ArrayList<String> events = new ArrayList<>();

        // Traverse from top to bottom
        StackNode current = top;
        while (current != null) {
            events.add(current.data);
            current = current.next;
        }
        return events;
    }

    public void clear() {
        top  = null;
        size = 0;
    }
}
