/* *********************************************************************** *
 * project: org.matsim.*
 * PopulationCreation.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package playground.balmermi.census2000v2;

import org.apache.log4j.Logger;
import org.matsim.core.api.facilities.Facilities;
import org.matsim.core.api.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.facilities.FacilitiesWriter;
import org.matsim.core.facilities.MatsimFacilitiesReader;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.population.PopulationWriter;
import org.matsim.world.MatsimWorldReader;
import org.matsim.world.World;
import org.matsim.world.WorldWriter;
import org.matsim.world.algorithms.WorldCheck;
import org.matsim.world.algorithms.WorldMappingInfo;

import playground.balmermi.census2000.data.Municipalities;
import playground.balmermi.census2000v2.data.Households;
import playground.balmermi.census2000v2.data.MicroCensus;
import playground.balmermi.census2000v2.modules.PersonAssignActivityChains;
import playground.balmermi.census2000v2.modules.PersonAssignLicenseModel;
import playground.balmermi.census2000v2.modules.PersonAssignMobilitiyToolModel;
import playground.balmermi.census2000v2.modules.PlansAnalyse;
import playground.balmermi.census2000v2.modules.PlansFilterPersons;
import playground.balmermi.census2000v2.modules.PlansWriteCustomAttributes;
import playground.balmermi.census2000v2.modules.PopulationAddCustomAttributes;
import playground.balmermi.census2000v2.modules.WorldParseFacilityZoneMapping;
import playground.balmermi.census2000v2.modules.WorldWriteFacilityZoneMapping;

public class IIDMGeneration {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private final static Logger log = Logger.getLogger(IIDMGeneration.class);

	//////////////////////////////////////////////////////////////////////
	// createPopulation()
	//////////////////////////////////////////////////////////////////////

	public static void createIIDM(String[] args) {

		log.info("MATSim-DB: create iidm.");

		Config config = Gbl.createConfig(args);
		World world = Gbl.createWorld();
		
		//////////////////////////////////////////////////////////////////////

		log.info("  extracting input directory... ");
		String indir = config.facilities().getInputFile();
		indir = indir.substring(0,indir.lastIndexOf("/"));
		log.info("    "+indir);
		log.info("  done.");

		log.info("  extracting output directory... ");
		String outdir = config.facilities().getOutputFile();
		outdir = outdir.substring(0,outdir.lastIndexOf("/"));
		log.info("    "+outdir);
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  reading world xml file...");
		final MatsimWorldReader worldReader = new MatsimWorldReader(world);
		worldReader.readFile(config.world().getInputFile());
		log.info("  done.");

		log.info("  reading facilities xml file...");
		Facilities facilities = (Facilities)world.createLayer(Facilities.LAYER_TYPE, null);
		new MatsimFacilitiesReader(facilities).readFile(config.facilities().getInputFile());
		world.complete();
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  parsing additional municipality information... ");
		Municipalities municipalities = new Municipalities(indir+"/gg25_2001_infos.txt");
		municipalities.parse();
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  running world modules... ");
		new WorldCheck().run(world);
		new WorldMappingInfo().run(world);
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  parsing f2z_mapping... ");
		new WorldParseFacilityZoneMapping(indir+"/f2z_mapping.txt").run(world);
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  running world modules... ");
		new WorldCheck().run(world);
		new WorldMappingInfo().run(world);
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  reding plans xml file... ");
		Population pop = new PopulationImpl();
		new MatsimPopulationReader(pop, null).readFile(config.plans().getInputFile());
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		// TODO: write some consistency tests

		//////////////////////////////////////////////////////////////////////

		log.info("  parsing households... ");
		Households households = new Households(municipalities);
		households.parse(indir+"/households.txt",pop);
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

//		// Test Demand for Timo Smiezek
//		// ch.cut.640000.200000.740000.310000.xml
//		Coord min = new CoordImpl(640000.0,200000.0);
//		Coord max = new CoordImpl(740000.0,310000.0);

//		log.info("  running person modules... ");
//		new PersonCreateFakePlanFromKnowledge().run(plans);
//		new PlansScenarioCut(min,max).run(plans);
//		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  removing persons... ");
		new PlansFilterPersons().run(pop);
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  adding custom attributes for persons... ");
		new PopulationAddCustomAttributes(indir+"/ETHZ_Pers.tab").run(pop);
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  reding mz plans xml file... ");
		Population mz_pop = new PopulationImpl();
		new MatsimPopulationReader(mz_pop, null).readFile(indir+"/mz.plans.xml.gz");
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  creating mz data stucture... ");
		MicroCensus mz = new MicroCensus(mz_pop);
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  runnning person models... ");
		new PersonAssignLicenseModel().run(pop);
		new PersonAssignMobilitiyToolModel().run(pop);
		new PersonAssignActivityChains(mz).run(pop);
		new PlansAnalyse().run(pop);
		new PlansAnalyse().run(mz_pop);
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  writing custom attributes of the persons... ");
		new PlansWriteCustomAttributes(outdir+"/output_persons.txt").run(pop);
		log.info("  done.");

		log.info("  writing households txt file... ");
		households.writeTable(outdir+"/output_households.txt");
		log.info("  done.");

		log.info("  writing plans xml file... ");
		PopulationWriter plans_writer = new PopulationWriter(pop);
		plans_writer.write();
		log.info("  done.");

		log.info("  writing f2z_mapping... ");
		new WorldWriteFacilityZoneMapping(outdir+"/output_f2z_mapping.txt").run(world);
		log.info("  done.");

		log.info("  writing facilities xml file... ");
		FacilitiesWriter fac_writer = new FacilitiesWriter(facilities);
		fac_writer.write();
		log.info("  done.");

		log.info("  writing world xml file... ");
		WorldWriter world_writer = new WorldWriter(world);
		world_writer.write();
		log.info("  done.");

		log.info("  writing config xml file... ");
		ConfigWriter config_writer = new ConfigWriter(config);
		config_writer.write();
		log.info("  done.");

		log.info("done.");
	}

	//////////////////////////////////////////////////////////////////////
	// main
	//////////////////////////////////////////////////////////////////////

	public static void main(final String[] args) {

		Gbl.startMeasurement();

		createIIDM(args);

		Gbl.printElapsedTime();
	}
}
