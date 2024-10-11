import java.util.function.Predicate;

interface IPred<T> {
  boolean apply(T t);
}


//class to represent a deque
class Deque<T> extends Sentinel<T> {
  Sentinel<T> header;

  //first constructor
  Deque() {
    this.header = new Sentinel<T>();
  }

  //second constructor
  Deque(Sentinel<T> header) {
    this.header = header;
  }

  // returns the total number of nodes
  int size() {
    return this.header.size();
  }

  // adds a new node at the start of the deque
  T addAtHead(T t) {
    return header.addAtHead(t);
  }

  //adds a new node at the end of the deque
  T addAtTail(T t) {
    return header.addAtTail(t);
  }

  //removes the first element
  T removeFromHead() {
    if (this.size() == 0) {
      throw new RuntimeException("Cannot remove from an empty list");
    }
    return header.removeFromHead();
  }

  //removes the last element
  T removeFromTail() {
    if (this.size() == 0) {
      throw new RuntimeException("Cannot remove from an empty list");
    }
    return header.removeFromTail();
  }

  //finds a node based on the predicate
  ANode<T> find(Predicate<T> predicate) {
    return this.header.find(predicate);
  }

}

abstract class ANode<T> {
  ANode<T> next;
  ANode<T> prev;

  // returns the total number of nodes in this deque
  int size(ANode<T> s) {
    if (this.next.equals(s)) {
      return 1;
    }
    else {
      return 1 + this.next.size(s);
    }
  }

  // removes this node from the deque
  T remove() {
    this.prev.next = this.next;
    this.next.prev = this.prev;
    return this.getData();
  }

  // returns the data for this node
  T getData() {
    return null;
  }

  ANode<T> find(IPred<T> p) {
    return this;
  }

  abstract ANode<T> findHelp(Predicate<T> predicate);

}

class Sentinel<T> extends ANode<T> {

  Sentinel() {
    this.next = this;
    this.prev = this;
  }

  int size() {
    if (this.next.equals(this)) {
      return 0;
    }
    return this.next.size(this);
  }

  T addAtHead(T t) {
    new Node<T>(t, this.next, this);
    return this.next.getData();
  }

  T addAtTail(T t) {
    new Node<T>(t, this, this.prev);
    return this.next.getData();
  }

  T removeFromHead() {
    return this.next.remove();
  }

  T removeFromTail() {
    return this.prev.remove();
  }

  ANode<T> find(Predicate<T> predicate) {
    return this.next.findHelp(predicate);
  }

  // method findHelp that actually finds the node
  ANode<T> findHelp(Predicate<T> predicate) {
    return this;
  }
}

class Node<T> extends ANode<T> {
  T data;

  //first constructor for a node
  Node(T data) {
    this.data = data;
    this.next = null;
    this.prev = null;
  }

  //second constructor for a node
  Node(T data, ANode<T> next, ANode<T> prev) {
    if ((next == null) || (prev == null)) {
      throw new IllegalArgumentException("Cannot accept null node");
    }
    this.data = data;
    this.next = next;
    this.prev = prev;
    prev.next = this;
    next.prev = this;
  }

  //looks for a node that fits the predicate
  ANode<T> find(Predicate<T> predicate) {
    return this.findHelp(predicate);
  }

  // method find that takes a Predicate<T> and produces the first node in this Deque
  // for which the given predicate returns true
  ANode<T> findHelp(Predicate<T> predicate) {
    if (predicate.test(this.data)) {
      return this;
    }
    else {
      return this.next.findHelp(predicate);
    }
  }

  //returns the content of the node
  T getData() {
    return this.data;
  }
}
