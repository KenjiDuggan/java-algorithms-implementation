package com.jwetherell.algorithms.data_structures;

import java.util.ArrayList;
import java.util.List;


/**
 * An AVL tree is a self-balancing binary search tree, and it was the first such data 
 * structure to be invented. In an AVL tree, the heights of the two child subtrees 
 * of any node differ by at most one. AVL trees are often compared with red-black trees 
 * because they support the same set of operations and because red-black trees also take 
 * O(log n) time for the basic operations. Because AVL trees are more rigidly balanced, 
 * they are faster than red-black trees for lookup intensive applications. However, 
 * red-black trees are faster for insertion and removal.
 * 
 * http://en.wikipedia.org/wiki/AVL_tree
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class AVLTree2<T extends Comparable<T>> extends BinarySearchTree<T> {

    private enum Position { LEFT, RIGHT };
    private enum Balance { LEFT_LEFT, LEFT_RIGHT, RIGHT_LEFT, RIGHT_RIGHT }; 


    @Override
    protected Node<T> addValue(T value) {
        AVLNode<T> nodeToAdd = new AVLNode<T>(null,value);
        super.add(nodeToAdd);
        nodeToAdd.updateHeight(true);

        while (nodeToAdd!=null) {
            balanceAfterInsert(nodeToAdd);
            nodeToAdd = (AVLNode<T>) nodeToAdd.parent;
        }

        return nodeToAdd;
    }

    private void balanceAfterInsert(AVLNode<T> node) {
        AVLNode<T> grandParent = (AVLNode<T>) node;
        //Only balance if height > 2
        int balanceFactor = grandParent.getBalanceFactor();
        if (balanceFactor>1 || balanceFactor<-1) {
            AVLNode<T> parent = null;
            AVLNode<T> child = null;
            Balance balance = null;
            if (balanceFactor<0) {
                parent = (AVLNode<T>) grandParent.lesser;
                balanceFactor = parent.getBalanceFactor();
                if (balanceFactor<0) {
                    child = (AVLNode<T>) parent.lesser;
                    balance = Balance.LEFT_LEFT;
                } else {
                    child = (AVLNode<T>) parent.greater;
                    balance = Balance.LEFT_RIGHT;
                }
            } else {
                parent = (AVLNode<T>) grandParent.greater;
                balanceFactor = parent.getBalanceFactor();
                if (balanceFactor<0) {
                    child = (AVLNode<T>) parent.lesser;
                    balance = Balance.RIGHT_LEFT;
                } else {
                    child = (AVLNode<T>) parent.greater;
                    balance = Balance.RIGHT_RIGHT;
                }
            }

            if (balance == Balance.LEFT_RIGHT) {
                //Left-Right (Left rotation, right rotation)
                leftRotation(parent);
                rightRotation(grandParent);
            } else if (balance == Balance.RIGHT_LEFT) {
                //Right-Left (Right rotation, left rotation)
                rightRotation(parent);
                leftRotation(grandParent);
            } else if (balance == Balance.LEFT_LEFT) {
                //Left-Left (Right rotation)
                rightRotation(grandParent);
            } else {
                //Right-Right (Left rotation)
                leftRotation(grandParent);
            }

            grandParent.updateHeight(false); //New child node
            child.updateHeight(false); //New child node
            parent.updateHeight(true); //New Parent node
        }
    }

    @Override
    protected Node<T> removeValue(T value) {
        //Remove node
        Node<T> nodeRemoved = root;
        AVLNode<T> nodeToMoveUp = null;
        AVLNode<T> nodeToRefactor = null;
        while (nodeRemoved != null) {
            if (value.compareTo(nodeRemoved.value) < 0) {
                // Node to remove is less than current node
                nodeRemoved = nodeRemoved.lesser;
            } else if (value.compareTo(nodeRemoved.value) > 0) {
                // Node to remove is greater then current node
                nodeRemoved = nodeRemoved.greater;
            } else if (value.compareTo(nodeRemoved.value) == 0) {
                // We found our node or the first occurrence of our node
                Node<T> parent = nodeRemoved.parent;
                if (parent == null) {
                    // Replacing the root node
                    if (nodeRemoved.lesser != null && nodeRemoved.greater == null) {
                        // Replace root with lesser subtree
                        root = nodeRemoved.lesser;
                        nodeToRefactor = (AVLNode<T>) root.parent;

                        // Root not should not have a parent
                        root.parent = null;
                    } else if (nodeRemoved.greater != null && nodeRemoved.lesser==null) {
                        // Replace root with greater subtree
                        root = nodeRemoved.greater;
                        nodeToRefactor = (AVLNode<T>) root.parent;

                        // Root not should not have a parent
                        root.parent = null;
                    } else if (nodeRemoved.greater != null && nodeRemoved.lesser!=null) {
                        //Two children
                        nodeToMoveUp = (AVLNode<T>) this.getLeast(nodeRemoved.greater);
                        Node<T> greater = null;
                        if (nodeToMoveUp==null) {
                            nodeToMoveUp = (AVLNode<T>) nodeRemoved.greater;

                            nodeToRefactor = (AVLNode<T>) nodeToMoveUp;
                        } else {
                            greater = nodeToMoveUp.greater;
                            nodeToMoveUp.parent.lesser = greater;
                            if (greater!=null) greater.parent = nodeToMoveUp.parent;

                            nodeToMoveUp.greater = nodeRemoved.greater;
                            nodeRemoved.greater.parent = nodeToMoveUp;

                            nodeToRefactor = (AVLNode<T>) nodeToMoveUp.parent;
                        }

                        nodeToMoveUp.lesser = nodeRemoved.lesser;
                        nodeRemoved.lesser.parent = nodeToMoveUp;
                        
                        root = nodeToMoveUp;
                        root.parent = null;
                    } else {
                        // No children...
                        root = null;
                    }
                } else if (parent.lesser != null && (parent.lesser.value.compareTo(nodeRemoved.value) == 0)) {
                    // If the node to remove is the parent's lesser node, replace
                    // the parent's lesser node with one of the node to remove's
                    // lesser/greater subtrees
                    if (nodeRemoved.lesser != null && nodeRemoved.greater == null) {
                        // Using the less subtree
                        nodeToMoveUp = (AVLNode<T>) nodeRemoved.lesser;
                        nodeToRefactor = (AVLNode<T>) nodeToMoveUp.parent;

                        parent.lesser = nodeToMoveUp;
                        nodeToMoveUp.parent = parent;
                    } else if (nodeRemoved.greater != null && nodeRemoved.lesser == null) {
                        // Using the greater subtree (there is no lesser subtree, no refactoring)
                        nodeToMoveUp = (AVLNode<T>) nodeRemoved.greater;
                        nodeToRefactor = (AVLNode<T>) nodeToMoveUp.parent;

                        parent.lesser = nodeToMoveUp;
                        nodeToMoveUp.parent = parent;
                    } else if (nodeRemoved.greater != null && nodeRemoved.lesser!=null) {
                        //Two children
                        nodeToMoveUp = (AVLNode<T>) this.getLeast(nodeRemoved.greater);
                        Node<T> greater = null;
                        if (nodeToMoveUp==null) {
                            nodeToMoveUp = (AVLNode<T>) nodeRemoved.greater;
                            
                            nodeToRefactor = (AVLNode<T>) nodeToMoveUp;
                        } else {
                            greater = nodeToMoveUp.greater;
                            nodeToMoveUp.parent.lesser = greater;
                            if (greater!=null) greater.parent = nodeToMoveUp.parent;

                            nodeToMoveUp.greater = nodeRemoved.greater;
                            nodeRemoved.greater.parent = nodeToMoveUp;

                            nodeToRefactor = (AVLNode<T>) nodeToMoveUp.parent;
                        }

                        nodeToMoveUp.lesser = nodeRemoved.lesser;
                        nodeRemoved.lesser.parent = nodeToMoveUp;
                        
                        parent.lesser = nodeToMoveUp;
                        nodeToMoveUp.parent = parent;
                    } else {
                        // No children...
                        parent.lesser = null;
                        nodeToRefactor = (AVLNode<T>) parent;
                    }
                } else if (parent.greater != null && (parent.greater.value.compareTo(nodeRemoved.value) == 0)) {
                    // If the node to remove is the parent's greater node, replace
                    // the parent's greater node with the node's greater node
                    if (nodeRemoved.lesser != null && nodeRemoved.greater == null) {
                        // Using the less subtree
                        nodeToMoveUp = (AVLNode<T>) nodeRemoved.lesser;
                        nodeToRefactor = (AVLNode<T>) nodeToMoveUp.parent;

                        parent.greater = nodeToMoveUp;
                        nodeToMoveUp.parent = parent;
                    } else if (nodeRemoved.greater != null && nodeRemoved.lesser == null) {
                        // Using the greater subtree (there is no lesser subtree, no refactoring)
                        nodeToMoveUp = (AVLNode<T>) nodeRemoved.greater;
                        
                        parent.greater = nodeToMoveUp;
                        nodeToMoveUp.parent = parent;
                        nodeToRefactor = (AVLNode<T>) parent;
                    } else if (nodeRemoved.greater != null && nodeRemoved.lesser!=null) {
                        //Two children
                        nodeToMoveUp = (AVLNode<T>) this.getGreatest(nodeRemoved.lesser);
                        Node<T> lesser = null;
                        if (nodeToMoveUp==null) {
                            nodeToMoveUp = (AVLNode<T>) nodeRemoved.lesser;
                            nodeToRefactor = (AVLNode<T>) nodeToMoveUp;
                        } else {
                            lesser = nodeToMoveUp.lesser;
                            nodeToMoveUp.parent.greater = lesser;
                            if (lesser!=null) lesser.parent = nodeToMoveUp.parent;

                            nodeToMoveUp.lesser = nodeRemoved.lesser;
                            nodeRemoved.lesser.parent = nodeToMoveUp;
                            nodeToRefactor = (AVLNode<T>) nodeToMoveUp.parent;
                        }

                        nodeToMoveUp.greater = nodeRemoved.greater;
                        nodeRemoved.greater.parent = nodeToMoveUp;
                        
                        parent.greater = nodeToMoveUp;
                        nodeToMoveUp.parent = parent;
                    } else {
                        // No children...
                        parent.greater = null;
                        nodeToRefactor = (AVLNode<T>) parent;
                    }
                }
                size--;
                break;
            }
        }
        if (nodeToRefactor!=null) {
            if (nodeToRefactor.lesser!=null) 
                ((AVLNode<T>)nodeToRefactor.lesser).updateHeight(false);
            if (nodeToRefactor.greater!=null) 
                ((AVLNode<T>)nodeToRefactor.greater).updateHeight(false);
            ((AVLNode<T>)nodeToRefactor).updateHeight(true);
        }

        if (nodeToRefactor!=null) {
            while (nodeToRefactor!=null) {
                balanceAfterDelete(nodeToRefactor);
                nodeToRefactor = (AVLNode<T>) nodeToRefactor.parent;
            }
        }

        return nodeRemoved;
    }

    private void balanceAfterDelete(AVLNode<T> node) {
        int balanceFactor = node.getBalanceFactor();
        if (balanceFactor==0 || balanceFactor>1 || balanceFactor<-1) {
            if (balanceFactor==-2) {
                AVLNode<T> ll = (AVLNode<T>) node.lesser.lesser;
                int lesser = (ll!=null)?ll.height:0;
                AVLNode<T> lr = (AVLNode<T>) node.lesser.greater;
                int greater = (lr!=null)?lr.height:0;
                if (lesser>=greater) {
                    rightRotation(node);
                } else {
                    leftRotation(node.lesser);
                    rightRotation(node);
                    
                    AVLNode<T> p = (AVLNode<T>) node.parent;
                    if (p.lesser!=null) ((AVLNode<T>)p.lesser).updateHeight(false);
                    if (p.greater!=null) ((AVLNode<T>)p.greater).updateHeight(false);
                    p.updateHeight(false);
                }
            } else if (balanceFactor==2) {
                AVLNode<T> rr = (AVLNode<T>) node.greater.greater;
                int greater = (rr!=null)?rr.height:0;
                AVLNode<T> rl = (AVLNode<T>) node.greater.lesser;
                int lesser = (rl!=null)?rl.height:0;
                if (greater>=lesser) {
                    leftRotation(node);
                } else {
                    rightRotation(node.greater);
                    leftRotation(node);

                    AVLNode<T> p = (AVLNode<T>) node.parent;
                    if (p.lesser!=null) ((AVLNode<T>)p.lesser).updateHeight(false);
                    if (p.greater!=null) ((AVLNode<T>)p.greater).updateHeight(false);
                    p.updateHeight(false);
                }
            } else {
                // zero
            }
            node.updateHeight(false);
            if (node.parent!=null) ((AVLNode<T>)node.parent).updateHeight(false);
        }
    }

    private void leftRotation(Node<T> node) {
        Position parentPosition = null;
        Node<T> parent = node.parent;
        if (parent!=null) {
            if (node.equals(parent.lesser)) {
                //Lesser
                parentPosition = Position.LEFT;
            } else {
                //Greater
                parentPosition = Position.RIGHT;
            }
        }
        
        Node<T> greater = node.greater;
        node.greater = null;
        Node<T> lesser = greater.lesser;
        
        greater.lesser = node;
        node.parent = greater;
        
        node.greater = lesser;
        if (lesser!=null) lesser.parent = node;
        
        if (parentPosition!=null) {
            if (parentPosition==Position.LEFT) {
                parent.lesser = greater;
            } else {
                parent.greater = greater;
            }
            greater.parent = parent;
        } else {
            root = greater;
            greater.parent = null;
        }
    }

    private void rightRotation(Node<T> node) {
        Position parentPosition = null;
        Node<T> parent = node.parent;
        if (parent!=null) {
            if (node.equals(parent.lesser)) {
                //Lesser
                parentPosition = Position.LEFT;
            } else {
                //Greater
                parentPosition = Position.RIGHT;
            }
        }
        
        Node<T> lesser = node.lesser;
        node.lesser = null;
        Node<T> greater = lesser.greater;
        
        lesser.greater = node;
        node.parent = lesser;
        
        node.lesser = greater;
        if (greater!=null) greater.parent = node;
        
        if (parentPosition!=null) {
            if (parentPosition==Position.LEFT) {
                parent.lesser = lesser;
            } else {
                parent.greater = lesser;
            }
            lesser.parent = parent;
        } else {
            root = lesser;
            lesser.parent = null;
        }
    }

    @Override
    protected boolean validateNode(Node<T> node) {
        boolean bst = super.validateNode(node);
        if (!bst) return false;

        AVLNode<T> avlNode = (AVLNode<T>) node;
        int balanceFactor = avlNode.getBalanceFactor();
        if (balanceFactor>1 || balanceFactor<-1) {
            return false;
        }
        if (avlNode.isLeaf() && avlNode.height!=1) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return AVLTreePrinter.getString(this);
    }


    protected static class AVLNode<T extends Comparable<T>> extends Node<T> {

        protected int height = 1;

        protected AVLNode(Node<T> parent, T value) {
            super(parent,value);
        }

        protected boolean isLeaf() {
            return ((lesser == null) && (greater == null));
        }
        
        protected void updateHeight(boolean recurseUpTheTree) {
            int lesserHeight = 0;
            int greaterHeight = 0;
            if (lesser != null) {
                AVLNode<T> lesserAVLNode = (AVLNode<T>) lesser;
                lesserHeight = lesserAVLNode.height;
            }
            if (greater != null) {
                AVLNode<T> greaterAVLNode = (AVLNode<T>) greater;
                greaterHeight = greaterAVLNode.height;
            }
            
            if (lesserHeight>greaterHeight) {
                height = lesserHeight+1;
            } else {
                height = greaterHeight+1;
            }
            
            if (recurseUpTheTree && parent!=null) {
                AVLNode<T> parentAVLNode = (AVLNode<T>) parent;
                parentAVLNode.updateHeight(true);
            }
        }

        protected int getBalanceFactor() {
            int lesserHeight = 0;
            int greaterHeight = 0;
            if (lesser != null) {
                AVLNode<T> lesserAVLNode = (AVLNode<T>) lesser;
                lesserHeight = lesserAVLNode.height;
            }
            if (greater != null) {
                AVLNode<T> greaterAVLNode = (AVLNode<T>) greater;
                greaterHeight = greaterAVLNode.height;
            }
            return greaterHeight-lesserHeight;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return AVLTreePrinter.getString(this);
            /*
            return "value=" + value + 
                   " height=" + height + 
                   " parent=" + ((parent != null) ? parent.value : "NULL") + 
                   " lesser=" + ((lesser != null) ? lesser.value : "NULL") + 
                   " greater=" + ((greater != null) ? greater.value : "NULL");
            */
        }
    }

    protected static class AVLTreePrinter {

        public static <T extends Comparable<T>> String getString(AVLTree2<T> tree) {
            if (tree.root == null) return "Tree has no nodes.";
            return getString((AVLNode<T>)tree.root, "", true);
        }

        public static <T extends Comparable<T>> String getString(AVLNode<T> node) {
            if (node == null) return "Sub-tree has no nodes.";
            return getString(node, "", true);
        }

        private static <T extends Comparable<T>> String getString(AVLNode<T> node, String prefix, boolean isTail) {
            StringBuilder builder = new StringBuilder();

            builder.append(prefix + (isTail ? "└── " : "├── ") + "(" + node.height + ") " + node.value + "\n");
            List<Node<T>> children = null;
            if (node.lesser != null || node.greater != null) {
                children = new ArrayList<Node<T>>(2);
                if (node.lesser != null) children.add(node.lesser);
                if (node.greater != null) children.add(node.greater);
            }
            if (children != null) {
                for (int i = 0; i < children.size() - 1; i++) {
                    builder.append(getString((AVLNode<T>)children.get(i), prefix + (isTail ? "    " : "│   "), false));
                }
                if (children.size() >= 1) {
                    builder.append(getString((AVLNode<T>)children.get(children.size() - 1), prefix + (isTail ? "    " : "│   "), true));
                }
            }

            return builder.toString();
        }
    }
}