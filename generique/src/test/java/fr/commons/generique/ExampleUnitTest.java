package fr.commons.generique;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.commons.generique.ui.AbstractGeneriqueAdapter;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
	@Test
	public void addition_isCorrect() {
		assertEquals(4, 2 + 2);
	}

	@Test
	public void filter_isCorrect(){
		// Params
		List<String> originalList = Arrays.asList("A","B","C","D","E","F");
		List<String> currentList = Arrays.asList("A","B","F");
		List<String> filterList = Arrays.asList("B","E","F");
		// Algo
		List<String> previousList = currentList;
		List<String> tmpList = new ArrayList<>(currentList);
		currentList = filterList;
		if (currentList == null) {
			currentList = new ArrayList<>();
		}

		System.out.println(tmpList.toString());
		int lastIdxValid = 0;
		for (int i = 0; i < originalList.size(); i++) {
			final String t = originalList.get(i);
			boolean isPresentBefore = previousList.contains(t);
			boolean isPresentAfter = currentList.contains(t);
			if (isPresentBefore) {
				int idx = tmpList.indexOf(t);
				if (!isPresentAfter) {
					System.out.println("Remove "+t+" at "+idx);
					tmpList.remove(idx);
					//notifyItemRemoved(idx);
				} else {
					lastIdxValid = idx;
					System.out.println("Reste "+t+" at "+idx);
				}
			} else {
				int idx = lastIdxValid+1;
				if (isPresentAfter) {
					System.out.println("Insert "+t+" at "+idx);
					tmpList.add(idx, t);
					//notifyItemInserted(i);
					lastIdxValid = idx;
				} else {
					System.out.println("Sans "+t+" at "+idx);
				}
			}
			System.out.println(tmpList.toString());
		}
		System.out.println(tmpList.toString());

		assertEquals("Aie", tmpList.toString(), currentList.toString());
	}
}