import static org.junit.Assert.*;


import gov.nasa.jpf.vm.Verify;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;

/**
 * Code by @author Wonsun Ahn
 * 
 * <p>Uses the Java Path Finder model checking tool to check BeanCounterLogic in
 * various modes of operation. It checks BeanCounterLogic in both "luck" and
 * "skill" modes for various numbers of slots and beans. It also goes down all
 * the possible random path taken by the beans during operation.
 */

public class BeanCounterLogicTest {
	private static BeanCounterLogic logic; // The core logic of the program
	private static Bean[] beans; // The beans in the machine
	private static String failString; // A descriptive fail string for assertions

	private static int slotCount; // The number of slots in the machine we want to test
	private static int beanCount; // The number of beans in the machine we want to test
	private static boolean isLuck; // Whether the machine we want to test is in "luck" or "skill" mode

	/**
	 * Sets up the test fixture.
	 */
	@BeforeClass
	public static void setUp() {
		/*
		 * TODO: Use the Java Path Finder Verify API to generate choices for slotCount,
		 * beanCount, and isLuck: slotCount should take values 1-5, beanCount should
		 * take values 0-3, and isLucky should be either true or false. For reference on
		 * how to use the Verify API, look at:
		 * https://github.com/javapathfinder/jpf-core/wiki/Verify-API-of-JPF
		 */
		
		/*
		slotCount = Verify.getInt(1, 5);
		beanCount = Verify.getInt(0, 3);
		isLuck = Verify.getBoolean();
		*/

		slotCount = 1;
		beanCount = 0;
		isLuck = false;
				
		// Create the internal logic
		logic = BeanCounterLogic.createInstance(slotCount);
		// Create the beans
		beans = new Bean[beanCount];
		for (int i = 0; i < beanCount; i++) {
			beans[i] = Bean.createInstance(slotCount, isLuck, new Random());
		}
		
		// A failstring useful to pass to assertions to get a more descriptive error.
		failString = "Failure in (slotCount=" + slotCount + ", beanCount=" + beanCount
				+ ", isLucky=" + isLuck + "):";
	}

	@AfterClass
	public static void tearDown() {
	}

	/**
	 * Test case for void void reset(Bean[] beans).
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 * Invariants: If beanCount is greater than 0,
	 *             remaining bean count is beanCount - 1
	 *             in-flight bean count is 1 (the bean initially at the top)
	 *             in-slot bean count is 0.
	 *             If beanCount is 0,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is 0.
	 */
	@Test
	public void testReset() {
		// TODO: Implement
		/*
		 * Currently, it just prints out the failString to demonstrate to you all the
		 * cases considered by Java Path Finder. If you called the Verify API correctly
		 * in setUp(), you should see all combinations of machines
		 * (slotCount/beanCount/isLucky) printed here:
		 * 
		 * Failure in (slotCount=1, beanCount=0, isLucky=false):
		 * Failure in (slotCount=1, beanCount=0, isLucky=true):
		 * Failure in (slotCount=1, beanCount=1, isLucky=false):
		 * Failure in (slotCount=1, beanCount=1, isLucky=true):
		 * ...
		 * 
		 * PLEASE REMOVE when you are done implementing.
		 */
		//System.out.println(failString);
		logic.reset(beans);
		if (beanCount > 0) {
			Assert.assertEquals(failString, beanCount - 1, logic.getRemainingBeanCount());
			Assert.assertEquals(failString, 1, ((BeanCounterLogicImpl)logic).getInFlightBeanCount(slotCount));
		} else {
			Assert.assertEquals(failString, 0, logic.getRemainingBeanCount());
			Assert.assertEquals(failString, 0, ((BeanCounterLogicImpl)logic).getInFlightBeanCount(slotCount));
		}

		Assert.assertEquals(failString, 0, ((BeanCounterLogicImpl)logic).getInSlotBeanCount(slotCount));

	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After each advanceStep(),
	 *             all positions of in-flight beans are legal positions in the logical coordinate system.
	 */
	@Test
	public void testAdvanceStepCoordinates() {
		// TODO: Implement
		int x;
		boolean legal;

		logic.reset(beans);
		while (logic.advanceStep()) {
			for (int y = 0; y < slotCount; y++) {
				x = logic.getInFlightBeanXPos(y);
				legal = (x >= 0 && x <= y) || x == -1;
				Assert.assertTrue(failString, legal);
			}
		}
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After each advanceStep(),
	 *             the sum of remaining, in-flight, and in-slot beans is equal to beanCount.
	 */
	@Test
	public void testAdvanceStepBeanCount() {
		// TODO: Implement
		int total;

		logic.reset(beans);
		while (logic.advanceStep()) {
			total = logic.getRemainingBeanCount();
			total = total + ((BeanCounterLogicImpl)logic).getInFlightBeanCount(slotCount);
			total = total + ((BeanCounterLogicImpl)logic).getInSlotBeanCount(slotCount);
			Assert.assertEquals(failString, beanCount, total);
		}
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After the machine terminates,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is beanCount.
	 */
	@Test
	public void testAdvanceStepPostCondition() {
		// TODO: Implement
		logic.reset(beans);
		while (logic.advanceStep()) {}
		Assert.assertEquals(failString, 0, logic.getRemainingBeanCount());
		Assert.assertEquals(failString, 0, ((BeanCounterLogicImpl)logic).getInFlightBeanCount(slotCount));
		Assert.assertEquals(failString, beanCount, ((BeanCounterLogicImpl)logic).getInSlotBeanCount(slotCount));
	}
	
	/**
	 * Test case for void lowerHalf()().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.lowerHalf().
	 * Invariants: After calling logic.lowerHalf(),
	 *             slots in the machine contain only the lower half of the original beans.
	 *             Remember, if there were an odd number of beans, (N+1)/2 beans should remain.
	 *             Check each slot for the expected number of beans after having called logic.lowerHalf().
	 */
	@Test
	public void testLowerHalf() {
		// TODO: Implement
		logic.reset(beans);
		while (logic.advanceStep()) {}

		int[] lowerSlots = new int[slotCount / 2];
		for (int i = 0; i < slotCount / 2; i++) {
			lowerSlots[i] = logic.getSlotBeanCount(i);
		}			
	
		logic.lowerHalf();

		int remainingBeans;
		if (beanCount % 2 == 0) {
			remainingBeans = beanCount / 2;
		} else {
			remainingBeans = (beanCount + 1) / 2;
		}
		
		Assert.assertEquals(failString, remainingBeans, ((BeanCounterLogicImpl)logic).getInSlotBeanCount(slotCount));
		
		for (int i = 0; i < slotCount; i++) {
			if (i < slotCount / 2) {
				Assert.assertEquals(failString, lowerSlots[i], logic.getSlotBeanCount(i));
			} else {
				Assert.assertEquals(failString, 0, logic.getSlotBeanCount(i));
			}

		}

	}
	
	/**
	 * Test case for void upperHalf().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.lowerHalf().
	 * Invariants: After calling logic.upperHalf(),
	 *             slots in the machine contain only the upper half of the original beans.
	 *             Remember, if there were an odd number of beans, (N+1)/2 beans should remain.
	 *             Check each slot for the expected number of beans after having called logic.upperHalf().
	 */
	@Test
	public void testUpperHalf() {
		// TODO: Implement
		logic.reset(beans);
		while (logic.advanceStep()) {}

		int[] upperSlots = new int[slotCount / 2];
		int j = 0;
		for (int i = slotCount / 2; i < slotCount; i++) {
			upperSlots[j] = logic.getSlotBeanCount(i);
			j++;
		}

		logic.upperHalf();

		int remainingBeans;
		if (beanCount % 2 == 0) {
			remainingBeans = beanCount / 2;
		} else {
			remainingBeans = (beanCount + 1) / 2;
		}

		Assert.assertEquals(failString, remainingBeans, ((BeanCounterLogicImpl)logic).getInSlotBeanCount(slotCount));
		
		j = 0;
		for (int i = 0; i < slotCount; i++) {
			if (i < slotCount / 2) {
				Assert.assertEquals(failString, 0, logic.getSlotBeanCount(i));
			} else {
				Assert.assertEquals(failString, upperSlots[j], logic.getSlotBeanCount(i));
				j++;
			}

		}

	}
	
	/**
	 * Test case for void repeat().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.repeat();
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: If the machine is operating in skill mode,
	 *             bean count in each slot is identical after the first run and second run of the machine. 
	 */
	@Test
	public void testRepeat() {
		// TODO: Implement
		logic.reset(beans);
		while (logic.advanceStep()) {}

		int[] slotVal = new int[slotCount];

		for (int i = 0; i < slotCount; i++) {
			slotVal[i] = logic.getSlotBeanCount(i);
		}

		logic.repeat();
		while (logic.advanceStep()) {}
		if (!isLuck) {
			for (int i = 0; i < slotCount; i++) {
				Assert.assertEquals(failString, slotVal[i], logic.getSlotBeanCount(i));
			}

		}

		

	}
}
