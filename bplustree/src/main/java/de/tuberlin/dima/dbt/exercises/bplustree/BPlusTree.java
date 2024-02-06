package de.tuberlin.dima.dbt.exercises.bplustree;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
            for(counter = 0; counter < numberOfKeys; counter++){
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

    /**
     * Insert value into leaf node (and propagate changes up)
     */
    private void insertIntoLeafNode(Integer key, String value, LeafNode node, Deque<InnerNode> parents){
        Integer[] leafKeys = node.getKeys();
        String[] leafValues = node.getValues();

        if(getNodeOccupancy(leafKeys) < this.capacity){
            //add new element at the end
            leafKeys[this.capacity - 1] = key;
            leafValues[this.capacity - 1] = value;

            //set keys and values
            node.setKeys(leafKeys);
            node.setValues(leafValues);

            //sort new node
            sortClean(node);

        }else{
            //not enough space -> split leaf
            int middle = this.capacity / 2;
            Integer []tmpKeys =  Arrays.copyOf(leafKeys, this.capacity + 2);
            String []tmpValues =  Arrays.copyOf(leafValues, this.capacity + 2);
            tmpKeys[this.capacity + 1] = key;
            tmpValues[this.capacity + 1] = value;
            LeafNode tmpLeaf = new LeafNode(tmpKeys, tmpValues, this.capacity + 2);
            sortClean(tmpLeaf);

            //copy results to right nodes
            //existing leaf
            leafKeys = new Integer[this.capacity];
            leafValues = new String[this.capacity];
            System.arraycopy(tmpLeaf.getKeys(), 0, leafKeys, 0, middle);
            System.arraycopy(tmpLeaf.getValues(), 0, leafValues, 0, middle);

            //new parent Value
            Integer newParentValue = tmpLeaf.getKeys()[middle];

            //new leaf
            Integer []newKeys = new Integer[this.capacity];
            String []newValues = new String[this.capacity];
            System.arraycopy(tmpLeaf.getKeys(), middle, newKeys, 0, middle + 1);
            System.arraycopy(tmpLeaf.getValues(), middle, newValues, 0, middle + 1);
            LeafNode newLeaf = new LeafNode(newKeys, newValues, this.capacity);

            //set keys and values
            node.setKeys(leafKeys);
            node.setValues(leafValues);
            
            //update parent
            updateParentInsert(parents, newParentValue, node, newLeaf);
        }
    }

    private void updateParentInsert(Deque<InnerNode> parents, Integer newKey, Node leftNode, Node rightNode){
        //right node is new
        int parentsSize = parents.size();
        if(parentsSize == 0){
            Integer[] innerKeys = Arrays.copyOf(new Integer[] {}, this.capacity);
            innerKeys[0] = newKey;
            Node[] children = Arrays.copyOf(new Node[] {}, this.capacity + 1);
            children[0] = leftNode;
            children[1] = rightNode;
            InnerNode newNode = new InnerNode(innerKeys, children, this.capacity);
            this.root = newNode;
            return;
        } 
        
        InnerNode parent = parents.getFirst();
        Node []children = parent.getChildren();
        Integer []innerKeys = parent.getKeys();
        if(getNodeOccupancy(parent.getKeys()) == this.capacity){
            Integer[] oversizeKeys = Arrays.copyOf(innerKeys, this.capacity + 1);
            Node[] oversizeChildren = Arrays.copyOf(children, this.capacity + 2);
            insertSortNode(oversizeKeys, oversizeChildren, newKey, rightNode);

            //get Key for parent
            int middle = this.capacity / 2;
            Integer middleKey = oversizeKeys[middle];

            //split inner node and push middle key up
            parent.setKeys(Arrays.copyOfRange(oversizeKeys, 0, middle));
            parent.setChildren(Arrays.copyOfRange(oversizeChildren, 0, middle + 1));

            //copy upper half exept middle key to new inner node
            Integer []newKeys = Arrays.copyOfRange(oversizeKeys, middle + 1, this.capacity + 1);
            Node []newChildren = Arrays.copyOfRange(oversizeChildren, middle + 1, this.capacity + 2);
            InnerNode newNode = new InnerNode(newKeys, newChildren, this.capacity);

            //update parent
            parents.removeFirst();
            updateParentInsert(parents, middleKey, parent, newNode);
        } else{
            insertSortNode(innerKeys, children, newKey, rightNode);

            //set children and keys
            parent.setChildren(children);
            parent.setKeys(innerKeys);
        }
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

    private int getNodeOccupancy(Object[] keys){
        int occupied = 0;
        for(int i=0; i<keys.length; i++){
            if(keys[i] != null){
                occupied++;
            }
        }
        return occupied;
    }

    private Integer getUpperBoundKey(Integer []keys, Integer key){
        int occupancy = getNodeOccupancy(keys);
        for(int i=0; i<occupancy; i++){
            if(key < keys[i]){
                return keys[i];
            }
        }
        return null;
    }

    private Node getNeighbor(InnerNode parent, Integer key, boolean getRightNode){
        Integer []innerNodeKeys = parent.getKeys();
        Integer upperBound = getUpperBoundKey(innerNodeKeys, key);
        boolean getLeftNode = !getRightNode;

        //return null if there is no neighbor
        if(upperBound == innerNodeKeys[0] && getLeftNode){
            return null;
        }
        if(upperBound == null && getRightNode){
            return null;
        }

        //with upperBound get Indexes of neighbors
        int occupancy = getNodeOccupancy(innerNodeKeys);
        Node []children = parent.getChildren();
        for(int i=0; i<occupancy; i++){
            if(innerNodeKeys[i] == upperBound){ 
                if(getRightNode){
                    return children[i + 1];
                } else{
                    return children[i - 1];
                }
            }
        }
        return null;
    }

    private void sortClean(LeafNode node){
        Integer []keys = node.getKeys();
        String []values = node.getValues();

        //create sorting list
        List<Map.Entry<Integer, String>> keyValueList = new ArrayList<>();

        //add all key value pairs to hashmap
        for(int i=0; i<keys.length; i++){ 
            keyValueList.add(new AbstractMap.SimpleImmutableEntry<>(keys[i], values[i]));
        }
        //sort list
        Collections.sort(keyValueList, new Comparator<Map.Entry<Integer, String>>() {
            public int compare(Map.Entry<Integer, String> kv1, Map.Entry<Integer, String> kv2) {
                if(kv1.getKey() == null) return 1;
                if(kv2.getKey() == null) return -1;
                return kv1.getKey() > kv2.getKey()  ? 1 : -1;
            }
        });

        for(int i=0; i<keys.length; i++){
            keys[i] = keyValueList.get(i).getKey();
            values[i] = keyValueList.get(i).getValue();
        }

        //extract keys and values and set keys and values
        node.setKeys(keys);
        node.setValues(values);
    }

    private void sortCleanInner(InnerNode node){
        Integer []keys = node.getKeys();
        Node []children = node.getChildren();

        Arrays.sort(keys, Comparator.nullsLast(null));
        Arrays.sort(children, Comparator.nullsLast(null));

        node.setKeys(keys);
        node.setChildren(children);
    }

    // Function to find the index of a value in an array of Integer objects
    private static int findIndex(Integer[] array, int target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null && array[i].equals(target)) {
                return i;  // Value found, return its index
            }
        }
        return -1;  // Value not found in the array
    }

    private void replaceParentKey(InnerNode parent, Integer[] child, Integer newKey, boolean fromRightNode){
        Integer[] keys = parent.getKeys();
        Integer max = -1;
        for(int i=0; i<child.length; i++){
            if(child[i] != null && child[i] > max) max = child[i];
        }
        int position = -1;
        for(int i=0; i<keys.length; i++){
            if(keys[i] == null || keys[i] > max){
                position = i;
                break;
            }
        }
        if(fromRightNode){
            keys[position - 1] = newKey;
        } else{
            keys[position] = newKey;
        }
        parent.setKeys(keys);
    }

    private void mergeNodes(InnerNode parent, LeafNode leftNode, LeafNode rightNode){
        //each node should contain >= capacity/2 keys
        Integer[] parentKeys = parent.getKeys();
        Integer[] leftKeys = leftNode.getKeys();
        Integer[] rightKeys = rightNode.getKeys();

        //get responsible Key
        Integer max = -1;
        for(int i=0; i<leftKeys.length; i++){
            if(leftKeys[i] != null && leftKeys[i] > max) max = leftKeys[i];
        }
        int position = -1;
        for(int i=0; i<parentKeys.length; i++){
            if(parentKeys[i] == null || parentKeys[i] > max){
                position = i;
                break;
            }
        }

        //combine nodes at left node
        Integer[] newKeys = Arrays.copyOf(leftKeys, leftKeys.length + rightKeys.length);
        System.arraycopy(rightKeys, 0, newKeys, leftKeys.length, rightKeys.length);

        String[] leftValues = leftNode.getValues();
        String[] rightValues = rightNode.getValues();
        String[] newValues = Arrays.copyOf(leftValues, leftValues.length + rightValues.length);
        System.arraycopy(rightValues, 0, newValues, leftValues.length, rightValues.length);

        LeafNode tmpNode = new LeafNode(newKeys, newValues, 2 * leftKeys.length);
        sortClean(tmpNode);

        leftNode.setKeys(Arrays.copyOf(tmpNode.getKeys(), leftKeys.length));
        leftNode.setValues(Arrays.copyOf(tmpNode.getValues(), leftKeys.length));


        //delete key and right node
        Node[] children = parent.getChildren();
        parentKeys[position] = null;
        children[position + 1] = null;
        parent.setChildren(children);

        sortCleanInner(parent);

    }

    private String deleteFromLeafNode(Integer key, LeafNode node,
                                      Deque<InnerNode> parents) {
        // TODO: delete value from leaf node (and propagate changes up)
        Integer[] leafKeys = node.getKeys();
        int numberOfKeys = leafKeys.length;
        int capacity = this.capacity;
        String value = null;

        int position = -1;
        for(int i=0; i < leafKeys.length; i++){
            if(leafKeys[i] == key){
                position = i;
                break;
            }
        }
        if(position == -1){
            return null;
        }

        Integer occupancy = getNodeOccupancy(leafKeys);
        if(occupancy >= (capacity / 2) + 1){
            //enough space in leaf available to just delete
            String []values = node.getValues();

            //get value
            value = values[position];

            //delete element
            if(position != numberOfKeys - 1){
                for(int i=position; i < numberOfKeys - 1; i++){
                    leafKeys[i] = leafKeys[i + 1];
                    values[i] = values[i + 1];
                }
            }
            leafKeys[numberOfKeys - 1] = null;
            values[numberOfKeys - 1] = null;
        } else{
            //not enough space so either steal or merge
            InnerNode parent = parents.getLast();
            LeafNode leftNeighbor = (LeafNode) getNeighbor(parent, key, false);
            LeafNode rightNeighbor = (LeafNode) getNeighbor(parent, key, true);
            
            Integer leftOccupancy = leftNeighbor == null ? 0 : getNodeOccupancy(leftNeighbor.getKeys());
            Integer rightOccupancy = rightNeighbor == null ? 0 : getNodeOccupancy(rightNeighbor.getKeys());
            if(leftOccupancy > (capacity / 2)){
                //we can steal from left
            } else if(rightOccupancy > (capacity / 2)){
                //we can steal from right
                Integer []ownKeys = leafKeys;
                Integer []neighborKeys = rightNeighbor.getKeys();

                String []ownValues = node.getValues();
                String []neighborValues = rightNeighbor.getValues();

                //get Value
                value = ownValues[position];

                //overwrite position with lowest entry in neighbor
                ownKeys[position] = neighborKeys[0];
                ownValues[position] = neighborValues[0];

                //adapt parent
                Integer newParentKey = neighborKeys[1];
                replaceParentKey(parent, neighborKeys, newParentKey, true);
                
                //remove value from neighbor
                neighborKeys[0] = null;
                neighborValues[0] = null;

                //set all values
                node.setKeys(ownKeys);
                node.setValues(ownValues);
                rightNeighbor.setKeys(neighborKeys);
                rightNeighbor.setValues(neighborValues);

                //sort new arrays
                sortClean(node);
                sortClean(rightNeighbor);
            } else if(rightNeighbor != null){
                //merge with right neighbor
                Integer []ownKeys = leafKeys;
                String []ownValues = node.getValues();

                //get deletion value
                value = ownValues[position];

                //deleteElement
                ownKeys[position] = null;
                ownValues[position] = null;

                //set deletion
                node.setKeys(ownKeys);
                node.setValues(ownValues);

                //sort new array
                sortClean(node);

                //correct bp tree
                mergeNodes(parent, node, rightNeighbor);

            } else if(leftNeighbor != null){
                //merge with left neighbor
            }
        }

        return value;
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
