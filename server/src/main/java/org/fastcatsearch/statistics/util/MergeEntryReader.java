package org.fastcatsearch.statistics.util;

public class MergeEntryReader<E extends RunEntry> {

	private int[] heap;
	private RunEntryReader<E>[] reader;
	private int runSize;

	private E entry;
	private E entryOld;

	public MergeEntryReader(RunEntryReader<E>[] entryReaderList) {
		this.reader = entryReaderList;

		runSize = entryReaderList.length;
		makeHeap(runSize);

	}

	protected E read() {
		// int kk = 0;
		int count = 0;
		
		E result = null;
		while (true) {
			int idx = heap[1];
			entry = reader[idx].entry();

			if (entry == null && entryOld == null) {
				// if cv and cvOld are null, it's done
				return null;
			}

			// cv == null일경우는 모든 reader가 종료되어 null이 된경우이며
			// cvOld 와 cv 가 다른 경우는 머징시 텀이 바뀐경우. cvOld를 기록해야한다.
			if ((entry == null || (!entry.equals(entryOld)) && entryOld != null)) {

				// entryOld를 리턴한다.
				result = entryOld;
			}

			// 갯수머징..
			if (entryOld != null) {
				entry.merge(entryOld);
			}

			// backup cv to old
			entryOld = entry;

			reader[idx].next();

			heapify(1, runSize);
			
			if(result != null){
				return result;
			}

		} // while(true)

	}

	protected void makeHeap(int heapSize) {
		heap = new int[heapSize + 1];
		// index starts from 1
		for (int i = 0; i < heapSize; i++) {
			heap[i + 1] = i;
		}

		int n = heapSize >> 1; // last inner node index

		for (int i = n; i > 0; i--) {
			heapify(i, heapSize);
		}

	}

	protected void heapify(int idx, int heapSize) {

		int temp = -1;
		int child = -1;

		while (idx <= heapSize) {
			int left = idx << 1;// *=2
			int right = left + 1;

			if (left <= heapSize) {
				if (right <= heapSize) {
					// 키워드가 동일할 경우 먼저 flush된 reader가 우선해야, docNo가 오름차순 정렬순서대로
					// 올바로 기록됨.
					// flush후 머징시 문제가 생기는 버그 해결됨 2013-5-21 swsong
					int c = compareKey(left, right);
					if (c < 0) {
						child = left;
					} else if (c > 0) {
						child = right;
					} else {
						child = right;
					}
				} else {
					// if there is no right el.
					child = left;
				}
			} else {
				// no children
				break;
			}

			// compare and swap
			int c = compareKey(child, idx);
			if (c < 0) {
				temp = heap[child];
				heap[child] = heap[idx];
				heap[idx] = temp;
				idx = child;
				// System.out.println("idx1="+idx);
			} else if (c == 0) {
				break;
			} else {
				// sorted, then do not check child
				break;
			}

		}
	}

	protected int compareKey(int one, int another) {
		int a = heap[one];
		int b = heap[another];

		return compareKey(reader[a].entry(), reader[b].entry());
	}

	private int compareKey(E entry, E entry2) {
		// reader gets EOS, returns null
		return entry.compareTo(entry2);
	}
}
