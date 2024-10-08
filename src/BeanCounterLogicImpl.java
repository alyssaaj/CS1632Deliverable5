import gov.nasa.jpf.vm.Verify;

import java.util.Arrays;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Code by @author Wonsun Ahn
 * 
 * <p>BeanCounterLogic: The bean counter, also known as a quincunx or the Galton
 * box, is a device for statistics experiments named after English scientist Sir
 * Francis Galton. It consists of an upright board with evenly spaced nails (or
 * pegs) in a triangular form. Each bean takes a random path and falls into a
 * slot.
 *
 * <p>Beans are dropped from the opening of the board. Every time a bean hits a
 * nail, it has a 50% chance of falling to the left or to the right. The piles
 * of beans are accumulated in the slots at the bottom of the board.
 * 
 * <p>This class implements the core logic of the machine. The MainPanel uses the
 * state inside BeanCounterLogic to display on the screen.
 * 
 * <p>Note that BeanCounterLogic uses a logical coordinate system to store the
 * positions of in-flight beans.For example, for a 4-slot machine:
 *                      (0, 0)
 *               (0, 1)        (1, 1)
 *        (0, 2)        (1, 2)        (2, 2)
 *  (0, 3)       (1, 3)        (2, 3)       (3, 3)
 * [Slot0]       [Slot1]       [Slot2]      [Slot3]
 */

public class BeanCounterLogicImpl implements BeanCounterLogic {
	// TODO: Add member methods and variables as needed
	int slotCount;			// total slots
	int beanCount;			// total beans
	int remainingBeans;		// beans that haven't drop yet
	int currBeanNum;		// current place in bean array
	int[] slots;
	BeanImpl[] beans;
	BeanImpl[] board;

	/**
	 * Constructor - creates the bean counter logic object that implements the core
	 * logic with the provided number of slots.
	 * 
	 * @param slotCount the number of slots in the machine
	 */
	BeanCounterLogicImpl(int slotCount) {
		this.slotCount = slotCount;
		this.slots = new int[slotCount];
		this.board = new BeanImpl[slotCount];
	}

	/**
	 * Returns the number of slots the machine was initialized with.
	 * 
	 * @return number of slots
	 */
	public int getSlotCount() {
		return this.slotCount;
	}
	
	/**
	 * Returns the number of beans remaining that are waiting to get inserted.
	 * 
	 * @return number of beans remaining
	 */
	public int getRemainingBeanCount() {
		return remainingBeans;
	}

	/**
	 * Returns the x-coordinate for the in-flight bean at the provided y-coordinate.
	 * 
	 * @param yPos the y-coordinate in which to look for the in-flight bean
	 * @return the x-coordinate of the in-flight bean; if no bean in y-coordinate, return NO_BEAN_IN_YPOS
	 */
	public int getInFlightBeanXPos(int yPos) {
		BeanImpl currBean = board[yPos];
		if (currBean != null) {
			return currBean.pos[yPos];
		} else {
			return NO_BEAN_IN_YPOS;
		}
	
	}

	/**
	 * Returns the number of beans in the ith slot.
	 * 
	 * @param i index of slot
	 * @return number of beans in slot
	 */
	public int getSlotBeanCount(int i) {
		return slots[i];
	}

	/**
	 * Calculates the average slot number of all the beans in slots.
	 * 
	 * @return Average slot number of all the beans in slots.
	 */
	public double getAverageSlotBeanCount() {
		int total = 0;
		for (int s = 0; s < slotCount; s++) {
			total = total + slots[s] * s;
		}
		int currBeanCount = getInSlotBeanCount(slotCount);
		if (currBeanCount == 0) {
			return 0;
		}
		return (double) total / (double) currBeanCount;

	}

	/**
	 * Removes the lower half of all beans currently in slots, keeping only the
	 * upper half. If there are an odd number of beans, remove (N-1)/2 beans, where
	 * N is the number of beans. So, if there are 3 beans, 1 will be removed and 2
	 * will be remaining.
	 */
	public void upperHalf() {
		
		int removeBeans;

		if (beanCount % 2 == 0) {
			removeBeans = beanCount / 2;
		} else {
			removeBeans = (beanCount - 1) / 2;
		}

		for (int i = 0; i < slotCount; i++) {

			if (slots[i] < removeBeans) {
				removeBeans = removeBeans - slots[i];
				slots[i] = 0;
			} else {
				slots[i] = slots[i] - removeBeans;
				removeBeans = 0;
				break;
			}
		}

	}

	/**
	 * Removes the upper half of all beans currently in slots, keeping only the
	 * lower half.  If there are an odd number of beans, remove (N-1)/2 beans, where
	 * N is the number of beans. So, if there are 3 beans, 1 will be removed and 2
	 * will be remaining.
	 */
	public void lowerHalf() {

		int removeBeans;

		if (beanCount % 2 == 0) {
			removeBeans = beanCount / 2;
		} else {
			removeBeans = (beanCount - 1) / 2;
		}

		for (int i = slotCount - 1; i >= 0; i--) {

			if (slots[i] < removeBeans) {
				removeBeans = removeBeans - slots[i];
				slots[i] = 0;
			} else {
				slots[i] = slots[i] - removeBeans;
				removeBeans = 0;
				break;
			}
		}
	}

	/**
	 * A hard reset. Initializes the machine with the passed beans. The machine
	 * starts with one bean at the top. Note: the Bean interface does not have any
	 * methods except the constructor, so you will need to downcast the passed Bean
	 * objects to BeanImpl objects to be able to work with them. This is always safe
	 * by construction (always, BeanImpl objects are created with
	 * BeanCounterLogicImpl objects and BeanBuggy objects are created with
	 * BeanCounterLogicBuggy objects according to the Config class).
	 * 
	 * @param beans array of beans to add to the machine
	 */
	public void reset(Bean[] beans) {
		this.beanCount = beans.length;
		this.remainingBeans = 0;
		this.currBeanNum = 0;

		this.slots = new int[slotCount];
		this.board = new BeanImpl[slotCount];

		if (beanCount > 0) {
			this.remainingBeans = this.beanCount - 1;
			this.beans = new BeanImpl[beans.length];
			for (int b = 0; b < beans.length; b++) {
				this.beans[b] = (BeanImpl) beans[b];
			}
			board[0] = this.beans[currBeanNum];
			currBeanNum++;
		}

	}

	/**
	 * Repeats the experiment by scooping up all beans in the slots and all beans
	 * in-flight and adding them into the pool of remaining beans. As in the
	 * beginning, the machine starts with one bean at the top.
	 */
	public void repeat() {
		this.remainingBeans = 0;
		this.currBeanNum = 0;

		this.slots = new int[slotCount];
		this.board = new BeanImpl[slotCount];

		if (beanCount > 0) {
			this.remainingBeans = this.beanCount - 1;
			board[0] = this.beans[currBeanNum];
			currBeanNum++;
		}
	}

	/**
	 * Advances the machine one step. All the in-flight beans fall down one step to
	 * the next peg. A new bean is inserted into the top of the machine if there are
	 * beans remaining.
	 * 
	 * @return whether there has been any status change. If there is no change, that
	 *         means the machine is finished.
	 */
	public boolean advanceStep() {
		BeanImpl finalBean;
		BeanImpl currBean;
		Boolean status = false;

		if (board[slotCount - 1] != null) {
			finalBean = board[slotCount - 1];
			int s = finalBean.pos[slotCount - 1];
			slots[s]++;
			board[slotCount - 1] = null;
			status = true;
		}

		for (int i = slotCount - 2; i >= 0 ;i--) {
			currBean = board[i];
			if (currBean != null) {
				board[i + 1] = currBean;
				board[i] = null;
				status = true;
			}
		}

		if (getRemainingBeanCount() > 0) {
			board[0] = beans[currBeanNum];
			status = true;
			currBeanNum++;
			remainingBeans--;
		}


		return status;
	}
	
	/**
	 * Number of spaces in between numbers when printing out the state of the machine.
	 * Make sure the number is odd (even numbers don't work as well).
	 */
	private int xspacing = 3;

	/**
	 * Calculates the number of spaces to indent for the given row of pegs.
	 * 
	 * @param yPos the y-position (or row number) of the pegs
	 * @return the number of spaces to indent
	 */
	private int getIndent(int yPos) {
		int rootIndent = (getSlotCount() - 1) * (xspacing + 1) / 2 + (xspacing + 1);
		return rootIndent - (xspacing + 1) / 2 * yPos;
	}

	/**
	 * Constructs a string representation of the bean count of all the slots.
	 * 
	 * @return a string with bean counts for each slot
	 */
	public String getSlotString() {
		StringBuilder bld = new StringBuilder();
		Formatter fmt = new Formatter(bld);
		String format = "%" + (xspacing + 1) + "d";
		for (int i = 0; i < getSlotCount(); i++) {
			fmt.format(format, getSlotBeanCount(i));
		}
		fmt.close();
		return bld.toString();
	}

	/**
	 * Constructs a string representation of the entire machine. If a peg has a bean
	 * above it, it is represented as a "1", otherwise it is represented as a "0".
	 * At the very bottom is attached the slots with the bean counts.
	 * 
	 * @return the string representation of the machine
	 */
	public String toString() {
		StringBuilder bld = new StringBuilder();
		Formatter fmt = new Formatter(bld);
		for (int yPos = 0; yPos < getSlotCount(); yPos++) {
			int xBeanPos = getInFlightBeanXPos(yPos);
			for (int xPos = 0; xPos <= yPos; xPos++) {
				int spacing = (xPos == 0) ? getIndent(yPos) : (xspacing + 1);
				String format = "%" + spacing + "d";
				if (xPos == xBeanPos) {
					fmt.format(format, 1);
				} else {
					fmt.format(format, 0);
				}
			}
			fmt.format("%n");
		}
		fmt.close();
		return bld.toString() + getSlotString();
	}

	/**
	 * Prints usage information.
	 */
	public static void showUsage() {
		System.out.println("Usage: java BeanCounterLogic slot_count bean_count <luck | skill> [debug]");
		System.out.println("Example: java BeanCounterLogic 10 400 luck");
		System.out.println("Example: java BeanCounterLogic 20 1000 skill debug");
	}
	
	/**
	 * Auxiliary main method. Runs the machine in text mode with no bells and
	 * whistles. It simply shows the slot bean count at the end.
	 * 
	 * @param args commandline arguments; see showUsage() for detailed information
	 */
	public static void main(String[] args) {
		boolean debug;
		boolean luck;
		int slotCount = 0;
		int beanCount = 0;

		if (args.length != 3 && args.length != 4) {
			showUsage();
			return;
		}

		try {
			slotCount = Integer.parseInt(args[0]);
			beanCount = Integer.parseInt(args[1]);
		} catch (NumberFormatException ne) {
			showUsage();
			return;
		}
		if (beanCount < 0) {
			showUsage();
			return;
		}

		if (args[2].equals("luck")) {
			luck = true;
		} else if (args[2].equals("skill")) {
			luck = false;
		} else {
			showUsage();
			return;
		}
		
		if (args.length == 4 && args[3].equals("debug")) {
			debug = true;
		} else {
			debug = false;
		}

		// Create the internal logic
		BeanCounterLogicImpl logic = new BeanCounterLogicImpl(slotCount);
		// Create the beans (in luck mode)
		BeanImpl[] beans = new BeanImpl[beanCount];
		for (int i = 0; i < beanCount; i++) {
			beans[i] = new BeanImpl(slotCount, luck, new Random());
		}
		// Initialize the logic with the beans
		logic.reset(beans);

		if (debug) {
			System.out.println(logic.toString());
		}

		// Perform the experiment
		while (true) {
			if (!logic.advanceStep()) {
				break;
			}
			if (debug) {
				System.out.println(logic.toString());
			}
		}
		// display experimental results
		System.out.println("Slot bean counts:");
		System.out.println(logic.getSlotString());
	}

	/**
	 * Returns the number of beans in flight.
	 * @param slotCount the number of slots in the machine
	 * @return number of beans in flight
	 */
	public int getInFlightBeanCount(int slotCount) {
		int inFlightBeanCount = 0;
		for (int l = 0; l < slotCount; l++) {
			if (getInFlightBeanXPos(l) != -1) {
				inFlightBeanCount = inFlightBeanCount + 1;
			}
		}
		return inFlightBeanCount;
	}

	/**
	 * Returns the number of beans in all the slots.
	 * @param slotCount the number of slots in the machine
	 * @return number of beans in slots
	 */
	public int getInSlotBeanCount(int slotCount) {
		int inSlotBeanCount = 0;
		for (int s = 0; s < slotCount; s++) {
			inSlotBeanCount = inSlotBeanCount + getSlotBeanCount(s);
		}

		return inSlotBeanCount;
	}

	/**
	 * Returns the which beans are at each level.
	 * @return which bean is at each level
	 */
	public BeanImpl[] getBeansInBoard() {
		return board.clone();
	}
}
