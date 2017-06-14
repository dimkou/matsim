package contrib.baseline.preparation;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static contrib.baseline.preparation.IVTConfigCreator.*;
import static contrib.baseline.preparation.secondaryFacilityCreation.FacilitiesFromBZ12.testFacilities;

/**
 * Adds activities to existing facilities.
 *
 * @author boescpa
 */
public class ActivityAdder {

    private final ActivityFacilitiesFactoryImpl factory = new ActivityFacilitiesFactoryImpl();
    private final ActivityFacilities facilities;

    protected ActivityAdder(ActivityFacilities facilities) {
        this.facilities = facilities;
    }

    public static void main(final String[] args) {
        final String pathToFacilities = args[0];
        final String pathToOutputFacilities = args[1];
        final int numberOfActivityCopies = Integer.parseInt(args[2]);

        // Read facilities and create Adder
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        MatsimFacilitiesReader facilitiesReader = new MatsimFacilitiesReader(scenario);
        facilitiesReader.readFile(pathToFacilities);
        ActivityAdder adder = new ActivityAdder(scenario.getActivityFacilities());

        // Add activities
        adder.addActivityToFacilities(REMOTE_WORK, WORK, 5, 0, 24*60*60);
        //adder.addActivityToFacilities(REMOTE_WORK, "primary_work", 5, 0, 24*60*60);
        adder.addActivityToFacilities(ESCORT_KIDS, EDUCATION, 10, 7*60*60, 18*60*60);
        adder.addActivityToFacilities(ESCORT_KIDS, LEISURE, 5, 7*60*60, 22*60*60);
        adder.addActivityToFacilities(ESCORT_OTHER, LEISURE, 5, 5*60*60, 24*60*60);
        adder.addActivityToFacilities(ESCORT_OTHER, SHOP, 5, 5*60*60, 24*60*60);
        adder.addActivityToFacilities(REMOTE_HOME, HOME, 1, 0, 24*60*60);

        // Multiply activities
		adder.multiplyActivities(numberOfActivityCopies);

        // Write facilities
        FacilitiesWriter facilitiesWriter = new FacilitiesWriter(scenario.getActivityFacilities());
        facilitiesWriter.write(pathToOutputFacilities);
        testFacilities(pathToOutputFacilities);
    }

	private void multiplyActivities(int numberOfActivityCopies) {
		for (ActivityFacility facility : facilities.getFacilities().values()) {
			List<String> actsToMultiply = new ArrayList<>(facility.getActivityOptions().size());
			actsToMultiply.addAll(facility.getActivityOptions().keySet());
			for (String activityOptionId : actsToMultiply) {
				ActivityOption activityOption = facility.getActivityOptions().get(activityOptionId);
				for (int i = 0; i < numberOfActivityCopies; i++) {
					// todo: check if we start with zero or with one...
					ActivityOption newActivity = factory.createActivityOption(activityOption.getType() + "_" + i);
					newActivity.setCapacity(activityOption.getCapacity());
					for (OpeningTime openingTime : activityOption.getOpeningTimes()) {
						newActivity.addOpeningTime(openingTime);
					}
					facility.addActivityOption(newActivity);
				}
				facility.getActivityOptions().remove(activityOptionId);
			}
		}
	}

	protected void addActivityToFacilities(String actTypeToAdd, String existingActToAddTo, double defaultCapacity,
                                           double defaultOpenFrom, double defaultOpenTill) {
        for (ActivityFacility facility : facilities.getFacilities().values()) {
            if (!facility.getActivityOptions().containsKey(actTypeToAdd) &&
                    facility.getActivityOptions().containsKey(existingActToAddTo)) {
                ActivityOption newActivity = factory.createActivityOption(actTypeToAdd);
                newActivity.setCapacity(defaultCapacity);
                newActivity.addOpeningTime(new OpeningTimeImpl(defaultOpenFrom, defaultOpenTill));
                facility.addActivityOption(newActivity);
				correctFacility(facility);
            }
        }
    }

	private void correctFacility(ActivityFacility facility) {
		if (facility.getActivityOptions().containsKey(IVTConfigCreator.HOME)) {
			facility.getActivityOptions().get(IVTConfigCreator.HOME).getOpeningTimes().clear();
		}
		if (facility.getActivityOptions().containsKey(IVTConfigCreator.REMOTE_HOME)) {
			facility.getActivityOptions().get(IVTConfigCreator.REMOTE_HOME).getOpeningTimes().clear();
		}
	}

}
