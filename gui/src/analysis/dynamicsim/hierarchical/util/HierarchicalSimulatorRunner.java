package analysis.dynamicsim.hierarchical.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import main.util.dataparser.TSDParser;
import analysis.dynamicsim.DynamicSimulation;

public class HierarchicalSimulatorRunner
{

	static double				timeLimit			= 5.0;
	static double				relativeError		= 1e-6;
	static double				absoluteError		= 1e-9;
	static int					numSteps;
	static double				maxTimeStep			= Double.POSITIVE_INFINITY;
	static double				minTimeStep			= 0.0;
	static long					randomSeed			= 0;
	static double				printInterval		= 0;
	static int					runs				= 1;
	static double				stoichAmpValue		= 1.0;
	static boolean				genStats			= false;
	static String				selectedSimulator	= "";
	static ArrayList<String>	interestingSpecies	= new ArrayList<String>();
	static String				quantityType		= "amount";
	static boolean				testSuite			= true;

	/**
	 * @param args
	 *            %d = args[0] = path to cases %n = args[1] = case id %o =
	 *            args[2] = output path
	 */
	public static void main(String[] args)
	{

		if (args.length < 1)
		{
			System.out.println("Missing arguments");
			return;
		}

		DynamicSimulation simulator = null;

		String separator = (File.separator.equals("\\")) ? "\\\\" : File.separator;

		if (testSuite)
		{
			String testcase = args[1];

			String[] casesNeedToChangeTimeStep = new String[] { "00028", "00080", "00128", "00173",
					"00194", "00196", "00197", "00198", "00200", "00201", "00269", "00274",
					"00400", "00460", "00276", "00278", "00279", "00870", "00872" };

			for (String s : casesNeedToChangeTimeStep)
			{
				if (s.equals(testcase))
				{
					maxTimeStep = 0.001;
					break;
				}
			}

			String filename = args[0] + separator + testcase + separator + testcase
					+ "-sbml-l3v1.xml";
			String outputDirectory = args[2];

			String settingsFile = args[0] + separator + testcase + separator + testcase
					+ "-settings.txt";

			simulator = new DynamicSimulation("hierarchical-rk");

			JLabel progressLabel = null;
			JProgressBar progress = null;
			JFrame running = null;

			readSettings(settingsFile);

			if (printInterval == 0)
			{
				printInterval = timeLimit / numSteps;
			}

			String[] intSpecies = new String[interestingSpecies.size()];
			int i = 0;

			for (String intSpec : interestingSpecies)
			{
				intSpecies[i] = intSpec;
				++i;
			}

			try
			{

				simulator.simulate(filename, outputDirectory, outputDirectory, timeLimit,
						maxTimeStep, minTimeStep, randomSeed, progress, printInterval, runs,
						progressLabel, running, stoichAmpValue, intSpecies, numSteps,
						relativeError, absoluteError, quantityType, false, null, null, null);

				TSDParser tsdp = new TSDParser(outputDirectory + "run-1.tsd", true);
				tsdp.outputCSV(outputDirectory + testcase + ".csv");

			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}

		}
		else
		{
			String filename = args[0];
			String outputDirectory = args[1];
			String settingsFile = args[2];

			readProperties(settingsFile);

			simulator = new DynamicSimulation("hierarchical-direct");

			JLabel progressLabel = null;
			JProgressBar progress = null;
			JFrame running = null;

			if (printInterval == 0)
			{
				printInterval = timeLimit / numSteps;
			}

			String[] intSpecies = new String[interestingSpecies.size()];
			int i = 0;

			for (String intSpec : interestingSpecies)
			{
				intSpecies[i] = intSpec;
				++i;
			}

			try
			{

				simulator.simulate(filename, outputDirectory, outputDirectory, timeLimit,
						maxTimeStep, minTimeStep, randomSeed, progress, printInterval, runs,
						progressLabel, running, stoichAmpValue, intSpecies, numSteps,
						relativeError, absoluteError, quantityType, false, null, null, null);

			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}

		}
	}

	private static void readSettings(String filename)
	{

		File f = new File(filename);
		Properties properties = new Properties();
		FileInputStream in;

		try
		{
			in = new FileInputStream(f);
			properties.load(in);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		timeLimit = Double.valueOf(properties.getProperty("duration"))
				- Double.valueOf(properties.getProperty("start"));
		relativeError = Double.valueOf(properties.getProperty("relative"));
		absoluteError = Double.valueOf(properties.getProperty("absolute"));
		numSteps = Integer.valueOf(properties.getProperty("steps"));

		for (String intSpecies : properties.getProperty("variables").replaceAll(" ", "").split(","))
		{
			interestingSpecies.add(intSpecies);
		}

		quantityType = properties.getProperty("concentration");
	}

	private static void readProperties(String filename)
	{

		File f = new File(filename);
		Properties properties = new Properties();
		FileInputStream in;

		try
		{
			in = new FileInputStream(f);
			properties.load(in);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		String simMethod = properties.getProperty("reb2sac.simulation.method");
		String prefix = "monte.carlo.";

		if (simMethod.equals("ODE"))
		{
			prefix = "ode.";
		}

		if (properties.containsKey(prefix + "simulation.time.limit"))
		{
			timeLimit = Double.valueOf(properties.getProperty(prefix + "simulation.time.limit"));
		}
		if (properties.containsKey(prefix + "simulation.time.step"))
		{
			if (properties.getProperty(prefix + "simulation.time.step").equals("inf"))
			{
				maxTimeStep = Double.POSITIVE_INFINITY;
			}
			else
			{
				maxTimeStep = Double.valueOf(properties
						.getProperty(prefix + "simulation.time.step"));
			}
		}
		if (properties.containsKey(prefix + "simulation.print.interval"))
		{
			printInterval = Double.valueOf(properties.getProperty(prefix
					+ "simulation.print.interval"));
		}
		if (properties.containsKey("monte.carlo.simulation.runs"))
		{
			runs = Integer.valueOf(properties.getProperty("monte.carlo.simulation.runs"));
		}
		if (properties.containsKey("reb2sac.generate.statistics"))
		{
			genStats = Boolean.valueOf(properties.getProperty("reb2sac.generate.statistics"));
		}
		if (properties.containsKey("monte.carlo.simulation.random.seed"))
		{
			randomSeed = Long.valueOf(properties.getProperty("monte.carlo.simulation.random.seed"));
		}
		if (properties.containsKey("simulation.printer.tracking.quantity"))
		{
			quantityType = properties.getProperty("simulation.printer.tracking.quantity");
		}
		if (properties.containsKey("monte.carlo.simulation.min.time.step"))
		{
			minTimeStep = Double.valueOf(properties
					.getProperty("monte.carlo.simulation.min.time.step"));
		}
		if (properties.containsKey("reb2sac.diffusion.stoichiometry.amplification.value"))
		{
			stoichAmpValue = Double.valueOf(properties
					.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));
		}
		if (properties.containsKey("selected.simulator"))
		{
			selectedSimulator = properties.getProperty("selected.simulator");
		}
		// relativeError = Double.valueOf(properties.getProperty("relative"));
		if (properties.containsKey("ode.simulation.absolute.error"))
		{
			absoluteError = Double.valueOf(properties.getProperty("ode.simulation.absolute.error"));
		}
		if (properties.containsKey(prefix + "simulation.number.steps"))
		{
			numSteps = Integer.valueOf(properties.getProperty(prefix + "simulation.number.steps"));
		}

		int intSpecies = 1;

		while (properties.containsKey("reb2sac.interesting.species." + intSpecies))
		{

			interestingSpecies.add(properties.getProperty("reb2sac.interesting.species."
					+ intSpecies));
			++intSpecies;
		}
	}
}
