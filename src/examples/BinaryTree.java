import gov.nasa.jpf.vm.Verify;

public class BinaryTree {

	Node root;

	class Node {
		int key;
		String name;

		Node leftChild;
		Node rightChild;

		Node(int key, String name) {
			this.key = key;
			this.name = name;
		}

		@Override
		public String toString() {
			return name + " has the value " + key;
		}
	}

	public void addNode(int key, String name) {
		Node newNode = new Node(key, name);

		if (root == null) {
			root = newNode;
		} else {
			Node focusNode = root;
			Node parent;

			while (true) {
				parent = focusNode;

				if (key < focusNode.key) {
					focusNode = focusNode.leftChild;
					if (focusNode == null) {
						parent.leftChild = newNode;
						return;
					}
				} else {
					focusNode = focusNode.rightChild;

					if (focusNode == null) {
						parent.rightChild = newNode;
						return;
					}
				}
			}
		}
	}

	public Node findNode(int key) {
		Node focusNode = root;

		while (focusNode.key != key) {
			if (key < focusNode.key) {
				focusNode = focusNode.leftChild;
			} else {
				focusNode = focusNode.rightChild;
			}

			if (focusNode == null)
				return null;
		}

		return focusNode;
	}

	public void preorderTraverseTree(Node focusNode) {
		if (focusNode != null) {
			System.out.println(focusNode);

			preorderTraverseTree(focusNode.leftChild);
			preorderTraverseTree(focusNode.rightChild);
		}
	}

	public static void main(String[] args) {
		BinaryTree theTree = new BinaryTree();
		int n;
		String str1;

		for (int x = 0; x < 1000; x++) {
			str1 = Integer.toString(x);
			n = Verify.random(50);
			theTree.addNode(n, str1);
			Verify.assertTrue(theTree.findNode(5).name == null);
		}

		theTree.preorderTraverseTree(theTree.root);

	}

}