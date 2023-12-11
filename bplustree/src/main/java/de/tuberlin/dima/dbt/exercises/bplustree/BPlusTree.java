package de.tuberlin.dima.dbt.exercises.bplustree;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Implementation of a B+ tree.
 * <p>
 * The capacity of the tree is given by the capacity argument to the
 * constructor. Each node has at least {capacity/2} and at most {capacity} many
 * keys. The values are strings and are stored at the leaves of the tree.
 * <p>
 * For each inner node, the following conditions hold:
 * <p>
 * {pre}
 * Integer[] keys = innerNode.getKeys();
 * Node[] children = innerNode.getChildren();
 * {pre}
 * <p>
 * - All keys in {children[i].getKeys()} are smaller than {keys[i]}.
 * - All keys in {children[j].getKeys()} are greater or equal than {keys[i]}
 * if j > i.
 */
public class BPlusTree {

    ///// Implement these methods

    private LeafNode findLeafNode(Integer key, Node node,
                                  Deque<InnerNode> parents) {
        if (node instanceof LeafNode) {
            return (LeafNode) node;
        } else {
            InnerNode innerNode = (InnerNode) node;
            if (parents != null) {
                parents.push(innerNode);
            } else{
                parents = new ArrayDeque<InnerNode>();
                parents.push(innerNode);
            }
            // TODO: traverse inner nodes to find leaf node

            Node[] children = innerNode.getChildren();
            int numberOfKeys = innerNode.keys.length;

            int counter;
            for(counter=0; counter<numberOfKeys; counter++){
                if(innerNode.keys[counter] == null){
                    break;
                }
                if(key < innerNode.keys[counter]){
                    return findLeafNode(key, children[counter], parents);
                }
            }
            return findLeafNode(key, children[counter], parents);
        }
    }

    /**
     * Lookup value in leaf node
     * @return The stored value, or {null} if the key does not exist.
     */
    private String lookupInLeafNode(Integer key, LeafNode node) {
        int numberOfKeys = node.keys.length;
        for(int i=0; i<numberOfKeys; i++){
            if(node.keys[i] == key){
                return node.getValues()[i];
            }
        }
        return null;
    }

    private void insertSort(Integer[] leafKeys, String[] values, Integer newKey, String newValue){
        //make sure there is space
        assert leafKeys[leafKeys.length -1] == null;

        int position = 0;
        for(int i=0; i<leafKeys.length; i++){
            if(leafKeys[i] == null){
                leafKeys[i] = newKey;
                values[i] = newValue;
                return;
            }
            if(leafKeys[i] > newKey){
                position = i;
                break;
            }
        }
        //copy array to the right and discard last element of array
        for(int i=leafKeys.length - 2; i >= position; i--){
            leafKeys[i] = leafKeys[i - 1];
            values[i] = values[i - 1];
        }
        //insert new key
        leafKeys[position] = newKey;
        values[position] = newValue;
    }

    private void insertSortNode(Integer[] NodeKeys, Node[] nodes, Integer newKey, Node newNode){
        //make sure there is space
        assert NodeKeys[NodeKeys.length -1] == null;

        int position = 0;
        for(int i=0; i<NodeKeys.length; i++){
            if(NodeKeys[i] == null){
                NodeKeys[i] = newKey;
                nodes[i + 1] = newNode;
                return;
            }
            if(NodeKeys[i] > newKey){
                position = i;
                break;
            }
        }
        //copy array to the right and discard last element of array
        for(int i=NodeKeys.length - 1; i > position; i--){
            NodeKeys[i] = NodeKeys[i - 1];
            nodes[i + 1] = nodes[i];
        }
        //insert new key
        NodeKeys[position] = newKey;
        nodes[position + 1] = newNode;
    }

    /**
     * Insert value into leaf node (and propagate changes up)
     */
    private void insertIntoLeafNode(Integer key, String value,
                                    LeafNode node, Deque<InnerNode> parents) {
        Integer[] leafKeys = node.getKeys();
        int numberOfKeys = leafKeys.length;
        if(leafKeys[numberOfKeys - 1] == null){
            //space in leaf available
            String []values = node.getValues();
            insertSort(leafKeys, values, key, value);
            node.setKeys(leafKeys);
            node.setValues(values);
        } else{
            //no space in leaf available
            //insert into oversize array first and split later
            int oversize = numberOfKeys + 1;
            Integer[] oversizeKeys = Arrays.copyOf(leafKeys, oversize);
            String[] oversizeValues = Arrays.copyOf(node.getValues(), oversize);
            insertSort(oversizeKeys, oversizeValues, key, value);

            //get the middle info
            int middle = (int) (oversize / 2);
            Integer middleValue = oversizeKeys[middle];

            //set initial leaf with lower half values
            node.setKeys(Arrays.copyOfRange(oversizeKeys, 0, middle));
            node.setValues(Arrays.copyOfRange(oversizeValues, 0, middle));

            //create new Leaf from upper half
            Integer []newKeys = Arrays.copyOfRange(oversizeKeys, middle, oversize);
            String []newValues = Arrays.copyOfRange(oversizeValues, middle, oversize);
            LeafNode newLeaf = new LeafNode(newKeys, newValues, numberOfKeys);

            //add new Value to Inner Node
            InnerNode parent = parents.getLast();
            Node []children = parent.getChildren();
            Integer []innerKeys = parent.getKeys();
            insertSortNode(innerKeys, children, middleValue, newLeaf);

            //set children and keys
            parent.setChildren(children);
            parent.setKeys(innerKeys);
        }

    }

    private String deleteFromLeafNode(Integer key, LeafNode node,
                                      Deque<InnerNode> parents) {
        // TODO: delete value from leaf node (and propagate changes up)
        return null;
    }

    ///// Public API
    ///// These can be left unchanged

    /**
     * Lookup the value stored under the given key.
     * @return The stored value, or {null} if the key does not exist.
     */
    public String lookup(Integer key) {
        LeafNode leafNode = findLeafNode(key, root);
        return lookupInLeafNode(key, leafNode);
    }

    /**
     * Insert the key/value pair into the B+ tree.
     */
    public void insert(int key, String value) {
        Deque<InnerNode> parents = new LinkedList<>();
        LeafNode leafNode = findLeafNode(key, root, parents);
        insertIntoLeafNode(key, value, leafNode, parents);
    }

    /**
     * Delete the key/value pair from the B+ tree.
     * @return The original value, or {null} if the key does not exist.
     */
    public String delete(Integer key) {
        Deque<InnerNode> parents = new LinkedList<>();
        LeafNode leafNode = findLeafNode(key, root, parents);
        return deleteFromLeafNode(key, leafNode, parents);
    }

    ///// Leave these methods unchanged

    private int capacity = 0;

    private Node root;

    public BPlusTree(int capacity) {
        this(new LeafNode(capacity), capacity);
    }

    public BPlusTree(Node root, int capacity) {
        assert capacity % 2 == 0;
        this.capacity = capacity;
        this.root = root;
    }

    public Node rootNode() {
        return root;
    }

    public String toString() {
        return new BPlusTreePrinter(this).toString();
    }

    private LeafNode findLeafNode(Integer key, Node node) {
        return findLeafNode(key, node, null);
    }

}
