package de.tuberlin.dima.dbt.exercises.bplustree;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static de.tuberlin.dima.dbt.grading.bplustree.BPlusTreeMatcher.isTree;
import static de.tuberlin.dima.dbt.exercises.bplustree.BPlusTreeUtilities.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;

public class BPlusTreeTest {

    // fail each test after 1 second
    @Rule
    public Timeout globalTimeout = new Timeout(1000);

    private BPlusTree tree;

    ///// Lookup tests

    @Test
    public void findKeyInLeaf() {
        // given
        tree = newTree(newLeaf(keys(1, 2, 3), values("a", "b", "c")));
        // when
        String value = tree.lookup(2);
        // then
        assertThat(value, is("b"));
    }

    @Test
    public void findNoKeyInLeaf() {
        // given
        tree = newTree(newLeaf(keys(1, 3), values("a", "c")));
        // when
        String value = tree.lookup(2);
        // then
        assertThat(value, is(nullValue()));
    }

    @Test
    public void findKeyInChild() {
        // given
        tree = newTree(newNode(keys(3),
                               nodes(newLeaf(keys(1, 2), values("a", "b")),
                                     newLeaf(keys(3, 4), values("c", "d")))));
        // when
        String value = tree.lookup(1);
        // then
        assertThat(value, is("a"));
    }

    @Test
    public void findNoKeyInChild() {
        // given
        tree = newTree(newNode(keys(3),
                               nodes(newLeaf(keys(1, 3), values("a", "c")),
                                     newLeaf(keys(5, 7), values("e", "g")))));
        // when
        String value = tree.lookup(6);
        // then
        assertThat(value, is(nullValue()));
    }

    ///// Insertion tests

    @Test
    public void insertIntoLeaf() {
        // given
        tree = newTree(newLeaf(keys(1, 3), values("a", "c")));
        // when
        tree.insert(2, "b");
        // then
        assertThat(tree, isTree(
                newTree(newLeaf(keys(1, 2, 3), values("a", "b", "c")))));
    }

    @Test
    public void splitLeafs() {
        // given
        tree = newTree(newNode(keys(3),
                               nodes(newLeaf(keys(1, 2), values("a", "b")),
                                     newLeaf(keys(3, 4, 5, 6),
                                             values("c", "d", "e", "f")))));
        // when
        tree.insert(7, "g");
        // then
        assertThat(tree, isTree(newTree(newNode(
                keys(3, 5),
                nodes(newLeaf(keys(1, 2), values("a", "b")),
                      newLeaf(keys(3, 4), values("c", "d")),
                      newLeaf(keys(5, 6, 7), values("e", "f", "g")))))));
    }

    @Test
    public void splitLeafSortParent() {
        // given
        tree = newTree(newNode(keys(6),
                               nodes(newLeaf(keys(1, 2, 3, 4), values("a", "b", "c", "d")),
                                     newLeaf(keys(6, 7, 8),
                                             values("f", "g", "h")))));
        // when
        tree.insert(5, "e");
        // then
        assertThat(tree, isTree(newTree(newNode(
                keys(3, 6),
                nodes(newLeaf(keys(1, 2), values("a", "b")),
                      newLeaf(keys(3, 4, 5), values("c", "d", "e")),
                      newLeaf(keys(6, 7, 8), values("f", "g", "h")))))));
    }

    ///// Deletion tests

    @Test
    public void deleteFromLeaf() {
        // given
        tree = newTree(newLeaf(keys(1, 2, 3), values("a", "b", "c")));
        // when
        String value = tree.delete(2);
        // then
        assertThat(value, is("b"));
        assertThat(tree, isTree(
                newTree(newLeaf(keys(1, 3), values("a", "c")))));
    }

    @Test
    public void deleteFromChild() {
        // given
        tree = newTree(newNode(
                keys(4), nodes(newLeaf(keys(1, 2, 3), values("a", "b", "c")),
                               newLeaf(keys(4, 5), values("d", "e")))));
        // when
        String value = tree.delete(1);
        // then
        assertThat(value, is("a"));
        assertThat(tree, isTree(newTree(newNode(
                keys(4), nodes(newLeaf(keys(2, 3), values("b", "c")),
                               newLeaf(keys(4, 5), values("d", "e")))))));
    }

    @Test
    public void deleteFromChildStealFromSibling() {
        // given
        tree = newTree(newNode(
                keys(3), nodes(newLeaf(keys(1, 2), values("a", "b")),
                               newLeaf(keys(3, 4, 5), values("c", "d", "e")))));
        // when
        String value = tree.delete(1);
        // then
        assertThat(value, is("a"));
        assertThat(tree, isTree(newTree(newNode(
                keys(4), nodes(newLeaf(keys(2, 3), values("b", "c")),
                               newLeaf(keys(4, 5), values("d", "e")))))));

    }

    @Test
    public void deleteFromChildMergeWithSibling() {
        // given
        tree = newTree(newNode(keys(3, 5),
                               nodes(newLeaf(keys(1, 2), values("a", "b")),
                                     newLeaf(keys(3, 4), values("c", "d")),
                                     newLeaf(keys(5, 6), values("e", "f")))));
        // when
        String value = tree.delete(2);
        // then
        assertThat(value, is("b"));
        assertThat(tree, isTree(newTree(newNode(
                keys(5), nodes(newLeaf(keys(1, 3, 4), values("a", "c", "d")),
                               newLeaf(keys(5, 6), values("e", "f")))))));
    }

    /*
     * ---------------- End of given university Tests ----------------
     * 
     * ----------------       Start of my Tests       ----------------
     */


    /**
     * lookup tests
     * 
     */

    @Test
    public void deepLookup() {
         // given
         tree = newTree(newNode(keys(20, 40, 60), nodes(
            newNode(keys(4, 7, 10), nodes(
                newLeaf(keys(1, 2, 3), values("a", "b", "c")),
                newLeaf(keys(4, 5, 6), values("d", "e", "f")),
                newLeaf(keys(7, 8, 9), values("g", "h", "i")),
                newLeaf(keys(10, 11, 12), values("j", "k", "l"))
            )),
            newNode(keys(24, 27), nodes(
                newLeaf(keys(21, 22, 23), values("m", "n", "o")),
                newLeaf(keys(24, 25, 26), values("p", "q", "r")),
                newLeaf(keys(27, 28, 29), values("s", "t", "u"))
            )),
            newNode(keys(44, 47), nodes(
                newLeaf(keys(41, 42, 43), values("v", "w", "x")),
                newLeaf(keys(44, 45, 46), values("y", "z", "A")),
                newLeaf(keys(47, 48, 49), values("B", "C", "D"))
            )),
            newNode(keys(64, 67), nodes(
                newLeaf(keys(61, 62, 63), values("E", "F", "G")),
                newLeaf(keys(64, 65, 66), values("H", "I", "J")),
                newLeaf(keys(67, 68, 69), values("K", "L", "M"))
            ))
         ))); //newLeaf(keys(1, 2, 3), values("a", "b", "c"))
         // when
         String value = tree.lookup(49);
         // then
         assertThat(value, is("D"));
    }


    /**
     * add tests
     * 
     */

    @Test
    public void addElementsInitialElement() {
        // given
        tree = newTree(newLeaf(new Integer[] {}, new String[] {}));
        // when
        tree.insert(1, "a");
        // then
        assertThat(tree, isTree(newTree(newLeaf(keys(1), values("a")))));
    }

    @Test
    public void addElementsNewLayer() {
        // given
        tree = newTree(newLeaf(keys(1, 2, 3), values("a", "b", "c")));
        // when
        tree.insert(4, "d");
        tree.insert(5, "e");
        // then
        assertThat(tree, isTree(newTree(newNode(keys(3), nodes(
            newLeaf(keys(1, 2), values("a", "b")),
            newLeaf(keys(3, 4, 5), values("c", "d", "e")))))));
    }

    @Test
    public void addElementSplitInner() {
        // given
        tree = newTree(newNode(keys(4, 7, 10, 13), nodes(
            newLeaf(keys(1, 2, 3), values("a", "b", "c")),
            newLeaf(keys(4, 5, 6), values("d", "e", "f")),
            newLeaf(keys(7, 8, 9), values("g", "h", "i")),
            newLeaf(keys(10, 11, 12), values("j", "k", "l")),
            newLeaf(keys(13, 14, 15, 16), values("m", "n", "o", "p"))
        )));
        // when
        tree.insert(17, "q");
        // then
        assertThat(tree, isTree(newTree(newNode(keys(10), nodes(
            newNode(keys(4, 7), nodes(
                newLeaf(keys(1, 2, 3), values("a", "b", "c")),
                newLeaf(keys(4, 5, 6), values("d", "e", "f")),
                newLeaf(keys(7, 8, 9), values("g", "h", "i"))
            )),
            newNode(keys(13, 15), nodes(
                newLeaf(keys(10, 11, 12), values("j", "k", "l")),
                newLeaf(keys(13, 14), values("m", "n")),
                newLeaf(keys(15, 16, 17), values("o", "p", "q"))
            ))
        )))));
    }

    @Test
    public void addManyElements(){
        // given
        tree = newTree(newLeaf(new Integer[] {}, new String[] {}));
        // when
        for(int i=1; i<38; i++){
           tree.insert(i, "abc");
        }
        // then
        assertThat(tree, isTree(newTree(newNode(keys(19), nodes(
            newNode(keys(7, 13), nodes(
                newNode(keys(3,5), nodes(
                    newLeaf(keys(1,2), values("abc","abc")),
                    newLeaf(keys(3,4), values("abc","abc")),
                    newLeaf(keys(5,6), values("abc","abc"))
                )),
                newNode(keys(9,11), nodes(
                    newLeaf(keys(7,8), values("abc","abc")),
                    newLeaf(keys(9,10), values("abc","abc")),
                    newLeaf(keys(11,12), values("abc","abc"))
                )),
                newNode(keys(15,17), nodes(
                    newLeaf(keys(13,14), values("abc","abc")),
                    newLeaf(keys(15,16), values("abc","abc")),
                    newLeaf(keys(17,18), values("abc","abc"))
                ))
            )),
            newNode(keys(25, 31), nodes(
                newNode(keys(21,23), nodes(
                    newLeaf(keys(19,20), values("abc","abc")),
                    newLeaf(keys(21,22), values("abc","abc")),
                    newLeaf(keys(23,24), values("abc","abc"))
                )),
                newNode(keys(27,29), nodes(
                    newLeaf(keys(25,26), values("abc","abc")),
                    newLeaf(keys(27,28), values("abc","abc")),
                    newLeaf(keys(29,30), values("abc","abc"))
                )),
                newNode(keys(33,35), nodes(
                    newLeaf(keys(31,32), values("abc","abc")),
                    newLeaf(keys(33,34), values("abc","abc")),
                    newLeaf(keys(35,36,37), values("abc","abc","abc"))
                ))
            ))
        )))));
    }

    @Test
    public void addElementsCenter(){
        // given
        tree = newTree(newLeaf(new Integer[] {}, new String[] {}));
        // when
        for(int i=1; i<4; i++){
            tree.insert(i, "abc");
        }
        for(int i=50; i<54; i++){
            tree.insert(i, "abc");
        }
        for(int i=10; i<13; i++){
            tree.insert(i, "abc");
        }
        // then
        assertThat(tree, isTree(newTree(newNode(keys(3,11,51), nodes(
            newLeaf(keys(1, 2), values("abc", "abc")),
            newLeaf(keys(3, 10), values("abc", "abc")),
            newLeaf(keys(11, 12, 50), values("abc", "abc", "abc")),
            newLeaf(keys(51, 52, 53), values("abc", "abc", "abc")))))));
    }

    @Test
    public void addElementsNotDefaultSize(){
         // given
         int testCapacity = 6;
         tree = new BPlusTree(new LeafNode(new Integer[] {1,2,3,4}, new String[] {"abc","abc","abc","abc"}, testCapacity), testCapacity);
         // when
         tree.insert(5, "abc");
         // then
         assertThat(tree, isTree(new BPlusTree(
            new LeafNode(new Integer[] {1,2,3,4,5}, new String[] {"abc","abc","abc","abc","abc"}, testCapacity), testCapacity
            )));
    }

    @Test
    public void addElementsSplitNotDefaultSize(){
        // given
        int testCapacity = 6;
        tree = new BPlusTree(new LeafNode(new Integer[] {1,2,3,4,5,6}, new String[] {"abc","abc","abc","abc","abc","abc",}, testCapacity), testCapacity);
        // when
        tree.insert(7, "abc");
        // then
        assertThat(tree, isTree(new BPlusTree(
            new InnerNode(new Integer[] {4}, new Node[]{
                new LeafNode(new Integer[] {1,2,3}, new String[] {"abc","abc","abc"}, testCapacity),
                new LeafNode(new Integer[] {4,5,6,7}, new String[] {"abc","abc","abc","abc"}, testCapacity)
            },testCapacity), testCapacity)));
    }

    /**
     * delete tests
     * 
     */

    @Test
    public void deleteFromChildStealFromLeftSibling() {
        // given
        tree = newTree(newNode(
                 keys(4), nodes(newLeaf(keys(1, 2, 3), values("a", "b", "c")),
                                newLeaf(keys(4, 5), values("d", "e")))));
        // when
        String value = tree.delete(4);
        // then
        assertThat(value, is("d"));
        assertThat(tree, isTree(newTree(newNode(
                 keys(3), nodes(newLeaf(keys(1, 2), values("a", "b")),
                                newLeaf(keys(3, 5), values("c", "e")))))));
 
    }

    @Test
    public void deleteFromChildMergeWithLeftSibling() {
        // given
        tree = newTree(newNode(keys(3, 5),
                               nodes(newLeaf(keys(1, 2), values("a", "b")),
                                     newLeaf(keys(3, 4), values("c", "d")),
                                     newLeaf(keys(5, 6), values("e", "f")))));
        // when
        String value = tree.delete(5);
        // then
        assertThat(value, is("e"));
        assertThat(tree, isTree(newTree(newNode(
                keys(3), nodes(newLeaf(keys(1, 2), values("a", "b")),
                               newLeaf(keys(3, 4, 6), values("c", "d", "f")))))));
    }

    @Test
    public void deleteHoleTree() {
        // given
        tree = newTree(newNode(keys(4, 7, 10, 13), nodes(
            newLeaf(keys(1, 2, 3), values("a", "b", "c")),
            newLeaf(keys(4, 5, 6), values("d", "e", "f")),
            newLeaf(keys(7, 8, 9), values("g", "h", "i")),
            newLeaf(keys(10, 11, 12), values("j", "k", "l")),
            newLeaf(keys(13, 14, 15, 16), values("m", "n", "o", "p"))
        )));
        // when
        for(int i=1; i<17; i++){
            tree.delete(i);
        }
        // then
        assertThat(tree, isTree(newTree(newLeaf(keys(), values()))));
    }

    /**
     * Eval server error
     */

     @Test
     public void firstEvalTest() {
         // given
         tree = newTree(newNode(keys(130,156,169,198), nodes(
             newLeaf(keys(114,124), values("QPw","qyP")),
             newLeaf(keys(130,146), values("NWM","Aak")),
             newLeaf(keys(156,163), values("eWo","lfZ")),
             newLeaf(keys(169,183), values("MoQ","oKk")),
             newLeaf(keys(198,207), values("QVs","ihL"))
         )));
         // when
         String value = tree.lookup(207);
         // then
         assertThat(value, is("ihL"));
     }

     @Test 
     public void thirdEvalTest(){
        // given
        tree = newTree(newLeaf(keys(98,117,125,128), values("McL","emy","KAo","EZw")));
        // when
        String value = tree.delete(128);
        // then
        assertThat(value, is("EZw"));
     }
}
