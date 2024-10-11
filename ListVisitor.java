import java.util.function.*;
import tester.Tester;

//interface to handle lists
interface IList<T> {
  <R> R accept(IListVisitor<T, R> vis);
}

//represents an empty list of generics
class MtList<T> implements IList<T> {
  @Override
  public <R> R accept(IListVisitor<T, R> vis) {
    return vis.visit(this);
  }
}

//represents a non-empty list of generics
class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;

  //constructor for non-empty list
  ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  //non-empty list accepts a visitor
  public <R> R accept(IListVisitor<T, R> vis) {
    return vis.visit(this);
  }
}

// represents a visitor
interface IListVisitor<T,R> {
  R visit(MtList<T> mt);

  R visit(ConsList<T> cons);
}

// maps the given function to an IList<T>
class MapVisitor<T, U> implements IListVisitor<T,IList<U>> {

  Function<T, U> function;

  //constructor for the MapVisitor
  MapVisitor(Function<T,U> function) {
    this.function = function;
  }

  //visits an empty list
  public IList<U> visit(MtList<T> mt) {
    return new MtList<U>();
  }

  //visits a non-empty list
  public IList<U> visit(ConsList<T> cons) {
    return new ConsList<U>(function.apply(cons.first), cons.rest.accept(this));
  }
}

// performs the given fold function to a list
class FoldRVisitor<T, R> implements IListVisitor<T, R> {

  BiFunction<T, R, R> function;
  R original;

  FoldRVisitor(BiFunction<T, R, R> function, R original) {
    this.function = function;
    this.original = original;
  }

  //visits an empty list
  public R visit(MtList<T> mt) {
    return original;
  }

  //visits a non-empty list
  public R visit(ConsList<T> cons) {
    return this.function.apply(cons.first, cons.rest.accept(this));
  }

}

// appends an element to a list
class AppendVisitor<T> implements IListVisitor<T, IList<T>> {

  IList<T> add;

  //constructor for appending
  AppendVisitor(IList<T> add) {
    this.add = add;
  }

  //appending an element to an empty list
  public IList<T> visit(MtList<T> mt) {
    return add;
  }

  //appending an element to a non-empty list
  public IList<T> visit(ConsList<T> cons) {
    return new ConsList<T>(cons.first, cons.rest.accept(this));
  }
}

//represents an ancestor tree
interface IAT {
  <R> R accept(IATVisitor<R> vis);
}

//represents an unknown ancestor tree
class Unknown implements IAT {

  //constructor for an Unknown
  Unknown() {

  }

  //accepts a visitor
  public <R> R accept(IATVisitor<R> vis) {
    return vis.visit(this);
  }
}

//class for a person in an ancestor tree
class Person implements IAT {
  String name;
  int yob;
  IAT parent1;
  IAT parent2;

  //constructor for a person
  Person(String name, int yob, IAT parent1, IAT parent2) {
    this.name = name;
    this.yob = yob;
    this.parent1 = parent1;
    this.parent2 = parent2;
  }

  //person accepts a visitor
  public <R> R accept(IATVisitor<R> vis) {
    return vis.visit(this);
  }
}

// represents an IAT visitor
interface IATVisitor<R> {
  R visit(Unknown un);

  R visit(Person person);
}

//makes a list of strings from the given tree
class NamesVisitor implements IATVisitor<IList<String>> {

  //visits an unknown and returns an empty list
  public IList<String> visit(Unknown un) {
    return new MtList<String>();
  }

  //visits a person and returns their information
  public IList<String> visit(Person person) {
    IList<String> parent1Names = person.parent1.accept(this);
    IList<String> parent2Names = person.parent2.accept(this);
    IList<String> parentNames = parent1Names.accept(new AppendVisitor<String>(parent2Names));

    return new ConsList<String>(person.name, parentNames);
  }
}

// examples of visitors
class ExamplesVisitors {

  IList<Integer> intList1 = new ConsList<Integer>(1,
      new ConsList<Integer>(2,
        new ConsList<Integer>(3,
          new ConsList<Integer>(4,
            new MtList<Integer>()))));

  IList<Integer> intListAdded = new ConsList<Integer>(5,
      new ConsList<Integer>(6,
        new ConsList<Integer>(7,
          new ConsList<Integer>(8,
            new MtList<Integer>()))));

  IList<Integer> intListAppended = new ConsList<Integer>(1,
      new ConsList<Integer>(2,
        new ConsList<Integer>(3,
          new ConsList<Integer>(4,
            new ConsList<Integer>(5,
              new ConsList<Integer>(6,
                new ConsList<Integer>(7,
                  new ConsList<Integer>(8,
                    new MtList<Integer>()))))))));
  IList<String> stringList1 = new ConsList<String>("Mary",
      new ConsList<String>("had",
        new ConsList<String>("a",
          new MtList<String>())));
  IList<String> stringList2 = new ConsList<String>("little",
      new ConsList<String>("lamb",
        new MtList<String>()));
  IList<String> stringList3 = new ConsList<String>("Mary",
      new ConsList<String>("had",
        new ConsList<String>("a",
          new ConsList<String>("little",
            new ConsList<String>("lamb",
              new MtList<String>())))));
  IList<String> stringList4 = new ConsList<String>("Mary ",
      new ConsList<String>("had ",
        new ConsList<String>("a ",
          new ConsList<String>("little ",
            new ConsList<String>("lamb ",
              new MtList<String>())))));

  Function<Integer, Integer> addFour = (n) -> n + 4;
  IListVisitor<Integer, IList<Integer>> addFourMap =
      new MapVisitor<Integer, Integer>(addFour);
  Function<String, String> addString = (n) -> n.concat(" ");
  IListVisitor<String, IList<String>> addStringMap =
      new MapVisitor<String, String>(addString);

  IAT un = new Unknown();
  IAT ty = new Person("ty", 1980, un, un);
  IAT kayla = new Person("kayla", 1980, un, un);
  IAT ken = new Person("ken", 2002, kayla, ty);
  IAT bella = new Person("bella", 2004, kayla, ty);

  IList<String> bellaNames = new ConsList<String>("ty",
      new ConsList<String>("kayla",
        new ConsList<String>("ken",
          new ConsList<String>("bella",
            new MtList<String>()))));

  // tests the MapVisitor
  boolean testMap(Tester t) {
    return t.checkExpect(intList1.accept(addFourMap), intListAdded)
        && t.checkExpect(stringList3.accept(addStringMap), stringList4);
  }

  // tests the AppendVisitor
  boolean testAppend(Tester t) {
    return t.checkExpect(intList1.accept(new AppendVisitor<Integer>(intListAdded)), intListAppended)
        && t.checkExpect(stringList1.accept(new AppendVisitor<String>(stringList2)), stringList3);
  }

  // tests the NamesVisitor
  boolean testNames(Tester t) {
    return t.checkExpect(un.accept(new NamesVisitor()), new MtList<String>())
        && t.checkExpect(bella.accept(new NamesVisitor()), bellaNames);
  }
}

