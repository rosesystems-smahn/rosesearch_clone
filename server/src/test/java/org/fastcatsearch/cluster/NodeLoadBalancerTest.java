package org.fastcatsearch.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * 여러 컬렉션을 검색할때 잘 분배되는지 테스트가 필요하다.
 * */
public class NodeLoadBalancerTest {

	@Test
	public void test() {

		int COUNT = 100000;

		NodeLoadBalancer nodeLoadBalancer = new NodeLoadBalancer();

		List<Node> list = new ArrayList<Node>();
		String id1 = "col1";
		list.add(makeNode(0));
		list.add(makeNode(1));
		nodeLoadBalancer.update(id1, list);

		List<Node> list2 = new ArrayList<Node>();
		String id2 = "col2";
		list2.add(makeNode(0));
		list2.add(makeNode(1));
		list2.add(makeNode(2));
		nodeLoadBalancer.update(id2, list2);

		int[] hit = new int[] { 0, 0, 0, 0 };

		for (int i = 0; i < COUNT; i++) {
			Node selectedNode = nodeLoadBalancer.getBalancedNode(id1);

			nodeLoadBalancer.getBalancedNode(id2);

			String idStr = selectedNode.id();
			String seq = idStr.substring(idStr.length() - 1, idStr.length());
			hit[Integer.parseInt(seq)]++;
		}

		for (int i = 0; i < hit.length; i++) {
			System.out.println(i + "] = " + hit[i]);
		}

	}

	AtomicInteger[] totalHit = new AtomicInteger[]{ new AtomicInteger(), new AtomicInteger(), new AtomicInteger() };
	@Test
	public void testMultiThread() {
		final NodeLoadBalancer nodeLoadBalancer = new NodeLoadBalancer();

		List<Node> list1 = new ArrayList<Node>();
		String id1 = "col0";
		list1.add(makeNode(0));
		list1.add(makeNode(1));
		list1.add(makeNode(2));
		nodeLoadBalancer.update(id1, list1);
		
		List<Node> list2 = new ArrayList<Node>();
		String id2 = "col1";
		list2.add(makeNode(0));
		list2.add(makeNode(1));
		list2.add(makeNode(2));
		nodeLoadBalancer.update(id2, list2);
		
		List<Node> list3 = new ArrayList<Node>();
		String id3 = "col2";
		list3.add(makeNode(0));
		list3.add(makeNode(1));
		list3.add(makeNode(2));
		nodeLoadBalancer.update(id3, list3);
		
		List<Node> list4 = new ArrayList<Node>();
		String id4 = "col3";
		list4.add(makeNode(0));
		list4.add(makeNode(1));
		list4.add(makeNode(2));
		nodeLoadBalancer.update(id4, list4);
		
		List<Node> list5 = new ArrayList<Node>();
		String id5 = "col4";
		list5.add(makeNode(0));
		list5.add(makeNode(1));
		list5.add(makeNode(2));
		nodeLoadBalancer.update(id5, list5);
		
		List<Node> list6 = new ArrayList<Node>();
		String id6 = "col5";
		list6.add(makeNode(0));
		list6.add(makeNode(1));
		list6.add(makeNode(2));
		nodeLoadBalancer.update(id6, list6);

		int count = 1000;
		Thread t1 = new TestThread("test-1", count, nodeLoadBalancer);
		Thread t2 = new TestThread("test-2", count, nodeLoadBalancer);
		Thread t3 = new TestThread("test-3", count, nodeLoadBalancer);
		t1.start();
		t2.start();
		t3.start();

		try {
			t1.join();
//			t2.join();
//			t3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < totalHit.length; i++) {
			System.out.println("Total node #" +i + " > hit=" + totalHit[i]);
		}
	}

	class TestThread extends Thread {
		String name;
		int count;
		NodeLoadBalancer nodeLoadBalancer;

		public TestThread(String name, int count, NodeLoadBalancer nodeLoadBalancer) {
			this.name = name;
			this.count = count;
			this.nodeLoadBalancer = nodeLoadBalancer;
		}

		@Override
		public void run() {
			int[] hit = new int[] { 0, 0, 0 };
			for (int i = 0; i < count; i++) {
//				randomSleep();
				if(i % 100 == 0){
					System.out.println(name+" "+ i+".. ");
				}
				for (int j = 0; j < 6; j++) {
					Node selectedNode = nodeLoadBalancer.getBalancedNode("col" + j);
					String idStr = selectedNode.id();
					
//					System.out.println("col" +j +" >> " + idStr);
					String seq = idStr.substring(idStr.length() - 1, idStr.length());
					hit[Integer.parseInt(seq)]++;
				}
			}

			System.out.println("---" + name);
			for (int i = 0; i < hit.length; i++) {
				System.out.println("node #" +i + " > hit=" + hit[i]);
				totalHit[i].addAndGet(hit[i]);
			}
		}
	}

	final Random r = new Random(System.currentTimeMillis());

	private void randomSleep() {
		try {
			Thread.sleep(r.nextInt(10));
		} catch (InterruptedException e) {
		}
	}

	private Node makeNode(int i) {
		Node node = new Node("node-" + i, "node-" + i, "localhost", 9090);
		node.setActive();
		node.setEnabled();
		return node;
	}

}
