import gov.nasa.jpf.annotation.FilterField;

import java.util.Random;

/**
 * Code by @author Wonsun Ahn
 * 
 * <p>
 * Bean: Each bean is assigned a skill level from 0-9 on creation according to a
 * normal distribution with average SKILL_AVERAGE and standard deviation
 * SKILL_STDEV. The formula to calculate the skill level is:
 * 
 * <p>
 * SKILL_AVERAGE = (double) SLOT_COUNT * 0.5
 * SKILL_STDEV = (double) Math.sqrt(SLOT_COUNT * 0.5 * (1 - 0.5))
 * SKILL_LEVEL = (int) Math.round(rand.nextGaussian() * SKILL_STDEV + SKILL_AVERAGE)
 * 
 * <p>
 * A skill level of 9 means it always makes the "right" choices (pun intended)
 * when the machine is operating in skill mode ("skill" passed on command line).
 * That means the bean will always go right when a peg is encountered, resulting
 * it falling into slot 9. A skill evel of 0 means that the bean will always go
 * left, resulting it falling into slot 0. For the in-between skill levels, the
 * bean will first go right then left. For example, for a skill level of 7, the
 * bean will go right 7 times then go left twice.
 * 
 * <p>
 * Skill levels are irrelevant when the machine operates in luck mode. In that
 * case, the bean will have a 50/50 chance of going right or left, regardless of
 * skill level. The formula to calculate the direction is: rand.nextInt(2). If
 * the return value is 0, the bean goes left. If the return value is 1, the bean
 * goes right.
 */

public class BeanImpl implements Bean {
	// TODO: Add member methods and variables as needed
	int[] dirChosen;
	double skillAverage;
	double skillStddev;
	double skillLevel;
	int[] position;
	//int level;
	public int[] pos;

	/**
	 * Constructor - creates a bean in either luck mode or skill mode.
	 * 
	 * @param slotCount the number of slots in the machine
	 * @param isLuck whether the bean is in luck mode
	 * @param rand   the random number generator
	 */
	BeanImpl(int slotCount, boolean isLuck, Random rand) {
		// TODO: Implement
		if (slotCount != 0) {
			dirChosen = new int[slotCount - 1];
			position = new int[2];
			pos = new int[slotCount];
			position[0] = 0;
			position[1] = 0;
			//level = 0;
			if (isLuck) {
				for (int i = 0; i < slotCount; i++) {
					dirChosen[i] = rand.nextInt(2);
				}
			} else {
				skillAverage = (double) slotCount * 0.5;
	 			skillStddev = (double) Math.sqrt(slotCount * 0.5 * (1 - 0.5));
	 			skillLevel = (int) Math.round(rand.nextGaussian() * skillStddev + skillAverage);

	 			for (int i = 0; i < slotCount - 1; i++) {
					if (i < skillLevel) {
						dirChosen[i] = 1;
					} else {
						dirChosen[i] = 0;
					}
				}
			}

			pos[0] = 0;
			for (int i = 1; i < slotCount; i++) {
				pos[i] = pos[i - 1] + dirChosen[i - 1];
			}
		}
	}


	/*public void move(int level) {
		int dir = dirChosen[level];
		position[1] = position[1]++;

		switch (dir){
			case 0:
				position[0]=
				break;
			case 1:
				break;
		}
		
	}*/
}