import java.util.*;
import tester.*;

/**
 * A class that defines a new permutation code, as well as methods for encoding
 * and decoding of the messages that use this code. The original list of
 */
class PermutationCode {

  ArrayList<Character> alphabet = new ArrayList<Character>(
      Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
          'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'));

  ArrayList<Character> code = new ArrayList<Character>(26);

  // A random number generator
  Random rand = new Random(1);

  // Create a new instance of the encoder/decoder with a new permutation code
  PermutationCode() {
    this.code = this.initEncoder();
  }

  // Create a new instance of the encoder/decoder with the given code
  PermutationCode(ArrayList<Character> code) {
    this.code = code;
  }

  // Initialize the encoding permutation of the characters
  ArrayList<Character> initEncoder() {
    ArrayList<Character> someList = new ArrayList<Character>(alphabet);
    this.code.clear();
    while (this.code.size() != this.alphabet.size()) {
      int randomize = rand.nextInt(someList.size());
      code.add(someList.get(randomize));
      someList.remove(randomize);
    }
    return code;
  }

  // produce an encoded String from the given String
  String encode(String source) {
    String encodedList = "";
    for (int i = 0; i != source.length(); i = i + 1) {
      int j = alphabet.indexOf(source.charAt(i));
      if (j == -1) {
        throw new IllegalArgumentException("it is empty");
      }
      encodedList = encodedList + this.code.get(j);
    }
    return encodedList;
  }

  // produce a decoded String from the given String
  String decode(String code) {
    String decoded = "";
    for (int i = 0; i != code.length(); i = i + 1) {
      int j = this.code.indexOf(code.charAt(i));
      if (j == -1) {
        throw new IllegalArgumentException("it is empty");
      }
      decoded = decoded + this.alphabet.get(j);
    }
    return decoded;
  }
}

// PermutationCode tests
class ExamplesPermutationCode {
  ArrayList<Character> alphabet = new ArrayList<Character>(
      Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
          'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'));
  ArrayList<Character> empty = new ArrayList<Character>();

  // this list was created by seeding the random value as new Random(1)
  ArrayList<Character> permutatatList = new ArrayList<Character>(
      Arrays.asList('r', 'n', 'h', 'o', 'y', 'q', 't', 'u', 'l', 'v', 'a', 'x', 'g', 'i', 'k', 'c',
          'e', 'j', 'z', 's', 'f', 'm', 'd', 'w', 'b', 'p'));

  PermutationCode sortedcode = new PermutationCode(alphabet);
  PermutationCode permutatCode = new PermutationCode(permutatatList);

  // initEncode tests
  void testInitEncoder(Tester t) {
    t.checkExpect(this.sortedcode.initEncoder(), permutatatList);
  }

  // decode tests
  void testDecode(Tester t) {
    t.checkExpect(permutatCode.decode("vc"), "jp");
    t.checkExpect(permutatCode.decode("ursyz"), "hates");
    t.checkExpect(permutatCode.decode("gy"), "me");
  }

  // encode tests
  void testEncode(Tester t) {
    t.checkExpect(permutatCode.encode("vinay"), "mlirb");
    t.checkExpect(permutatCode.encode("loves"), "xkmyz");
    t.checkExpect(permutatCode.encode("kid"), "alo");
  }

}
