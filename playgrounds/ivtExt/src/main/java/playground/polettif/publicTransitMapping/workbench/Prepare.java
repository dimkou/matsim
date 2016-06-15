/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.polettif.publicTransitMapping.workbench;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.PopulationReader;
import org.matsim.core.population.PopulationReaderMatsimV5;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Counter;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.Vehicles;
import playground.polettif.publicTransitMapping.tools.NetworkTools;
import playground.polettif.publicTransitMapping.tools.ScheduleCleaner;
import playground.polettif.publicTransitMapping.tools.ScheduleTools;

import java.util.*;

/**
 * workbench class to do some scenario preparation
 *
 * @author polettif
 */
public class Prepare {

	protected static Logger log = Logger.getLogger(Prepare.class);

	private Config config;

	private Network network;
	private TransitSchedule schedule;
	private Vehicles vehicles;
	private Population population;


	public static void main(final String[] args) {

		String inputNetwork = "../../output/2016-06-09-1800/ch_ll_network.xml.gz";
		String inputSchedule = "../../output/2016-06-09-1800/ch_ll_schedule.xml.gz";
		String inputVehicles = "../../data/vehicles/ch_hafas_vehicles.xml.gz";

		// zurich 1%
		String inputPopulation = "../population/zh_1prct.xml.gz";
		Prepare prepareZH1 = new Prepare("config.xml", inputNetwork, inputSchedule, inputVehicles, inputPopulation);
		prepareZH1.removeInvalidLines();
		prepareZH1.networkAndSchedule();
		prepareZH1.population();
		prepareZH1.vehicles(0.005);
		prepareZH1.writeFiles("zurich_one");

		// zurich 10%
		inputPopulation = "../population/zh_10prct.xml.gz";
		Prepare prepareZH10 = new Prepare("config.xml", inputNetwork, inputSchedule, inputVehicles, inputPopulation);
		prepareZH10.removeInvalidLines();
		prepareZH10.networkAndSchedule();
		prepareZH10.population();
		prepareZH10.vehicles(0.005);
		prepareZH10.writeFiles("zurich_ten");

		// switzerland 10%
		inputPopulation = "../population/ch_10prct.xml.gz";
		Prepare prepareCH = new Prepare("config.xml", inputNetwork, inputSchedule, inputVehicles, inputPopulation);
		prepareCH.removeInvalidLines();
		prepareCH.networkAndSchedule();
		prepareCH.population();
		prepareCH.vehicles(0.005);
		prepareCH.writeFiles("ch_ten");
	}

	private void removeInvalidLines() {
		Set<String> set = new HashSet<>();

		// copy/paste
		set.add("AB-_line21");
		set.add("AB-_line21");
		set.add("RVB_line2");
		set.add("RVB_line2");
		set.add("RVB_line2");
		set.add("RVB_line2");
		set.add("RVB_line2");
		set.add("RVB_line2");
		set.add("RVB_line4");
		set.add("RVB_line4");
		set.add("RVB_line4");
		set.add("RVB_line4");
		set.add("RVB_line4");
		set.add("RVB_line4");
		set.add("SBG_line7312");
		set.add("SBG_line7312");
		set.add("VBZ_line303");
		set.add("VBZ_line303");
		set.add("VBZ_line303");
		set.add("VBZ_line303");
		set.add("VBZ_line303");
		set.add("VBZ_line303");
		set.add("VBZ_line303");
		set.add("PAG_line212");
		set.add("PAG_line212");
		set.add("PAG_line212");
		set.add("PAG_line212");
		set.add("PAG_line581");

		for(String e : set) {
			TransitLine tl = schedule.getTransitLines().get(Id.create(e, TransitLine.class));
			if(tl != null) schedule.removeTransitLine(tl);
		}
	}

	private void removeInvalidRoutes() {
		List<String[]> list = new ArrayList<>();

		// copy/paste
//		list.add(new String[]{	"asd", "fad"	});

		for(String[] e : list) {
			ScheduleCleaner.removeRoute(schedule, Id.create(e[0], TransitLine.class), Id.create(e[2], TransitRoute.class));
		}
	}

	public Prepare(String scenarioConfig, String inputNetwork, String inputSchedule, String inputVehicles, String inputPopulation) {
		config = ConfigUtils.loadConfig(scenarioConfig);

		network = NetworkTools.readNetwork(inputNetwork);
		schedule = ScheduleTools.readTransitSchedule(inputSchedule);
		vehicles = ScheduleTools.readVehicles(inputVehicles);

		Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		PopulationReader reader = new PopulationReaderMatsimV5(sc);
		reader.readFile(inputPopulation);
		population = sc.getPopulation();
	}

	private void writeFiles(String folder) {
		ScheduleTools.writeVehicles(vehicles, folder+config.transit().getVehiclesFile());
		ScheduleTools.writeTransitSchedule(schedule, folder+config.transit().getTransitScheduleFile());
		NetworkTools.writeNetwork(network, folder+config.network().getInputFile());
		new PopulationWriter(population, network).write(folder+config.plans().getInputFile());
	}

	private void vehicles(double percentage) {
		for(VehicleType vt : vehicles.getVehicleTypes().values()) {
			vt.setPcuEquivalents(vt.getPcuEquivalents()*percentage);
		}
	}

	/**
	 * modifiy population
	 */
	private void population() {
		log.info("creating car only network");
		Network carNetwork = NetworkTools.filterNetworkByLinkMode(network, Collections.singleton("car"));

		// only home and work activities
		log.info("adapting plans...");
		Counter personCounter = new Counter(" person # ");
		for(Person person : population.getPersons().values()) {
			Plan plan = person.getSelectedPlan();
			personCounter.incCounter();
			List<PlanElement> elements = plan.getPlanElements();
			for(PlanElement e : elements) {
				if(e instanceof ActivityImpl) {
					Activity activity = (Activity) e;
					switch (activity.getType()) {
						case "home" :
							break;
						case "work" :
							break;
						default :
							activity.setType("work");
					}
					activity.setFacilityId(null);
					activity.setLinkId(NetworkTools.getNearestLink(carNetwork, activity.getCoord()).getId());
				}
			}
		}
	}

	private void networkAndSchedule() {
//		NetworkTools.replaceNonCarModesWithPT(network);
//		ScheduleCleaner.replaceScheduleModes(schedule, TransportMode.pt);

		Coord effretikon = new Coord(2693780.0, 1253409.0);
		double radius = 20000;

		ScheduleCleaner.cutSchedule(schedule, effretikon, radius);
		Set<String> modesToKeep = new HashSet<>();
		modesToKeep.add("car");
		modesToKeep.add("rail");
		modesToKeep.add("light_rail");
		ScheduleCleaner.removeNotUsedTransitLinks(schedule, network, modesToKeep, true);
		ScheduleCleaner.cleanVehicles(schedule, vehicles);
	}

	private void editSchedule() {
		/*
		Network network = null;
		TransitSchedule schedule = null;
		ScheduleEditor editor = new BasicScheduleEditor(schedule, network);
		Link l = network.getLinks().get(Id.createLinkId("350213"));
		editor.addLink(Id.createLinkId("350213-2"), l.getToNode().getId(), l.getFromNode().getId(), l.getId());
		editor.refreshSchedule();

		TransitScheduleValidator.ValidationResult result = TransitScheduleValidator.validateAll(schedule, network);
		TransitScheduleValidator.printResult(result);
		*/
	}

	private static void createConfig() {
		Config config = ConfigUtils.createConfig();
		new ConfigWriter(config).write("config.xml");
	}

	
}